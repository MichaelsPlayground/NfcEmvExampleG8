package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.nfcemvexample.nfccreditcards.AidValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.PdolUtil;
import de.androidcrypto.nfcemvexample.nfccreditcards.TagValues;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "NfcCreditCardAct";

    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etData, etLog;

    private NfcAdapter mNfcAdapter;
    private byte[] tagId;

    final String TechIsoDep = "android.nfc.tech.IsoDep";

    boolean debugPrint = true; // if set to true the writeToUi method will print to console
    String aidSelectedForAnalyze = "";
    String aidSelectedForAnalyzeName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        tv1 = findViewById(R.id.tv1);
        etData = findViewById(R.id.etData);
        etLog = findViewById(R.id.etLog);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
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
                    writeToUiAppend(etLog, "Found tag 0x4F " + tag4fList.size() + " times:");
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
                        byte[] responseSelectedAidOk = checkResponse(responseSelectedAid);
                        if (responseSelectedAidOk != null) {
                            writeToUiAppend(etLog, "03 select AID response length " + responseSelectedAidOk.length + " data: " + bytesToHex(responseSelectedAidOk));
                            BerTlvs tlvsAid = parser.parse(responseSelectedAidOk);
                            writeToUiAppend(etLog, "");
                            writeToUiAppend(etLog, "04 search for tag 0x9F38 in the selectAid response");
                            /**
                             * note: different behaviour between Visa and Mastercard and German Girocards
                             * Mastercard has NO PDOL, Visa gives PDOL in tag 9F38
                             * tag 50 and/or tag 9F12 has an application label or application name
                             * nex step: search for tag 9F38 Processing Options Data Object List (PDOL)
                             */
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
                                writeToUiAppend(etLog, "");
                                writeToUiAppend(etLog, "05 get the processing options command length: " + commandGpoRequest.length + " data: " + bytesToHex(commandGpoRequest));
                                byte[] responseGpoRequest = nfc.transceive(commandGpoRequest);
                                System.out.println("*** responseGpoRequest: " + bytesToHex(responseGpoRequest));
                                if (!responseSendWithPdolFailure(responseGpoRequest)) {
                                    System.out.println("** responseGpoRequest: " + bytesToHex(responseGpoRequest));
                                    byte[] responseGpoRequestOk = checkResponse(responseGpoRequest);
                                    if (responseGpoRequestOk != null) {
                                        writeToUiAppend(etLog, "05 select GPO response length: " + responseGpoRequestOk.length + " data: " + bytesToHex(responseGpoRequestOk));

                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "06 read the files from card and search for tag 0x57 in each file");
                                        String pan_expirationDate = readPanFromFilesFromGpo(nfc, responseGpoRequestOk);
                                        String[] parts = pan_expirationDate.split("_");
                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                        writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etLog, "PAN: " + parts[0]);
                                        writeToUiAppend(etLog, "Expiration date (YYMM): " + parts[1]);
                                        writeToUiAppend(etData, "");
                                        writeToUiAppend(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etData, "PAN: " + parts[0]);
                                        writeToUiAppend(etData, "Expiration date (YYMM): " + parts[1]);
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
                                        System.out.println("guessedPdolResult: " + bytesToHex(guessedPdolResult));
                                        // read the PAN & Expiration date
                                        String pan_expirationDate = readPanFromFilesFromGpo(nfc, guessedPdolResult);
                                        String[] parts = pan_expirationDate.split("_");
                                        writeToUiAppend(etLog, "");
                                        writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                        writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etLog, "PAN: " + parts[0]);
                                        writeToUiAppend(etLog, "Expiration date (YYMM): " + parts[1]);
                                        writeToUiAppend(etData, "");
                                        writeToUiAppend(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etData, "PAN: " + parts[0]);
                                        writeToUiAppend(etData, "Expiration date (YYMM): " + parts[1]);
                                    } else {
                                        System.out.println("guessedPdolResult is NULL");
                                    }
                                }
                            } else { // could not find a tag 0x9f38 in the selectAid response means there is no PDOL request available
                                // instead we use an empty PDOL of length 0
                                // this is usually a mastercard
                                writeToUiAppend(etLog, "No PDOL found in the selectAid response");
                                writeToUiAppend(etLog, "Xtry to request the get processing options (GPO) with an empty PDOL");

                                byte[] responseGpoRequestOk = pu.getGpo(0);
                                if (responseGpoRequestOk != null) {
                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "05 select GPO response length: " + responseGpoRequestOk.length + " data: " + bytesToHex(responseGpoRequestOk));

                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "06 read the files from card and search for tag 0x57 in each file");
                                    String pan_expirationDate = readPanFromFilesFromGpo(nfc, responseGpoRequestOk);
                                    String[] parts = pan_expirationDate.split("_");
                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                    writeToUiAppend(etLog, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                    writeToUiAppend(etLog, "PAN: " + parts[0]);
                                    writeToUiAppend(etLog, "Expiration date (YYMM): " + parts[1]);
                                    writeToUiAppend(etData, "");
                                    writeToUiAppend(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                    writeToUiAppend(etData, "PAN: " + parts[0]);
                                    writeToUiAppend(etData, "Expiration date (YYMMDD): " + parts[1]);
                                }
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
            return pan + "_" + expirationDate;
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
                            //if ((resultReadRecord[resultReadRecord.length - 2] == (byte) 0x90) && (resultReadRecord[resultReadRecord.length - 1] == (byte) 0x00)) {
                            //writeToUiAppend(etLog, "Success: read record result: " + bytesToHex(resultReadRecord));
                            //writeToUiAppend(etLog, "* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                            //System.out.println("* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                            // this is for complete parsing
                            //parseAflDataToTextView(resultReadRecord, etLog);
                            //System.out.println("parse " + iRecords + " result: " + bytesToHex(resultReadRecordOk));
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
                                    //writeToUiAppend(etData, "Xdata for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                    //writeToUiAppend(etLog, "XPAN: " + bytesToHex(tag5aBytes));
                                    //writeToUiAppend(etData, "XPAN: " + bytesToHex(tag5aBytes));
                                    //System.out.println("record " + iRecords + " XPAN: " + bytesToHex(tag5aBytes));
                                    pan = bytesToHex(tag5aBytes);
                                }
                                BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                if (tag5f24 != null) {
                                    byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                    //writeToUiAppend(etLog, "XExp. Date: " + bytesToHex(tag5f24Bytes));
                                    //writeToUiAppend(etData, "XExp. Date (YYMMDD): " + bytesToHex(tag5f24Bytes));
                                    //System.out.println("Exp. Date: " + bytesToHex(tag5f24Bytes));
                                    expirationDate = bytesToHex(tag5f24Bytes);
                                } else {
                                    //System.out.println("record: " + iRecords + " Tag 5F24 not found");
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
     * gets the byte value of a tag from tranceive response
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
        if (data.length < 5) return null; // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x9000) {
            return null;
        } else {
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

    // special version, needs a boolean variable in class header: boolean debugPrint = true;
    // if true this method will print the output additionally to the console
    private void writeToUiAppend(TextView textView, String message) {
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


}