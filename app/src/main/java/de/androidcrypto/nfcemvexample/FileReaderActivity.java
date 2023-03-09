package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;

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

public class FileReaderActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "NfcCCFileReaderAct";

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
        setContentView(R.layout.activity_file_reader);

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
        MediaPlayer mp = MediaPlayer.create(FileReaderActivity.this, R.raw.single_ping);
        mp.start();
    }

    private void playDoublePing() {
        MediaPlayer mp = MediaPlayer.create(FileReaderActivity.this, R.raw.double_ping);
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

                    // step 03 here we are reading the directory for the first AID only
                    byte[] aidSelected = aidList.get(0);
                    aidSelectedForAnalyze = bytesToHex(aidSelected);
                    aidSelectedForAnalyzeName = aidV.getAidName(aidSelected);
                    writeToUiAppend(etLog, "");
                    writeToUiAppend(etLog, "************************************");
                    writeToUiAppend(etLog, "03 select application by AID " + aidSelectedForAnalyze + " (number " + (1) + ")");
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
                        // pretty print of response
                        if (isPrettyPrintResponse) prettyPrintData(etLog, responseSelectedAidOk);

                        byte[] applicationTransactionCounterBeforeReading = getApplicationTransactionCounter(nfc);
                        writeToUiAppendNoExport(etLog, "application transaction counter (ATC) before reading");
                        printSingleData(etLog, applicationTransactionCounterBeforeReading);
                        writeToUiAppend(etLog, "");
                        writeToUiAppend(etLog, "04 read the files for sector 1-4 and records 1-10 for each sector");
                        completeFileReading(nfc);
                        byte[] applicationTransactionCounterAfterReading = getApplicationTransactionCounter(nfc);
                        writeToUiAppendNoExport(etLog, "application transaction counter (ATC) after reading");
                        printSingleData(etLog, applicationTransactionCounterAfterReading);
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
        // limit the loop to 1-4, could be 31
        //for (int sfi = 1; sfi < 5; ++sfi) {
        for (int sfi = 1; sfi < 32; ++sfi) {
            for (int record = 1; record < 10; ++record) {
                byte[] readResult = readFile(nfc, sfi, record);
                sb.append("SFI: ").append(String.valueOf(sfi)).append("\n");
                sb.append("Record: ").append(String.valueOf(record)).append("\n");
                if (readResult != null) {
                    sb.append(bytesToHex(readResult)).append("\n");
                    // pretty print of response
                    if (isPrettyPrintResponse) sb.append(prettyPrintDataToString(readResult)).append("\n");
                } else {
                    sb.append("NULL").append("\n");
                }
                sb.append("-----------------------").append("\n");
            }
        }
        resultString = sb.toString();
        writeToUiAppend(etData, resultString);
        writeToUiAppendNoExport(etLog, "reading complete");
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

    private void prettyPrintData(TextView textView, byte[] responseData) {
        writeToUiAppend(textView, "------------------------------------");
        String responseGetAppCryptoString = TlvUtil.prettyPrintAPDUResponse(responseData);
        writeToUiAppend(textView, trimLeadingLineFeeds(responseGetAppCryptoString));
        writeToUiAppend(textView, "------------------------------------");
    }

    private String prettyPrintDataToString(byte[] responseData) {
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------").append("\n");
        sb.append(trimLeadingLineFeeds(TlvUtil.prettyPrintAPDUResponse(responseData))).append("\n");
        sb.append("------------------------------------").append("\n");
        return sb.toString();
    }

    public static String trimLeadingLineFeeds(String input) {
        String[] output = input.split("^\\n+", 2);
        return output.length > 1 ? output[1] : output[0];
    }

    private void printSingleData(TextView textView, byte[] applicationTransactionCounter) {
        writeToUiAppend(etLog, "");
        if (applicationTransactionCounter != null) {
            writeToUiAppend(etLog, "applicationTransactionCounter: " + bytesToHex(applicationTransactionCounter)
                    + " (hex), " + BinaryUtils.intFromByteArrayV4(applicationTransactionCounter) + " (dec)");
        } else {
            writeToUiAppend(etLog, "applicationTransactionCounter: NULL");
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
                Intent intent = new Intent(FileReaderActivity.this, MainActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mFileReaderActivity = menu.findItem(R.id.action_activity_file_reader);
        mFileReaderActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                //Intent intent = new Intent(MainActivity.this, FileReaderActivity.class);
                //startActivity(intent);
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