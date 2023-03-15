package de.androidcrypto.nfcemvexample;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

import de.androidcrypto.nfcemvexample.emulate.Aids;

public class ViewEmulationDataActivity extends AppCompatActivity {

    TextView readResult;

    private Context contextSave;
    private String jsonLoaded;

    private SharedPreferences sharedPreferences; // for text size storage
    private final String PREFERENCES_FILENAME = "shared_prefs";
    private final String TEXT_SIZE = "textsize";
    private final int defaultTextSizeInDp = 6;
    private final int MINIMUM_TEXT_SIZE_IN_DP = 3;

    private String dumpExportString;
    private String dumpFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_emulation_data);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.view_toolbar);
        setSupportActionBar(myToolbar);
        contextSave = getApplicationContext();

        // for storage of the text size
        sharedPreferences = getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        readResult = findViewById(R.id.tvMainReadResult);
        readResult.setTextSize(coverPixelToDP(sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp)));
    }

    private void writeToUiAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            String newString = textView.getText().toString() + "\n" + message;
            textView.setText(newString);
        });
    }

    private void writeToUiReverseAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            String newString = message + "\n" + textView.getText().toString();
            textView.setText(newString);
        });
    }

    private void writeToUiToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_LONG).show();
        });
    }

    private int coverPixelToDP(int dps) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dps * scale);
    }

    private void showFileContent() {
        if (jsonLoaded != null) {

            Thread DoShowContent = new Thread() {
                public void run() {
                    Gson gson = new Gson();
                    Aids aids = null;
                    try {
                        aids = gson.fromJson(jsonLoaded, Aids.class);
                    } catch (IllegalStateException | JsonSyntaxException e) {
                        writeToUiToast("Cannot view the view - is it really an Export emulation data file ?");
                    }

                    if (aids != null) {
                        Aids finalAids = aids;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                writeToUiAppend(readResult, "Data from file : " + dumpFileName);
                                writeToUiAppend(readResult, "");
                                writeToUiAppend(readResult, finalAids.dumpAids());
                            }
                        });
                    }
                }
            };
            DoShowContent.start();
        }
    }

    /**
     * section open a file
     */

    private void openFileFromExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        boolean pickerInitialUri = false;
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        fileOpenActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> fileOpenActivityResultLauncher = registerForActivityResult(
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
                                //contentLoaded = readBytesFromUri(uri);
                                jsonLoaded = readTextFromUri(uri);
                                //showFileContent();
                            } catch (IOException e) {
                                //contentLoaded = null;
                                jsonLoaded = null;
                                e.printStackTrace();
                                writeToUiToast("ERROR: " + e.toString());
                                return;
                            }
                        }
                    }
                }
            });

    private String readTextFromUri(Uri uri) throws IOException {
        if (contextSave != null) {
            ContentResolver contentResolver = contextSave.getContentResolver();
            String filename = queryName(contentResolver, uri);
            writeToUiAppend(readResult, "content of file " + filename);
            dumpFileName = filename;
            // warning: contextSave needs to get filled
            Thread DoReadFile = new Thread() {
                public void run() {
                    StringBuilder stringBuilder = new StringBuilder();
                    //try (InputStream inputStream = getContentResolver().openInputStream(uri);
                    // warning: contextSave needs to get filled
                    try (InputStream inputStream = contentResolver.openInputStream(uri);
                         BufferedReader reader = new BufferedReader(
                                 new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line + "\n");
                        }
                        jsonLoaded = stringBuilder.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFileContent();
                                //Toast.makeText(DeleteGoogleDriveFile.this, "selected file deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            DoReadFile.start();
        }
        return null;
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    /**
     * section export and mail dump file
     */

    private void mailDumpFile() {
        if (dumpExportString.isEmpty()) {
            writeToUiToast("open a file first before sending emails :-)");
            return;
        }
        String subject = "Dump of file " + dumpFileName;
        String body = dumpExportString;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void exportDumpFile() {
        if (dumpExportString.isEmpty()) {
            writeToUiToast("open a file first before writing files :-)");
            return;
        }
        //verifyPermissionsWriteString();
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
        String filename = dumpFileName + ".txt";
        // sanity check
        if (filename.equals("")) {
            writeToUiToast("scan a tag before writing the content to a file :-)");
            return;
        }
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        fileSaveActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> fileSaveActivityResultLauncher = registerForActivityResult(
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
                                String fileContent = dumpExportString;
                                writeTextToUri(uri, fileContent);
                                String message = "file written to external shared storage: " + uri.toString();
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
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(contextSave.getContentResolver().openOutputStream(uri));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            System.out.println("Exception File write failed: " + e.toString());
        }
    }

    /**
     * section on OptionsMenu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_view, menu);

        MenuItem mOpenFile = menu.findItem(R.id.action_open_file);
        mOpenFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Intent i = new Intent(MainActivity.this, AddEntryActivity.class);
                //startActivity(i);
                readResult.setText("");
                dumpFileName = "";
                dumpExportString = "";
                openFileFromExternalSharedStorage();
                return false;
            }
        });

        MenuItem mPlusTextSize = menu.findItem(R.id.action_plus_text_size);
        mPlusTextSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int textSizeInDp = sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp) + 1;
                readResult.setTextSize(coverPixelToDP(textSizeInDp));
                System.out.println("textSizeInDp: " + textSizeInDp);
                try {
                    sharedPreferences.edit().putInt(TEXT_SIZE, textSizeInDp).apply();
                } catch (Exception e) {
                    writeToUiToast("Error on size storage: " + e.getMessage());
                    return false;
                }
                return false;
            }
        });

        MenuItem mMinusTextSize = menu.findItem(R.id.action_minus_text_size);
        mMinusTextSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int textSizeInDp = sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp) - 1;
                if (textSizeInDp < MINIMUM_TEXT_SIZE_IN_DP) {
                    writeToUiToast("You cannot decrease text size any further");
                    return false;
                }
                readResult.setTextSize(coverPixelToDP(textSizeInDp));
                try {
                    sharedPreferences.edit().putInt(TEXT_SIZE, textSizeInDp).apply();
                } catch (Exception e) {
                    writeToUiToast("Error on size storage: " + e.getMessage());
                    return false;
                }
                return false;
            }
        });

        MenuItem mExportDumpFile = menu.findItem(R.id.action_export_dump_file);
        mExportDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                exportDumpFile();
                return false;
            }
        });

        MenuItem mMailDumpFile = menu.findItem(R.id.action_mail_dump_file);
        mMailDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mailDumpFile();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}