package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHexNpe;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.androidcrypto.nfcemvexample.johnzweng.EmvKeyReader;
import de.androidcrypto.nfcemvexample.johnzweng.EmvParsingException;
import de.androidcrypto.nfcemvexample.johnzweng.IssuerIccPublicKey;
import de.androidcrypto.nfcemvexample.sasc.CA;
import de.androidcrypto.nfcemvexample.sasc.ICCPublicKeyCertificate;
import de.androidcrypto.nfcemvexample.sasc.IssuerPublicKey;
import de.androidcrypto.nfcemvexample.sasc.IssuerPublicKeyCertificate;
import de.androidcrypto.nfcemvexample.sasc.Record;
import de.androidcrypto.nfcemvexample.sasc.StaticDataAuthenticationTagList;
import de.androidcrypto.nfcemvexample.sasc.Util;

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

                // list of CA Public Keys
                // https://www.eftlab.co.uk/knowledge-base/list-of-ca-public-keys
/*
Visa key Exponent 3, public key index: 09, RID: A000000003, Key length: 1984, valid 31.12.2028
Modulus:
9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41
SHA-1
1FF80A40173F52D7D27E0F26A146A1C8CCB29046

 */

                byte[] caPublicKeyVisa09Modulus = hexToBytes("9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41");
                byte[] caPublicKeyVisa09Exponent = hexToBytes(("03"));
                byte[] caPublicKeyVisa09Sha1 = hexToBytes("1FF80A40173F52D7D27E0F26A146A1C8CCB29046");
                byte[] visaRid = hexToBytes("A000000003");
                byte[] visaChksum = CA.calculateCAPublicKeyCheckSum(visaRid, Util.intToByteArray(9), caPublicKeyVisa09Modulus, new byte[]{0x03});
                writeToUiAppend(tv1, "visaChksum: " + bytesToHexNpe(visaChksum));

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

                // Retrieval of Issuer Public Key
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Retrieval of Issuer Public Key");

                writeToUiAppend(tv1, "IssuerPublicKeyCertificate: " + bytesToHexNpe(tag90_IssuerPublicKeyCertificate));
                byte[] recoveredIssuerPublicKey = performRSA(tag90_IssuerPublicKeyCertificate, caPublicKeyVisa09Exponent, caPublicKeyVisa09Modulus);
                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredIssuerPublicKey));
/*
6a02487178ff12280431ef0101b001bf19e3eb0d7cd72b45a02661ea4ab87e7a60cb7ab45fd170f5e9a650aee5154124b64e85bd3444c76fddb28f9e30c1304761713773fa2d5ea05be757cfacb2df7b80e8acbd585ec5e1606f3fc91241245f9d929e7e06790d996245eccbab1a37933268e31c622f9d1a486f6ba5340ceec7b794dc0f3303b5de4662efdcfc92f6953eab65a86bb4c8d58d3308c88b5329e2a10d6bec4465c485e5b0a223d87538b10ed755891767f5f4f86068f65de4f1bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb786706bd50c5618f7f69d42326d6966877ed609fbc
 */

                // see package johnzweng
                EmvKeyReader emvKeyReader = new EmvKeyReader();
                try {
                    EmvKeyReader.RecoveredIssuerPublicKey recoveredIssuerPublicKeyParsed = emvKeyReader.parseIssuerPublicKeyCert(recoveredIssuerPublicKey, caPublicKeyVisa09Modulus.length);
                    writeToUiAppend(tv1, "parsed recovered Issuer Public Key\n" + recoveredIssuerPublicKeyParsed.dump());
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                // next step: Terminal decrypt ICC public key certificate using the issuer public key


                /*
                CA.initFromFile("/certificationauthorities_test.xml");
                CA visaCa = CA.getCA(visaRid);
                IssuerPublicKeyCertificate visaCert = new IssuerPublicKeyCertificate(visaCa);
                visaCert.setCAPublicKeyIndex(9);

                byte[] recoverdIssuerPublicKey = Util.performRSA(tag90_IssuerPublicKeyCertificate, new byte[]{(byte) 0x03}, visaCa.getPublicKey(9).getModulus());
                writeToUiAppend(tv1, "recoveredIssuerPublicKey: " + bytesToHexNpe(recoverdIssuerPublicKey));
*/


                //visaCert.setSignedBytes();
                //IssuerPublicKey issuerPublicKey = new IssuerPublicKey();
                //issuerPublicKey.setModulus();

/*
                writeToUiAppend(tv1, "==============================");
                byte[] rid = Util.fromHexString("a0 00 00 00 03"); // visa
                byte[] mod = Util.fromHexString("BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B");
                byte[] chksum = CA.calculateCAPublicKeyCheckSum(rid, Util.intToByteArray(149), mod, new byte[]{0x03});
                System.out.println(Util.prettyPrintHexNoWrap(chksum));
                writeToUiAppend(tv1, "chksum: " + Util.prettyPrintHexNoWrap(chksum));
                CA.initFromFile("/certificationauthorities_test.xml");
                CA ca = CA.getCA(rid);
                IssuerPublicKeyCertificate cert = new IssuerPublicKeyCertificate(ca);
                cert.setCAPublicKeyIndex(149);
                String signedBytesStr = "8b 39 01 f6 25 30 48 a8 b2 cb 08 97 4a 42 45 d9" +
                        "0e 1f 0c 4a 2a 69 bc a4 69 61 5a 71 db 21 ee 7b" +
                        "3a a9 42 00 cf ae dc d6 f0 a7 d9 ad 0b f7 92 13" +
                        "b6 a4 18 d7 a4 9d 23 4e 5c 97 15 c9 14 0d 87 94" +
                        "0f 2e 04 d6 97 1f 4a 20 4c 92 7a 45 5d 4f 8f c0" +
                        "d6 40 2a 79 a1 ce 05 aa 3a 52 68 67 32 98 53 f5" +
                        "ac 2f eb 3c 6f 59 ff 6c 45 3a 72 45 e3 9d 73 45" +
                        "14 61 72 57 95 ed 73 09 70 99 96 3b 82 eb f7 20" +
                        "3c 1f 78 a5 29 14 0c 18 2d bb e6 b4 2a e0 0c 02";
                byte[] signedBytes = Util.fromHexString(signedBytesStr);
                cert.setSignedBytes(signedBytes);

                String remainderStr = "33 f5 e4 44 7d 4a 32 e5 93 6e 5a 13 39 32 9b b4 e8 dd 8b f0 04 4c e4 42 8e 24 d0 86 6f ae fd 23 48 80 9d 71";
                cert.getIssuerPublicKey().setExponent(new byte[]{0x03});
                cert.getIssuerPublicKey().setRemainder(Util.fromHexString(remainderStr));

                System.out.println(cert.toString());
                writeToUiAppend(tv1, "cert:\n" + cert.toString());

                // now with own data
                // rid is already visa
*/


            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn3");
                //EMVApplication app = new EMVApplication();

                //EMVUtil.parseProcessingOpts(Util.fromHexString("80 0e 3c 00 08 02 02 00 10 01 01 00 18 01 03 01"), app);

                //System.out.println(app.getApplicationFileLocator());

                byte[] appRecord = Util.fromHexString("70 56 5f 25 03 12 08 01 5f 24 03 15 08 31 5a 08"
                        +"46 92 98 20 36 76 95 49 5f 34 01 01 9f 07 02 ff"
                        +"80 8e 14 00 00 00 00 00 00 00 00 02 01 44 03 01"
                        +"03 02 03 1e 03 1f 00 9f 0d 05 b8 60 ac 88 00 9f"
                        +"0e 05 00 10 00 00 00 9f 0f 05 b8 68 bc 98 00 5f"
                        +"28 02 06 42 9f 4a 01 82");

                //EMVUtil.printResponse(appRecord, true);

                Record record = new Record(appRecord, 1, true);
                //app.getApplicationFileLocator().getApplicationElementaryFiles().get(2).setRecord(1, record);

                StaticDataAuthenticationTagList staticDataAuthTagList = new StaticDataAuthenticationTagList(new byte[]{(byte)0x82});
                //app.setStaticDataAuthenticationTagList(staticDataAuthTagList);

                IssuerPublicKeyCertificate issuerPKCert = new IssuerPublicKeyCertificate(CA.getCA(Util.fromHexString("A0 00 00 00 03")));

                String signedBytesStr = "8b 39 01 f6 25 30 48 a8 b2 cb 08 97 4a 42 45 d9" +
                        "0e 1f 0c 4a 2a 69 bc a4 69 61 5a 71 db 21 ee 7b" +
                        "3a a9 42 00 cf ae dc d6 f0 a7 d9 ad 0b f7 92 13" +
                        "b6 a4 18 d7 a4 9d 23 4e 5c 97 15 c9 14 0d 87 94" +
                        "0f 2e 04 d6 97 1f 4a 20 4c 92 7a 45 5d 4f 8f c0" +
                        "d6 40 2a 79 a1 ce 05 aa 3a 52 68 67 32 98 53 f5" +
                        "ac 2f eb 3c 6f 59 ff 6c 45 3a 72 45 e3 9d 73 45" +
                        "14 61 72 57 95 ed 73 09 70 99 96 3b 82 eb f7 20" +
                        "3c 1f 78 a5 29 14 0c 18 2d bb e6 b4 2a e0 0c 02";

                issuerPKCert.setSignedBytes(Util.fromHexString(signedBytesStr));
                //issuerPKCert.setSignedBytes(Util.fromHexString(" SIGNED BYTES HERE "));

                issuerPKCert.setCAPublicKeyIndex(7);

                issuerPKCert.getIssuerPublicKey().setExponent(new byte[]{0x03});
                String remainderStr = "33 f5 e4 44 7d 4a 32 e5 93 6e 5a 13 39 32 9b b4 e8 dd 8b f0 04 4c e4 42 8e 24 d0 86 6f ae fd 23 48 80 9d 71";
                issuerPKCert.getIssuerPublicKey().setRemainder(Util.fromHexString(remainderStr));

                byte[] offlineDataAuthenticationRecords = new byte[0];

                ICCPublicKeyCertificate iccPKCert = new ICCPublicKeyCertificate(offlineDataAuthenticationRecords, issuerPKCert);
                //iccPKCert.setSignedBytes(Util.fromHexString(" SIGNED BYTES HERE "));
                iccPKCert.setSignedBytes(Util.fromHexString(signedBytesStr));

                iccPKCert.getICCPublicKey().setExponent(new byte[]{0x03});

                writeToUiAppend(tv1, "iccPKCert:\n" + iccPKCert);
                //System.out.println(iccPKCert);

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

    public static byte[] performRSA(byte[] dataBytes, byte[] expBytes, byte[] modBytes) {
        // source: https://github.com/sasc999/javaemvreader/blob/master/src/main/java/sasc/util/Util.java
        int inBytesLength = dataBytes.length;
        if (expBytes[0] >= (byte) 0x80) {
            //Prepend 0x00 to modulus
            byte[] tmp = new byte[expBytes.length + 1];
            tmp[0] = (byte) 0x00;
            System.arraycopy(expBytes, 0, tmp, 1, expBytes.length);
            expBytes = tmp;
        }
        if (modBytes[0] >= (byte) 0x80) {
            //Prepend 0x00 to modulus
            byte[] tmp = new byte[modBytes.length + 1];
            tmp[0] = (byte) 0x00;
            System.arraycopy(modBytes, 0, tmp, 1, modBytes.length);
            modBytes = tmp;
        }
        if (dataBytes[0] >= (byte) 0x80) {
            //Prepend 0x00 to signed data to avoid that the most significant bit is interpreted as the "signed" bit
            byte[] tmp = new byte[dataBytes.length + 1];
            tmp[0] = (byte) 0x00;
            System.arraycopy(dataBytes, 0, tmp, 1, dataBytes.length);
            dataBytes = tmp;
        }
        BigInteger exp = new BigInteger(expBytes);
        BigInteger mod = new BigInteger(modBytes);
        BigInteger data = new BigInteger(dataBytes);
        byte[] result = data.modPow(exp, mod).toByteArray();
        if (result.length == (inBytesLength+1) && result[0] == (byte)0x00) {
            //Remove 0x00 from beginning of array
            byte[] tmp = new byte[inBytesLength];
            System.arraycopy(result, 1, tmp, 0, inBytesLength);
            result = tmp;
        }
        return result;
    }

    public static byte[] calculateSHA1(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return sha1.digest(data);
    }


    // special version, needs a boolean variable in class header: boolean debugPrint = true;
    // if true this method will print the output additionally to the console
    // this version does not append the string to the exportString
    private void writeToUiAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(message);
            } else {
                String newString = textView.getText().toString() + "\n" + message;
                textView.setText(newString);
            }
            System.out.println(message);
        });
    }

    /**
     * section for OptionsMenu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mExportDumpFile = menu.findItem(R.id.action_export_text_file);
        mExportDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportDumpFile");
                //exportDumpFile();
                return false;
            }
        });

        MenuItem mMailDumpFile = menu.findItem(R.id.action_export_mail);
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