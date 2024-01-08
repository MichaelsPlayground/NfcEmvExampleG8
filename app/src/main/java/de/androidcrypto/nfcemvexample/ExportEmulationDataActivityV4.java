package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;
import static de.androidcrypto.nfcemvexample.BinaryUtils.intToByteArrayV4;

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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

import org.apache.commons.lang3.ArrayUtils;

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

public class ExportEmulationDataActivityV4 extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "ExportEmulationDataAct";

    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etData, etLog, etGivenName;
    RadioButton saveNotAnonymized, saveAnonymized, saveRandomAnonymized;
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
        saveNotAnonymized = findViewById(R.id.rbSaveNotAnonymized);
        saveAnonymized = findViewById(R.id.rbSaveAnonymized);
        saveRandomAnonymized = findViewById(R.id.rbSaveRandomAnonymized);
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
        MediaPlayer mp = MediaPlayer.create(ExportEmulationDataActivityV4.this, R.raw.single_ping);
        mp.start();
    }

    private void playDoublePing() {
        MediaPlayer mp = MediaPlayer.create(ExportEmulationDataActivityV4.this, R.raw.double_ping);
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
                            byte[] gpoRequestCommand;
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
                                gpoRequestCommand = getGpoFromPdol(pdolValue); // advanced one, build it dynamically
                            } else {
                                // could not find a tag 0x9f38 in the selectAid response means there is no PDOL request available
                                // instead we use an empty PDOL of length 0
                                /**
                                 * MasterCard code
                                 */
                                writeToUiAppend(etLog, "");
                                writeToUiAppend(etLog, "### processing the MasterCard path ###");
                                writeToUiAppend(etLog, "");

                                writeToUiAppend(etLog, "No PDOL found in the selectAid response");
                                writeToUiAppend(etLog, "try to request the get processing options (GPO) with an empty PDOL");

                                /**
                                 * simple code
                                 */
                                //byte[] responseGpoRequestOk = pu.getGpo(0);

                                /**
                                 * advanced code
                                 */
                                gpoRequestCommand = getGpoFromPdol(new byte[0]); // empty PDOL
                            }
                            // from here the steps are equals
                            writeToUiAppend(etLog, "");
                            printStepHeader(etLog, 5, "get the processing options");
                            writeToUiAppend(etLog, "05 get the processing options command length: " + gpoRequestCommand.length + " data: " + bytesToHex(gpoRequestCommand));
                            byte[] gpoRequestResponse = nfc.transceive(gpoRequestCommand);
                            byte[] gpoRequestResponseOk;
                            if (!responseSendWithPdolFailure(gpoRequestResponse)) {
                                byte[][] internalAuthorization = null;
                                byte[][] applicationCrypto = null;
                                String pan = "";
                                String expirationDate = "";
                                List<FilesModel> filesInAfl = new ArrayList<>();
                                gpoRequestResponseOk = checkResponse(gpoRequestResponse);
                                if (gpoRequestResponseOk != null) {
                                    writeToUiAppend(etLog, "05 run GPO response length: " + gpoRequestResponseOk.length + " data: " + bytesToHex(gpoRequestResponseOk));

                                    // pretty print of response
                                    if (isPrettyPrintResponse)
                                        prettyPrintData(etLog, gpoRequestResponseOk);

                                    writeToUiAppend(etLog, "");
                                    printStepHeader(etLog, 6, "read files & search PAN");
                                    writeToUiAppend(etLog, "06 read the files from card and search for tag 0x57 in each file");

                                    // new - check for pan and afl and read files
                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "*** new checks for pan and afl ***");
                                    String pan_exp = checkForPanInResponse(gpoRequestResponseOk);
                                    String[] panExpParts = pan_exp.split("_");
                                    pan = "";
                                    expirationDate = "";
                                    if (pan_exp.equals("_")) {
                                        writeToUiAppend(etLog, "no PAN was included in gpoRequestResponse");
                                    } else {
                                        foundPan = panExpParts[0];
                                        pan = panExpParts[0];
                                        expirationDate = panExpParts[1];
                                        //writeToUiAppend(etLog, "PAN was included in gpoRequestResponse");
                                        //writeToUiAppend(etLog, "PAN: " + panExpParts[0]);
                                        //writeToUiAppend(etLog, "Expiration date (YYMM): " + panExpParts[1]);
                                    }
                                    // check for afl in response
                                    List<byte[]> aflList = checkForAflInGpoResponse(gpoRequestResponseOk);
                                    if (aflList.size() == 0) {
                                        writeToUiAppend(etLog, "no AFL list found in gpoRequestResponse");
                                    } else {
                                        writeToUiAppend(etLog, "AFL list found with " + aflList.size() + " entries");
                                        // now reading the files in afl list
                                        filesInAfl = readAllFilesFromAfl(nfc, aflList);
                                        int filesInAflSize = filesInAfl.size();
                                        if (filesInAflSize == 0) {
                                            writeToUiAppend(etLog, "no files read from AFL list");
                                        } else {
                                            writeToUiAppend(etLog, "read files from AFL list has " + filesInAflSize + " entries");
                                            for (int iFiles = 0; iFiles < filesInAflSize; iFiles++) {
                                                // show all contents
                                                FilesModel filesModel = filesInAfl.get(iFiles);
                                                writeToUiAppend(etLog, "");
                                                writeToUiAppend(etLog, "entry " + iFiles + "\n" + filesModel.dumpFilesModel());
                                                String panInFile = checkForPanInResponse(hexToBytes(filesModel.getContent()));
                                                if (!panInFile.equals("_")) {
                                                    // there is a PAN in the string, here the short cutted version
                                                    writeToUiAppend(etLog, "# PAN found in file " + filesModel.getAddressAfl() + " : " + panInFile);
                                                    if (isPrettyPrintResponse)
                                                        prettyPrintData(etLog, hexToBytes(filesModel.getContent()));
                                                    panExpParts = panInFile.split("_");
                                                    foundPan = panExpParts[0];
                                                    pan = panExpParts[0];
                                                    expirationDate = panExpParts[1];
                                                }
                                            }
                                        }
                                    }
                                    writeToUiAppend(etLog, "");
                                    printStepHeader(etLog, 7, "print PAN & expire date");
                                    writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                    writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                    writeToUiAppend(etLog, "PAN: " + pan);
                                    writeToUiAppend(etLog, "Expiration date (YYMM): " + expirationDate);
                                    writeToUiAppendNoExport(etData, "");
                                    writeToUiAppendNoExport(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                    writeToUiAppendNoExport(etData, "PAN: " + pan);
                                    writeToUiAppendNoExport(etData, "Expiration date (YYMMDD): " + expirationDate);

                                    // checks for get internal authorization and get application crypto
                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "get the internal authentication");
                                    internalAuthorization = getInternalAuthorization(nfc);
                                    writeToUiAppend(etLog, "internalAuthCommand: " + internalAuthorization[0].length + " data: " + bytesToHex(internalAuthorization[0]));
                                    if (internalAuthorization[1] == null) {
                                        writeToUiAppend(etLog, "internalAuthResponse failure");
                                    } else {
                                        writeToUiAppend(etLog, "internalAuthResponse: " + internalAuthorization[1].length + " data: " + bytesToHex(internalAuthorization[1]));
                                        if (isPrettyPrintResponse)
                                            prettyPrintData(etLog, internalAuthorization[1]);
                                    }

                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "get the application cryptogram");
                                    // check that it was found in any file
                                    writeToUiAppend(etLog, "### tag0x8cFound: " + bytesToHex(tag0x8cFound));
                                    //byte[] getApplicationCryptoCommand;
                                    //byte[] getApplicationCryptoResponse;
                                    //byte[] getApplicationCryptoResponseOk = null;
                                    if (tag0x8cFound.length > 1) {
                                        applicationCrypto = getApplicationCrypto(nfc, tag0x8cFound);
                                        writeToUiAppend(etLog, "getApplicationCryptoCommand length: " + applicationCrypto[0].length + " data: " + bytesToHex(applicationCrypto[0]));
                                        if (applicationCrypto[1] != null) {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + applicationCrypto[1].length + " data: " + bytesToHex(applicationCrypto[1]));
                                            if (isPrettyPrintResponse)
                                                prettyPrintData(etLog, applicationCrypto[1]);
                                        } else {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse fails");
                                        }
                                    } else {
                                        writeToUiAppend(etLog, "no CDOL1 found in files, using an empty one");
                                        applicationCrypto = getApplicationCrypto(nfc, new byte[0]);
                                        writeToUiAppend(etLog, "getApplicationCryptoCommand length: " + applicationCrypto[0].length + " data: " + bytesToHex(applicationCrypto[0]));
                                        if (applicationCrypto[1] != null) {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + applicationCrypto[1].length + " data: " + bytesToHex(applicationCrypto[1]));
                                            if (isPrettyPrintResponse)
                                                prettyPrintData(etLog, applicationCrypto[1]);
                                        } else {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse fails");
                                        }
                                    }
                                    // this is the visacard + girocard processing
                                    String aidCard = aidSelectedForAnalyze;
                                    String aidCardName = aidSelectedForAnalyzeName;
                                    String selectAidCommandString = bytesToHex(selectAidCommand);
                                    String selectAidResponseString = bytesToHex(selectAidResponseOk);
                                    String gpoCommandString = bytesToHex(gpoRequestCommand);
                                    String gpoResponseString = bytesToHex(gpoRequestResponseOk);
                                    int checkFirstBytesGetProcessingOptions = 6;
                                    String panFound = pan;
                                    String expirationDateFound = expirationDate;
                                    int numberOfFiles = filesInAfl.size();
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
                                    if (internalAuthorization[0] != null)
                                        internalAuthenticationCommandString = bytesToHex(internalAuthorization[0]);
                                    String internalAuthenticationResponseString = "";
                                    if (internalAuthorization[1] != null)
                                        internalAuthenticationResponseString = bytesToHex(internalAuthorization[1]);
                                    String applicationCryptogramCommandString = "";
                                    if (applicationCrypto[0] != null)
                                        applicationCryptogramCommandString = bytesToHex(applicationCrypto[0]);
                                    String applicationCryptogramResponseString = "";
                                    if (applicationCrypto[1] != null)
                                        applicationCryptogramResponseString = bytesToHex(applicationCrypto[1]);

                                    Aid aidForJson = new Aid(aidCard, aidCardName, selectAidCommandString, selectAidResponseString, gpoCommandString, gpoResponseString,
                                            checkFirstBytesGetProcessingOptions, panFound, expirationDateFound, numberOfFiles, aflString,
                                            applicationTransactionCounterString, leftPinTryCounterString, lastOnlineATCRegisterString, logFormatString,
                                            internalAuthenticationCommandString, internalAuthenticationResponseString, applicationCryptogramCommandString,
                                            applicationCryptogramResponseString);
                                    for (int fileCount = 0; fileCount < filesInAfl.size(); fileCount++) {
                                        FilesModel fm = filesInAfl.get(fileCount);
                                        aidForJson.setFile(fileCount, fm);
                                    }
                                    aids.setAidEntry(aidForJson, aidNumber);
                                    // end of exporting
                                }
                            }
                            // print single data
                            printSingleData(etLog, applicationTransactionCounter, pinTryCounter, lastOnlineATCRegister, logFormat);
                        }
                    }
                }

                // print the complete Log
                writeToUiFinal(etLog);
                setLoadingLayoutVisibility(false);


                // export the file
                if (aids != null) {
                    exportString = new GsonBuilder().setPrettyPrinting().create().toJson(aids, Aids.class);
                    // todo anonymize with all foundPan, not only the last one
                    // todo GiroCard does not work properly
                    if (saveAnonymized.isChecked()) {
                        Log.d(TAG, "the exported json file is anonymized");
                        exportString = anonymizePan(exportString, foundPan);
                    } else {
                        Log.d(TAG, "the exported json file is NOT anonymized");
                    }

                    exportStringFileName = "emv.json";
                    writeStringToExternalSharedStorage();

                    System.out.println("***********************");
                    System.out.println(aids.dumpAids());
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

    // see https://en.wikipedia.org/wiki/Luhn_algorithm
    public static boolean isValidLuhn(String number) {
        int checksum = Character.getNumericValue(number.charAt(number.length() - 1));
        int total = 0;

        for (int i = number.length() - 2; i >= 0; i--) {
            int sum = 0;
            int digit = Character.getNumericValue(number.charAt(i));
            if (i % 2 == 0) {
                digit *= 2;
            }

            sum = digit / 10 + digit % 10;
            total += sum;
        }

        return 10 - total % 10 == checksum;
    }

    /**
     * section for anonymize the export string
     */

    // todo anonymizePan does not work for GiroCard, PAN ends on F at the end
    private String anonymizePan(@NonNull String exportString, @NonNull String panFoundInAid) {
        final String ANONYMIZED_PAN = "1122334455667788";
        int numberSubstrings = substring_rec(exportString, panFoundInAid);
        Log.d(TAG, "the panFoundInAid was found " + numberSubstrings + " times before anonymize");
        String tempString = exportString.replaceAll(panFoundInAid, ANONYMIZED_PAN);
        numberSubstrings = substring_rec(exportString, panFoundInAid);
        Log.d(TAG, "the panFoundInAid was found " + numberSubstrings + " times after anonymize");
        return tempString;
    }

    /**
     * count the number of substrings in a string recursively
     *
     * @param str complete string
     * @param sub sub string
     * @return number or 0 if nothing found
     */
    private int substring_rec(@NonNull String str, @NonNull String sub) {
        if (str.contains(sub)) {
            return 1 + substring_rec(str.replaceFirst(sub, ""), sub);
        }
        return 0;
    }

    /**
     * section for PAN/Expiration Date searching and read files from AFL
     */

    /**
     * checks if a pan is included in response
     * checks for the following tags
     * tag 0x57   Track 2 Equivalent Data
     * tag 0x5a   Application Primary Account Number (PAN)
     * tag 0x5f24 Application Expiration Date
     * not included:
     * tag 0x56   Track 1 equivalent data (only on MagStripe)
     * tag 0x9f6b Track 2 Equivalent Data (only on MagStripe)
     *
     * @param response could be from getProcessingOptionsResponse or readFile
     * @return a string pan + "_" + expirationDate
     * if no pan was found it returns " _ "
     */
    private String checkForPanInResponse(byte[] response) {
        String pan = "";
        String expirationDate = "";
        try {
            BerTlvParser parser = new BerTlvParser();
            BerTlvs tlvs = parser.parse(response);
            // 57 Track 2 Equivalent Data field as well
            // 5a = Application Primary Account Number (PAN)
            // 5F34 = Application Primary Account Number (PAN) Sequence Number
            // 5F25  = Application Effective Date (card valid from)
            // 5F24 = Application Expiration Date
            // search for track 2 equivalent data
            BerTlv tag57 = tlvs.find(new BerTag(0x57));
            if (tag57 != null) {
                Log.d(TAG, "found tag 0x57 track 2 equivalent data and extract pan and expiration date");
                byte[] tag57Bytes = tag57.getBytesValue();
                String track2DataString = bytesToHex(tag57Bytes);
                int posSeparator = track2DataString.toUpperCase().indexOf("D");
                pan = track2DataString.substring(0, posSeparator);
                expirationDate = track2DataString.substring((posSeparator + 1), (posSeparator + 5));
                return pan + "_" + expirationDate;
            }
            // search for pan
            BerTlv tag5a = tlvs.find(new BerTag(0x5a));
            if (tag5a != null) {
                Log.d(TAG, "found tag 0x5a Application Primary Account Number (PAN)");
                byte[] tag5aBytes = tag5a.getBytesValue();
                pan = removeTrailingF(bytesToHex(tag5aBytes));
            }
            // search for expiration date
            BerTlv tag5f24 = tlvs.find(new BerTag(0x5f, 0x24));
            if (tag5f24 != null) {
                Log.d(TAG, "found tag 0x5f24 Application Expiration Date");
                byte[] tag5f24Bytes = tag5f24.getBytesValue();
                expirationDate = bytesToHex(tag5f24Bytes);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //System.out.println("ERROR: ArrayOutOfBoundsException: " + e.getMessage());
        }
        return pan + "_" + expirationDate;
    }

    /**
     * remove all trailing 0xF's trailing in the 10 length fiel tag 0x5a = PAN
     * PAN is padded with 'F'
     *
     * @param input
     * @return
     */
    private String removeTrailingF(String input) {
        int index;
        for (index = input.length() - 1; index >= 0; index--) {
            if (input.charAt(index) != 'f') {
                break;
            }
        }
        return input.substring(0, index + 1);
    }

    /**
     * gpoResponse can be a tag 77 Response Message Template Format 2
     * (found with my Visa-, Master- and German Giro-Cards)
     * or a tag 80 Response Message Template Format 1
     * (found with my American Express card)
     * <p>
     * First we check if a tag 0x94 Application File Locator (AFL) is available in gpoResponse
     * if there is no tag 0x94 we check for a tag 0x80 Response Message Template Format 1
     * and get the AFL data by a hard-coded sequence
     *
     * @param gpoResponse
     * @return the list with afl entries (each of 4 byte)
     * if no afl was found it returns an empty list
     */
    private List<byte[]> checkForAflInGpoResponse(byte[] gpoResponse) {
        List<byte[]> aflList = new ArrayList<>();
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvs = parser.parse(gpoResponse);
        byte[] aflBytes = null;
        // search for tag 0x94 Application File Locator (AFL)
        BerTlv tag94 = tlvs.find(new BerTag(0x94));
        if (tag94 != null) {
            // it is a template 2
            Log.d(TAG, "found tag 0x94 Application File Locator (AFL)");
            aflBytes = tag94.getBytesValue();
        }
        BerTlv tag80 = tlvs.find(new BerTag(0x80));
        if (tag80 != null) {
            // it is a template 1
            Log.d(TAG, "found the AFL in tag 0x80 Response Message Template Format 1");
            byte[] dataTemp = tag80.getBytesValue();
            // first 2 bytes are AIP, followed by xx AFL bytes
            dataTemp = ArrayUtils.subarray(dataTemp, 2, dataTemp.length);
            if (dataTemp != null) {
                aflBytes = dataTemp.clone();
            }
        }
        if (aflBytes != null) {
            // split array by 4 bytes
            List<byte[]> tag94BytesList = divideArray(aflBytes, 4);
            aflList.addAll(tag94BytesList);
            /*
            for (int i = 0; i < tag94BytesList.size(); i++) {
                aflList.add(tag94BytesList.get(i));
            }
             */
        } else {
            Log.d(TAG, "found NO tag 0x94 Application File Locator (AFL) or tag 0x80 Response Message Template Format 1");
        }
        return aflList;
    }

    /**
     * reads a single file (sfi + sector) of an EMV card
     * source: https://stackoverflow.com/a/38999989/8166854 answered Aug 17, 2016
     * by Michael Roland
     *
     * @param nfc
     * @param sfi    as it comes from AFL
     * @param record
     * @return the data read or new byte[0] if no data found
     */
    private byte[] readFileAflFormat(IsoDep nfc, int sfi, int record) {
        int sfiNew = (byte) sfi | 0x04; // add 4 = set bit 3
        byte[] cmd = hexToBytes("00B2000400");
        cmd[2] = (byte) (record & 0x0FF);
        cmd[3] |= (byte) (sfiNew & 0x0FF);
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            // do nothing
        }
        //writeToUiAppend(etLog, bytesToHex(result));
        return checkResponse(result);
    }

    private List<FilesModel> readAllFilesFromAfl(IsoDep nfc, @NonNull List<byte[]> aflList) {
        List<FilesModel> readFiles = new ArrayList<>();
        int aflListLength = aflList.size();
        Log.d(TAG, "The AFL contains " + aflListLength + " entries to read");
        writeToUiAppend(etLog, "");
        writeToUiAppend(etLog, "The AFL contains " + aflListLength + " entries to read");
        if (aflListLength == 0) {
            Log.d(TAG, "no entries to read found, return an empty list");
            return readFiles;
        }
        // at this point we have files to read
        for (int i = 0; i < aflListLength; i++) {
            //writeToUiAppend(etLog, "get sfi + record for array " + i + " data: " + bytesToHex(tag94BytesList.get(i)));
            // get sfi from first byte, 2nd byte is first record, 3rd byte is last record, 4th byte is offline transactions
            byte[] aflListEntry = aflList.get(i);
            writeToUiAppend(etLog, "aflListEntry: " + bytesToHex(aflListEntry));
            final byte sfi = aflListEntry[0];
            final byte rec1 = aflListEntry[1];
            final byte recL = aflListEntry[2];
            final byte offl = aflListEntry[3]; // offline authorization
            byte[] readRecordResponseOk = new byte[0];
            // now we loop through all files requested by rec1 (first record) and recL (last record)
            for (int iRecord = (int) rec1; iRecord <= (int) recL; iRecord++) {
                // build the read command
                writeToUiAppend(etLog, "** read file from sfi " + sfi + " rec " + iRecord);
                readRecordResponseOk = readFileAflFormat(nfc, sfi, iRecord); // read record responses with a checked response
                if (readRecordResponseOk != null) {
                    //
                    final String addressAfl = String.format("%02X%02d", sfi, iRecord);
                    final int sfiSector = sfi >>> 3;
                    FilesModel filesModel = new FilesModel(addressAfl, sfiSector, iRecord, readRecordResponseOk.length, bytesToHex(readRecordResponseOk), offl);
                    readFiles.add(filesModel);
                    // finally check for CDOL1
                    try {
                        BerTlvParser parser = new BerTlvParser();
                        BerTlvs tlvs = parser.parse(readRecordResponseOk);
                        findTag0x8c(tlvs);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        //System.out.println("ERROR: ArrayOutOfBoundsException: " + e.getMessage());
                    }
                } else {
                    //writeToUiAppend(etLog, "** readRecordResponse failure");
                }
            }
        }
        return readFiles;
    }

    /**
     * searches for tag 0x8c = CDOL1
     *
     * @param berTlvs return the value of tag in global variable tag0x8cFound
     */
    private void findTag0x8c(BerTlvs berTlvs) {
        BerTlv tag = berTlvs.find(new BerTag(0x8c));
        if (tag != null) {
            tag0x8cFound = tag.getBytesValue();
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

    /**
     * get the internal authorization, returns a [][]
     *
     * @param nfc
     * @return null if command fails
     * [0] contains the command
     * [1] contains the response
     */
    private byte[][] getInternalAuthorization(@NonNull IsoDep nfc) {
        // internal authentication
        // probably not supported
        byte[][] returnData = new byte[2][]; // return the command in [0] and the responseOk in [1]
        String internalAuthString = "0088000004E153F3E800";
        byte[] internalAuthCommand = hexToBytes(internalAuthString);
        byte[] internalAuthResponse = new byte[0];
        byte[] internalAuthResponseOk = null;
        try {
            internalAuthResponse = nfc.transceive(internalAuthCommand);
            if (internalAuthResponse != null) {
                internalAuthResponseOk = checkResponse(internalAuthResponse);
                if (internalAuthResponseOk != null) {
                    returnData[0] = internalAuthCommand;
                    returnData[1] = internalAuthResponseOk;
                    return returnData;
                }
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }
        returnData[0] = internalAuthCommand;
        returnData[1] = null;
        return returnData;
    }

    /**
     * get the application crypto, returns a [][]
     *
     * @param nfc
     * @param cdol is the CDOL list from any response
     * @return null if command fails
     * [0] contains the command
     * [1] contains the response
     */
    private byte[][] getApplicationCrypto(@NonNull IsoDep nfc, @NonNull byte[] cdol) {
        byte[][] returnData = new byte[2][]; // return the command in [0] and the responseOk in [1]
        byte[] applicationCryptoCommand = getAppCryptoCommandFromCdol(cdol);
        byte[] applicationCryptoResponse = new byte[0];
        byte[] applicationCryptoResponseOk = null;
        try {
            applicationCryptoResponse = nfc.transceive(applicationCryptoCommand);
            if (applicationCryptoResponse != null) {
                applicationCryptoResponseOk = checkResponse(applicationCryptoResponse);
                if (applicationCryptoResponseOk != null) {
                    returnData[0] = applicationCryptoCommand;
                    returnData[1] = applicationCryptoResponseOk;
                    return returnData;
                }
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }
        returnData[0] = applicationCryptoCommand;
        returnData[1] = null;
        return returnData;
    }

    /**
     * DOL utilities
     */

    /**
     * take the PDOL list from selectAidResponse and returns the complete getProcessingOptions command
     *
     * @param pdol
     * @return
     */
    private byte[] getGpoFromPdol(@NonNull byte[] pdol) {
        // get the tags in a list
        List<TagAndLength> tagAndLength = TlvUtil.parseTagAndLength(pdol);
        int tagAndLengthSize = tagAndLength.size();
        if (tagAndLengthSize < 1) {
            // there are no pdols in the list
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
                    //Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default is too long, cut to: " + bytesToHex(usedValue));
                } else if (defaultValue.length < lengthOfTag) {
                    // increase length
                    usedValue = new byte[lengthOfTag];
                    System.arraycopy(defaultValue, 0, usedValue, 0, defaultValue.length);
                    //Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default is too short, increased to: " + bytesToHex(usedValue));
                } else {
                    // correct length
                    usedValue = defaultValue.clone();
                    //Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " default found: " + bytesToHex(usedValue));
                }
            } else {
                // defaultValue is null means the tag was not found in our tags database for default values
                usedValue = new byte[lengthOfTag];
                //Log.i(TAG, "asked for tag: " + bytesToHex(tal.getTag().getTagBytes()) + " NO default found, generate zeroed: " + bytesToHex(usedValue));
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

    /**
     * takes the CDOL1 list from any response (file reading) and returns the getApplicationCrypto command
     *
     * @param cdol
     * @return
     */
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
                                //System.out.println("## data to write: " + exportString);
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
            //System.out.println("** data to write: " + data);
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
                Intent intent = new Intent(ExportEmulationDataActivityV4.this, FileReaderActivity.class);
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