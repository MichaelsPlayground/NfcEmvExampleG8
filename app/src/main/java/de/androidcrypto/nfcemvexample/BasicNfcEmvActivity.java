package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHexNpe;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.nfcemvexample.extended.TagListParser;
import de.androidcrypto.nfcemvexample.extended.TagNameValue;

public class BasicNfcEmvActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "BasicNfcEmvAct";
    private com.google.android.material.textfield.TextInputEditText etLog;
    private View loadingLayout;
    private NfcAdapter mNfcAdapter;

    private String outputString = ""; // used for the UI output
    private String exportString = ""; // used for exporting the log to a text file
    private String exportStringFileName = "emv.html";
    private final String stepSeparatorString = "*********************************";
    private final String lineSeparatorString = "---------------------------------";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_nfc_emv);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.basic_toolbar);
        setSupportActionBar(myToolbar);

        etLog = findViewById(R.id.etLog);
        loadingLayout = findViewById(R.id.loading_layout);

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
        clearData(etLog);
        Log.d(TAG, "NFC tag discovered");
        writeToUiAppend("NFC tag discovered");
        setLoadingLayoutVisibility(true);
        byte[] tagId = tag.getId();
        writeToUiAppend("TagId: " + bytesToHexNpe(tagId));
        String[] techList = tag.getTechList();
        writeToUiAppend("TechList found with these entries:");
        boolean isoDepInTechList = false;
        for (int i = 0; i < techList.length; i++) {
            writeToUiAppend(techList[i]);
            if (techList[i].equals("android.nfc.tech.IsoDep")) isoDepInTechList = true;
        }
        // proceed only if tag has IsoDep in the techList
        if (isoDepInTechList) {
            IsoDep nfc = null;
            nfc = IsoDep.get(tag);
            if (nfc != null) {
                try {
                    nfc.connect();
                    Log.d(TAG, "connection with card success");
                    writeToUiAppend("connection with card success");
                    // here we are going to start our journey through the card

                    printStepHeader(1, "our journey begins");

                    writeToUiAppend("abc");


                    printStepHeader(1, "select PPSE");
                    byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PPSE
                    byte[] selectPpseCommand = selectApdu(PPSE);
                    byte[] selectPpseResponse = nfc.transceive(selectPpseCommand);
                    writeToUiAppend("01 select PPSE command  length " + selectPpseCommand.length + " data: " + bytesToHex(selectPpseCommand));
                    writeToUiAppend("01 select PPSE response length " + selectPpseResponse.length + " data: " + bytesToHex(selectPpseResponse));
                    writeToUiAppend(prettyPrintDataToString(selectPpseResponse));

                    byte[] selectPpseResponseOk = checkResponse(selectPpseResponse);
                    // proceed only when te do have a positive read result = 0x'9000' at the end of response data
                    if (selectPpseResponseOk != null) {

                        // get the tags from respond
                        BerTlvParser parser = new BerTlvParser();
                        BerTlvs tlvs = parser.parse(selectPpseResponseOk, 0, selectPpseResponseOk.length);
                        List<BerTlv> selectPpseResponseTagList = tlvs.getList();
                        int selectPpseResponseTagListSize = selectPpseResponseTagList.size();
                        writeToUiAppend("found " + selectPpseResponseTagListSize + " tags in response");
                        // show iterating
                        for (int i = 0; i < selectPpseResponseTagListSize; i++) {
                            BerTlv tlv = selectPpseResponseTagList.get(i);
                            writeToUiAppend(tlv.toString());
                            BerTag berTag = tlv.getTag();
                            boolean berTagIsConstructed = berTag.isConstructed();

                        }

                        /*
                        // devnied
                        writeToUiAppend("");
                        List<TagAndLength> parsedList = TlvUtil.parseTagAndLength(selectPpseResponseOk);
                        int parsedListSize = parsedList.size();
                        writeToUiAppend("parsedListSize: " + parsedListSize);
                        writeToUiAppend(parsedList.toString());
*/
                        writeToUiAppend("");
                        List<TagNameValue> parsedList = TagListParser.parseRespond(selectPpseResponseOk);
                        int parsedListSize = parsedList.size();
                        writeToUiAppend("parsedListSize: " + parsedListSize);
                        for (int i = 0; i < parsedListSize; i++) {
                            TagNameValue p = parsedList.get(i);
                            writeToUiAppend("tag " + i + " : " + bytesToHexNpe(p.getTagBytes()) + " has the value " + bytesToHexNpe(p.getTagValueBytes()));
                            writeToUiAppend("tag " + i + " : " + bytesToHexNpe(p.getTagBytes()) + " has the name " + p.getTagName());
                        }



                    } else {
                        // if (selectPpseResponseOk != null)
                        writeToUiAppend("the result of the reading was not successful so the workflow ends here, sorry.");
                        startEndSequence(nfc);
                    }

                    /*
                    https://www.europeanpaymentscouncil.eu/sites/default/files/KB/files/EPC050-16%20SCS%20Volume%207%201%20-%20Bul%2001%20-%2020160229%20-%20Book%202%20-%20EEA%20Product%20Identification%20and%20usage%20in%20Selection%20of%20Application.pdf
                    The first byte is defined as follows.
Value IFR Product Type
‘01’ Debit Product
‘02’ Credit Product
‘03’ Commercial Product
‘04’ Pre-paid Product
All other values Reserved for future use
Bytes 2 to 5 are reserved for future use by the CSG and if present, they shall be filled with '00' for the
current version.
Presence of Tag ‘9F0A’ with ID = ‘0001’ indicates an EEA issued card
                     Application Selection Registered Proprietary Data (Tag ‘9F0A’),

different returns
Visa comd debit:
MC AAB credit:

                     */

                    printStepHeader(12, "our journey ends");


                } catch (IOException e) {
                    writeToUiAppend("connection with card failure");
                    writeToUiAppend(e.getMessage());
                    playPing();
                    writeToUiFinal(etLog);
                    setLoadingLayoutVisibility(false);
                    // throw new RuntimeException(e);
                    return;
                }
            }
        } else {
            // if (isoDepInTechList) {
            writeToUiAppend("The discovered NFC tag does not have an IsoDep interface.");
        }
        // final cleanup
        playPing();
        writeToUiFinal(etLog);
        setLoadingLayoutVisibility(false);
    }

    private void startEndSequence(IsoDep nfc) {
        playPing();
        writeToUiFinal(etLog);
        setLoadingLayoutVisibility(false);
        try {
            nfc.close();
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }
        return;
    }

    /**
     * section for emv reading
     */




    /**
     * build a select apdu command
     *
     * @param data
     * @return
     */
    private byte[] selectApdu(@NonNull byte[] data) {
        byte[] commandApdu = new byte[6 + data.length];
        commandApdu[0] = (byte) 0x00;  // CLA
        commandApdu[1] = (byte) 0xA4;  // INS
        commandApdu[2] = (byte) 0x04;  // P1
        commandApdu[3] = (byte) 0x00;  // P2
        commandApdu[4] = (byte) (data.length & 0x0FF);       // Lc
        System.arraycopy(data, 0, commandApdu, 5, data.length);
        commandApdu[commandApdu.length - 1] = (byte) 0x00;  // Le
        return commandApdu;
    }

    /**
     * checks if the response has an 0x'9000' at the end means success
     * and the method returns the data without 0x'9000' at the end
     * if any other trailing bytes show up the method returns NULL
     * @param data
     * @return
     */
    private byte[] checkResponse(@NonNull byte[] data) {
        // simple sanity check
        if (data.length < 5) {
            return null;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x9000) {
            return null;
        } else {
            return Arrays.copyOfRange(data, 0, data.length - 2);
        }
    }

    /**
     * section for NFC
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

    /**
     * important is the disabling of the ReaderMode when activity is pausing
     */

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(this);
    }

    /**
     * section for UI
     */

    /**
     * shows a progress bar as long as the reading lasts
     *
     * @param isVisible
     */

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
     * play a sound when reading is done
     */
    private void playPing() {
        /**
         * filename: conveniencestorering_96090.mp3
         * sound: ConvenienceStoreRing
         * created by: DataOperativeX
         * https://pixabay.com/sound-effects/id-96090/
         * Sound Effect from <a href="https://pixabay.com/sound-effects/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=96090">Pixabay</a>
         */
        //MediaPlayer mp = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        //MediaPlayer mp = MediaPlayer.create(this, R.raw.conveniencestorering_96090);
        //MediaPlayer mp = MediaPlayer.create(this, R.raw.single_ping);

        // https://ringtonesdump.com/ping-ringtone.html
        /*
        filename: ping_ringtone.mp3
        Audio Size:	10 Kb
        Duration:
        Format:	mp3 / m4r
        Bitrate:	266 Kbps
        Sample rate:	44.1 khz
        Category:	Message
        Views:	1034
        Downloads:	366
        Licence:	Intended exclusively for private use
         */
        MediaPlayer mp = MediaPlayer.create(this, R.raw.ping_ringtone);
        mp.start();
    }


    /**
     * prints a nice header for each step
     *
     * @param step
     * @param message
     */
    private void printStepHeader(int step, String message) {
        // message should not extend 29 characters, longer messages will get trimmed
        String emptyMessage = "                                 ";
        StringBuilder sb = new StringBuilder();
        sb.append(outputString); // has already a line feed at the end
        sb.append("").append("\n");
        sb.append(stepSeparatorString).append("\n");
        sb.append("************ step ").append(String.format("%02d", step)).append(" ************").append("\n");
        sb.append("* ").append((message + emptyMessage).substring(0, 29)).append(" *").append("\n");
        sb.append(stepSeparatorString).append("\n");
        outputString = sb.toString();
    }

    /**
     * used for printing the card responses in a human readable format to a string
     *
     * @param responseData
     * @return
     */
    private String prettyPrintDataToString(byte[] responseData) {
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------").append("\n");
        sb.append(trimLeadingLineFeeds(TlvUtil.prettyPrintAPDUResponse(responseData))).append("\n");
        sb.append("------------------------------------").append("\n");
        return sb.toString();
    }

    /**
     * trim leading line feeds if existing
     *
     * @param input
     * @return
     */
    public static String trimLeadingLineFeeds(String input) {
        String[] output = input.split("^\\n+", 2);
        return output.length > 1 ? output[1] : output[0];
    }

    private void clearData(final TextView textView) {
        runOnUiThread(() -> {
            outputString = "";
            exportString = "";
            textView.setText("");
        });
    }

    private void writeToUiAppend(String message) {
        outputString = outputString + message + "\n";
    }

    private void writeToUiFinal(final TextView textView) {
        if (textView == (TextView) etLog) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(outputString);
                    System.out.println(outputString); // print the data to console
                }
            });
        }
    }

    private void provideTextViewDataForExport(TextView textView) {
        exportString = textView.getText().toString();
    }

    private void writeToUiToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * section OptionsMenu export text file methods
     */

    private void exportTextFile() {
        provideTextViewDataForExport(etLog);
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
        getMenuInflater().inflate(R.menu.menu_activity_basic, menu);

        MenuItem mCopyData = menu.findItem(R.id.action_copy_data);
        mCopyData.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("BasicNfcEmvReader", etLog.getText());
                clipboard.setPrimaryClip(clip);
                // show toast only on Android versions < 13
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                    Toast.makeText(getApplicationContext(), "copied", Toast.LENGTH_SHORT).show();
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