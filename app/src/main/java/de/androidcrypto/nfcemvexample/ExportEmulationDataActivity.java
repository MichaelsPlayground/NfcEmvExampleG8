package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;
import static de.androidcrypto.nfcemvexample.BinaryUtils.intToByteArrayV4;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.GsonBuilder;
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.nfcemvexample.emulate.Aid;
import de.androidcrypto.nfcemvexample.emulate.Aids;
import de.androidcrypto.nfcemvexample.emulate.FilesModel;
import de.androidcrypto.nfcemvexample.nfccreditcards.AidValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.DolValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.PdolUtil;
import de.androidcrypto.nfcemvexample.nfccreditcards.TagValues;

public class ExportEmulationDataActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "ExportEmulationDataAct";

    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etData, etLog, etGivenName;
    SwitchMaterial prettyPrintResponse;
    private View loadingLayout;

    private NfcAdapter mNfcAdapter;
    private byte[] tagId;

    final String TechIsoDep = "android.nfc.tech.IsoDep";

    boolean debugPrint = true; // if set to true the writeToUi method will print to console
    boolean isPrettyPrintResponse = false; // default
    String aidSelectedForAnalyze = "";
    String aidSelectedForAnalyzeName = "";

    byte[] tag0x8cFound = new byte[0]; // tag 0x8c = CDOL1
    String foundPan = "";
    String outputString = ""; // used for the UI output

    // exporting the data
    String givenName;
    String exportString = "";
    String exportJsonString = "";
    String exportStringFileName = "emv.html";
    String exportJsonFileName = "emv.json";
    String stepSeparatorString = "*********************************";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_emulation_data);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        tv1 = findViewById(R.id.tv1);
        etGivenName = findViewById(R.id.etGivenName);
        etData = findViewById(R.id.etData);
        etLog = findViewById(R.id.etLog);
        prettyPrintResponse = findViewById(R.id.swPrettyPrint);
        loadingLayout = findViewById(R.id.loading_layout);

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
        if (TextUtils.isEmpty(etGivenName.getText().toString())) {
            writeToUiToast("before reading the card you need to provide a name for this card");
            return;
        } else {
            givenName = etGivenName.getText().toString();
        }
        playPing();
        setLoadingLayoutVisibility(true);
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
        MediaPlayer mp = MediaPlayer.create(ExportEmulationDataActivity.this, R.raw.single_ping);
        mp.start();
    }

    private void playDoublePing() {
        MediaPlayer mp = MediaPlayer.create(ExportEmulationDataActivity.this, R.raw.double_ping);
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
                //byte[] command;
                writeToUiAppend(etLog, "");
                printStepHeader(etLog, 1, "select PPSE");
                byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PPSE
                byte[] selectPpseCommand = selectApdu(PPSE);
                byte[] selectPpseResponse = nfc.transceive(selectPpseCommand);
                writeToUiAppend(etLog, "01 select PPSE command length " + selectPpseCommand.length + " data: " + bytesToHex(selectPpseCommand));
                writeToUiAppend(etLog, "01 select PPSE response length " + selectPpseResponse.length + " data: " + bytesToHex(selectPpseResponse));
                boolean selectPpseNotAllowed = responseNotAllowed(selectPpseResponse);
                if (selectPpseNotAllowed) {
                    // todo The card must not have a PSE or PPSE, then try with known AIDs
                    writeToUiAppend(etLog, "01 selecting PPSE is not allowed on card");
                    writeToUiAppend(etLog, "");
                    writeToUiAppend(etLog, "The card is not a credit card, reading aborted");
                    setLoadingLayoutVisibility(false);
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                byte[] selectPpseResponseOk = checkResponse(selectPpseResponse);
                Aids aids = null;
                if (selectPpseResponseOk != null) {
                    aids = null;
                    // pretty print of response
                    if (isPrettyPrintResponse) prettyPrintData(etLog, selectPpseResponseOk);

                    writeToUiAppend(etLog, "");
                    printStepHeader(etLog, 2, "search applications on card");
                    writeToUiAppend(etLog, "02 analyze select PPSE response and search for tag 0x4F (applications on card)");

                    BerTlvParser parser = new BerTlvParser();
                    BerTlvs tlv4Fs = parser.parse(selectPpseResponseOk);
                    // by searching for tag 4f
                    List<BerTlv> tag4fList = tlv4Fs.findAll(new BerTag(0x4F));
                    if (tag4fList.size() < 1) {
                        writeToUiAppend(etLog, "there is no tag 0x4F available, stopping here");
                        setLoadingLayoutVisibility(false);
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
                        byte[] tlv4fBytes = tlv4f.getBytesValue();
                        aidList.add(tlv4fBytes);
                        writeToUiAppend(etLog, "application Id (AID): " + bytesToHex(tlv4fBytes));
                    }
                /*
                // old code starts here
                writeToUiAppend(etLog, "try to read a payment card with PPSE");
                byte[] command;
                writeToUiAppend(etLog, "");
                printStepHeader(etLog, 1, "select PPSE");
                byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PPSE
                command = selectApdu(PPSE);
                byte[] responsePpse = nfc.transceive(command);
                writeToUiAppend(etLog, "01 select PPSE command length " + command.length + " data: " + bytesToHex(command));
                writeToUiAppend(etLog, "01 select PPSE response length " + responsePpse.length + " data: " + bytesToHex(responsePpse));
                boolean responsePpseNotAllowed = responseNotAllowed(responsePpse);
                if (responsePpseNotAllowed) {
                    // todo The card must not have a PSE or PPSE, then try with known AIDs
                    writeToUiAppend(etLog, "01 selecting PPSE is not allowed on card");
                    writeToUiAppend(etLog, "");
                    writeToUiAppend(etLog, "The card is not a credit card, reading aborted");
                    setLoadingLayoutVisibility(false);
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                byte[] responsePpseOk = checkResponse(responsePpse);
                Aids aids = null; // used for export
                if (responsePpseOk != null) {
                    // pretty print of response
                    if (isPrettyPrintResponse) prettyPrintData(etLog, responsePpseOk);

                    writeToUiAppend(etLog, "");
                    printStepHeader(etLog, 2, "search applications on card");
                    writeToUiAppend(etLog, "02 analyze select PPSE response and search for tag 0x4F (applications on card)");

                    BerTlvParser parser = new BerTlvParser();
                    BerTlvs tlv4Fs = parser.parse(responsePpseOk);
                    // by searching for tag 4f
                    List<BerTlv> tag4fList = tlv4Fs.findAll(new BerTag(0x4F));
                    if (tag4fList.size() < 1) {
                        writeToUiAppend(etLog, "there is no tag 0x4F available, stopping here");
                        setLoadingLayoutVisibility(false);
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
                        byte[] tlv4fBytes = tlv4f.getBytesValue();
                        aidList.add(tlv4fBytes);
                        writeToUiAppend(etLog, "application Id (AID): " + bytesToHex(tlv4fBytes));
                    }
                                        // starting the export with setting up the aids-model ("master file")
                    byte[] firstAid = aidList.get(0);
                    String cardType = aidV.getAidName(firstAid); // taken from the first AID found on card
                    String selectPpseCommand = bytesToHex(command);
                    String selectPpseResponse = bytesToHex(responsePpseOk);
                    int numberOfAid = aidList.size();
                    aids = new Aids(cardType, givenName, selectPpseCommand, selectPpseResponse, numberOfAid);
                    // old code goes up to here
                    */
                    byte[] firstAid = aidList.get(0);
                    String cardType = aidV.getAidName(firstAid); // taken from the first AID found on card
                    String selectPpseCommandString = bytesToHex(selectPpseCommand);
                    String selectPpseResponseString = bytesToHex(selectPpseResponseOk);
                    int numberOfAid = aidList.size();
                    aids = new Aids(cardType, givenName, selectPpseCommandString, selectPpseResponseString, numberOfAid);

                    // step 03: iterating through aidList by selecting AID
                    for (int aidNumber = 0; aidNumber < tag4fList.size(); aidNumber++) {
                        byte[] aidSelected = aidList.get(aidNumber);
                        aidSelectedForAnalyze = bytesToHex(aidSelected);
                        aidSelectedForAnalyzeName = aidV.getAidName(aidSelected);
                        writeToUiAppend(etLog, "");
                        printStepHeader(etLog, 3, "select application by AID");
                        writeToUiAppend(etLog, "03 select application by AID " + aidSelectedForAnalyze + " (number " + (aidNumber + 1) + ")");
                        writeToUiAppend(etLog, "card is a " + aidSelectedForAnalyzeName);
                        byte[] selectAidCommand = selectApdu(aidSelected);
                        byte[] selectAidResponse = nfc.transceive(selectAidCommand);
                        writeToUiAppend(etLog, "");
                        writeToUiAppend(etLog, "03 select AID command length " + selectAidCommand.length + " data: " + bytesToHex(selectAidCommand));
                        boolean responseSelectAidNotAllowed = responseNotAllowed(selectAidResponse);
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

                        byte[] selectAidResponseOk = checkResponse(selectAidResponse);
                        if (selectAidResponseOk != null) {
                            writeToUiAppend(etLog, "03 select AID response length " + selectAidResponseOk.length + " data: " + bytesToHex(selectAidResponseOk));
                            // pretty print of response
                            if (isPrettyPrintResponse)
                                prettyPrintData(etLog, selectAidResponseOk);

                            // intermediate step - get single data from card, will be printed later
                            byte[] applicationTransactionCounter = getApplicationTransactionCounter(nfc);
                            byte[] pinTryCounter = getPinTryCounter(nfc);
                            byte[] lastOnlineATCRegister = getLastOnlineATCRegister(nfc);
                            byte[] logFormat = getLogFormat(nfc);

                            writeToUiAppend(etLog, "");
                            printStepHeader(etLog, 4, "search for tag 0x9F38");
                            writeToUiAppend(etLog, "04 search for tag 0x9F38 in the selectAid response");
                            /**
                             * note: different behaviour between VisaCard, Mastercard and German GiroCards
                             * Mastercard has NO PDOL, Visa gives PDOL in tag 9F38
                             * tag 50 and/or tag 9F12 has an application label or application name
                             * next step: search for tag 9F38 Processing Options Data Object List (PDOL)
                             */
                            BerTlvs tlvsAid = parser.parse(selectAidResponseOk);
                            BerTlv tag9f38 = tlvsAid.find(new BerTag(0x9F, 0x38));
                            // tag9f38 is null when not found
                            if (tag9f38 != null) {
                                /**
                                 * the following code is for VisaCards and (German) GiroCards as we found a PDOL
                                 */
                                writeToUiAppend(etLog, "");
                                writeToUiAppend(etLog, "### processing the VisaCard and GiroCard path ###");
                                writeToUiAppend(etLog, "");
                                byte[] pdolValue = tag9f38.getBytesValue();
                                writeToUiAppend(etLog, "found tag 0x9F38 in the selectAid with this length: " + pdolValue.length + " data: " + bytesToHex(pdolValue));
                                // code will run for VISA and NOT for MasterCard
                                // we are using a generalized selectGpo command

                                /**
                                 * BASIC CODE
                                 */
                                // byte[] commandGpoRequest = hexToBytes(pu.getPdolWithCountryCode()); // basic code

                                /**
                                 * ADVANCED CODE
                                 */
                                byte[] gpoRequestCommand = getGpoFromPdol(pdolValue); // advanced one, build it dynamically

                                writeToUiAppend(etLog, "");
                                printStepHeader(etLog, 5, "get the processing options");
                                writeToUiAppend(etLog, "05 get the processing options command length: " + gpoRequestCommand.length + " data: " + bytesToHex(gpoRequestCommand));
                                byte[] gpoRequestResponse = nfc.transceive(gpoRequestCommand);
                                if (!responseSendWithPdolFailure(gpoRequestResponse)) {
                                    byte[] gpoRequestResponseOk = checkResponse(gpoRequestResponse);
                                    if (gpoRequestResponseOk != null) {
                                        writeToUiAppend(etLog, "05 run GPO response length: " + gpoRequestResponseOk.length + " data: " + bytesToHex(gpoRequestResponseOk));

                                        // pretty print of response
                                        if (isPrettyPrintResponse)
                                            prettyPrintData(etLog, gpoRequestResponseOk);

                                        writeToUiAppend(etLog, "");
                                        printStepHeader(etLog, 6, "read files & search PAN");
                                        writeToUiAppend(etLog, "06 read the files from card and search for tag 0x57 in each file");
                                        String pan_expirationDate = readPanFromFilesFromGpo(nfc, gpoRequestResponseOk);
                                        String[] parts = pan_expirationDate.split("_");
                                        writeToUiAppend(etLog, "");
                                        printStepHeader(etLog, 7, "print PAN & expire date");
                                        writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                        writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etLog, "PAN: " + parts[0]);
                                        writeToUiAppend(etLog, "Expiration date (YYMM): " + parts[1]);
                                        writeToUiAppendNoExport(etData, "");
                                        writeToUiAppendNoExport(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppendNoExport(etData, "PAN: " + parts[0]);
                                        writeToUiAppendNoExport(etData, "Expiration date (YYMM): " + parts[1]);
                                        foundPan = parts[0];
                                        // print single data
                                        printSingleData(etLog, applicationTransactionCounter, pinTryCounter, lastOnlineATCRegister, logFormat);

                                        // internal authentication
                                        // probably not supported
                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "get the internal authentication");
                                        String internalAuthString = "0088000004E153F3E800";
                                        byte[] internalAuthCommand = hexToBytes(internalAuthString);
                                        writeToUiAppend(etLog, "internalAuthCommand: " + internalAuthCommand.length + " data: " + bytesToHex(internalAuthCommand));
                                        byte[] internalAuthResponse = nfc.transceive(internalAuthCommand);
                                        byte[] internalAuthResponseOk = new byte[0];
                                        if (internalAuthResponse != null) {
                                            internalAuthResponseOk = checkResponse(internalAuthResponse);
                                            if (internalAuthResponseOk != null) {
                                                writeToUiAppend(etLog, "internalAuthResponse: " + internalAuthResponseOk.length + " data: " + bytesToHex(internalAuthResponseOk));
                                                prettyPrintData(etLog, internalAuthResponseOk);
                                            }
                                        } else {
                                            writeToUiAppend(etLog, "internalAuthResponse failure");
                                        }

                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "get the application cryptogram");
                                        // check that it was found in any file
                                        writeToUiAppend(etLog, "### tag0x8cFound: " + bytesToHex(tag0x8cFound));
                                        byte[] getApplicationCryptoCommand;
                                        byte[] getApplicationCryptoResponse;
                                        byte[] getApplicationCryptoResponseOk = new byte[0];
                                        if (tag0x8cFound.length > 1) {
                                            getApplicationCryptoCommand = getAppCryptoCommandFromCdol(tag0x8cFound);
                                            writeToUiAppend(etLog, "getApplicationCryptoCommand length: " + getApplicationCryptoCommand.length + " data: " + bytesToHex(getApplicationCryptoCommand));
                                            getApplicationCryptoResponse = nfc.transceive(getApplicationCryptoCommand);
                                            if (getApplicationCryptoResponse != null) {
                                                getApplicationCryptoResponseOk = checkResponse(getApplicationCryptoResponse);
                                                if (getApplicationCryptoResponseOk != null) {
                                                    writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponseOk.length + " data: " + bytesToHex(getApplicationCryptoResponseOk));
                                                    if (isPrettyPrintResponse)
                                                        prettyPrintData(etLog, getApplicationCryptoResponseOk);
                                                } else {
                                                    writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponse.length + " data: " + bytesToHex(getApplicationCryptoResponse));
                                                }
                                            } else {
                                                writeToUiAppend(etLog, "getApplicationCryptoResponse fails");
                                            }
                                        } else {
                                            // no cdol1 found
                                            // work with an empty cdol1
                                            writeToUiAppend(etLog, "no CDOL1 found in files, using an empty one");
                                            getApplicationCryptoCommand = getAppCryptoCommandFromCdol(new byte[0]);
                                            writeToUiAppend(etLog, "getApplicationCryptoCommand length: " + getApplicationCryptoCommand.length + " data: " + bytesToHex(getApplicationCryptoCommand));
                                            getApplicationCryptoResponse = nfc.transceive(getApplicationCryptoCommand);
                                            if (getApplicationCryptoResponse != null) {
                                                getApplicationCryptoResponseOk = checkResponse(getApplicationCryptoResponse);
                                                if (getApplicationCryptoResponseOk != null) {
                                                    writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponseOk.length + " data: " + bytesToHex(getApplicationCryptoResponseOk));
                                                    if (isPrettyPrintResponse)
                                                        prettyPrintData(etLog, getApplicationCryptoResponseOk);
                                                } else {
                                                    writeToUiAppend(etLog, "getApplicationCryptoResponse failure");
                                                }
                                            } else {
                                                writeToUiAppend(etLog, "getApplicationCryptoResponse failure");
                                            }
                                        }

                                        // this is the visacard + girocard processing
                                        // export this aid
                                        String aidCard = aidSelectedForAnalyze;
                                        String aidCardName = aidSelectedForAnalyzeName;
                                        String selectAidCommandString = bytesToHex(selectAidCommand);
                                        String selectAidResponseString = bytesToHex(selectAidResponseOk);
                                        String gpoCommandString = bytesToHex(gpoRequestCommand);
                                        String gpoResponseString = bytesToHex(gpoRequestResponseOk);
                                        int checkFirstBytesGetProcessingOptions = 6;
                                        String panFoundInTrack2Data = "yes";
                                        String panFoundInFiles = "no";
                                        int numberOfFiles = 0;
                                        String aflString = getAflFromGetProcessingOptionsResponse(gpoRequestResponseOk);
                                        String applicationTransactionCounterString = "";
                                        if (applicationTransactionCounter != null)
                                            applicationTransactionCounterString = bytesToHex(applicationTransactionCounter);
                                        String leftPinTryCounterString = "";
                                        if (pinTryCounter != null)
                                            leftPinTryCounterString = bytesToHex(pinTryCounter);
                                        String lastOnlineATCRegisterString = "";
                                        if (lastOnlineATCRegister != null)
                                            lastOnlineATCRegisterString = bytesToHex(lastOnlineATCRegister);
                                        String logFormatString = "";
                                        if (logFormat != null)
                                            logFormatString = bytesToHex(logFormat);
                                        String internalAuthenticationCommandString = "";
                                        if (internalAuthCommand != null)
                                            internalAuthenticationCommandString = bytesToHex(internalAuthCommand);
                                        String internalAuthenticationResponseString = "";
                                        if (internalAuthResponseOk != null)
                                            internalAuthenticationResponseString = bytesToHex(internalAuthResponseOk);
                                        String applicationCryptogramCommandString = "";
                                        if (getApplicationCryptoCommand != null)
                                            applicationCryptogramCommandString = bytesToHex(getApplicationCryptoCommand);
                                        String applicationCryptogramResponseString = "";
                                        if (getApplicationCryptoResponseOk != null)
                                            applicationCryptogramResponseString = bytesToHex(getApplicationCryptoResponseOk);

                                        Aid aidForJson = new Aid(aidCard, aidCardName, selectAidCommandString, selectAidResponseString, gpoCommandString, gpoResponseString,
                                                checkFirstBytesGetProcessingOptions, panFoundInTrack2Data, panFoundInFiles, numberOfFiles, aflString,
                                                applicationTransactionCounterString, leftPinTryCounterString, lastOnlineATCRegisterString, logFormatString,
                                                internalAuthenticationCommandString, internalAuthenticationResponseString, applicationCryptogramCommandString,
                                                applicationCryptogramResponseString, null);
                                        aids.setAidEntry(aidForJson, aidNumber);
                                        // end of exporting
                                    }
                                }
                            } else { // could not find a tag 0x9f38 in the selectAid response means there is no PDOL request available
                                // instead we use an empty PDOL of length 0
                                // this is usually a mastercard
                                /**
                                 * MasterCard code
                                 */
                                writeToUiAppend(etLog, "");
                                writeToUiAppend(etLog, "### processing the MasterCard path ###");
                                writeToUiAppend(etLog, "");

                                writeToUiAppend(etLog, "No PDOL found in the selectAid response");
                                writeToUiAppend(etLog, "try to request the get processing options (GPO) with an empty PDOL");

                                printStepHeader(etLog, 5, "get the processing options");

                                /**
                                 * simple code
                                 */
                                //byte[] responseGpoRequestOk = pu.getGpo(0);

                                /**
                                 * advanced code
                                 */
                                byte[] gpoRequestCommand = getGpoFromPdol(new byte[0]); // empty PDOL

                                writeToUiAppend(etLog, "05 get the processing options command length: " + gpoRequestCommand.length + " data: " + bytesToHex(gpoRequestCommand));
                                byte[] gpoRequestResponse = nfc.transceive(gpoRequestCommand);
                                if (!responseSendWithPdolFailure(gpoRequestResponse)) {
                                    byte[] gpoRequestResponseOk = checkResponse(gpoRequestResponse);
                                    if (gpoRequestResponseOk != null) {
                                        writeToUiAppend(etLog, "05 select GPO response length: " + gpoRequestResponseOk.length + " data: " + bytesToHex(gpoRequestResponseOk));

                                        // pretty print of response
                                        if (isPrettyPrintResponse)
                                            prettyPrintData(etLog, gpoRequestResponseOk);

                                        // the template contains the tag 0x9F36 = Application Transaction Counter (ATC) !
                                        // todo get the ATC from response

                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "06 read the files from card and search for tag 0x57 in each file");
                                        printStepHeader(etLog, 6, "read files & search PAN");
                                        String pan_expirationDate = readPanFromFilesFromGpo(nfc, gpoRequestResponseOk);
                                        String[] parts = pan_expirationDate.split("_");
                                        writeToUiAppend(etLog, "");
                                        printStepHeader(etLog, 7, "print PAN & expire date");
                                        writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                        writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etLog, "PAN: " + parts[0]);
                                        writeToUiAppend(etLog, "Expiration date (YYMM): " + parts[1]);
                                        writeToUiAppendNoExport(etData, "");
                                        writeToUiAppendNoExport(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppendNoExport(etData, "PAN: " + parts[0]);
                                        writeToUiAppendNoExport(etData, "Expiration date (YYMMDD): " + parts[1]);
                                        foundPan = parts[0];
                                    }
                                    // print single data
                                    printSingleData(etLog, applicationTransactionCounter, pinTryCounter, lastOnlineATCRegister, logFormat);

                                    // internal authentication
                                    // probably not supported
                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "get the internal authentication");
                                    String internalAuthString = "0088000004E153F3E800";
                                    byte[] internalAuthCommand = hexToBytes(internalAuthString);
                                    writeToUiAppend(etLog, "internalAuthCommand: " + internalAuthCommand.length + " data: " + bytesToHex(internalAuthCommand));
                                    byte[] internalAuthResponse = nfc.transceive(internalAuthCommand);
                                    byte[] internalAuthResponseOk = new byte[0];
                                    if (internalAuthResponse != null) {
                                        internalAuthResponseOk = checkResponse(internalAuthResponse);
                                        if (internalAuthResponseOk != null) {
                                            writeToUiAppend(etLog, "internalAuthResponse: " + internalAuthResponseOk.length + " data: " + bytesToHex(internalAuthResponseOk));
                                            prettyPrintData(etLog, internalAuthResponseOk);
                                        }
                                    } else {
                                        writeToUiAppend(etLog, "internalAuthResponse failure");
                                    }

                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "get the application cryptogram");
                                    // check that it was found in any file
                                    writeToUiAppend(etLog, "### tag0x8cFound: " + bytesToHex(tag0x8cFound));
                                    byte[] getApplicationCryptoCommand;
                                    byte[] getApplicationCryptoResponse;
                                    byte[] getApplicationCryptoResponseOk = new byte[0];
                                    if (tag0x8cFound.length > 1) {
                                        getApplicationCryptoCommand = getAppCryptoCommandFromCdol(tag0x8cFound);
                                        writeToUiAppend(etLog, "getApplicationCryptoCommand length: " + getApplicationCryptoCommand.length + " data: " + bytesToHex(getApplicationCryptoCommand));
                                        getApplicationCryptoResponse = nfc.transceive(getApplicationCryptoCommand);
                                        if (getApplicationCryptoResponse != null) {
                                            getApplicationCryptoResponseOk = checkResponse(getApplicationCryptoResponse);
                                            if (getApplicationCryptoResponseOk != null) {
                                                writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponseOk.length + " data: " + bytesToHex(getApplicationCryptoResponseOk));
                                                if (isPrettyPrintResponse)
                                                    prettyPrintData(etLog, getApplicationCryptoResponseOk);
                                            } else {
                                                writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponse.length + " data: " + bytesToHex(getApplicationCryptoResponse));
                                            }
                                        } else {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse fails");
                                        }
                                    } else {
                                        // no cdol1 found
                                        // work with an empty cdol1
                                        writeToUiAppend(etLog, "no CDOL1 found in files, using an empty one");
                                        getApplicationCryptoCommand = getAppCryptoCommandFromCdol(new byte[0]);
                                        writeToUiAppend(etLog, "getApplicationCryptoCommand length: " + getApplicationCryptoCommand.length + " data: " + bytesToHex(getApplicationCryptoCommand));
                                        getApplicationCryptoResponse = nfc.transceive(getApplicationCryptoCommand);
                                        if (getApplicationCryptoResponse != null) {
                                            getApplicationCryptoResponseOk = checkResponse(getApplicationCryptoResponse);
                                            if (getApplicationCryptoResponseOk != null) {
                                                writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponseOk.length + " data: " + bytesToHex(getApplicationCryptoResponseOk));
                                                if (isPrettyPrintResponse)
                                                    prettyPrintData(etLog, getApplicationCryptoResponseOk);
                                            } else {
                                                writeToUiAppend(etLog, "getApplicationCryptoResponse failure");
                                            }
                                        } else {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse failure");
                                        }
                                    }

                                    // export this aid
                                    // this is the mastercard processing
                                    String aidCard = aidSelectedForAnalyze;
                                    String aidCardName = aidSelectedForAnalyzeName;
                                    String selectAidCommandString = bytesToHex(selectAidCommand);
                                    String selectAidResponseString = bytesToHex(selectAidResponseOk);
                                    String gpoCommandString = bytesToHex(gpoRequestCommand);
                                    String gpoResponseString = bytesToHex(gpoRequestResponseOk);
                                    int checkFirstBytesGetProcessingOptions = 6;
                                    String panFoundInTrack2Data = "yes";
                                    String panFoundInFiles = "no";
                                    int numberOfFiles = 0;
                                    String aflString = getAflFromGetProcessingOptionsResponse(gpoRequestResponseOk);
                                    String applicationTransactionCounterString = "";
                                    if (applicationTransactionCounter != null)
                                        applicationTransactionCounterString = bytesToHex(applicationTransactionCounter);
                                    String leftPinTryCounterString = "";
                                    if (pinTryCounter != null)
                                        leftPinTryCounterString = bytesToHex(pinTryCounter);
                                    String lastOnlineATCRegisterString = "";
                                    if (lastOnlineATCRegister != null)
                                        lastOnlineATCRegisterString = bytesToHex(lastOnlineATCRegister);
                                    String logFormatString = "";
                                    if (logFormat != null)
                                        logFormatString = bytesToHex(logFormat);
                                    String internalAuthenticationCommandString = "";
                                    if (internalAuthCommand != null)
                                        internalAuthenticationCommandString = bytesToHex(internalAuthCommand);
                                    String internalAuthenticationResponseString = "";
                                    if (internalAuthResponseOk != null)
                                        internalAuthenticationResponseString = bytesToHex(internalAuthResponseOk);
                                    String applicationCryptogramCommandString = "";
                                    if (getApplicationCryptoCommand != null)
                                        applicationCryptogramCommandString = bytesToHex(getApplicationCryptoCommand);
                                    String applicationCryptogramResponseString = "";
                                    if (getApplicationCryptoResponseOk != null)
                                        applicationCryptogramResponseString = bytesToHex(getApplicationCryptoResponseOk);

                                    Aid aidForJson = new Aid(aidCard, aidCardName, selectAidCommandString, selectAidResponseString, gpoCommandString, gpoResponseString,
                                            checkFirstBytesGetProcessingOptions, panFoundInTrack2Data, panFoundInFiles, numberOfFiles, aflString,
                                            applicationTransactionCounterString, leftPinTryCounterString, lastOnlineATCRegisterString, logFormatString,
                                            internalAuthenticationCommandString, internalAuthenticationResponseString, applicationCryptogramCommandString,
                                            applicationCryptogramResponseString, null);
                                    aids.setAidEntry(aidForJson, aidNumber);
                                    // end of exporting

                                }
                            }
                        }
                    }
                }
                // print the complete Log
                writeToUiFinal(etLog);
                setLoadingLayoutVisibility(false);


                // export the file
                if (aids != null) {
                    exportString = new GsonBuilder().setPrettyPrinting().create().toJson(aids, Aids.class);
                    exportStringFileName = "emv.json";
                    writeStringToExternalSharedStorage();
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

    private String getAflFromGetProcessingOptionsResponse(@NonNull byte[] getProcessingOptions) {
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvsGpo02 = parser.parse(getProcessingOptions);
        BerTlv tag94 = tlvsGpo02.find(new BerTag(0x94));
        if (tag94 != null) {
            return bytesToHex(tag94.getBytesValue());
        } else {
            return "";
        }
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


    private byte[] getCommandGetAppCryptoMastercard() {
        // runs with MC AAB
        // https://stackoverflow.com/questions/63547124/unable-to-generate-application-cryptogram
        // generate AC https://stackoverflow.com/questions/66419082/emv-issuer-authenticate-in-second-generate-ac
        return new byte[]{
                (byte) 0x80, (byte) 0xAE, // CLA INS
                (byte) 0x80, 0x00, // P1 P2
                0x42, // length // hex 66 decimal = 42 hex
                0x00, 0x00, 0x00, 0x00, 0x01, 0x00, // 6 amount ok
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 6 other amount ok
                0x06, 0x42, // 2 terminal country ok
                0x00, 0x00, 0x00, 0x00, 0x00, // 5 tvr terminal ok
                0x09, 0x46, // 2 currency code ok
                0x20, 0x08, 0x23, // 3 transaction date ok, todo fix date ?
                0x00, // 1 transaction type ok
                0x11, 0x22, 0x33, 0x44, // 4 UN ok
                0x22, // 1 terminal type ok
                0x00, 0x00,// 2 data auth code ok
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 8 icc dynamic ok
                0x00, 0x00, 0x00, // 3 cvm results ok
                0x11, 0x10, 0x09, // 3 Transaction Time (HHMMSS) added
                //0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  // 8 cut
                //0x54, 0x11, // 2 merchant category cut
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// 20 merchant name or location now 20 bytes
                0x00, // LE
        };
    }

    private byte[] getCommandGetAppCryptoMastercard2() {
        // runs with ??
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
                0x00, 0x00, 0x00, // 3 cvm results ok
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
            System.out.println("*** getAcMasterCard result: " + bytesToHex(result));
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
     * DOL utilities
     */

    private byte[] getGpoFromPdol(@NonNull byte[] pdol) {
        // get the tags in a list
        List<TagAndLength> tagAndLength = TlvUtil.parseTagAndLength(pdol);
        int tagAndLengthSize = tagAndLength.size();
        if (tagAndLengthSize < 1) {
            // there are no pdols in the list
            //Log.e(TAG, "there are no PDOLs in the pdol array, aborted");
            //return null;
            // returning an empty PDOL
            String tagLength2d = "00"; // length value
            String tagLength2dAnd2 = "02"; // length value + 2
            String constructedGpoCommandString = "80A80000" + tagLength2dAnd2 + "83" + tagLength2d + "" + "00";
            return hexToBytes(constructedGpoCommandString);
        }
        int valueOfTagSum = 0; // total length
        StringBuilder sb = new StringBuilder(); // takes the default values of the tags
        DolValues dolValues = new DolValues();
        for (int i = 0; i < tagAndLengthSize; i++) {
            // get a single tag
            TagAndLength tal = tagAndLength.get(i); // eg 9f3704
            byte[] tagToSearch = tal.getTag().getTagBytes(); // gives the tag 9f37
            int lengthOfTag = tal.getLength(); // 4
            valueOfTagSum += tal.getLength(); // add it to the sum
            // now we are trying to find a default value
            byte[] defaultValue = dolValues.getDolValue(tagToSearch);
            byte[] usedValue = new byte[0];
            if (defaultValue != null) {
                if (defaultValue.length > lengthOfTag) {
                    // cut it to correct length
                    usedValue = Arrays.copyOfRange(defaultValue, 0, lengthOfTag);
                    Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default is too long, cut to: " + bytesToHex(usedValue));
                } else if (defaultValue.length < lengthOfTag) {
                    // increase length
                    usedValue = new byte[lengthOfTag];
                    System.arraycopy(defaultValue, 0, usedValue, 0, defaultValue.length);
                    Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default is too short, increased to: " + bytesToHex(usedValue));
                } else {
                    // correct length
                    usedValue = defaultValue.clone();
                    Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default found: " + bytesToHex(usedValue));
                }
            } else {
                // defaultValue is null means the tag was not found in our tags database for default values
                usedValue = new byte[lengthOfTag];
                Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " NO default found, generate zeroed: " + bytesToHex(usedValue));
            }
            // now usedValue does have the correct length
            sb.append(bytesToHex(usedValue));
        }
        String constructedGpoString = sb.toString();
        String tagLength2d = bytesToHex(intToByteArrayV4(valueOfTagSum)); // length value
        String tagLength2dAnd2 = bytesToHex(intToByteArrayV4(valueOfTagSum + 2)); // length value + 2
        String constructedGpoCommandString = "80A80000" + tagLength2dAnd2 + "83" + tagLength2d + constructedGpoString + "00";
        return hexToBytes(constructedGpoCommandString);
    }

    private byte[] getAppCryptoCommandFromCdol(@NonNull byte[] cdol) {
        // get the tags in a list
        List<TagAndLength> tagAndLength = TlvUtil.parseTagAndLength(cdol);
        int tagAndLengthSize = tagAndLength.size();
        if (tagAndLengthSize < 1) {
            // there are no cdols in the list
            Log.e(TAG, "there are no CDOLs in the cdol array, aborted");
            //return null;
            // returning an empty cdolCommand
            String constructedGetAcCommandString = "80AE8000" + "00" + "" + "00";
            return hexToBytes(constructedGetAcCommandString);
        }
        int valueOfTagSum = 0; // total length
        StringBuilder sb = new StringBuilder(); // takes the default values of the tags
        DolValues dolValues = new DolValues();
        for (int i = 0; i < tagAndLengthSize; i++) {
            // get a single tag
            TagAndLength tal = tagAndLength.get(i); // eg 9F0206
            byte[] tagToSearch = tal.getTag().getTagBytes(); // gives the tag 9F02
            int lengthOfTag = tal.getLength(); // 2
            valueOfTagSum += tal.getLength(); // add it to the sum
            // now we are trying to find a default value
            byte[] defaultValue = dolValues.getDolValue(tagToSearch);
            byte[] usedValue = new byte[0];
            if (defaultValue != null) {
                if (defaultValue.length > lengthOfTag) {
                    // cut it to correct length
                    usedValue = Arrays.copyOfRange(defaultValue, 0, lengthOfTag);
                    Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default is too long, cut to: " + bytesToHex(usedValue));
                } else if (defaultValue.length < lengthOfTag) {
                    // increase length
                    usedValue = new byte[lengthOfTag];
                    System.arraycopy(defaultValue, 0, usedValue, 0, defaultValue.length);
                    Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default is too short, increased to: " + bytesToHex(usedValue));
                } else {
                    // correct length
                    usedValue = defaultValue.clone();
                    Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default found: " + bytesToHex(usedValue));
                }
            } else {
                // defaultValue is null means the tag was not found in our tags database for default values
                usedValue = new byte[lengthOfTag];
                Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " NO default found, generate zeroed: " + bytesToHex(usedValue));
            }
            // now usedValue does have the correct length
            sb.append(bytesToHex(usedValue));
        }
        String constructedGetAcString = sb.toString();
        String tagLength2d = bytesToHex(intToByteArrayV4(valueOfTagSum)); // length value
        String constructedGetAcCommandString = "80AE8000" + tagLength2d + constructedGetAcString + "00";
        return hexToBytes(constructedGetAcCommandString);
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

    private void printStepHeader(TextView textView, int step, String message) {
        // message should not extend 29 characters
        String emptyMessage = "                                 ";
        writeToUiAppend(textView, "");
        writeToUiAppend(textView, stepSeparatorString);
        writeToUiAppend(textView, "************ step  " + String.valueOf(step) + " ************");
        //writeToUiAppend(textView, "*            step " + String.valueOf(step) + "             *");
        writeToUiAppend(textView, "* " + (message + emptyMessage).substring(0, 29) + " *");
        writeToUiAppend(textView, stepSeparatorString);
    }

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

    public static String trimLeadingLineFeeds(String input) {
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
    // to avoid heavy printing on UI thread this method "prints" message to an outputString variable
    // to show the messages you need to call writeToUiFinal(TextView view)
    private void writeToUiAppend(final TextView textView, String message) {
        exportString += message + "\n";
        runOnUiThread(() -> {
            if (TextUtils.isEmpty(textView.getText().toString())) {
                if (textView == (TextView) etLog) {
                    outputString += message + "\n";
                } else {
                    textView.setText(message);
                }
            } else {
                String newString = textView.getText().toString() + "\n" + message;
                if (textView == (TextView) etLog) {
                    outputString += newString + "\n";
                } else {
                    textView.setText(newString);
                }
            }
            if (debugPrint) System.out.println(message);
        });
    }

    private void writeToUiFinal(final TextView textView) {
        if (textView == (TextView) etLog) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(outputString);
                    outputString = ""; // clear the outputString
                }
            });
        }
    }

    // special version, needs a boolean variable in class header: boolean debugPrint = true;
    // if true this method will print the output additionally to the console
    // a second variable is need for export of a log file exportString
    private void writeToUiAppendOld(TextView textView, String message) {
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

    private void setLoadingLayoutVisibility(boolean isVisible) {
        runOnUiThread(() -> {
            if (isVisible) {
                loadingLayout.setVisibility(View.VISIBLE);
            } else {
                loadingLayout.setVisibility(View.GONE);
            }
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
                Intent intent = new Intent(ExportEmulationDataActivity.this, FileReaderActivity.class);
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