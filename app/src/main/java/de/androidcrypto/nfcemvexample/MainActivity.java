package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexBlankToBytes;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;
import static de.androidcrypto.nfcemvexample.BinaryUtils.intFromByteArrayV4;
import static de.androidcrypto.nfcemvexample.sasc.Log.getPrintWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.nfcemvexample.nfccreditcards.AidValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.PdolUtil;
import de.androidcrypto.nfcemvexample.nfccreditcards.TagValues;
import de.androidcrypto.nfcemvexample.sasc.ApplicationInterchangeProfile;
import de.androidcrypto.nfcemvexample.sasc.ApplicationUsageControl;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "NfcCreditCardAct";

    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etData, etLog;
    SwitchMaterial prettyPrintResponse;

    private NfcAdapter mNfcAdapter;
    private byte[] tagId;

    final String TechIsoDep = "android.nfc.tech.IsoDep";

    boolean debugPrint = true; // if set to true the writeToUi method will print to console
    boolean isPrettyPrintResponse = false; // default
    String aidSelectedForAnalyze = "";
    String aidSelectedForAnalyzeName = "";

    // exporting the data
    String exportString = "";
    String exportStringFileName = "emv.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        tv1 = findViewById(R.id.tv1);
        etData = findViewById(R.id.etData);
        etLog = findViewById(R.id.etLog);
        prettyPrintResponse = findViewById(R.id.swPrettyPrint);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        prettyPrintResponse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPrettyPrintResponse = prettyPrintResponse.isChecked();
            }
        });
    }

    /**
     * section for NFC
     */

    /**
     * This method is run in another thread when a card is discovered
     * This method cannot cannot direct interact with the UI Thread
     * Use `runOnUiThread` method to change the UI from this method
     *
     * @param tag discovered tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        runOnUiThread(() -> {
            etLog.setText("");
            etData.setText("");
            exportString = "";
            aidSelectedForAnalyze = "";
            aidSelectedForAnalyzeName = "";
        });
        playPing();
        writeToUiAppend(etLog, "NFC tag discovered");

        tagId = tag.getId();
        writeToUiAppend(etLog, "TagId: " + bytesToHex(tagId));
        String[] techList = tag.getTechList();
        writeToUiAppend(etLog, "TechList found with these entries:");
        for (int i = 0; i < techList.length; i++) {
            writeToUiAppend(etLog, techList[i]);
        }
        // the next steps depend on the TechList found on the device
        for (int i = 0; i < techList.length; i++) {
            String tech = techList[i];
            writeToUiAppend(etLog, "");
            switch (tech) {
                case TechIsoDep: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology IsoDep");
                    readIsoDep(tag);
                    break;
                }
                default: {
                    // do nothing
                    break;
                }
            }
        }
    }

    private void playPing() {
        MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.single_ping);
        mp.start();
    }

    private void playDoublePing() {
        MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.double_ping);
        mp.start();
    }

    private void readIsoDep(Tag tag) {
        Log.i(TAG, "read a tag with IsoDep technology");
        IsoDep nfc = null;
        nfc = IsoDep.get(tag);
        if (nfc != null) {
            // init of the service methods
            TagValues tv = new TagValues();
            AidValues aidV = new AidValues();
            PdolUtil pu = new PdolUtil(nfc);

            try {
                nfc.connect();
                writeToUiAppend(etLog, "try to read a payment card with PPSE");
                byte[] command;
                writeToUiAppend(etLog, "");
                writeToUiAppend(etLog, "01 select PPSE");
                byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PPSE
                command = selectApdu(PPSE);
                byte[] responsePpse = nfc.transceive(command);
                writeToUiAppend(etLog, "01 select PPSE command length " + command.length + " data: " + bytesToHex(command));
                writeToUiAppend(etLog, "01 select PPSE response length " + responsePpse.length + " data: " + bytesToHex(responsePpse));
                boolean responsePpseNotAllowed = responseNotAllowed(responsePpse);
                if (responsePpseNotAllowed) {
                    // todo The card must not have a PSE or PPSE, then try with known AIDs
                    writeToUiAppend(etLog, "01 selecting PPSE is not allowed on card");
                }

                if (responsePpseNotAllowed) {
                    writeToUiAppend(etLog, "");
                    writeToUiAppend(etLog, "The card is not a credit card, reading aborted");
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                byte[] responsePpseOk = checkResponse(responsePpse);
                if (responsePpseOk != null) {
                    // pretty print of response
                    if (isPrettyPrintResponse) prettyPrintData(etLog, responsePpseOk);

                    writeToUiAppend(etLog, "");
                    writeToUiAppend(etLog, "02 analyze select PPSE response and search for tag 0x4F (applications on card)");

                    BerTlvParser parser = new BerTlvParser();
                    BerTlvs tlv4Fs = parser.parse(responsePpseOk);
                    // by searching for tag 4f
                    List<BerTlv> tag4fList = tlv4Fs.findAll(new BerTag(0x4F));
                    if (tag4fList.size() < 1) {
                        writeToUiAppend(etLog, "there is no tag 0x4F available, stopping here");
                        try {
                            nfc.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    writeToUiAppend(etLog, "Found tag 0x4F " + tag4fList.size() + " time(s):");
                    ArrayList<byte[]> aidList = new ArrayList<>();
                    for (int i4f = 0; i4f < tag4fList.size(); i4f++) {
                        BerTlv tlv4f = tag4fList.get(i4f);
                        BerTag berTag4f = tlv4f.getTag();
                        byte[] tlv4fBytes = tlv4f.getBytesValue();
                        aidList.add(tlv4fBytes);
                        writeToUiAppend(etLog, "application Id (AID): " + bytesToHex(tlv4fBytes));
                    }

                    // step 03: iterating through aidList by selecting AID
                    for (int aidNumber = 0; aidNumber < tag4fList.size(); aidNumber++) {
                        byte[] aidSelected = aidList.get(aidNumber);
                        aidSelectedForAnalyze = bytesToHex(aidSelected);
                        aidSelectedForAnalyzeName = aidV.getAidName(aidSelected);
                        writeToUiAppend(etLog, "");
                        writeToUiAppend(etLog, "************************************");
                        writeToUiAppend(etLog, "03 select application by AID " + aidSelectedForAnalyze + " (number " + (aidNumber + 1) + ")");
                        writeToUiAppend(etLog, "card is a " + aidSelectedForAnalyzeName);
                        command = selectApdu(aidSelected);
                        byte[] responseSelectedAid = nfc.transceive(command);
                        writeToUiAppend(etLog, "");
                        writeToUiAppend(etLog, "03 select AID command length " + command.length + " data: " + bytesToHex(command));
                        boolean responseSelectAidNotAllowed = responseNotAllowed(responseSelectedAid);
                        if (responseSelectAidNotAllowed) {
                            writeToUiAppend(etLog, "03 selecting AID is not allowed on card");
                            writeToUiAppend(etLog, "");
                            writeToUiAppend(etLog, "The card is not a credit card, reading aborted");
                            try {
                                nfc.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }

                        // manual break - read complete file content
                        /*
                        completeFileReading(nfc);
                        try {
                            nfc.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (!responseSelectAidNotAllowed) return;
                        */

                        byte[] responseSelectedAidOk = checkResponse(responseSelectedAid);
                        if (responseSelectedAidOk != null) {
                            writeToUiAppend(etLog, "03 select AID response length " + responseSelectedAidOk.length + " data: " + bytesToHex(responseSelectedAidOk));
                            // pretty print of response
                            if (isPrettyPrintResponse) prettyPrintData(etLog, responseSelectedAidOk);

                            // intermediate step - get single data from card, will be printed later
                            byte[] applicationTransactionCounter = getApplicationTransactionCounter(nfc);
                            byte[] pinTryCounter = getPinTryCounter(nfc);
                            byte[] lastOnlineATCRegister = getLastOnlineATCRegister(nfc);
                            byte[] logFormat = getLogFormat(nfc);

                            writeToUiAppend(etLog, "");
                            writeToUiAppend(etLog, "04 search for tag 0x9F38 in the selectAid response");
                            /**
                             * note: different behaviour between Visa and Mastercard and German Girocards
                             * Mastercard has NO PDOL, Visa gives PDOL in tag 9F38
                             * tag 50 and/or tag 9F12 has an application label or application name
                             * nex step: search for tag 9F38 Processing Options Data Object List (PDOL)
                             */
                            BerTlvs tlvsAid = parser.parse(responseSelectedAidOk);
                            BerTlv tag9f38 = tlvsAid.find(new BerTag(0x9F, 0x38));
                            // tag9f38 is null when not found
                            if (tag9f38 != null) {
                                // this is mainly for Visa cards and GiroCards
                                byte[] pdolValue = tag9f38.getBytesValue();
                                writeToUiAppend(etLog, "found tag 0x9F38 in the selectAid with this length: " + pdolValue.length + " data: " + bytesToHex(pdolValue));
                                // code will run for VISA and NOT for MasterCard
                                // we are using a generalized selectGpo command
                                byte[] commandGpoRequest = hexToBytes(pu.getPdolWithCountryCode());
                                //byte[] commandGpoRequest = hexToBytes(pu.getPdolWithCountryCode2());
                                //byte[] commandGpoRequest = hexToBytes(pu.getPdolVisaComdirect());
                                writeToUiAppend(etLog, "");
                                writeToUiAppend(etLog, "05 get the processing options command length: " + commandGpoRequest.length + " data: " + bytesToHex(commandGpoRequest));
                                byte[] responseGpoRequest = nfc.transceive(commandGpoRequest);
                                System.out.println("*** responseGpoRequest: " + bytesToHex(responseGpoRequest));
                                if (!responseSendWithPdolFailure(responseGpoRequest)) {
                                    System.out.println("** responseGpoRequest: " + bytesToHex(responseGpoRequest));
                                    byte[] responseGpoRequestOk = checkResponse(responseGpoRequest);
                                    if (responseGpoRequestOk != null) {
                                        writeToUiAppend(etLog, "05 select GPO response length: " + responseGpoRequestOk.length + " data: " + bytesToHex(responseGpoRequestOk));

                                        // pretty print of response
                                        if (isPrettyPrintResponse) prettyPrintData(etLog, responseGpoRequestOk);

                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "06 read the files from card and search for tag 0x57 in each file");
                                        String pan_expirationDate = readPanFromFilesFromGpo(nfc, responseGpoRequestOk);
                                        String[] parts = pan_expirationDate.split("_");
                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                        writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etLog, "PAN: " + parts[0]);
                                        writeToUiAppend(etLog, "Expiration date (YYMM): " + parts[1]);
                                        writeToUiAppendNoExport(etData, "");
                                        writeToUiAppendNoExport(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppendNoExport(etData, "PAN: " + parts[0]);
                                        writeToUiAppendNoExport(etData, "Expiration date (YYMM): " + parts[1]);

                                        // print single data
                                        printSingleData(etLog, applicationTransactionCounter, pinTryCounter, lastOnlineATCRegister, logFormat);

                                        // get application crypto with an empty command
                                        writeToUiAppend(etLog, "");
                                        byte[] responseGetAppCrypto = getApplicationCrypto(nfc, getCommandGetAppCryptoVisacard());
                                        writeToUiAppend(etLog, "get AC command  length: " + getCommandGetAppCryptoVisacard().length + " data: " + bytesToHex(getCommandGetAppCryptoVisacard()));
                                        if (responseGetAppCrypto != null) {
                                            writeToUiAppend(etLog, "get AC response length: " + responseGetAppCrypto.length + " data: " + bytesToHex(responseGetAppCrypto));
                                            // pretty print of response
                                            if (isPrettyPrintResponse) prettyPrintData(etLog, responseGetAppCrypto);
                                        } else {
                                            writeToUiAppend(etLog, "get AC failed");
                                        }



                                        // check for aip + cvm
                                        // see: https://github.com/sasc999/javaemvreader/tree/master/src/main/java/sasc/emv
                                        // todo get real card data from tag 0x82
                                        ApplicationInterchangeProfile aip = new ApplicationInterchangeProfile((byte) 0x20, (byte) 0x20);
                                        writeToUiAppend(etLog, aip.toString());

                                        // todo get real card data from tag 0x9f07 /e.g. AAB MasterCard ff co
                                        ApplicationUsageControl auc = new ApplicationUsageControl((byte)0xff, (byte)0xc0);
                                        //ApplicationUsageControl auc = new ApplicationUsageControl((byte)0xab, (byte)0x80);
                                        writeToUiAppend(etLog, auc.toString());



/*
comd visa
I/System.out: 77 81 C6 -- Response Message Template Format 2
I/System.out:          82 02 -- Application Interchange Profile
I/System.out:                20 20 (BINARY)
lloyds visa:
I/System.out:                20 00 (BINARY)

lloyds mc
I/System.out: 77 81 C6 -- Response Message Template Format 2
I/System.out:          82 02 -- Application Interchange Profile
I/System.out:                19 80 (BINARY)

in: data from file SFI 16 record 1
I/System.out: ------------------------------------
I/System.out: 70 81 81 -- Record Template (EMV Proprietary)
I/System.out:          8E 0E -- Cardholder Verification Method (CVM) List
I/System.out:                00 00 00 00 00 00 00 00 42 03 1E 03 1F 03 (BINARY)

aab mc
I/System.out: 77 12 -- Response Message Template Format 2
I/System.out:       82 02 -- Application Interchange Profile
I/System.out:             19 80 (BINARY)

in I/System.out: data from file SFI 16 record 1
I/System.out: ------------------------------------
I/System.out: 70 81 A6 -- Record Template (EMV Proprietary)
I/System.out:          8E 0E -- Cardholder Verification Method (CVM) List
I/System.out:                00 00 00 00 00 00 00 00 42 03 1E 03 1F 03 (BINARY)
 */

                                    }
                                } else {
                                    // we tried to get the processing options with a predefined pdolWithCountryCode but that failed
                                    // this code is working for German GiroCards
                                    // this is a very simplified version to read the requested pdol length
                                    // pdolValue contains the full pdolRequest, e.g.
                                    // we assume that all requested tag are 2 byte tags, e.g.
                                    // if remainder is 0 we can try to sum the length data in pdolValue[2], pdolValue[5]...
                                    int modulus = pdolValue.length / 3;
                                    int remainder = pdolValue.length % 3;
                                    int guessedPdolLength = 0;
                                    if (remainder == 0) {
                                        for (int i = 0; i < modulus; i++) {
                                            guessedPdolLength += (int) pdolValue[(i * 3) + 2];
                                        }
                                    } else {
                                        guessedPdolLength = 999;
                                    }
                                    System.out.println("** guessedPdolLength: " + guessedPdolLength);
                                    // need to select AID again because it could be found before, then a selectPdol does not work anymore...
                                    //command = selectApdu(aidSelected);
                                    //responseSelectedAid = nfc.transceive(command);
                                    //System.out.println("selectAid again, result: " + bytesToHex(responseSelectedAid));
                                    //byte[] guessedPdolResult = gp.getPdol(guessedPdolLength);
                                    byte[] guessedPdolResult = pu.getGpo(guessedPdolLength);
                                    if (guessedPdolResult != null) {

                                        // pretty print of response
                                        if (isPrettyPrintResponse) prettyPrintData(etLog, guessedPdolResult);

                                        // read the PAN & Expiration date
                                        String pan_expirationDate = readPanFromFilesFromGpo(nfc, guessedPdolResult);
                                        String[] parts = pan_expirationDate.split("_");
                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                        writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etLog, "PAN: " + parts[0]);
                                        writeToUiAppend(etLog, "Expiration date (YYMMDD): " + parts[1]);
                                        writeToUiAppendNoExport(etData, "");
                                        writeToUiAppendNoExport(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppendNoExport(etData, "PAN: " + parts[0]);
                                        writeToUiAppendNoExport(etData, "Expiration date (YYMMDD): " + parts[1]);
                                    } else {
                                        System.out.println("guessedPdolResult is NULL");
                                    }

                                    // print single data
                                    printSingleData(etLog, applicationTransactionCounter, pinTryCounter, lastOnlineATCRegister, logFormat);

                                }
                            } else { // could not find a tag 0x9f38 in the selectAid response means there is no PDOL request available
                                // instead we use an empty PDOL of length 0
                                // this is usually a mastercard
                                writeToUiAppend(etLog, "No PDOL found in the selectAid response");
                                writeToUiAppend(etLog, "try to request the get processing options (GPO) with an empty PDOL");

                                byte[] responseGpoRequestOk = pu.getGpo(0);
                                if (responseGpoRequestOk != null) {
                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "05 select GPO response length: " + responseGpoRequestOk.length + " data: " + bytesToHex(responseGpoRequestOk));

                                    // pretty print of response
                                    if (isPrettyPrintResponse) prettyPrintData(etLog, responseGpoRequestOk);

/*
https://stackoverflow.com/questions/63547124/unable-to-generate-application-cryptogram
Basis CDOL1:
TAG  LENGTH
9F02 06
9F03 06
9F1A 02
95   05
5F2A 02
9A   03
9C   01
9F37 04
9F35 01
9F45 02
9F4C 08
9F34 03
9F1D 08
9F15 02
9F4E 14

byte_t get_app_crypto[] = {
    0x80, 0xAE, // CLA INS
    0x80, 0x00, // P1 P2
    0x43, // length
    0x00, 0x00, 0x00, 0x00, 0x01, 0x00, // amount
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // other amount
    0x06, 0x42, // terminal country
    0x00, 0x00, 0x00, 0x00, 0x00, // tvr terminal
    0x09, 0x46, // currency code
    0x20, 0x08, 0x23, // transaction date
    0x00, // transaction type
    0x11, 0x22, 0x33, 0x44, // UN
    0x22, // terminal type
    0x00, 0x00,// data auth code
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // icc dynamic
    0x00, 0x00, 0x00, // cvm results
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // 8
    0x54, 0x11, // 2 merchant category
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 14 merchant name or location
    0x00, // LE
    };

AAB Mastercard needs:
I/System.out:          8C 27 -- Card Risk Management Data Object List 1 (CDOL1)
I/System.out:                9F 02 06 -- Amount, Authorised (Numeric)
I/System.out:                9F 03 06 -- Amount, Other (Numeric)
I/System.out:                9F 1A 02 -- Terminal Country Code
I/System.out:                95 05 -- Terminal Verification Results (TVR)
I/System.out:                5F 2A 02 -- Transaction Currency Code
I/System.out:                9A 03 -- Transaction Date
I/System.out:                9C 01 -- Transaction Type
I/System.out:                9F 37 04 -- Unpredictable Number
I/System.out:                9F 35 01 -- Terminal Type
I/System.out:                9F 45 02 -- Data Authentication Code
I/System.out:                9F 4C 08 -- ICC Dynamic Number
I/System.out:                9F 34 03 -- Cardholder Verification (CVM) Results
I/System.out:                9F 21 03 -- Transaction Time (HHMMSS)
I/System.out:                9F 7C 14 -- Merchant Custom Data
              total: 66
 */



/*
I/System.out: get AC command  length: 72 data: 80ae80004200000000010000000000000006420000000000094620082300112233442200000000000000000000000000111009000000000000000000000000000000000000000000
I/System.out: get AC response length: 45 data: 77299f2701809f3602050c9f26084aad83bb1506f1389f10120110a00003240000000000000000000000ff9000
I/System.out: ------------------------------------
I/System.out:
I/System.out: 77 29 -- Response Message Template Format 2
I/System.out:       9F 27 01 -- Cryptogram Information Data
I/System.out:                80 (BINARY)
I/System.out:       9F 36 02 -- Application Transaction Counter (ATC)
I/System.out:                05 0C (BINARY)
I/System.out:       9F 26 08 -- Application Cryptogram
I/System.out:                4A AD 83 BB 15 06 F1 38 (BINARY)
I/System.out:       9F 10 12 -- Issuer Application Data
I/System.out:                01 10 A0 00 03 24 00 00 00 00 00 00 00 00 00 00
I/System.out:                00 FF (BINARY)
I/System.out: 90 00 -- Command successfully executed (OK)
 */
                                    // the template contains the tag 0x9F36 = Application Transaction Counter (ATC) !
                                    // todo get the ATC from response

                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "06 read the files from card and search for tag 0x57 in each file");
                                    String pan_expirationDate = readPanFromFilesFromGpo(nfc, responseGpoRequestOk);
                                    String[] parts = pan_expirationDate.split("_");
                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                    writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                    writeToUiAppend(etLog, "PAN: " + parts[0]);
                                    writeToUiAppend(etLog, "Expiration date (YYMM): " + parts[1]);
                                    writeToUiAppendNoExport(etData, "");
                                    writeToUiAppendNoExport(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                    writeToUiAppendNoExport(etData, "PAN: " + parts[0]);
                                    writeToUiAppendNoExport(etData, "Expiration date (YYMMDD): " + parts[1]);
                                }
                                // print single data
                                printSingleData(etLog, applicationTransactionCounter, pinTryCounter, lastOnlineATCRegister, logFormat);

                                // get application crypto
                                writeToUiAppend(etLog, "");
                                byte[] responseGetAppCrypto = getAcMasterCard(nfc);
                                writeToUiAppend(etLog, "get AC command  length: " + getCommandGetAppCryptoMastercard().length + " data: " + bytesToHex(getCommandGetAppCryptoMastercard()));
                                if (responseGetAppCrypto != null) {
                                    writeToUiAppend(etLog, "get AC response length: " + responseGetAppCrypto.length + " data: " + bytesToHex(responseGetAppCrypto));
                                    // pretty print of response
                                    if (isPrettyPrintResponse) prettyPrintData(etLog, responseGetAppCrypto);
                                } else {
                                    writeToUiAppend(etLog, "get AC failed");
                                }

                                // https://werner.rothschopf.net/201703_arduino_esp8266_nfc.htm example communication
                                // https://mstcompany.net/blog/acquiring-emv-transaction-flow-part-5-read-records
                                // https://github.com/AndreasFagschlunger/O2Xfs
                                // https://nicolas.riousset.com/category/software-methodologies/example-of-a-mastercard-paypass-contactless-transaction/
                                // https://hackernoon.com/examining-your-emv-chip-cards-frc93wa6
                                // https://sdk.supply/list-of-commands-used-in-emv-applications/
                                // https://www.openscdp.org/scripts/tutorial/emv/cardactionanalysis.html Analyzis of generate AC response !
                                // https://www.linkedin.com/pulse/decoding-emv-contactless-kenny-shi
                                // .. The first section is the input and out of readWithPSE() which returns the available AID of A0000000031010 which is one of Visa's AIDs.
                                // Second section is to selectPID(A0000000031010) which returns the PDOL.
                                // Third section is to getProcessingOptions(PDOL), which returns more interesting card data, including:
                                // Track2 equivalent (that contains PAN/card number which I masked, card expiration date)
                                // Card holder name (in this case, this card doesn't return the real name)
                                // Application Cryptogram (AC)
                                // Cryptogram Information Data (CID) - it's 0x80, which means the AC is an Application ReQuest Cryptogram (ARQC), which forces
                                //   the transaction to be online (issuer must authorize the transaction)
                                // Application Transaction Counter (ATC) - it's 0x0032, which is 50 in decimal. This is a sequential counter the card remembers
                                //   and increments whenever there is a new inquiry.
                                // https://www.youtube.com/watch?v=DChVE1NEME0 34C3 - Decoding Contactless (Card) Payments
                                // https://developer.pxp-solutions.com/docs/emv
                                // list of commands:
                                // https://github.com/sasc999/javaemvreader/blob/master/src/main/java/sasc/emv/EMVAPDUCommands.java
                                // https://github.com/sasc999/javaemvreader/blob/master/src/main/java/sasc/emv/system/mastercard/MCTags.java
                                // https://github.com/sasc999/javaemvreader/blob/master/src/main/java/sasc/emv/system/visa/VISATags.java

                                // pin verify https://stackoverflow.com/questions/21019137/emv-verify-command-returning-69-85
                                // https://stackoverflow.com/questions/36300447/is-plaintext-offline-pin-verification-on-emv-card-by-micro-usb-otg-reader
                                // https://stackoverflow.com/questions/52676530/smartcard-verify-pin-apdu-command-problem-in-android




                                String CDOL1 = "9F02069F03069F1A0295055F2A029A039C019F37049F35019F45029F4C089F34039F21039F7C14";
                                String CDOL2 = "910A8A0295059F37049F4C08";
                                byte[] cdol1Byte = hexToBytes(CDOL1);
                                // try to examine CDOL1 for length of fields
                                List<TagAndLength> tagAndLengthParsed = TlvUtil.parseTagAndLength(cdol1Byte);
                                int tlpSize = tagAndLengthParsed.size();
                                System.out.println("*** CDOL1: " + bytesToHex(cdol1Byte));
                                System.out.println("*** tlpSize: " + tlpSize);
                                int valueOfTagSum = 0;
                                for (int i = 0; i < tlpSize; i++) {
                                    System.out.println("*** tlp number: " + i);
                                    TagAndLength tal = tagAndLengthParsed.get(i);
                                    ITag talTag = tal.getTag();
                                    System.out.println("*** talTag getName: " +talTag.getName());
                                    byte[] talTagValue = tal.getBytes();
                                    System.out.println("*** talTag getBytes: " + bytesToHex(talTagValue));
                                    byte[] talTagBytes = tal.getTag().getTagBytes();
                                    System.out.println("*** talTagBytes: " + bytesToHex(talTagBytes));
                                    int talTagNumBytes = tal.getTag().getNumTagBytes();
                                    System.out.println("*** talTagNumBytes: " + talTagNumBytes);
                                    int posLength = talTagBytes.length - talTagNumBytes;
                                    byte[] valueOfTagBytes = Arrays.copyOfRange(talTagValue, talTagNumBytes, talTagValue.length);
                                    System.out.println("*** valueOfTagBytes: " + bytesToHex(valueOfTagBytes));
                                    int valueOfTag;
                                    if (valueOfTagBytes.length > 1) {
                                        valueOfTag = intFromByteArrayV4(valueOfTagBytes);
                                    } else {
                                        valueOfTag = (byte) valueOfTagBytes[0];
                                    }
                                    System.out.println("*** valueOfTag: " + valueOfTag);
                                    valueOfTagSum += valueOfTag;
                                }
                                System.out.println("*** total length of values: " + valueOfTagSum);
                                writeToUiAppend(etLog, "CDOL1 total length: " + valueOfTagSum);

                                byte[] cdol2Byte = hexToBytes(CDOL2);
                                // try to examine CDOL2 for length of fields
                                List<TagAndLength> tagAndLengthParsed2 = TlvUtil.parseTagAndLength(cdol2Byte);
                                int tlp2Size = tagAndLengthParsed2.size();
                                System.out.println("*** CDOL2: " + bytesToHex(cdol2Byte));
                                System.out.println("*** tlp2Size: " + tlp2Size);
                                int valueOfTagSum2 = 0;
                                for (int i = 0; i < tlp2Size; i++) {
                                    System.out.println("*** tlp number: " + i);
                                    TagAndLength tal = tagAndLengthParsed.get(i);
                                    ITag talTag = tal.getTag();
                                    System.out.println("*** talTag getName: " +talTag.getName());
                                    byte[] talTagValue = tal.getBytes();
                                    System.out.println("*** talTag getBytes: " + bytesToHex(talTagValue));
                                    byte[] talTagBytes = tal.getTag().getTagBytes();
                                    System.out.println("*** talTagBytes: " + bytesToHex(talTagBytes));
                                    int talTagNumBytes = tal.getTag().getNumTagBytes();
                                    System.out.println("*** talTagNumBytes: " + talTagNumBytes);
                                    byte[] valueOfTagBytes = Arrays.copyOfRange(talTagValue, talTagNumBytes, talTagValue.length);
                                    System.out.println("*** valueOfTagBytes: " + bytesToHex(valueOfTagBytes));
                                    int valueOfTag;
                                    if (valueOfTagBytes.length > 1) {
                                        valueOfTag = intFromByteArrayV4(valueOfTagBytes);
                                    } else {
                                        valueOfTag = (byte) valueOfTagBytes[0];
                                    }
                                    System.out.println("*** valueOfTag: " + valueOfTag);
                                    valueOfTagSum2 += valueOfTag;
                                }
                                System.out.println("*** total length of values: " + valueOfTagSum2);
                                writeToUiAppend(etLog, "CDOL2 total length: " + valueOfTagSum2);

/*
8D 0C -- Card Risk Management Data Object List 2 (CDOL2)
               91 0a -- Issuer Authentication Data
               8A 02 -- Authorisation Response Code
               95 05 -- Terminal Verification Results (TVR)
               9F 37 04 -- Unpredictable Number
               9F 4C 08 -- ICC Dynamic Number
 */
                            }
                        }
                    }
                }

            } catch (IOException e) {
                Log.e(TAG, "IsoDep Error on connecting to card: " + e.getMessage());
                //throw new RuntimeException(e);
            }
            try {
                nfc.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        playDoublePing();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        }
    }

    /**
     * section for brute force reading of afl
     */

    private void completeFileReading(IsoDep nfc) {
        writeToUiAppend(etLog, "");
        writeToUiAppend(etLog, "complete reading of files in EMV card");

        String resultString = "";
        StringBuilder sb = new StringBuilder();
        for (int sfi = 1; sfi < 10; ++sfi) {
            for (int record = 1; record < 10; ++record) {
                byte[] readResult = readFile(nfc, sfi, record);
                sb.append("SFI: ").append(String.valueOf(sfi)).append("\n");
                sb.append("Record: ").append(String.valueOf(record)).append("\n");
                if (readResult != null) {
                    sb.append(bytesToHex(readResult)).append("\n");
                } else {
                    sb.append("NULL").append("\n");
                }
                sb.append("-----------------------").append("\n");
            }
        }
        resultString = sb.toString();
        writeToUiAppendNoExport(etData, resultString);
        writeToUiAppend(etLog, "reading complete");
    }

    /**
     * reads a single file (sector) of an EMV card
     * source: https://stackoverflow.com/a/38999989/8166854 answered Aug 17, 2016
     * by Michael Roland
     *
     * @param nfc
     * @param sfi
     * @param record
     * @return
     */
    private byte[] readFile(IsoDep nfc, int sfi, int record) {
        byte[] cmd = new byte[]{(byte) 0x00, (byte) 0xB2, (byte) 0x00, (byte) 0x04, (byte) 0x00};
        cmd[2] = (byte) (record & 0x0FF);
        cmd[3] |= (byte) ((sfi << 3) & 0x0F8);
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* readFile sfi " + sfi + " record " + record +
                    " result length: " + 0 + " data: NULL");
            return null;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk != null) {
            System.out.println("* readFile sfi " + sfi + " record " + record +
                    " result length: " + resultOk.length + " data: " + bytesToHex(resultOk));
        } else {
            System.out.println("* readFile sfi " + sfi + " record " + record +
                    " result length: " + 0 + " data: NULL");
        }
        return resultOk;
    }

    /**
     * reads all files on card using track2 or afl data
     *
     * @param getProcessingOptions
     * @return a String with PAN and Expiration date if found
     */
    private String readPanFromFilesFromGpo(IsoDep nfc, byte[] getProcessingOptions) {
        String pan = "";
        String expirationDate = "";
        BerTlvParser parser = new BerTlvParser();
        // first check if getProcessingOption contains a tag 0x57 = Track 2 Equivalent Data
        byte[] track2Data = getTagValueFromResult(getProcessingOptions, (byte) 0x57);
        if (track2Data != null) {
            writeToUiAppend(etLog, "found tag 0x57 = Track 2 Equivalent Data");
            String track2DataString = bytesToHex(track2Data);
            int posSeparator = track2DataString.toUpperCase().indexOf("D");
            pan = track2DataString.substring(0, posSeparator);
            expirationDate = track2DataString.substring((posSeparator + 1), (posSeparator + 5));
 //           return pan + "_" + expirationDate;
        } else {
            writeToUiAppend(etLog, "tag 0x57 not found, try to find in tag 0x94 = AFL");
        }
        // search for tag 0x94 = AFL
        BerTlvs tlvsGpo02 = parser.parse(getProcessingOptions);
        BerTlv tag94 = tlvsGpo02.find(new BerTag(0x94));
        if (tag94 != null) {
            byte[] tag94Bytes = tag94.getBytesValue();
            //writeToUiAppend(etLog, "AFL data: " + bytesToHex(tag94Bytes));
            //System.out.println("AFL data: " + bytesToHex(tag94Bytes));
            // split array by 4 bytes
            List<byte[]> tag94BytesList = divideArray(tag94Bytes, 4);
            int tag94BytesListLength = tag94BytesList.size();
            //writeToUiAppend(etLog, "tag94Bytes divided into " + tag94BytesListLength + " arrays");
            for (int i = 0; i < tag94BytesListLength; i++) {
                //writeToUiAppend(etLog, "get sfi + record for array " + i + " data: " + bytesToHex(tag94BytesList.get(i)));
                // get sfi from first byte, 2nd byte is first record, 3rd byte is last record, 4th byte is offline transactions
                byte[] tag94BytesListEntry = tag94BytesList.get(i);
                byte sfiOrg = tag94BytesListEntry[0];
                byte rec1 = tag94BytesListEntry[1];
                byte recL = tag94BytesListEntry[2];
                byte offl = tag94BytesListEntry[3]; // offline authorization
                //writeToUiAppend(etLog, "sfiOrg: " + sfiOrg + " rec1: " + ((int) rec1) + " recL: " + ((int) recL));
                int sfiNew = (byte) sfiOrg | 0x04; // add 4 = set bit 3
                //writeToUiAppend(etLog, "sfiNew: " + sfiNew + " rec1: " + ((int) rec1) + " recL: " + ((int) recL));

                // read records
                byte[] resultReadRecord = new byte[0];

                for (int iRecords = (int) rec1; iRecords <= (int) recL; iRecords++) {
                    //System.out.println("** for loop start " + (int) rec1 + " to " + (int) recL + " iRecords: " + iRecords);

                    //System.out.println("*#* readRecors iRecords: " + iRecords);
                    byte[] cmd = hexToBytes("00B2000400");
                    cmd[2] = (byte) (iRecords & 0x0FF);
                    cmd[3] |= (byte) (sfiNew & 0x0FF);
                    try {
                        resultReadRecord = nfc.transceive(cmd);
                        //writeToUiAppend(etLog, "readRecordCommand length: " + cmd.length + " data: " + bytesToHex(cmd));
                        byte[] resultReadRecordOk = checkResponse(resultReadRecord);
                        if (resultReadRecordOk != null) {

                            // pretty print of response
                            if (isPrettyPrintResponse) {
                                writeToUiAppend(etLog, "data from file SFI " + sfiOrg + " record " + iRecords);
                                prettyPrintData(etLog, resultReadRecordOk);
                            }
                            // this is the shortened one
                            try {
                                BerTlvs tlvsAfl = parser.parse(resultReadRecordOk);
                                // todo there could be a 57 Track 2 Equivalent Data field as well
                                // 5a = Application Primary Account Number (PAN)
                                // 5F34 = Application Primary Account Number (PAN) Sequence Number
                                // 5F25  = Application Effective Date (card valid from)
                                // 5F24 = Application Expiration Date
                                BerTlv tag5a = tlvsAfl.find(new BerTag(0x5a));
                                if (tag5a != null) {
                                    byte[] tag5aBytes = tag5a.getBytesValue();
                                    pan = bytesToHex(tag5aBytes);
                                }
                                BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                if (tag5f24 != null) {
                                    byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                    expirationDate = bytesToHex(tag5f24Bytes);
                                } else {
                                    // System.out.println("record: " + iRecords + " Tag 5F24 not found");
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                //System.out.println("ERROR: ArrayOutOfBoundsException: " + e.getMessage());
                            }
                        } else {
                            //writeToUiAppend(etLog, "ERROR: read record failed, result: " + bytesToHex(resultReadRecord));
                            resultReadRecord = new byte[0];
                        }
                    } catch (IOException e) {
                        //throw new RuntimeException(e);
                        //return "";
                    }
                }
            } // for (int i = 0; i < tag94BytesListLength; i++) { // = number of records belong to this afl
        }
        return pan + "_" + expirationDate;
    }

    /**
     * gets the byte value of a tag from transceive response
     *
     * @param data
     * @param search
     * @return
     */
    private byte[] getTagValueFromResult(byte[] data, byte... search) {
        int argumentsLength = search.length;
        if (argumentsLength < 1) return null;
        if (argumentsLength > 2) return null;
        if (data.length > 253) return null;
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvDatas = parser.parse(data);
        BerTlv tag;
        if (argumentsLength == 1) {
            tag = tlvDatas.find(new BerTag(search[0]));
        } else {
            tag = tlvDatas.find(new BerTag(search[0], search[1]));
        }
        byte[] tagBytes;
        if (tag == null) {
            return null;
        } else {
            return tag.getBytesValue();
        }
    }

    public static List<byte[]> divideArray(byte[] source, int chunksize) {
        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }
        return result;
    }

    private byte[] checkResponse(byte[] data) {
        System.out.println("checkResponse: " + bytesToHex(data));
        //if (data.length < 5) return null; // not ok
        if (data.length < 5) {
            System.out.println("checkResponse: data length " + data.length);
            return null;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x9000) {
            System.out.println("status: " + status);
            return null;
        } else {
            System.out.println("will return: " + bytesToHex(Arrays.copyOfRange(data, 0, data.length - 2)));
            return Arrays.copyOfRange(data, 0, data.length - 2);
        }
    }

    private boolean responseSendWithPdolFailure(byte[] data) {
        byte[] RESULT_FAILUE = hexToBytes("6700");
        if (Arrays.equals(data, RESULT_FAILUE)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean responseNotAllowed(byte[] data) {
        byte[] RESULT_FAILUE = hexToBytes("6a82");
        if (Arrays.equals(data, RESULT_FAILUE)) {
            return true;
        } else {
            return false;
        }
    }

    // https://stackoverflow.com/a/51338700/8166854
    private byte[] selectApdu(byte[] aid) {
        byte[] commandApdu = new byte[6 + aid.length];
        commandApdu[0] = (byte) 0x00;  // CLA
        commandApdu[1] = (byte) 0xA4;  // INS
        commandApdu[2] = (byte) 0x04;  // P1
        commandApdu[3] = (byte) 0x00;  // P2
        commandApdu[4] = (byte) (aid.length & 0x0FF);       // Lc
        System.arraycopy(aid, 0, commandApdu, 5, aid.length);
        commandApdu[commandApdu.length - 1] = (byte) 0x00;  // Le
        return commandApdu;
    }

    /**
     * section for single read commands
     * overview: https://github.com/sasc999/javaemvreader/blob/master/src/main/java/sasc/emv/EMVAPDUCommands.java
     */

    // Get the data of ATC(Application Transaction Counter, tag '9F36')), template 77 or 80
    private byte[] getApplicationTransactionCounter(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x36, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* getApplicationTransactionCounter failed");
            return null;
        }
        //System.out.println("*** getAtc: " + bytesToHex(result));
        // e.g. visa returns 9f360200459000
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            return null;
        } else {
            return getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x36);
        }
    }

    private byte[] getPinTryCounter(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x17, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* getPinTryCounterCounter failed");
            return null;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            return null;
        } else {
            return getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x17);
        }
    }

    private byte[] getLastOnlineATCRegister(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x13, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* getLastOnlineATCRegister failed");
            return null;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            return null;
        } else {
            return getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x13);
        }
    }

    private byte[] getLogFormat(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x4F, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* getLastOnlineATCRegister failed");
            return null;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            return null;
        } else {
            return getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x4F);
        }
    }

    private byte[] getCommandGetAppCryptoVisacard() {
        // https://stackoverflow.com/questions/63547124/unable-to-generate-application-cryptogram
        // generate AC https://stackoverflow.com/questions/66419082/emv-issuer-authenticate-in-second-generate-ac
        /*
        // does not run
        return new byte[]{
                // empty cdol1
                (byte) 0x80, (byte) 0xAE, // CLA INS
                (byte) 0x40, 0x00, // P1 P2
                0x00, // length // hex 00 decimal = 00 hex = empty
                0x00, // LE
        };
        */
        // does not run, returns 6a81
        return hexBlankToBytes("80 AE 40 00 2B 00 00 00 00 02 01 00 00 00 00 00 00 08 26 00 00 00 00 00 08 26 20 07 01 00 30 90 1B 6A 22 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        // https://stackoverflow.com/questions/62745297/generate-application-cryptogram-response-6985-not-using-pdol
        // 80 AE 40 00 2B 00 00 00 00 02 01 00 00 00 00 00 00 08 26 00 00 00 00 00 08 26 20 07 01 00 30 90 1B 6A 22 00 00 00 00 00 00 00 00 00 00 00 00 00 00
    }

    private byte[] getApplicationCrypto(IsoDep nfc, byte[] command) {
        // https://stackoverflow.com/questions/63547124/unable-to-generate-application-cryptogram
        // generate AC https://stackoverflow.com/questions/66419082/emv-issuer-authenticate-in-second-generate-ac
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(command);
            System.out.println("getApplicationCrypto result: " + bytesToHex(result));
            //result = nfc.transceive(getCommandGetAppCryptoMastercard2());
        } catch (IOException e) {
            System.out.println("* getAC failed: " + e.getMessage());
            return null;
        }
        byte[] resultOk = checkResponse(result);

        if (resultOk == null) {
            return null;
        } else {
            return resultOk;
        }
    }

    private byte[] getCommandGetAppCryptoMastercard() {
        // https://stackoverflow.com/questions/63547124/unable-to-generate-application-cryptogram
        // generate AC https://stackoverflow.com/questions/66419082/emv-issuer-authenticate-in-second-generate-ac
        // runs with MC AAB
        return new byte[]{
                (byte) 0x80, (byte) 0xAE, // CLA INS
                (byte) 0x80, 0x00, // P1 P2
                0x42, // length // hex 66 decimal = 42 hex
                0x00, 0x00, 0x00, 0x00, 0x01, 0x00, // amount ok
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // other amount ok
                0x06, 0x42, // terminal country ok
                0x00, 0x00, 0x00, 0x00, 0x00, // tvr terminal ok
                0x09, 0x46, // currency code ok
                0x20, 0x08, 0x23, // transaction date ok, todo fix date ?
                0x00, // transaction type ok
                0x11, 0x22, 0x33, 0x44, // UN ok
                0x22, // terminal type ok
                0x00, 0x00,// data auth code ok
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // icc dynamic ok
                0x00, 0x00, 0x00, // cvm results ok
                0x11, 0x10, 0x09, // Transaction Time (HHMMSS) added
                //0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // 8 cut
                //0x54, 0x11, // 2 merchant category cut
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// 14 merchant name or location now 20 bytes
                0x00, // LE
        };
    }


    private byte[] getCommandGetAppCryptoMastercard2() {
        // runs with MC Lloyds old
        // https://stackoverflow.com/questions/63547124/unable-to-generate-application-cryptogram
        // generate AC https://stackoverflow.com/questions/66419082/emv-issuer-authenticate-in-second-generate-ac
        return new byte[]{
                (byte) 0x80, (byte) 0xAE, // CLA INS
                (byte) 0x80, 0x00, // P1 P2
                0x2B, // length // hex 43 decimal = 2B hex
                0x00, 0x00, 0x00, 0x00, 0x01, 0x00, // 6 amount ok
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 6 other amount ok
                0x06, 0x42, // 2 terminal country ok
                0x00, 0x00, 0x00, 0x00, 0x00, // 5 tvr terminal ok
                0x09, 0x46, // 2 currency code ok
                0x20, 0x08, 0x23, // 3 transaction date ok
                0x00, // 1 transaction type ok
                0x11, 0x22, 0x33, 0x44, // 4 UN ok
                0x22, // 1 terminal type ok (30 up to here)
                0x00, 0x00,// 2 data auth code ok
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 8 icc dynamic ok
                0x00, 0x00, 0x00, // 3 cvm results ok total 43
                0x00, // LE
        };
    }

    private byte[] getAcMasterCard(IsoDep nfc) {
        // https://stackoverflow.com/questions/63547124/unable-to-generate-application-cryptogram
        // generate AC https://stackoverflow.com/questions/66419082/emv-issuer-authenticate-in-second-generate-ac
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(getCommandGetAppCryptoMastercard());
            //result = nfc.transceive(getCommandGetAppCryptoMastercard2());
        } catch (IOException e) {
            System.out.println("* getAC failed");
            return null;
        }
        byte[] resultOk = checkResponse(result);

        if (resultOk == null) {
            return null;
        } else {
            return resultOk;
        }
    }

    /**
     * section for activity workflow - important is the disabling of the ReaderMode when activity is pausing
     */

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled())
                showWirelessSettings();
            Bundle options = new Bundle();
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);
            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag afer reading
            mNfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(this);
    }

    /**
     * section for UI
     */

    private void provideTextViewDataForExport(TextView textView) {
        System.out.println("*# get Data:" + textView.getText().toString());
        exportString = textView.getText().toString();
        System.out.println("*# get Data:" + exportString);
    }

    private void prettyPrintData(TextView textView, byte[] responseData) {
            writeToUiAppend(textView, "------------------------------------");
            String responseGetAppCryptoString = TlvUtil.prettyPrintAPDUResponse(responseData);
            writeToUiAppend(textView, trimLeadingLineFeeds(responseGetAppCryptoString));
            writeToUiAppend(textView, "------------------------------------");
    }

    public static String trimLeadingLineFeeds (String input) {
        String[] output = input.split("^\\n+", 2);
        return output.length > 1 ? output[1] : output[0];
    }

    private void printSingleData(TextView textView, byte[] applicationTransactionCounter, byte[] pinTryCounter, byte[] lastOnlineATCRegister, byte[] logFormat) {
        writeToUiAppend(etLog, "");
        writeToUiAppend(etLog, "single data retrieved from card");
        if (applicationTransactionCounter != null) {
            writeToUiAppend(etLog, "applicationTransactionCounter: " + bytesToHex(applicationTransactionCounter)
            + " (hex), " + BinaryUtils.intFromByteArrayV4(applicationTransactionCounter) + " (dec)");
        } else {
            writeToUiAppend(etLog, "applicationTransactionCounter: NULL");
        }
        if (pinTryCounter != null) {
            writeToUiAppend(etLog, "pinTryCounter: " + bytesToHex(pinTryCounter));
        } else {
            writeToUiAppend(etLog, "pinTryCounter: NULL");
        }
        if (lastOnlineATCRegister != null) {
            writeToUiAppend(etLog, "lastOnlineATCRegister: " + bytesToHex(lastOnlineATCRegister));
        } else {
            writeToUiAppend(etLog, "lastOnlineATCRegister: NULL");
        }
        if (logFormat != null) {
            writeToUiAppend(etLog, "logFormat: " + bytesToHex(logFormat));
        } else {
            writeToUiAppend(etLog, "logFormat: NULL");
        }
    }

    // special version, needs a boolean variable in class header: boolean debugPrint = true;
    // if true this method will print the output additionally to the console
    // a second variable is need for export of a log file exportString
    private void writeToUiAppend(TextView textView, String message) {
        exportString += message + "\n";
        runOnUiThread(() -> {
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(message);
            } else {
                String newString = textView.getText().toString() + "\n" + message;
                textView.setText(newString);
            }
            if (debugPrint) System.out.println(message);
        });
    }

    // special version, needs a boolean variable in class header: boolean debugPrint = true;
    // if true this method will print the output additionally to the console
    // this version does not append the string to the exportString
    private void writeToUiAppendNoExport(TextView textView, String message) {
        runOnUiThread(() -> {
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(message);
            } else {
                String newString = textView.getText().toString() + "\n" + message;
                textView.setText(newString);
            }
            if (debugPrint) System.out.println(message);
        });
    }

    private void writeToUiAppendOrg(TextView textView, String message) {
        runOnUiThread(() -> {
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(message);
            } else {
                String newString = textView.getText().toString() + "\n" + message;
                textView.setText(newString);
            }
        });
    }

    private void writeToUiAppendReverse(TextView textView, String message) {
        runOnUiThread(() -> {
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + textView.getText().toString();
                textView.setText(newString);
            }
        });
    }

    private void writeToUiToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * section OptionsMenu mail data methods
     */

    private void exportMail() {
        if (exportString.isEmpty()) {
            writeToUiToast("Scan a tag first before sending emails :-)");
            return;
        }
        String subject = "Dump data";
        String body = exportString;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * section OptionsMenu export text file methods
     */

    private void exportTextFile() {
        if (exportString.isEmpty()) {
            writeToUiToast("Scan a tag first before writing files :-)");
            return;
        }
        writeStringToExternalSharedStorage();
    }

    private void writeStringToExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //boolean pickerInitialUri = false;
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        // get filename from edittext
        String filename = exportStringFileName;
        // sanity check
        if (filename.equals("")) {
            writeToUiToast("scan a tag before writing the content to a file :-)");
            return;
        }
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        selectTextFileActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> selectTextFileActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            // Perform operations on the document using its URI.
                            try {
                                // get file content from edittext
                                String fileContent = exportString;
                                System.out.println("## data to write: " + exportString);
                                writeTextToUri(uri, fileContent);
                                writeToUiToast("file written to external shared storage: " + uri.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                                writeToUiToast("ERROR: " + e.toString());
                                return;
                            }
                        }
                    }
                }
            });

    private void writeTextToUri(Uri uri, String data) throws IOException {
        try {
            System.out.println("** data to write: " + data);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().getContentResolver().openOutputStream(uri));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            System.out.println("Exception File write failed: " + e.toString());
        }
    }

    /**
     * section for OptionsMenu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mMainActivity = menu.findItem(R.id.action_activity_main);
        mMainActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                //Intent intent = new Intent(MainActivity.this, MainActivity.class);
                //startActivity(intent);
                return false;
            }
        });

        MenuItem mFileReaderActivity = menu.findItem(R.id.action_activity_file_reader);
        mFileReaderActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, FileReaderActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mExportEmulationDataActivity = menu.findItem(R.id.action_activity_export_emulation_data);
        mExportEmulationDataActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MainActivity.this, ExportEmulationDataActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mExportMail = menu.findItem(R.id.action_export_mail);
        mExportMail.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportMail");
                exportMail();
                return false;
            }
        });

        MenuItem mExportTextFile = menu.findItem(R.id.action_export_text_file);
        mExportTextFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportTextFile");
                exportTextFile();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}