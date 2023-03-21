package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CryptoStuffActivity extends AppCompatActivity {

    private final String TAG = "CryptoStuffAct";

    Button btn1, btn2Decrypt, btn3, btn4, btn5, btn6, btn7;
    TextView tv1;
    EditText et1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_stuff);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        btn1 = findViewById(R.id.btn1);
        btn2Decrypt = findViewById(R.id.btnDecrypt);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        tv1 = findViewById(R.id.tv1);
        et1 = findViewById(R.id.et1);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1 back to main menu");
                Intent intent = new Intent(CryptoStuffActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn2Decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn2Decrypt");

                // here we are starting the crypto stuff and try to decrypt some data from the card
                // we do need the content of some tags:
                // these tags are available on Visa comd M
                byte[] tag90_IssuerPublicKeyCertificate;
                byte[] tag8f_CertificationAuthorityPublicKeyIndex;
                byte[] tag9f32_IssuerPublicKeyExponent;
                byte[] tag9f46_IccPublicKeyCertificate;
                byte[] tag9f47_IccPublicKeyExponent;
                byte[] tag9f4a_StaticDataAuthenticationTagList;
                byte[] tag9f69_Udol;
                byte[] tag9f4b_SignedDynamicApplicationData;
                byte[] tag9f10_IssuerApplicationData;
                byte[] tag9f26_ApplicationCryptogram;

                tag90_IssuerPublicKeyCertificate = hexToBytes("5ab54faf4ad810b3cca4ed42c38e1e768fca3187ed1be4196c6779c4633cbe88751889c12b05e10ee87cb198518793ff61e87534f66850e96239b76648429eced4cc207608d0d2a932dd9e8c4bb0d139c4eca59e1ef5f4708f72d80dc5b66c45f4566c91b55384dfdeabb55faa622c6764cc9fb4c4900b6ab2cec5abad9057e2cf63a881bb4ec2a5d96634d7c11366eb908a168d33aa3c544822fc83e74c104b9275b2ef1cf41375b404a260bbf8fb3d4452af3d0630bb1ec2a01676ba588ae7820727622a6d9df5c93a3ce807d54b79ae007c3d401f8787dc3e235e8b9ae6b1b9279328cb1ca94105434010f15eb07f487f4d5c94f4a5a7");
                tag8f_CertificationAuthorityPublicKeyIndex = hexToBytes("09");
                tag9f32_IssuerPublicKeyExponent = hexToBytes("03");
                tag9f46_IccPublicKeyCertificate = hexToBytes("170f0114117fc8b6d8676ed9f860e48034803fe0217dd04280d7fd2dae673c822cf2fbd5e099d1de87ce66a2c513d08c328d72003b63b66ae3430146654b521c8356daca8511d912bb8a44bb940c85493e6502ecde00a84de323d26d5f0cc2889bd3553b8ef6877860dd8d960caf3b7568aca6d1e1175ede14a92b01227175647d87ca8e0047a5c0cb9a3dbe6b35282739bdaa3bc41a813d1e49ba901a6c2d0566f2bf7f7262f822eefefde90adb715f");
                tag9f47_IccPublicKeyExponent = hexToBytes("03");
                tag9f4a_StaticDataAuthenticationTagList = hexToBytes("82");
                tag9f69_Udol = hexToBytes("01182138708400");
                tag9f4b_SignedDynamicApplicationData = hexToBytes("2731459c144bbc637d0cac5db31ca7b7c1a0e270436f0a4c690d544de494e01330040d0df4f7878440e01e626ea3d43c74c06bdf8773f1554afb2c9b5ff758d83d0e1184c6da6e8ddc73ba8b6586d374e8e1d46b5f23f89b2723444f3e2c7ef2aa6e87afc43a0c595b6d707bad93dbebea1f74a8649dbca30b6a55387e70e1f2");
                tag9f10_IssuerApplicationData = hexToBytes("06011203a00000");
                tag9f26_ApplicationCryptogram = hexToBytes("bffdfe76b1b8fc4e");




            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn3");

            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn4");

            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn5");

            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn 6");

            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn 7");

            }
        });
    }


    /**
     * section for OptionsMenu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mOpenFile = menu.findItem(R.id.action_open_file);
        mOpenFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mOpenFile");
                //Intent i = new Intent(MainActivity.this, AddEntryActivity.class);
                //startActivity(i);
                //readResult.setText("");
                //dumpFileName = "";
                //dumpExportString = "";
                //openFileFromExternalSharedStorage();
                return false;
            }
        });

        MenuItem mPlusTextSize = menu.findItem(R.id.action_plus_text_size);
        mPlusTextSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mPlusTextSize");
                /*
                //int textSizeInDp = sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp) + 1;
                //readResult.setTextSize(coverPixelToDP(textSizeInDp));
                System.out.println("textSizeInDp: " + textSizeInDp);
                try {
                    sharedPreferences.edit().putInt(TEXT_SIZE, textSizeInDp).apply();
                } catch (Exception e) {
                    writeToUiToast("Error on size storage: " + e.getMessage());
                    return false;
                }

                 */
                return false;
            }
        });

        MenuItem mMinusTextSize = menu.findItem(R.id.action_minus_text_size);
        mMinusTextSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mMinusTextSize");
                /*
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

                 */
                return false;
            }
        });

        MenuItem mExportDumpFile = menu.findItem(R.id.action_export_dump_file);
        mExportDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportDumpFile");
                //exportDumpFile();
                return false;
            }
        });

        MenuItem mMailDumpFile = menu.findItem(R.id.action_mail_dump_file);
        mMailDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mMailDumpFile");
                //mailDumpFile();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}