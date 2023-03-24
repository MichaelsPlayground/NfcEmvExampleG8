package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHexNpe;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexBlankToBytes;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;
import static de.androidcrypto.nfcemvexample.BinaryUtils.intToByteArrayV4;
import static de.androidcrypto.nfcemvexample.johnzweng.EmvKeyReader.concatenateModulus;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.nfcemvexample.cardvalidation.CardValidationResult;
import de.androidcrypto.nfcemvexample.cardvalidation.RegexCardValidator;
import de.androidcrypto.nfcemvexample.emulate.FilesModel;
import de.androidcrypto.nfcemvexample.extended.TagListParser;
import de.androidcrypto.nfcemvexample.extended.TagNameValue;
import de.androidcrypto.nfcemvexample.extended.TagSet;
import de.androidcrypto.nfcemvexample.johnzweng.EmvKeyReader;
import de.androidcrypto.nfcemvexample.johnzweng.EmvParsingException;
import de.androidcrypto.nfcemvexample.johnzweng.IssuerIccPublicKeyNew;
import de.androidcrypto.nfcemvexample.johnzweng.SignedDynamicApplicationData;
import de.androidcrypto.nfcemvexample.nfccreditcards.AidValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.DolValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.PdolUtil;
import de.androidcrypto.nfcemvexample.paymentcardgenerator.CardType;
import de.androidcrypto.nfcemvexample.paymentcardgenerator.PaymentCardGeneratorImpl;
import de.androidcrypto.nfcemvexample.sasc.CA;
import de.androidcrypto.nfcemvexample.sasc.ICCPublicKey;
import de.androidcrypto.nfcemvexample.sasc.ICCPublicKeyCertificate;
import de.androidcrypto.nfcemvexample.sasc.IssuerPublicKeyCertificate;
import de.androidcrypto.nfcemvexample.sasc.Record;
import de.androidcrypto.nfcemvexample.sasc.StaticDataAuthenticationTagList;
import de.androidcrypto.nfcemvexample.sasc.Util;

public class MinimalisticReaderActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "MinimalisticReaderAct";

    Button btn1, btn2Decrypt, btn3, btn4, btn5, btn6, btn7;
    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etLog;

    private View loadingLayout;
    private NfcAdapter mNfcAdapter;
    private byte[] tagId;
    final String TechIsoDep = "android.nfc.tech.IsoDep";
    byte[] tag0x8cFound = new byte[0]; // tag 0x8c = CDOL1
    List<TagSet> tsList = new ArrayList<>(); // holds the tags found during reading
    String foundPan = "";
    String outputString = ""; // used for the UI output
    // exporting the data
    String exportString = "";
    String exportStringFileName = "emv.html";
    String stepSeparatorString = "*********************************";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minimalistic_reader);

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
        etLog = findViewById(R.id.etLog);
        loadingLayout = findViewById(R.id.loading_layout);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1 back to main menu");
                Intent intent = new Intent(MinimalisticReaderActivity.this, MainActivity.class);
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
                Log.d(TAG, "start minimalistic reader");

                // run the complete visacard workflow
                // 1 select PPSE not done
                // 2 select AID


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

        writeToUiAppend(etLog, "********* realtime ************");
        writeToUiAppend(etLog, "*********   fDDA   ************");
        writeToUiAppend(etLog, "9f37 unpredictable number:      " + bytesToHexNpe(t9f37));
        writeToUiAppend(etLog, "9f02 transaction amount:        " + bytesToHexNpe(t9f02));
        writeToUiAppend(etLog, "5f2a transaction currency code: " + bytesToHexNpe(t5f2a));
        writeToUiAppend(etLog, "9f69 card auth data = UDOL:     " + bytesToHexNpe(t9f69));
        writeToUiAppend(etLog, "hashStreamByte2:\n" + bytesToHexNpe(hashStreamByte2));
        //writeToUiAppend(tv1, "sDDAcomplete B:\n" + bytesToHexNpe(sDDAcompleteByte));
        //byte[] calculatedHash = calculateSHA1(hashStream.toByteArray());
        byte[] calculatedHash2 = calculateSHA1(hashStreamByte2);
        writeToUiAppend(etLog, "calculatedHash2: " + bytesToHexNpe(calculatedHash2));
        writeToUiAppend(etLog, "hashResult:      " + bytesToHexNpe(sDAD.getHashResult()));
        writeToUiAppend(etLog, "fDDA validation equals hashResult: " + Arrays.equals(calculatedHash2, sDAD.getHashResult()));
        writeToUiAppend(etLog, "********* realtime ************");
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


    /**
     * section for getting the tags from read responses
     */

    private byte[] getTagValueFromList(@NonNull List<TagSet> tagSetList, @NonNull byte[] tag) {
        int size = tagSetList.size();
        for (int i = 0; i < size; i++) {
            TagSet tagSet = tagSetList.get(i);
            if (Arrays.equals(tag, tagSet.getTag())) {
                return tagSet.getTagValue();
            }
        }
        return null;
    }

    private List<TagSet> getTagSetFromResponse(@NonNull byte[] data, @NonNull String tagsFound) {
        List<TagSet> tagsSet = new ArrayList<>();
        List<TagNameValue> parsedTags = TagListParser.parseRespond(data);
        int parsedTagsSize = parsedTags.size();
        //writeToUiAppend(etLog, "selectPpseResponseParsedSize: " + selectPpseResponseParsedSize);
        for (int i = 0; i < parsedTagsSize; i++) {
            TagNameValue parsedTag = parsedTags.get(i);
            //writeToUiAppend(etLog, "selectPpseResponseParsed " + i + ": " + selectPpseResponseParsed.toString());
            byte[] eTag = parsedTag.getTagBytes();
            String eTagName = parsedTag.getTagName();
            //String eTagName = selectPpseTag.getTag().getDescription();
            byte[] eTagValue = parsedTag.getTagValueBytes();
            String eTagValueType = parsedTag.getTagValueType();
            //TagValueTypeEnum eTagValueType = selectPpseParsedTag.getTagValueType();
            //String eTagFound = "selectPpse";
            TagSet tagSet = new TagSet(eTag, eTagName, eTagValue, eTagValueType, tagsFound);
            tagsSet.add(tagSet);
            //writeToUiAppend(etLog, "--- tag nr " + i + " ---");
            //writeToUiAppend(etLog, eTagSet.dump());
        }
        return tagsSet;
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
        runOnUiThread(this::clearData);
        setLoadingLayoutVisibility(true);
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
        MediaPlayer mp = MediaPlayer.create(MinimalisticReaderActivity.this, R.raw.single_ping);
        mp.start();
    }

    private void playDoublePing() {
        MediaPlayer mp = MediaPlayer.create(MinimalisticReaderActivity.this, R.raw.double_ping);
        mp.start();
    }

    private void clearData() {
        etLog.setText("");
        setLoadingLayoutVisibility(false);
        tag0x8cFound = new byte[0];
    }

    private void readIsoDep(Tag tag) {
        Log.i(TAG, "read a tag with IsoDep technology");
        IsoDep nfc = null;
        nfc = IsoDep.get(tag);
        if (nfc != null) {
            // init of the service methods
            AidValues aidV = new AidValues();
            PdolUtil pu = new PdolUtil(nfc);

            try {
                nfc.connect();
                writeToUiAppend(etLog, "");
                printStepHeader(etLog, 1, "select PPSE = skipped =");
                printStepHeader(etLog, 2, "search applications = skipped =");
                writeToUiAppend(etLog, "");
                printStepHeader(etLog, 3, "select application by AID");
                byte[] aidSelected = hexToBytes("A0000000031010");
                String aidSelectedName = "VISA credit/debit";
                writeToUiAppend(etLog, "03 select application by AID " + bytesToHexNpe(aidSelected));
                writeToUiAppend(etLog, "card is a " + aidSelectedName);
                byte[] selectAidCommand = selectApdu(aidSelected);
                byte[] selectAidResponse = nfc.transceive(selectAidCommand);
                writeToUiAppend(etLog, "");
                writeToUiAppend(etLog, "03 select AID command length " + selectAidCommand.length + " data: " + bytesToHex(selectAidCommand));
                byte[] selectAidResponseOk = checkResponse(selectAidResponse);
                if (selectAidResponseOk != null) {
                    writeToUiAppend(etLog, "03 select AID response length " + selectAidResponseOk.length + " data: " + bytesToHex(selectAidResponseOk));
                    tsList.addAll(getTagSetFromResponse(selectAidResponseOk, "selectAid " + aidSelectedName));
                    prettyPrintData(etLog, selectAidResponseOk);


                    writeToUiAppend(etLog, "");
                    printStepHeader(etLog, 4, "search for tag 0x9F38");
                    writeToUiAppend(etLog, "04 search for tag 0x9F38 in the selectAid response");
                    BerTlvParser parser = new BerTlvParser();

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
                        /**
                         * BASIC CODE
                         */
                        // byte[] commandGpoRequest = hexToBytes(pu.getPdolWithCountryCode()); // basic code = fixed GPO

                        /**
                         * ADVANCED CODE
                         */
                        gpoRequestCommand = getGpoFromPdol(pdolValue); // advanced one, build it dynamically

                        writeToUiAppend(etLog, "");
                        printStepHeader(etLog, 5, "get the processing options");
                        writeToUiAppend(etLog, "05 get the processing options command length: " + gpoRequestCommand.length + " data: " + bytesToHex(gpoRequestCommand));
                        byte[] gpoRequestResponse = nfc.transceive(gpoRequestCommand);
                        if (!responseSendWithPdolFailure(gpoRequestResponse)) {
                            byte[] gpoRequestResponseOk = checkResponse(gpoRequestResponse);
                            if (gpoRequestResponseOk != null) {
                                writeToUiAppend(etLog, "05 run GPO response length: " + gpoRequestResponseOk.length + " data: " + bytesToHex(gpoRequestResponseOk));
                                tsList.addAll(getTagSetFromResponse(gpoRequestResponseOk, "get processing options"));
                                prettyPrintData(etLog, gpoRequestResponseOk);

                                /**
                                 * response can be a tag 77 Response Message Template Format 2
                                 * (found with my Visa-, Master- and German Giro-Cards)
                                 * or a tag 80 Response Message Template Format 1
                                 * (found with my American Express card)
                                 */

                                writeToUiAppend(etLog, "");
                                printStepHeader(etLog, 6, "read files & search PAN");
                                writeToUiAppend(etLog, "06 read the files from card and search for tag 0x57 in each file");
                                String pan_expirationDate = readPanFromFilesFromGpo(nfc, gpoRequestResponseOk);
                                String[] parts = pan_expirationDate.split("_");
                                writeToUiAppend(etLog, "");
                                printStepHeader(etLog, 7, "print PAN & expire date");
                                writeToUiAppend(etLog, "07 get PAN and Expiration date from tag 0x57 (Track 2 Equivalent Data)");
                                writeToUiAppend(etLog, "data for AID " + bytesToHexNpe(aidSelected) + " (" + aidSelectedName + ")");
                                writeToUiAppend(etLog, "PAN: " + parts[0]);
                                writeToUiAppend(etLog, "Expiration date (YYMM): " + parts[1]);
                                foundPan = parts[0];


                                // intermediate step - get single data from card, will be printed later
                                byte[] applicationTransactionCounter = getApplicationTransactionCounter(nfc);
                                byte[] pinTryCounter = getPinTryCounter(nfc);
                                byte[] lastOnlineATCRegister = getLastOnlineATCRegister(nfc);
                                byte[] logFormat = getLogFormat(nfc);

                                writeToUiAppend(etLog, "");
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
                                if (internalAuthResponse != null) {
                                    writeToUiAppend(etLog, "internalAuthResponse: " + internalAuthResponse.length + " data: " + bytesToHex(internalAuthResponse));
                                    prettyPrintData(etLog, internalAuthResponse);
                                } else {
                                    writeToUiAppend(etLog, "internalAuthResponse failure");
                                }

                                writeToUiAppend(etLog, "");
                                writeToUiAppend(etLog, "get the application cryptogram");
                                // check that it was found in any file
                                writeToUiAppend(etLog, "### tag0x8cFound: " + bytesToHex(tag0x8cFound));
                                byte[] getApplicationCryptoResponseOk = null;
                                if (tag0x8cFound.length > 1) {
                                    byte[] getApplicationCryptoCommand = getAppCryptoCommandFromCdol(tag0x8cFound);
                                    writeToUiAppend(etLog, "getApplicationCryptoCommand length: " + getApplicationCryptoCommand.length + " data: " + bytesToHex(getApplicationCryptoCommand));
                                    byte[] getApplicationCryptoResponse = nfc.transceive(getApplicationCryptoCommand);
                                    if (getApplicationCryptoResponse != null) {
                                        getApplicationCryptoResponseOk = checkResponse(getApplicationCryptoResponse);
                                        if (getApplicationCryptoResponseOk != null) {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponseOk.length + " data: " + bytesToHex(getApplicationCryptoResponseOk));
                                            prettyPrintData(etLog, getApplicationCryptoResponseOk);
                                        } else {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponse.length + " data: " + bytesToHex(getApplicationCryptoResponse));
                                        }
                                    } else {
                                        writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponse.length + " data: " + bytesToHex(getApplicationCryptoResponse));
                                    }
                                } else {
                                    // no cdol1 found
                                    // work with an empty cdol1
                                    writeToUiAppend(etLog, "no CDOL1 found in files, using an empty one");
                                    byte[] getApplicationCryptoCommand = getAppCryptoCommandFromCdol(new byte[0]);
                                    writeToUiAppend(etLog, "getApplicationCryptoCommand length: " + getApplicationCryptoCommand.length + " data: " + bytesToHex(getApplicationCryptoCommand));
                                    byte[] getApplicationCryptoResponse = nfc.transceive(getApplicationCryptoCommand);
                                    if (getApplicationCryptoResponse != null) {
                                        getApplicationCryptoResponseOk = checkResponse(getApplicationCryptoResponse);
                                        if (getApplicationCryptoResponseOk != null) {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse length: " + getApplicationCryptoResponseOk.length + " data: " + bytesToHex(getApplicationCryptoResponseOk));
                                            prettyPrintData(etLog, getApplicationCryptoResponseOk);
                                        } else {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse failure");
                                        }
                                    } else {
                                        writeToUiAppend(etLog, "getApplicationCryptoResponse failure");
                                    }
                                } // get application crypto
                                if (getApplicationCryptoResponseOk != null) {
                                    String applicationCryptoResponseString = dumpApplicationCryptoResponseMessageTemplate1(getApplicationCryptoResponseOk);
                                    writeToUiAppend(etLog, "Response Message Template Format 1 applicationCrypto:\n" + applicationCryptoResponseString);

                                }

                                /**
                                 * dump the tag list
                                 */

                                writeToUiAppend(etLog, "---- tagSet list ----");
                                for (int i = 0; i < tsList.size(); i++) {
                                    writeToUiAppend(etLog, "--- tag nr " + i + " ---");
                                    writeToUiAppend(etLog, tsList.get(i).dump());
                                }

                                /**
                                 * retrieve some data fields for crypto
                                 */

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
                                // todo null check ?
                                // todo 9F2F Integrated Circuit Card (ICC) PIN Encipherment Public Key Remainder Remaining digits of the ICC PIN Encipherment Public Key Modulus
                                tag90_IssuerPublicKeyCertificate = getTagValueFromList(tsList, new byte[]{(byte) 0x90});
                                tag8f_CertificationAuthorityPublicKeyIndex = getTagValueFromList(tsList, new byte[]{(byte) 0x8f});
                                tag9f32_IssuerPublicKeyExponent = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x32});
                                tag92_IssuerPublicKeyRemainder = getTagValueFromList(tsList, new byte[]{(byte) 0x92});
                                tag9f46_IccPublicKeyCertificate = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x46});
                                tag9f47_IccPublicKeyExponent = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x47});
                                tag9f48_IccPublicKeyRemainder = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x48});
                                tag9f4a_StaticDataAuthenticationTagList = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x4a});
                                tag9f69_Udol = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x69});
                                tag9f4b_SignedDynamicApplicationData = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x4b});
                                tag9f10_IssuerApplicationData = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x10});
                                tag9f26_ApplicationCryptogram = getTagValueFromList(tsList, new byte[]{(byte) 0x9f, (byte) 0x26});

                                // note: the output looks strange but it is for a copy & paste to manually transfer the data to CryptoStuffActivity
                                writeToUiAppend(etLog, "tag90_IssuerPublicKeyCertificate = hexToBytes(\"" + bytesToHexNpe(tag90_IssuerPublicKeyCertificate) + "\");");
                                writeToUiAppend(etLog, "tag8f_CertificationAuthorityPublicKeyIndex = hexToBytes(\"" + bytesToHexNpe(tag8f_CertificationAuthorityPublicKeyIndex) + "\");");
                                writeToUiAppend(etLog, "tag9f32_IssuerPublicKeyExponent = hexToBytes(\"" + bytesToHexNpe(tag9f32_IssuerPublicKeyExponent) + "\");");
                                writeToUiAppend(etLog, "tag92_IssuerPublicKeyRemainder = hexToBytes(\"" + bytesToHexNpe(tag92_IssuerPublicKeyRemainder) + "\");");
                                writeToUiAppend(etLog, "tag9f46_IccPublicKeyCertificate = hexToBytes(\"" + bytesToHexNpe(tag9f46_IccPublicKeyCertificate) + "\");");
                                writeToUiAppend(etLog, "tag9f47_IccPublicKeyExponent = hexToBytes(\"" + bytesToHexNpe(tag9f47_IccPublicKeyExponent) + "\");");
                                writeToUiAppend(etLog, "tag9f48_IccPublicKeyRemainder = hexToBytes(\"" + bytesToHexNpe(tag9f48_IccPublicKeyRemainder) + "\");");
                                writeToUiAppend(etLog, "tag9f4a_StaticDataAuthenticationTagList = hexToBytes(\"" + bytesToHexNpe(tag9f4a_StaticDataAuthenticationTagList) + "\");");
                                writeToUiAppend(etLog, "tag9f69_Udol = hexToBytes(\"" + bytesToHexNpe(tag9f69_Udol) + "\");");
                                writeToUiAppend(etLog, "tag9f4b_SignedDynamicApplicationData = hexToBytes(\"" + bytesToHexNpe(tag9f4b_SignedDynamicApplicationData) + "\");");
                                writeToUiAppend(etLog, "tag9f10_IssuerApplicationData = hexToBytes(\"" + bytesToHexNpe(tag9f10_IssuerApplicationData) + "\");");
                                writeToUiAppend(etLog, "tag9f26_ApplicationCryptogram = hexToBytes(\"" + bytesToHexNpe(tag9f26_ApplicationCryptogram) + "\");");

                                // crypto stuff
                                byte[] visaRid = hexToBytes("A000000003");
                                byte[] caPublicKeyVisa09Modulus = hexToBytes("9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41");
                                byte[] caPublicKeyVisa09Exponent = hexToBytes(("03"));
                                byte[] caPublicKeyVisa09Sha1 = hexToBytes("1FF80A40173F52D7D27E0F26A146A1C8CCB29046");
                                // https://www.linkedin.com/pulse/emv-application-specification-offline-data-oda-part-farghaly-1f?trk=pulse-article
                                // see package johnzweng
                                EmvKeyReader emvKeyReader = new EmvKeyReader();

                                // step 1 - get the issuer public key from issuer public key certificate
                                writeToUiAppend(etLog, "==============================");
                                writeToUiAppend(etLog, "step 1 Retrieval of Issuer Public Key by johnzweng");
                                // this is the johnzweng method to decrypt
                                IssuerIccPublicKeyNew issuerIccPublicKeyNew = null;
                                try {
                                    issuerIccPublicKeyNew = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyVisa09Exponent, caPublicKeyVisa09Modulus, tag90_IssuerPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                                    writeToUiAppend(etLog, "decrypted the tag90_IssuerPublicKeyCertificate to the public key");
                                    writeToUiAppend(etLog, "issuerIccPublicKey recovered: " + bytesToHexNpe(issuerIccPublicKeyNew.getRecoveredBytes()));
                                } catch (EmvParsingException e) {
                                    //throw new RuntimeException(e);
                                }

                                EmvKeyReader.RecoveredIssuerPublicKey recoveredIssuerPublicKeyParsed = null;
                                try {
                                    recoveredIssuerPublicKeyParsed = emvKeyReader.parseIssuerPublicKeyCert(issuerIccPublicKeyNew.getRecoveredBytes(), caPublicKeyVisa09Modulus.length);
                                    writeToUiAppend(etLog, "parsed recovered Issuer Public Key\n" + recoveredIssuerPublicKeyParsed.dump());
                                } catch (EmvParsingException e) {
                                    //throw new RuntimeException(e);
                                }

                                writeToUiAppend(etLog, "==============================");
                                writeToUiAppend(etLog, "Validate the Issuer Public Key");
                                try {
                                    boolean issuerPublicKeyIsValid = emvKeyReader.validateIssuerPublicKey(caPublicKeyVisa09Exponent, caPublicKeyVisa09Modulus, tag90_IssuerPublicKeyCertificate, null, tag9f32_IssuerPublicKeyExponent);
                                    writeToUiAppend(etLog, "the decrypted Issuer Public Key is valid: " + issuerPublicKeyIsValid);
                                } catch (EmvParsingException e) {
                                    //throw new RuntimeException(e);
                                }
/*
                                // step 2 - get the icc public key from icc public key certificate
                                writeToUiAppend(tv1, "==============================");
                                writeToUiAppend(tv1, "step 2 decrypting the IccPublicKeyCertificate");
                                byte[] fullKeyModulus = concatenateModulus(recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), recoveredIssuerPublicKeyParsed.getOptionalPadding());
                                byte[] recoveredIccPublicKeyCertificate = performRSA(tag9f46_IccPublicKeyCertificate, tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits());
                                writeToUiAppend(tv1, "decrypted: " + bytesToHexNpe(recoveredIccPublicKeyCertificate));
*/
                                writeToUiAppend(etLog, "==============================");
                                writeToUiAppend(etLog, "step 2 Retrieval of ICC Public Key by johnzweng");
                                ICCPublicKey iccPublicKey;
                                IssuerIccPublicKeyNew issuerIccPublicKey2New = null;
                                try {
                                    issuerIccPublicKey2New = emvKeyReader.parseIccPublicKeyNew(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
                                    writeToUiAppend(etLog, "decrypted the IccPublicKeyCertificate to the public key");
                                    writeToUiAppend(etLog, "iccPublicKey recovered: " + bytesToHexNpe(issuerIccPublicKey2New.getRecoveredBytes()));
                                } catch (EmvParsingException e) {
                                    //throw new RuntimeException(e);
                                }

                                EmvKeyReader.RecoveredIccPublicKey recoveredIccPublicKey = null;
                                try {
                                    recoveredIccPublicKey = emvKeyReader.parseIccPublicKeyCert(issuerIccPublicKey2New.getRecoveredBytes(), recoveredIssuerPublicKeyParsed.getIssuerPublicKeyLength());
                                    writeToUiAppend(etLog, "parsed recovered ICC Public Key\n" + recoveredIccPublicKey.dump());
                                } catch (EmvParsingException e) {
                                    //throw new RuntimeException(e);
                                }

                                writeToUiAppend(etLog, "==============================");
                                writeToUiAppend(etLog, "Validate the ICC Public Key (will fail !!)");
                                try {
                                    boolean iccPublicKeyIsValid = emvKeyReader.validateIccPublicKey(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, null, tag9f47_IccPublicKeyExponent);
                                    writeToUiAppend(etLog, "the decrypted ICC Public Key is valid: " + iccPublicKeyIsValid);
                                } catch (EmvParsingException e) {
                                    //throw new RuntimeException(e);
                                }

                                // now 2 ways
                                // a) ask the card with an internal auth command and decrypt the response - or -
                                // b) you already received Signed Dynamic Data as shown here and decrypt these

                                // way b) decrypt tag9f4b_SignedDynamicApplicationData for visa comd
                                writeToUiAppend(etLog, "==============================");
                                writeToUiAppend(etLog, "step 3 decrypting the SignedDynamicApplicationData with the ICC public key");
                                //byte[] fullKeyModulus = concatenateModulus(recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), recoveredIssuerPublicKeyParsed.getOptionalPadding());
                                byte[] recoveredSignedDynamicApplicationData = performRSA(tag9f4b_SignedDynamicApplicationData, tag9f47_IccPublicKeyExponent, recoveredIccPublicKey.getLeftMostPubKeyDigits());
                                writeToUiAppend(etLog, "decrypted: " + bytesToHexNpe(recoveredSignedDynamicApplicationData));
                                SignedDynamicApplicationData signedDynamicApplicationData = new SignedDynamicApplicationData(recoveredSignedDynamicApplicationData);
                                writeToUiAppend(etLog, "parsed SignedDynamicApplicationData\n" + signedDynamicApplicationData.dump());

                                // validate the SignedDynamicApplicationData
                                //Step 5: Concatenation of Signed Data Format, Hash Algorithm Indicator,
                                //        ICC Dynamic Data Length, ICC Dynamic Data, Pad Pattern, random number
                                writeToUiAppend(etLog, "==============================");
                                writeToUiAppend(etLog, "validate the SignedDynamicApplicationData");
                                try {
                                    boolean isSignedDynamicApplicationDataValid = validateSignedDynamicApplicationDataFDda(signedDynamicApplicationData, tag9f69_Udol);
                                    writeToUiAppend(etLog, "isSignedDynamicApplicationDataValid: " + isSignedDynamicApplicationDataValid);
                                } catch (EmvParsingException | NoSuchAlgorithmException e) {
                                    //throw new RuntimeException(e);
                                }


                            }
                        } else {
                            // we do not need this path
                            writeToUiAppend(etLog, "Found a strange behaviour - get processing options got wrong data to proceed... sorry");
                        }
                    }

                }

                // print the complete Log
                writeToUiFinal(etLog);
                setLoadingLayoutVisibility(false);
            } catch (IOException e) {
                Log.e(TAG, "IsoDep Error on connecting to card: " + e.getMessage());
                //throw new RuntimeException(e);
            }
            try {
                nfc.close();
            } catch (IOException e) {
                //throw new RuntimeException(e);
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

    private String dumpApplicationCryptoResponseMessageTemplate1(
            byte[] applicationCryptoResponseOk) {
        // check that response is a 0x80 Response Message Template Format 1
        byte[] respHeader = Arrays.copyOfRange(applicationCryptoResponseOk, 0, 2);
        if (Arrays.equals(respHeader, new byte[]{(byte) 0x80, (byte) 0x12})) {
            byte[] resp9F27 = Arrays.copyOfRange(applicationCryptoResponseOk, 2, 3);
            byte[] resp9F36 = Arrays.copyOfRange(applicationCryptoResponseOk, 3, 5);
            byte[] resp9F26 = Arrays.copyOfRange(applicationCryptoResponseOk, 5, 13);
            byte[] resp9F10 = Arrays.copyOfRange(applicationCryptoResponseOk, 14, 21);
            StringBuilder sb = new StringBuilder();
            sb.append("80 12 -- Response Message Template Format 1").append("\n");
            sb.append("- tag 0x9F27 length 01\n  Cryptogram Information Data (CID)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F27)).append("\n");
            sb.append("- tag 0x9F36 length 02\n  Appl. Transaction Counter (ATC)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F36)).append("\n");
            sb.append("- tag 0x9F26 length 08\n  Application Cryptogram\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F26)).append("\n");
            sb.append("- tag 0x9F10 length 07\n  Issuer Application Data (IAD)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F10)).append("\n");
            return sb.toString();
        } else {
            return "";
        }
        /*
                                    https://stackoverflow.com/a/35892602/8166854
                                    if response is tag 0x80 Response Message Template Format 1
                                    - x9F27:  # EMV, Cryptogram Information Data (CID)
                                        val: "80" # Cryptogram Information Data (CID).
                                        # 10______ - bits 8-7, ARQC
                                        # _____000 - bits 3-1 (Reason/Advice/Referral Code), No information given
                                    + x9F36: "0001" # EMV, Application Transaction Counter (ATC)
                                    + x9F26: "0102030405060708" # EMV, Cryptogram, Application
                                    + x9F10: "06010A03A40000" # EMV, Issuer Application Data (IAD)

                                    8012
                                        80
                                          000e
                                              03ab88079529a75c
                                                              06590203a00000
                                     */
    }

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
            pan = bytesToHex(tag5aBytes);
        }
        // search for expiration date
        BerTlv tag5f24 = tlvs.find(new BerTag(0x5f, 0x24));
        if (tag5f24 != null) {
            Log.d(TAG, "found tag 0x5f24 Application Expiration Date");
            byte[] tag5f24Bytes = tag5f24.getBytesValue();
            expirationDate = bytesToHex(tag5f24Bytes);
        }
        return pan + "_" + expirationDate;
    }

    /**
     * checks that a tag 0x94 Application File Locator (AFL) is available in gpoResponse
     *
     * @param gpoResponse
     * @return the list with afl entries (each of 4 byte)
     * if no afl was found it returns an empty list
     */
    private List<byte[]> checkForAflInGpoResponse(byte[] gpoResponse) {
        List<byte[]> aflList = new ArrayList<>();
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvs = parser.parse(gpoResponse);
        // search for tag 0x94 Application File Locator (AFL)
        BerTlv tag94 = tlvs.find(new BerTag(0x94));
        if (tag94 != null) {
            Log.d(TAG, "found tag 0x94 Application File Locator (AFL)");
            byte[] tag94Bytes = tag94.getBytesValue();
            // split array by 4 bytes
            List<byte[]> tag94BytesList = divideArray(tag94Bytes, 4);
            aflList.addAll(tag94BytesList);
            /*
            for (int i = 0; i < tag94BytesList.size(); i++) {
                aflList.add(tag94BytesList.get(i));
            }
             */
        } else {
            Log.d(TAG, "found NO tag 0x94 Application File Locator (AFL)");
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
                } else {
                    //writeToUiAppend(etLog, "** readRecordResponse failure");
                }
            }
        }
        return readFiles;
    }


    /**
     * reads all files on card using track2 or afl data
     *
     * @param getProcessingOptions
     * @return a String with PAN and Expiration date if found
     */
    private String readPanFromFilesFromGpo(IsoDep nfc, byte[] getProcessingOptions) {
        writeToUiAppend(etLog, "");
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

        byte[] aflBytes = null;

        /**
         * response can be a tag 77 Response Message Template Format 2
         * (found with my Visa-, Master- and German Giro-Cards)
         * or a tag 80 Response Message Template Format 1
         * (found with my American Express card)
         */

        // search for tag 0x80 = Response Message Template Format 1:
        // 80 12 180008010100080303000805050010020200
        //       1800 = AIP
        //           08010100080303000805050010020200 = AFL 4 blocks of 4 bytes

        // search for tag 0x94 = AFL in tag 77 Response Message Template Format 2
        BerTlvs tlvsGpo02 = parser.parse(getProcessingOptions);
        BerTlv tag94 = tlvsGpo02.find(new BerTag(0x94));
        // it is a template 2
        if (tag94 != null) {
            aflBytes = tag94.getBytesValue();
        }
        // template 1
        BerTlv tag80 = tlvsGpo02.find(new BerTag(0x80));
        if (tag80 != null) {
            byte[] dataTemp = tag80.getBytesValue();
            // first 2 bytes are AIP, followed by xx AFL bytes
            dataTemp = ArrayUtils.subarray(dataTemp, 2, dataTemp.length);
            if (dataTemp != null) {
                aflBytes = dataTemp.clone();
            }
        }
        if (aflBytes != null) {
            // split array by 4 bytes
            //List<byte[]> tag94BytesList = divideArray(tag94Bytes, 4);
            List<byte[]> tag94BytesList = divideArray(aflBytes, 4);
            int tag94BytesListLength = tag94BytesList.size();
            //writeToUiAppend(etLog, "tag94Bytes divided into " + tag94BytesListLength + " arrays");
            writeToUiAppend(etLog, "");
            writeToUiAppend(etLog, "The AFL contains " + tag94BytesListLength + " entries to read");
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

                    //System.out.println("*#* readRecord iRecords: " + iRecords);
                    byte[] cmd = hexToBytes("00B2000400");
                    cmd[2] = (byte) (iRecords & 0x0FF);
                    cmd[3] |= (byte) (sfiNew & 0x0FF);
                    writeToUiAppend(etLog, "");
                    writeToUiAppend(etLog, "read command length: " + cmd.length + " data: " + bytesToHex(cmd));

                    try {
                        resultReadRecord = nfc.transceive(cmd);
                        //writeToUiAppend(etLog, "readRecordCommand length: " + cmd.length + " data: " + bytesToHex(cmd));
                        byte[] resultReadRecordOk = checkResponse(resultReadRecord);
                        if (resultReadRecordOk != null) {
                            //writeToUiAppend(etLog, "data from AFL " + bytesToHex(tag94BytesListEntry)); // given wrong output for second or third files in multiple records
                            writeToUiAppend(etLog, "data from AFL was: " + bytesToHex(tag94BytesListEntry));
                            writeToUiAppend(etLog, "data from AFL " + "SFI: " + String.format("%02X", sfiOrg) + " REC: " + String.format("%02d", iRecords));

                            byte sfiFile = (byte) (cmd[3] >>> 3);

                            writeToUiAppend(etLog, "data from AFL " + "SFI: " + String.format("%02X", sfiFile) + " REC: " + String.format("%02d", iRecords));
                            writeToUiAppend(etLog, "read result length: " + resultReadRecordOk.length + " data: " + bytesToHex(resultReadRecordOk));
                            prettyPrintData(etLog, resultReadRecordOk);
                            tsList.addAll(getTagSetFromResponse(resultReadRecordOk, "read file from AFL " + "SFI: " + String.format("%02X", sfiOrg) + " REC: " + String.format("%02d", iRecords)));
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
                                    pan = removeTrailingF(bytesToHex(tag5aBytes));
                                    Log.e(TAG, "found tag 0x5A PAN: " + pan);
                                }
                                BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                if (tag5f24 != null) {
                                    byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                    expirationDate = bytesToHex(tag5f24Bytes);
                                } else {
                                    // System.out.println("record: " + iRecords + " Tag 5F24 not found");
                                }
                                /**
                                 * ADVANCED CODE
                                 */
                                findTag0x8c(tlvsAfl);
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

    //

    /**
     * ADVANCED CODE
     */

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
        //System.out.println("checkResponse: " + bytesToHex(data));
        //if (data.length < 5) return null; // not ok
        if (data.length < 5) {
            //System.out.println("checkResponse: data length " + data.length);
            return null;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x9000) {
            //System.out.println("status: " + status);
            return null;
        } else {
            //System.out.println("will return: " + bytesToHex(Arrays.copyOfRange(data, 0, data.length - 2)));
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
        // e.g. visa returns 9f36020045 9000
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
/* amex:
I/System.out: getApplicationCryptoResponse length: 20 data: 8012800005d116c2f20f228b7b06590203a00000
I/System.out: ------------------------------------
I/System.out: 80 12 -- Response Message Template Format 1
I/System.out:       80 00 05 D1 16 C2 F2 0F 22 8B 7B 06 59 02 03 A0
I/System.out:       00 00 (BINARY)
I/System.out: ------------------------------------
see: https://stackoverflow.com/a/35892602/8166854
 */
/* amex:
 - DATA:
  - x80:
     tag: "80"
     len: "12" #   // 18
   - val:  # Template, Response Message Format 1.
    - x9F27:  # EMV, Cryptogram Information Data (CID)
       val: "80" # Cryptogram Information Data (CID).
       # 10______ - bits 8-7, ARQC
       # _____000 - bits 3-1 (Reason/Advice/Referral Code), No information given
     + x9F36: "0001" # EMV, Application Transaction Counter (ATC)
     + x9F26: "0102030405060708" # EMV, Cryptogram, Application
     + x9F10: "06010A03A40000" # EMV, Issuer Application Data (IAD)

 */

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

    private void printSingleData(TextView textView, byte[] applicationTransactionCounter,
                                 byte[] pinTryCounter, byte[] lastOnlineATCRegister, byte[] logFormat) {
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
            System.out.println(message);
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

        MenuItem mClearData = menu.findItem(R.id.action_clear_data);
        mClearData.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                clearData();
                return false;
            }
        });

        MenuItem mAnonymizePan = menu.findItem(R.id.action_anonymize_pan);
        mAnonymizePan.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                //anonymizePan();
                return false;
            }
        });

        MenuItem mGeneratePan = menu.findItem(R.id.action_generate_pan);
        mGeneratePan.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                clearData();
                PaymentCardGeneratorImpl pcg = new PaymentCardGeneratorImpl();
                String visaPan = pcg.generateByCardType(CardType.VISA_FIX16);
                String masterPan = pcg.generateByCardType(CardType.MASTERCARD_FIX);
                String amexcoPan = pcg.generateByCardType(CardType.AMERICAN_EXPRESS);
                StringBuilder sb = new StringBuilder();
                //masterPan = "5375050000160110";
                sb.append("Generated CreditCard numbers").append("\n");
                sb.append("Visa:   ").append(visaPan).append("\n");
                sb.append("Master: ").append(masterPan).append("\n");
                sb.append("Amexco: ").append(amexcoPan).append("\n");
                //etData.setText(sb.toString());

                // validate pan
                CardValidationResult visaPanValid = RegexCardValidator.isValid(visaPan);
                CardValidationResult masterPanValid = RegexCardValidator.isValid(masterPan);
                CardValidationResult amexcoPanValid = RegexCardValidator.isValid(amexcoPan);
                StringBuilder sbV = new StringBuilder();
                sbV.append("").append("\n");
                sbV.append("Validation of CC numbers").append("\n");
                sbV.append("Visa:   ").append(visaPanValid.isValid()).append("\n");
                sbV.append("Master: ").append(masterPanValid.isValid()).append("\n");
                sbV.append("Amexco: ").append(amexcoPanValid.isValid()).append("\n");
                //writeToUiAppend(etData, sbV.toString());
                return false;
            }
        });

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
                Intent intent = new Intent(MinimalisticReaderActivity.this, FileReaderActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mExportEmulationDataActivity = menu.findItem(R.id.action_activity_export_emulation_data);
        mExportEmulationDataActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MinimalisticReaderActivity.this, ExportEmulationDataActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mViewEmulationDataActivity = menu.findItem(R.id.action_activity_view_emulation_data);
        mViewEmulationDataActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MinimalisticReaderActivity.this, ViewEmulationDataActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mExtendedReadActivity = menu.findItem(R.id.action_activity_extended_read);
        mExtendedReadActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MinimalisticReaderActivity.this, ExtendedReadActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mCryptoStuffActivity = menu.findItem(R.id.action_activity_crypto);
        mCryptoStuffActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MinimalisticReaderActivity.this, CryptoStuffActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mMinimalisticReaderActivity = menu.findItem(R.id.action_activity_minimalistic_reader);
        mMinimalisticReaderActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(MinimalisticReaderActivity.this, MinimalisticReaderActivity.class);
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