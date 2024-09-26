package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHexNpe;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexBlankToBytes;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;
import static de.androidcrypto.nfcemvexample.johnzweng.EmvKeyReader.concatenateModulus;
import static de.androidcrypto.nfcemvexample.johnzweng.EmvUtils.calculateSHA1;

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

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import de.androidcrypto.nfcemvexample.johnzweng.EmvKeyReader;
import de.androidcrypto.nfcemvexample.johnzweng.EmvParsingException;
import de.androidcrypto.nfcemvexample.johnzweng.IssuerIccPublicKey;
import de.androidcrypto.nfcemvexample.johnzweng.IssuerIccPublicKeyNew;
import de.androidcrypto.nfcemvexample.johnzweng.SignedDynamicApplicationData;
import de.androidcrypto.nfcemvexample.nfccreditcards.DolTag;
import de.androidcrypto.nfcemvexample.sasc.ApplicationInterchangeProfile;
import de.androidcrypto.nfcemvexample.sasc.CA;
import de.androidcrypto.nfcemvexample.sasc.CVMList;
import de.androidcrypto.nfcemvexample.sasc.ICCPublicKey;
import de.androidcrypto.nfcemvexample.sasc.ICCPublicKeyCertificate;
import de.androidcrypto.nfcemvexample.sasc.IssuerPublicKey;
import de.androidcrypto.nfcemvexample.sasc.IssuerPublicKeyCertificate;
import de.androidcrypto.nfcemvexample.sasc.Record;
import de.androidcrypto.nfcemvexample.sasc.StaticDataAuthenticationTagList;
import de.androidcrypto.nfcemvexample.sasc.Util;

public class CryptoStuffActivity extends AppCompatActivity {

    private final String TAG = "CryptoStuffAct";

    Button btn1, btn2Decrypt, btn3, btn4, btn5, btn6, btnVisaDkbDecrypt;
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
        btnVisaDkbDecrypt = findViewById(R.id.btnVisaDkbDecrypt);
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

                // https://www.linkedin.com/pulse/emv-application-specification-offline-data-oda-part-farghaly-1f?trk=pulse-article
                // see package johnzweng
                EmvKeyReader emvKeyReader = new EmvKeyReader();

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Retrieval of Issuer Public Key by johnzweng");
                // this is the johnzweng method to decrypt
                IssuerIccPublicKeyNew issuerIccPublicKeyNew;
                try {
                    issuerIccPublicKeyNew = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyVisa09Exponent, caPublicKeyVisa09Modulus, tag90_IssuerPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                    writeToUiAppend(tv1, "decrypted the tag90_IssuerPublicKeyCertificate to the public key");
                    writeToUiAppend(tv1, "issuerIccPublicKey recovered: " + bytesToHexNpe(issuerIccPublicKeyNew.getRecoveredBytes()));
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }


                EmvKeyReader.RecoveredIssuerPublicKey recoveredIssuerPublicKeyParsed;
                try {
                    recoveredIssuerPublicKeyParsed = emvKeyReader.parseIssuerPublicKeyCert(recoveredIssuerPublicKey, caPublicKeyVisa09Modulus.length);
                    writeToUiAppend(tv1, "parsed recovered Issuer Public Key\n" + recoveredIssuerPublicKeyParsed.dump());
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Validate the Issuer Public Key");
                try {
                    boolean issuerPublicKeyIsValid = emvKeyReader.validateIssuerPublicKey(caPublicKeyVisa09Exponent, caPublicKeyVisa09Modulus, tag90_IssuerPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                    writeToUiAppend(tv1, "the decrypted Issuer Public Key is valid: " + issuerPublicKeyIsValid);
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }


                // next step: Terminal decrypt ICC public key certificate using the issuer public key
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "decrypting the IccPublicKeyCertificate");
                byte[] fullKeyModulus = concatenateModulus(recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), recoveredIssuerPublicKeyParsed.getOptionalPadding());
                byte[] recoveredIccPublicKeyCertificate = performRSA(tag9f46_IccPublicKeyCertificate, tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits());
                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredIccPublicKeyCertificate));

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Retrieval of ICC Public Key by johnzweng");
                ICCPublicKey iccPublicKey;
                IssuerIccPublicKeyNew issuerIccPublicKey2New;
                try {
                    issuerIccPublicKey2New = emvKeyReader.parseIccPublicKeyNew(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
/*
public IssuerIccPublicKey parseIccPublicKey(byte[] issuerPublicKeyExponent, byte[] issuerPublicKeyModulus, byte[] iccPublicKeyCertificate,
                                                byte[] iccRemainder, byte[] iccPublicKeyExponent)
 */
                    //iccPublicKey = emvKeyReader.parseIccPublicKey(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                    writeToUiAppend(tv1, "decrypted the IccPublicKeyCertificate to the public key");
                    writeToUiAppend(tv1, "iccPublicKey recovered: " + bytesToHexNpe(issuerIccPublicKey2New.getRecoveredBytes()));
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }


                EmvKeyReader.RecoveredIccPublicKey recoveredIccPublicKey;
                try {
                    recoveredIccPublicKey = emvKeyReader.parseIccPublicKeyCert(recoveredIccPublicKeyCertificate, recoveredIssuerPublicKeyParsed.getIssuerPublicKeyLength());
                    writeToUiAppend(tv1, "parsed recovered ICC Public Key\n" + recoveredIccPublicKey.dump());
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Validate the ICC Public Key (will fail !!)");
                try {
                    boolean iccPublicKeyIsValid = emvKeyReader.validateIccPublicKey(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
                    writeToUiAppend(tv1, "the decrypted ICC Public Key is valid: " + iccPublicKeyIsValid);
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                // now 2 ways
                // a) ask the card with an internal auth command and decrypt the response - or -
                // b) you already received Signed Dynamic Data as shown here and decrypt these

                // way b) decrypt tag9f4b_SignedDynamicApplicationData
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "decrypting the SignedDynamicApplicationData with the ICC public key");
                //byte[] fullKeyModulus = concatenateModulus(recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), recoveredIssuerPublicKeyParsed.getOptionalPadding());
                byte[] recoveredSignedDynamicApplicationData = performRSA(tag9f4b_SignedDynamicApplicationData, tag9f47_IccPublicKeyExponent, recoveredIccPublicKey.getLeftMostPubKeyDigits());
                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredSignedDynamicApplicationData));
                SignedDynamicApplicationData signedDynamicApplicationData = new SignedDynamicApplicationData(recoveredSignedDynamicApplicationData);
                writeToUiAppend(tv1, "parsed SignedDynamicApplicationData\n" + signedDynamicApplicationData.dump());

                // validate the SignedDynamicApplicationData
                //Step 5: Concatenation of Signed Data Format, Hash Algorithm Indicator,
                //        ICC Dynamic Data Length, ICC Dynamic Data, Pad Pattern, random number
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "validate the SignedDynamicApplicationData");
                try {
                    boolean isSignedDynamicApplicationDataValid = validateSignedDynamicApplicationData(signedDynamicApplicationData, tag9f69_Udol);
                    writeToUiAppend(tv1, "isSignedDynamicApplicationDataValid: " + isSignedDynamicApplicationDataValid);
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

/*
Signed Dynamic Application Data
Now we decode the Signed Dynamic Application Data

	var picKey = new Key();
	picKey.setType(Key.PUBLIC);
	picKey.setComponent(Key.MODULUS, iccPublicKeyModulus);
	picKey.setComponent(Key.EXPONENT, this.emv.cardDE[0x9F47]);

	var decryptedSDAD = crypto.decrypt(picKey, Crypto.RSA, SDAD);
Field Name	Length	Description
Recovered Data Header	1	Hex value '6A'
Signed Data Format	1	Hex value '05'
Hash Algorithm Indicator	1	Identifies the hash algorithm used to produce the Hash Result in the digital signature scheme
ICC Dynamic Data Length	1	Identifies the length of the ICC Dynamic Data in bytes
ICC Dynamic Data Length	LDD	Dynamic data generated by and/or stored in the ICC
Pad Pattern	NIC - LDD - 25	(NIC - LDD - 25) padding bytes of value 'BB'
Hash Result	20	Hash of the Dynamic Application Data and its related infromation
Recovered Data Trailer	1	Hex value'BC'
 */



/*
decrypted: 48e26a471054d1ae93d86ab9daaa30a8036d47997e0b556101e950462f67cbc8b92033aefd7132cd1c01c32e8a9e47cdceb80a9f9aded4f8fa951e7fb938357264508d73ea159ea88fba9dc2dabc9a49ebe5ddf93235c2140dde2ee35306f6c1bcfb646f55e408f45f653cf2580082556b526d861f263781874324facb100d7f3a66616d2bcf8b2a41a3a2b5511fd64362c0282d292ae6668608242e3640dd6f33fb671e5141d1bc97afe08f44b4b117
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
                        + "46 92 98 20 36 76 95 49 5f 34 01 01 9f 07 02 ff"
                        + "80 8e 14 00 00 00 00 00 00 00 00 02 01 44 03 01"
                        + "03 02 03 1e 03 1f 00 9f 0d 05 b8 60 ac 88 00 9f"
                        + "0e 05 00 10 00 00 00 9f 0f 05 b8 68 bc 98 00 5f"
                        + "28 02 06 42 9f 4a 01 82");

                //EMVUtil.printResponse(appRecord, true);

                Record record = new Record(appRecord, 1, true);
                //app.getApplicationFileLocator().getApplicationElementaryFiles().get(2).setRecord(1, record);

                StaticDataAuthenticationTagList staticDataAuthTagList = new StaticDataAuthenticationTagList(new byte[]{(byte) 0x82});
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

                // https://www.eftlab.co.uk/knowledge-base/list-of-ca-public-keys
                byte[] caPublicKeyMc05Modulus = hexToBytes("B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597");
                byte[] caPublicKeyMc05Exponent = hexToBytes(("03"));
                byte[] caPublicKeyVisa09Sha1 = hexToBytes("EBFA0D5D06D8CE702DA3EAE890701D45E274C845");
                byte[] mcRid = hexToBytes("A000000004");

                // here we are starting the crypto stuff and try to decrypt some data from the card
                // we do need the content of some tags:
                // these tags are available on Visa comd M
                byte[] tag90_IssuerPublicKeyCertificate;
                byte[] tag8f_CertificationAuthorityPublicKeyIndex;
                byte[] tag9f32_IssuerPublicKeyExponent;
                byte[] tag92_IssuerPublicKeyRemainder;
                byte[] tag9f46_IccPublicKeyCertificate;
                byte[] tag9f47_IccPublicKeyExponent;
                byte[] tag9f48_IccPublicKeyRemainder;
                byte[] tag9f4a_StaticDataAuthenticationTagList;
                byte[] tag9f69_Udol;
                byte[] tag9f4b_SignedDynamicApplicationData;
                byte[] tag9f10_IssuerApplicationData;
                byte[] tag9f26_ApplicationCryptogram;

                // data from MasterCard AAB
                tag90_IssuerPublicKeyCertificate = hexToBytes("04cc60769cabe557a9f2d83c7c73f8b177dbf69288e332f151fba10027301bb9a18203ba421bda9c2cc8186b975885523bf6707f287a5e88f0f6cd79a076319c1404fcdd1f4fa011f7219e1bf74e07b25e781d6af017a9404df9fd805b05b76874663ea88515018b2cb6140dc001a998016d28c4af8e49dfcc7d9cee314e72ae0d993b52cae91a5b5c76b0b33e7ac14a7294b59213ca0c50463cfb8b040bb8ac953631b80fa85a698b00228b5ff44223");
                tag8f_CertificationAuthorityPublicKeyIndex = hexToBytes("05");
                tag9f32_IssuerPublicKeyExponent = hexToBytes("03");
                tag92_IssuerPublicKeyRemainder = hexToBytes("abfd2ebc115c3796e382be7e9863b92c266ccabc8bd014923024c80563234e8a11710a01");
                tag9f46_IccPublicKeyCertificate = hexToBytes("3cada902afb40289fbdfea01950c498191442c1b48234dcaff66bca63cbf821a3121fa808e4275a4e894b154c1874bddb00f16276e92c73c04468253b373f1e6a9a89e2705b4670682d0adff05617a21d7684031a1cdb438e66cd98d591dc376398c8aab4f137a2226122990d9b2b4c72ded6495d637338fefa893ae7fb4eb845f8ec2e260d2385a780f9fda64b3639a9547adad806f78c9bc9f17f9d4c5b26474b9ba03892a754ffdf24df04c702f86");
                tag9f47_IccPublicKeyExponent = hexToBytes("03");
                tag9f4a_StaticDataAuthenticationTagList = hexToBytes("82");
                tag9f69_Udol = hexToBytes("");
                tag9f4b_SignedDynamicApplicationData = hexToBytes("6d7b7d1c95d45442537104ff16574386cdf4509e6b3a5eb0fa6ca5fab1508f571362cebb9257e9e7c978bf314d1689ff521a82eefb915c9930aaf1c575647c42703547df26b40ea9444775dc274a4fd212f95fcdcf4b7357740d58c1726547e51055118274e3e13bad1cea5516b2b945ca5ba5ab2948aff0e1230baa2c8dd013");
                tag9f10_IssuerApplicationData = hexToBytes("0110a08003240000000000000000000000ff");
                tag9f26_ApplicationCryptogram = hexToBytes("2b71adccea8e0046");

                EmvKeyReader emvKeyReader = new EmvKeyReader();

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Retrieval of Issuer Public Key by johnzweng");
                // this is the johnzweng method to decrypt
                IssuerIccPublicKeyNew issuerIccPublicKeyNew;
                try {
                    //issuerIccPublicKeyNew = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyMc05Exponent, caPublicKeyMc05Modulus, tag90_IssuerPublicKeyCertificate, tag92_IssuerPublicKeyRemainder, tag9f32_IssuerPublicKeyExponent);
                    issuerIccPublicKeyNew = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyMc05Exponent, caPublicKeyMc05Modulus, tag90_IssuerPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                    writeToUiAppend(tv1, "decrypted the tag90_IssuerPublicKeyCertificate to the public key");
                    writeToUiAppend(tv1, "issuerIccPublicKey recovered: " + bytesToHexNpe(issuerIccPublicKeyNew.getRecoveredBytes()));
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                EmvKeyReader.RecoveredIssuerPublicKey recoveredIssuerPublicKeyParsed;
                try {
                    recoveredIssuerPublicKeyParsed = emvKeyReader.parseIssuerPublicKeyCert(issuerIccPublicKeyNew.getRecoveredBytes(), caPublicKeyMc05Modulus.length);
                    writeToUiAppend(tv1, "parsed recovered Issuer Public Key\n" + recoveredIssuerPublicKeyParsed.dump());
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Validate the Issuer Public Key");
                try {
                    boolean issuerPublicKeyIsValid = emvKeyReader.validateIssuerPublicKey(caPublicKeyMc05Exponent, caPublicKeyMc05Modulus, tag90_IssuerPublicKeyCertificate, tag92_IssuerPublicKeyRemainder, tag9f32_IssuerPublicKeyExponent);
                    writeToUiAppend(tv1, "the decrypted Issuer Public Key is valid: " + issuerPublicKeyIsValid);
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }


                // manual decryption
                // Retrieval of Issuer Public Key
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Manual Retrieval of Issuer Public Key");

                writeToUiAppend(tv1, "IssuerPublicKeyCertificate: " + bytesToHexNpe(tag90_IssuerPublicKeyCertificate));
                byte[] recoveredIssuerPublicKey = performRSA(tag90_IssuerPublicKeyCertificate, caPublicKeyMc05Exponent, caPublicKeyMc05Modulus);
                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredIssuerPublicKey));

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Manual Retrieval of ICC Public Key");

                writeToUiAppend(tv1, "IccPublicKeyCertificate: " + bytesToHexNpe(tag9f46_IccPublicKeyCertificate));
                byte[] fullModulus = concatenateModulus(recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag92_IssuerPublicKeyRemainder);
                //byte[] recoveredIccPublicKeyManual = performRSA(tag9f46_IccPublicKeyCertificate, tag9f47_IccPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits());
                byte[] recoveredIccPublicKeyManual = performRSA(tag9f46_IccPublicKeyCertificate, tag9f47_IccPublicKeyExponent, fullModulus);
                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredIccPublicKeyManual));


                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Retrieval of ICC Public Key by johnzweng");
                IssuerIccPublicKeyNew issuerIccPublicKey2New;
                try {
                    //issuerIccPublicKey2New = emvKeyReader.parseIccPublicKeyNew(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, tag92_IssuerPublicKeyRemainder, tag9f47_IccPublicKeyExponent);
                    issuerIccPublicKey2New = emvKeyReader.parseIccPublicKeyNew(tag9f32_IssuerPublicKeyExponent, fullModulus, tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
                    writeToUiAppend(tv1, "decrypted the IccPublicKeyCertificate to the public key");
                    writeToUiAppend(tv1, "iccPublicKey recovered: " + bytesToHexNpe(issuerIccPublicKey2New.getRecoveredBytes()));
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                EmvKeyReader.RecoveredIccPublicKey recoveredIccPublicKey;
                try {
                    recoveredIccPublicKey = emvKeyReader.parseIccPublicKeyCert(issuerIccPublicKey2New.getRecoveredBytes(), recoveredIssuerPublicKeyParsed.getIssuerPublicKeyLength());
                    writeToUiAppend(tv1, "parsed recovered ICC Public Key\n" + recoveredIccPublicKey.dump());
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Validate the ICC Public Key (will fail !!)");
                try {
                    boolean iccPublicKeyIsValid = emvKeyReader.validateIccPublicKey(tag9f32_IssuerPublicKeyExponent, fullModulus, tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
                    //boolean iccPublicKeyIsValid = emvKeyReader.validateIccPublicKey(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
                    writeToUiAppend(tv1, "the decrypted ICC Public Key is valid: " + iccPublicKeyIsValid);
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }

                // now 2 ways
                // a) ask the card with an internal auth command and decrypt the response - or -
                // b) you already received Signed Dynamic Data as shown here and decrypt these

                // way ask the card with an internal auth command and decrypt the response
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "decrypting the SignedDynamicApplicationData with the ICC public key");
                //byte[] fullKeyModulus = concatenateModulus(recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), recoveredIssuerPublicKeyParsed.getOptionalPadding());
                byte[] recoveredSignedDynamicApplicationData = performRSA(tag9f4b_SignedDynamicApplicationData, tag9f47_IccPublicKeyExponent, recoveredIccPublicKey.getLeftMostPubKeyDigits());
                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredSignedDynamicApplicationData));
                SignedDynamicApplicationData signedDynamicApplicationData = new SignedDynamicApplicationData(recoveredSignedDynamicApplicationData);
                writeToUiAppend(tv1, "parsed SignedDynamicApplicationData\n" + signedDynamicApplicationData.dump());

                // validate the SignedDynamicApplicationData
                //Step 5: Concatenation of Signed Data Format, Hash Algorithm Indicator,
                //        ICC Dynamic Data Length, ICC Dynamic Data, Pad Pattern, random number
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "validate the SignedDynamicApplicationData");
                try {
                    boolean isSignedDynamicApplicationDataValid = validateSignedDynamicApplicationData(signedDynamicApplicationData, tag9f69_Udol);
                    writeToUiAppend(tv1, "isSignedDynamicApplicationDataValid: " + isSignedDynamicApplicationDataValid);
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // shows AIP dump data
                // Visa comd m 20 20
                // MC aab 19 80
                // GC voba 18 00
                // Visa Lloyds 20 00
                ApplicationInterchangeProfile aipVisaComd = new ApplicationInterchangeProfile((byte) 0x20, (byte) 0x20);
                writeToUiAppend(tv1, "AIP Visa comd: " + aipVisaComd.toString());
                ApplicationInterchangeProfile aipMcAab = new ApplicationInterchangeProfile((byte) 0x19, (byte) 0x80);
                writeToUiAppend(tv1, "AIP MC AAB: " + aipMcAab.toString());
                ApplicationInterchangeProfile aipGcVoba = new ApplicationInterchangeProfile((byte) 0x18, (byte) 0x00);
                writeToUiAppend(tv1, "AIP GC Voba: " + aipGcVoba.toString());
                ApplicationInterchangeProfile aipVisaLloyds = new ApplicationInterchangeProfile((byte) 0x20, (byte) 0x00);
                writeToUiAppend(tv1, "AIP Visa Lloyds: " + aipVisaLloyds.toString());

                // CVM List is not used in qVSDC/Kernel C-3 path.
                //CVMList cvmList = new CVMList();
            }
        });

        btnVisaDkbDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This for MF DKB Visa Credit Card, exp. 09/2026
                System.out.println("VISA DKB Decrypt");
                writeToUiAppend(tv1, "VISA DKB decryption");
                writeToUiAppend(tv1, "==============================");
                // list of CA Public Keys
                // https://www.eftlab.co.uk/knowledge-base/list-of-ca-public-keys
                byte[] caPublicKeyVisa09Modulus = hexToBytes("9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41");
                byte[] caPublicKeyVisa09Exponent = hexToBytes(("03"));
                byte[] caPublicKeyVisa09Sha1 = hexToBytes("1FF80A40173F52D7D27E0F26A146A1C8CCB29046");
                byte[] visaRid = hexToBytes("A000000003");
                byte[] visaChksum = CA.calculateCAPublicKeyCheckSum(visaRid, Util.intToByteArray(9), caPublicKeyVisa09Modulus, new byte[]{0x03});
                writeToUiAppend(tv1, "Verify Visa Chksum: " + bytesToHexNpe(visaChksum));

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

                // taken from file in SFI 10 file 03: 70 81 FB -- Record Template (EMV Proprietary)
                // 90 81 F8 -- Issuer Public Key Certificate
                tag90_IssuerPublicKeyCertificate = hexToBytes("8893cf85a81325ab8da6a4196eb5787291db7205f61b172b26deb867da427f1d0e438e86400aea81a0f2826b250da618108389bdabe2a75c0168a28bb97645158b57ca8faa1d38d7a56e0a4171ec0d5e048d048dd98106bcadb3b5cac80485ff9c0fc970b4ea95d557fb9dd065bf75eb06f51df5a2c20479058ede6c8a376d9bfbf0c05b9e2b5aac1ec5982e2a9d861573e892da87b68357306e88cb054ab0090e01670a73d23fa239f4ae1283110fca40d46edc6c8021d15b3c147251b3c5e754f0fa9d82b7934ed34a12ef3d0a66c0c2a26a32e9722b10653516b356440aa8eece8d1d023829394adc2f9309ff60fc5baf51c0b24690be");

                // taken from file in SFI 10 file 04: 70 07 -- Record Template (EMV Proprietary)
                // 8F 01 -- Certification Authority Public Key Index - card 09 (BINARY)
                tag8f_CertificationAuthorityPublicKeyIndex = hexToBytes("09");

                // taken from file in SFI 10 file 05: 70 07 -- Record Template (EMV Proprietary)
                // 9F 32 01 -- Issuer Public Key Exponent 03 (BINARY)
                tag9f32_IssuerPublicKeyExponent = hexToBytes("03");

                // taken from file in SFI 10 file 05: 70 07 -- Record Template (EMV Proprietary)
                // 9F 46 81 B0 -- ICC Public Key Certificate
                tag9f46_IccPublicKeyCertificate = hexToBytes("7e3b33a489fb75a23643407d2ebf48a808957165aa538d681213d71495b577086e63a24e847ed29d2ceba4bb3b1784361221287607ace4b8bfce09dd8364d4709293ed52b528623472fb6157094b12367534d7cf5c20b810058c817fb87c130111ee53c3855fd2b2a95449d03795541ea7c6ef942b0b069bfa7caa5d0ec6db0e428f18d03adcf7f92fb7e5516403adc629f3ffbd6900a1f308fbe5d28cba795c6c62d7573333abed15ad00a4da4ba8a9");

                // taken from file in SFI 10 file 05: 70 07 -- Record Template (EMV Proprietary)
                // 9F 47 01 -- ICC Public Key Exponent 03 (BINARY)
                tag9f47_IccPublicKeyExponent = hexToBytes("03");

                // taken from file in SFI 10 file 05: 70 07 -- Record Template (EMV Proprietary)
                // 9F 4A 01 -- Static Data Authentication Tag List 82 (BINARY)
                tag9f4a_StaticDataAuthenticationTagList = hexToBytes("82");

                // taken from file in SFI 10 file 05: 70 07 -- Record Template (EMV Proprietary)
                // 9F 69 07 -- UDOL 01 B4 19 C7 27 38 00 (BINARY)
                tag9f69_Udol = hexToBytes("01B419C7273800");

                //tag9f4b_SignedDynamicApplicationData = hexToBytes("2731459c144bbc637d0cac5db31ca7b7c1a0e270436f0a4c690d544de494e01330040d0df4f7878440e01e626ea3d43c74c06bdf8773f1554afb2c9b5ff758d83d0e1184c6da6e8ddc73ba8b6586d374e8e1d46b5f23f89b2723444f3e2c7ef2aa6e87afc43a0c595b6d707bad93dbebea1f74a8649dbca30b6a55387e70e1f2");

                // taken from Get Processing Options response: 77 81 C6 -- Response Message Template Format 2
                // 9F 10 07 -- Issuer Application Data 06 01 0A 03 A0 20 00 (BINARY)
                tag9f10_IssuerApplicationData = hexToBytes("06010A03A02000");

                // taken from Get Processing Options response: 77 81 C6 -- Response Message Template Format 2
                // 9F 26 08 -- Application Cryptogram DE 1C 42 51 18 6F 7A 5E (BINARY)
                tag9f26_ApplicationCryptogram = hexToBytes("DE1C4251186F7A5E");

                // Retrieval of Issuer Public Key
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Retrieval of Issuer Public Key");
                writeToUiAppend(tv1, "IssuerPublicKeyCertificate: " + bytesToHexNpe(tag90_IssuerPublicKeyCertificate));
                byte[] recoveredIssuerPublicKey = performRSA(tag90_IssuerPublicKeyCertificate, caPublicKeyVisa09Exponent, caPublicKeyVisa09Modulus);
                writeToUiAppend(tv1, " ");
                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredIssuerPublicKey));
                writeToUiAppend(tv1, " ");
                // https://www.linkedin.com/pulse/emv-application-specification-offline-data-oda-part-farghaly-1f?trk=pulse-article
                // see package johnzweng
                EmvKeyReader emvKeyReader = new EmvKeyReader();

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Retrieval of Issuer Public Key by johnzweng");
                // this is the johnzweng method to decrypt
                IssuerIccPublicKeyNew issuerIccPublicKeyNew;
                try {
                    issuerIccPublicKeyNew = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyVisa09Exponent, caPublicKeyVisa09Modulus, tag90_IssuerPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                    writeToUiAppend(tv1, "decrypted the tag90_IssuerPublicKeyCertificate to the public key");
                    writeToUiAppend(tv1, "issuerIccPublicKey recovered: " + bytesToHexNpe(issuerIccPublicKeyNew.getRecoveredBytes()));
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }
                writeToUiAppend(tv1, " ");

                EmvKeyReader.RecoveredIssuerPublicKey recoveredIssuerPublicKeyParsed;
                try {
                    recoveredIssuerPublicKeyParsed = emvKeyReader.parseIssuerPublicKeyCert(recoveredIssuerPublicKey, caPublicKeyVisa09Modulus.length);
                    writeToUiAppend(tv1, "parsed recovered Issuer Public Key\n" + recoveredIssuerPublicKeyParsed.dump());
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }
                writeToUiAppend(tv1, " ");

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Validate the Issuer Public Key");
                try {
                    boolean issuerPublicKeyIsValid = emvKeyReader.validateIssuerPublicKey(caPublicKeyVisa09Exponent, caPublicKeyVisa09Modulus, tag90_IssuerPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                    writeToUiAppend(tv1, "the decrypted Issuer Public Key is valid: " + issuerPublicKeyIsValid);
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }
                writeToUiAppend(tv1, " ");

                // next step: Terminal decrypt ICC public key certificate using the issuer public key
                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Terminal will decrypting the IccPublicKeyCertificate");
                //byte[] fullKeyModulus = concatenateModulus(recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), recoveredIssuerPublicKeyParsed.getOptionalPadding());
                byte[] recoveredIccPublicKeyCertificate = performRSA(tag9f46_IccPublicKeyCertificate, tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits());
                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredIccPublicKeyCertificate));
                writeToUiAppend(tv1, " ");

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Retrieval of ICC Public Key by johnzweng");
                ICCPublicKey iccPublicKey;
                IssuerIccPublicKeyNew issuerIccPublicKey2New;
                try {
                    issuerIccPublicKey2New = emvKeyReader.parseIccPublicKeyNew(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
/*
public IssuerIccPublicKey parseIccPublicKey(byte[] issuerPublicKeyExponent, byte[] issuerPublicKeyModulus, byte[] iccPublicKeyCertificate,
                                                byte[] iccRemainder, byte[] iccPublicKeyExponent)
 */
                    //iccPublicKey = emvKeyReader.parseIccPublicKey(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                    writeToUiAppend(tv1, "decrypted the IccPublicKeyCertificate to the public key");
                    writeToUiAppend(tv1, "iccPublicKey recovered: " + bytesToHexNpe(issuerIccPublicKey2New.getRecoveredBytes()));
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }
                writeToUiAppend(tv1, " ");
                EmvKeyReader.RecoveredIccPublicKey recoveredIccPublicKey;
                try {
                    recoveredIccPublicKey = emvKeyReader.parseIccPublicKeyCert(recoveredIccPublicKeyCertificate, recoveredIssuerPublicKeyParsed.getIssuerPublicKeyLength());
                    writeToUiAppend(tv1, "parsed recovered ICC Public Key\n" + recoveredIccPublicKey.dump());
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }
                writeToUiAppend(tv1, " ");

                writeToUiAppend(tv1, "==============================");
                writeToUiAppend(tv1, "Validate the ICC Public Key (will fail !!)");
                try {
                    boolean iccPublicKeyIsValid = emvKeyReader.validateIccPublicKey(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
                    writeToUiAppend(tv1, "the decrypted ICC Public Key is valid: " + iccPublicKeyIsValid);
                } catch (EmvParsingException e) {
                    throw new RuntimeException(e);
                }
                writeToUiAppend(tv1, " ");

                writeToUiAppend(tv1, "==============================");

            }
        });


    }

    /**
     * Check if cert is valid and if the calculated hash matches the hash in the certificate
     *
     * @param sDAD
     * @return true if validation is successful, false otherwise
     * @throws EmvParsingException, NoSuchAlgorithmException
     */
    public boolean validateSignedDynamicApplicationData(SignedDynamicApplicationData sDAD, byte[] t9f69) throws EmvParsingException, NoSuchAlgorithmException {
        // Concatenation of Signed Data Format, Hash Algorithm Indicator,
        //        ICC Dynamic Data Length, ICC Dynamic Data, Pad Pattern, random number

        //String sDADOrg = "6a05010908d89a8ab98969d82abbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1cd1cdd6d05915423ed517767c2160015ac87c19bc";
        //String sDDANew = "05010908d89a8ab98969d82abbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
        //String randomNumberString = "01020304";
        //String sDDAcomplete = sDDANew + randomNumberString;
        //byte[] sDDAcompleteByte = hexToBytes(sDDAcomplete);

        // data that was used on GPO
        // DolTag t9f02 = setTag(new byte[]{(byte) 0x9f, (byte) 0x02}, "Transaction Amount", hexBlankToBytes("00 00 00 00 10 00")); // 00 00 00 00 10 00
        // DolTag t9f37 = setTag(new byte[]{(byte) 0x9f, (byte) 0x37}, "Unpredictable Number", hexBlankToBytes("38 39 30 31"));
        // DolTag t5f2a = setTag(new byte[]{(byte) 0x5f, (byte) 0x2a}, "Transaction Currency Code", hexBlankToBytes("09 78")); // eur
        // tag 0x9f69 = UDOL = 01 8C C9 F8 07 84 00

        byte[] t9f37 = hexBlankToBytes("38 39 30 31");
        byte[] t9f02 = hexBlankToBytes("00 00 00 00 10 00");
        byte[] t5f2a = hexBlankToBytes("09 78");
        //byte[] t9f69 = hexBlankToBytes("01 8C C9 F8 07 84 00");
        
/*
see C-3 Kernel 3 V 2.10 page 121
Table C-1: Terminal Dynamic Data for Input to DDA Hash Algorithm
Tag Data Element                         Length
'9F37' Unpredictable Number (UN)         4 bytes
'9F02' Amount, Authorised                6 bytes
'5F2A' Transaction Currency Code         2 bytes
'9F69' Card Authentication Related Data  var bytes

I/System.out: read command length: 5 data: 00b2031400
I/System.out: data from AFL was: 10010300
I/System.out: data from AFL SFI: 10 REC: 03
I/System.out: data from AFL SFI: 02 REC: 03

 9F 69 07 -- UDOL
          01 8C C9 F8 07 84 00 (BINARY)

Card Authentication Related Data
F: b
T: ‘9F69’
L: var. 5-16 S: Card
Conditional
If fDDA supported
Contains the fDDA Version Number, Card Unpredictable Number, and Card Transaction Qualifiers.
For transactions where fDDA is performed, the Card Authentication Related Data is returned
in the last record specified by the Application File Locator for that transaction.
Byte 1:    fDDA Version Number (‘01’)
Byte 2-5:  (Card) Unpredictable Number
Byte 6-7:  Card Transaction Qualifiers
 */

        // this is for DDA (MasterCard)
        ByteArrayOutputStream hashStream = new ByteArrayOutputStream();
        // calculate our own hash for comparison:
        //hashStream.write((byte) 5);
        //hashStream.write((byte) 1);
        //hashStream.write((byte) 9);
        hashStream.write(sDAD.getSignedDataFormat(), 0, sDAD.getSignedDataFormat().length);
        hashStream.write(sDAD.getHashAlgorithmIndicator(), 0, sDAD.getHashAlgorithmIndicator().length);
        hashStream.write(sDAD.getIccDynamicDataLength(), 0, sDAD.getIccDynamicDataLength().length);
        hashStream.write(sDAD.getIccDynamicData(), 0, sDAD.getIccDynamicData().length);
        hashStream.write(sDAD.getPadPattern(), 0, sDAD.getPadPattern().length);
        // todo get the used random number from authentication command
        // todo here the fixed number
        byte[] randomNumber = hexToBytes("E153F3E8");
        // first version E153F3E8
        // second version 01020304 // mastercard
        hashStream.write(randomNumber, 0, randomNumber.length);
        // calculate hash:
        writeToUiAppend(tv1, "*********************");
        byte[] hashStreamByte = hashStream.toByteArray();
        writeToUiAppend(tv1, "hashStreamByte:\n" + bytesToHexNpe(hashStreamByte));
        byte[] calculatedHash = calculateSHA1(hashStreamByte);
        writeToUiAppend(tv1, "calculatedHash: " + bytesToHexNpe(calculatedHash));
        writeToUiAppend(tv1, "hashResult:     " + bytesToHexNpe(sDAD.getHashResult()));
        writeToUiAppend(tv1, "*********************");
        // compare it with value in cert:
        return Arrays.equals(calculatedHash, sDAD.getHashResult());
    }

    /**
     * Check if cert is valid and if the calculated hash matches the hash in the certificate
     * this is for fDDA (some VisaCard only !)
     *
     * @param sDAD
     * @return true if validation is successful, false otherwise
     * @throws EmvParsingException, NoSuchAlgorithmException
     */
    public boolean validateSignedDynamicApplicationDataFDda(SignedDynamicApplicationData sDAD, byte[] t9f69) throws EmvParsingException, NoSuchAlgorithmException {
        // Concatenation of Signed Data Format, Hash Algorithm Indicator,
        //        ICC Dynamic Data Length, ICC Dynamic Data, Pad Pattern, random number

        //String sDADOrg = "6a05010908d89a8ab98969d82abbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb1cd1cdd6d05915423ed517767c2160015ac87c19bc";
        //String sDDANew = "05010908d89a8ab98969d82abbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
        //String randomNumberString = "01020304";
        //String sDDAcomplete = sDDANew + randomNumberString;
        //byte[] sDDAcompleteByte = hexToBytes(sDDAcomplete);

        // data that was used on GPO
        // DolTag t9f02 = setTag(new byte[]{(byte) 0x9f, (byte) 0x02}, "Transaction Amount", hexBlankToBytes("00 00 00 00 10 00")); // 00 00 00 00 10 00
        // DolTag t9f37 = setTag(new byte[]{(byte) 0x9f, (byte) 0x37}, "Unpredictable Number", hexBlankToBytes("38 39 30 31"));
        // DolTag t5f2a = setTag(new byte[]{(byte) 0x5f, (byte) 0x2a}, "Transaction Currency Code", hexBlankToBytes("09 78")); // eur
        // tag 0x9f69 = UDOL = 01 8C C9 F8 07 84 00

        byte[] t9f37 = hexBlankToBytes("38 39 30 31");
        byte[] t9f02 = hexBlankToBytes("00 00 00 00 10 00");
        byte[] t5f2a = hexBlankToBytes("09 78");

        /*
        see EMV 4.3 Book 2 page 80 for DDA and C-3 Kernel 3 v 2.10 page 121 for fDDA
        Concatenate from left to right the second to the sixth data elements in Table 17
        (that is, Signed Data Format through Pad Pattern),
        followed by the data elements specified by the DDOL.
         */

        // this is for fDDA (VisaCard)
        ByteArrayOutputStream hashStream2 = new ByteArrayOutputStream();
        // elements for DDA
        hashStream2.write(sDAD.getSignedDataFormat(), 0, sDAD.getSignedDataFormat().length);
        hashStream2.write(sDAD.getHashAlgorithmIndicator(), 0, sDAD.getHashAlgorithmIndicator().length);
        hashStream2.write(sDAD.getIccDynamicDataLength(), 0, sDAD.getIccDynamicDataLength().length);
        hashStream2.write(sDAD.getIccDynamicData(), 0, sDAD.getIccDynamicData().length);
        hashStream2.write(sDAD.getPadPattern(), 0, sDAD.getPadPattern().length);
        // elements instead of ddol elements for fDDA
        hashStream2.write(t9f37, 0, t9f37.length); // unpredictable number
        hashStream2.write(t9f02, 0, t9f02.length); // transaction amount
        hashStream2.write(t5f2a, 0, t5f2a.length); // transaction currency code
        hashStream2.write(t9f69, 0, t9f69.length); // Card Authentication Related Data = UDOL
        byte[] hashStreamByte2 = hashStream2.toByteArray();

        writeToUiAppend(tv1, "********* realtime ************");
        writeToUiAppend(tv1, "*********   fDDA   ************");
        writeToUiAppend(tv1, "9f37 unpredictable number:      " + bytesToHexNpe(t9f37));
        writeToUiAppend(tv1, "9f02 transaction amount:        " + bytesToHexNpe(t9f02));
        writeToUiAppend(tv1, "5f2a transaction currency code: " + bytesToHexNpe(t5f2a));
        writeToUiAppend(tv1, "9f69 card auth data = UDOL:     " + bytesToHexNpe(t9f69));
        writeToUiAppend(tv1, "hashStreamByte2:\n" + bytesToHexNpe(hashStreamByte2));
        //writeToUiAppend(tv1, "sDDAcomplete B:\n" + bytesToHexNpe(sDDAcompleteByte));
        //byte[] calculatedHash = calculateSHA1(hashStream.toByteArray());
        byte[] calculatedHash2 = calculateSHA1(hashStreamByte2);
        writeToUiAppend(tv1, "calculatedHash2: " + bytesToHexNpe(calculatedHash2));
        writeToUiAppend(tv1, "hashResult:      " + bytesToHexNpe(sDAD.getHashResult()));
        writeToUiAppend(tv1, "fDDA validation equals hashResult: " + Arrays.equals(calculatedHash2, sDAD.getHashResult()));
        writeToUiAppend(tv1, "********* realtime ************");
/*
see C-3 Kernel 3 V 2.10 page 121
Table C-1: Terminal Dynamic Data for Input to DDA Hash Algorithm
Tag Data Element                         Length
'9F37' Unpredictable Number (UN)         4 bytes
'9F02' Amount, Authorised                6 bytes
'5F2A' Transaction Currency Code         2 bytes
'9F69' Card Authentication Related Data  var bytes

I/System.out: read command length: 5 data: 00b2031400
I/System.out: data from AFL was: 10010300
I/System.out: data from AFL SFI: 10 REC: 03
I/System.out: data from AFL SFI: 02 REC: 03

 9F 69 07 -- UDOL
          01 8C C9 F8 07 84 00 (BINARY)

Card Authentication Related Data
F: b
T: ‘9F69’
L: var. 5-16 S: Card
Conditional
If fDDA supported
Contains the fDDA Version Number, Card Unpredictable Number, and Card Transaction Qualifiers.
For transactions where fDDA is performed, the Card Authentication Related Data is returned
in the last record specified by the Application File Locator for that transaction.
Byte 1:    fDDA Version Number (‘01’)
Byte 2-5:  (Card) Unpredictable Number
Byte 6-7:  Card Transaction Qualifiers

 */
        return Arrays.equals(calculatedHash2, sDAD.getHashResult());
    }

    /**
     * decrypts the encrypted data from the card
     * tag 0x90   - IssuerPublicKeyCertificate
     * tag 0x9f46 - IccPublicKeyCertificate
     * tag 0x9f4b - SignedDynamicApplicationData
     *
     * @param dataBytes ciphertext
     * @param expBytes  exponent of parent certificate
     * @param modBytes  modulus of parent certificate
     * @return decrypted data
     */

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
        if (result.length == (inBytesLength + 1) && result[0] == (byte) 0x00) {
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


    // this method will print the output additionally to the console
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