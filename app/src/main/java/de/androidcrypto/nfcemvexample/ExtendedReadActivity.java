package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHexNpe;
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

import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.nfcemvexample.cardvalidation.CardValidationResult;
import de.androidcrypto.nfcemvexample.cardvalidation.RegexCardValidator;
import de.androidcrypto.nfcemvexample.emulate.FilesModel;
import de.androidcrypto.nfcemvexample.extended.TagListParser;
import de.androidcrypto.nfcemvexample.extended.TagNameValue;
import de.androidcrypto.nfcemvexample.extended.TagSet;
import de.androidcrypto.nfcemvexample.nfccreditcards.AidValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.DolValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.PdolUtil;
import de.androidcrypto.nfcemvexample.paymentcardgenerator.CardType;
import de.androidcrypto.nfcemvexample.paymentcardgenerator.PaymentCardGeneratorImpl;
import de.androidcrypto.nfcemvexample.sasc.CA;
import de.androidcrypto.nfcemvexample.sasc.IssuerPublicKeyCertificate;
import de.androidcrypto.nfcemvexample.sasc.Util;

public class ExtendedReadActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "NfcCcExtendedReadAct";

    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etData, etLog;
    SwitchMaterial prettyPrintResponse;
    private View loadingLayout;

    private NfcAdapter mNfcAdapter;
    private byte[] tagId;

    final String TechIsoDep = "android.nfc.tech.IsoDep";

    boolean debugPrint = true; // if set to true the writeToUi method will print to console
    boolean isPrettyPrintResponse = true; // default
    String aidSelectedForAnalyze = "";
    String aidSelectedForAnalyzeName = "";
    // there vars are filled during reading of files from AFL
    byte[] tag0x8cFound = new byte[0]; // tag 0x8c = CDOL1

    List<TagSet> tsList = new ArrayList<>(); // holds the tags found during reading

    String outputString = ""; // used for the UI output
    // exporting the data
    String exportString = "";
    String foundPan = "";
    private final String ANONYMIZED_PAN = "1122334455667788";
    private final String ANONYMIZED_PAN_WITH_SPACE = "11 22 33 44 55 66 77 88 ";
    private boolean runAnonymizing = false;
    String exportStringFileName = "emv.html";
    String stepSeparatorString = "*********************************";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended_read);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        tv1 = findViewById(R.id.tv1);
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
        MediaPlayer mp = MediaPlayer.create(ExtendedReadActivity.this, R.raw.single_ping);
        mp.start();
    }

    private void playDoublePing() {
        MediaPlayer mp = MediaPlayer.create(ExtendedReadActivity.this, R.raw.double_ping);
        mp.start();
    }

    private void clearData() {
        etLog.setText("");
        etData.setText("");
        exportString = "";
        aidSelectedForAnalyze = "";
        aidSelectedForAnalyzeName = "";
        outputString = "";
        tag0x8cFound = new byte[0];
        foundPan = "";
        runAnonymizing = false;
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
                tsList = new ArrayList<>(); // holds the tags found during reading

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
                    writeToUiFinal(etLog);
                    setLoadingLayoutVisibility(false);
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                byte[] selectPpseResponseOk = checkResponse(selectPpseResponse);
                if (selectPpseResponseOk != null) {
                    // pretty print of response
                    if (isPrettyPrintResponse) prettyPrintData(etLog, selectPpseResponseOk);

                    // extended
                    tsList.addAll(getTagSetFromResponse(selectPpseResponseOk, "selectPpse"));
                    /* old
                    List<TagAndLength> selectPpseTags = com.github.devnied.emvnfccard.utils.TlvUtil.parseTagAndLength(selectPpseResponseOk);
                    int selectPpseTagsSize = selectPpseTags.size();
                    writeToUiAppend(etLog, "selectPpseTagsSize: " + selectPpseTagsSize);
                    for (int i = 0; i < selectPpseTagsSize; i++) {
                        TagAndLength selectPpseTag = selectPpseTags.get(i);
                        writeToUiAppend(etLog, "selectPpseTag " + i + ": " + selectPpseTag.toString());
                        byte[] eTag = selectPpseTag.getTag().getTagBytes();
                        String eTagName = selectPpseTag.getTag().getName();
                        //String eTagName = selectPpseTag.getTag().getDescription();
                        byte[] eTagValue = selectPpseTag.getBytes();
                        TagValueTypeEnum eTagValueType = selectPpseTag.getTag().getTagValueType();
                        String eTagFound = "selectPpse";
                        TagSet eTagSet = new TagSet(eTag, eTagName, eTagValue, eTagValueType.toString(), eTagFound);
                        writeToUiAppend(etLog, "--- tag nr " + i + " ---");
                        writeToUiAppend(etLog, eTagSet.dump());
                    }

                    List<TagNameValue> selectPpseResponseParsed =  TagListParser.parseRespond(selectPpseResponseOk);
                    int selectPpseResponseParsedSize = selectPpseResponseParsed.size();
                    writeToUiAppend(etLog, "selectPpseResponseParsedSize: " + selectPpseResponseParsedSize);
                    for (int i = 0; i < selectPpseResponseParsedSize; i++) {
                        TagNameValue selectPpseParsedTag = selectPpseResponseParsed.get(i);
                        writeToUiAppend(etLog, "selectPpseResponseParsed " + i + ": " + selectPpseResponseParsed.toString());
                        byte[] eTag = selectPpseParsedTag.getTagBytes();
                        String eTagName = selectPpseParsedTag.getTagName();
                        //String eTagName = selectPpseTag.getTag().getDescription();
                        byte[] eTagValue = selectPpseParsedTag.getTagValueBytes();
                        String eTagValueType = selectPpseParsedTag.getTagValueType();
                        //TagValueTypeEnum eTagValueType = selectPpseParsedTag.getTagValueType();
                        String eTagFound = "selectPpse";
                        TagSet eTagSet = new TagSet(eTag, eTagName, eTagValue, eTagValueType, eTagFound);
                        writeToUiAppend(etLog, "--- tag nr " + i + " ---");
                        writeToUiAppend(etLog, eTagSet.dump());
                    }
                     */

                    writeToUiAppend(etLog, "");
                    printStepHeader(etLog, 2, "search applications on card");
                    writeToUiAppend(etLog, "02 analyze select PPSE response and search for tag 0x4F (applications on card)");

                    BerTlvParser parser = new BerTlvParser();
                    BerTlvs tlv4Fs = parser.parse(selectPpseResponseOk);
                    // by searching for tag 4f
                    List<BerTlv> tag4fList = tlv4Fs.findAll(new BerTag(0x4F));
                    if (tag4fList.size() < 1) {
                        writeToUiAppend(etLog, "there is no tag 0x4F available, stopping here");
                        writeToUiFinal(etLog);
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
                        boolean selectAidResponseNotAllowed = responseNotAllowed(selectAidResponse);
                        if (selectAidResponseNotAllowed) {
                            writeToUiAppend(etLog, "03 selecting AID is not allowed on card");
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

                        System.out.println("*#*#*# some stuff");
                        writeToUiAppend(etLog, "*** generate RSA keypair ***");
                        System.out.println("*** generate RSA keypair ***");
                        generateRsaKeyPair();
                        try {
                            System.out.println("IssuerPublicKeyCertificate start: ");
                            IssuerPublicKeyCertificate.main(null);
                            System.out.println("IssuerPublicKeyCertificate individual start: ");
                            /*
                            data from Visa comd m:
                            PAN is 4871 7800 8277 0574 Exp 07/25 A000000003

                            SFI: 2 Record: 1
                            7081fb9081f85ab54faf4ad810b3cca4ed42c38e1e768fca3187ed1be4196c6779c4633cbe88751889c12b05e10ee87cb198518793ff61e87534f66850e96239b76648429eced4cc207608d0d2a932dd9e8c4bb0d139c4eca59e1ef5f4708f72d80dc5b66c45f4566c91b55384dfdeabb55faa622c6764cc9fb4c4900b6ab2cec5abad9057e2cf63a881bb4ec2a5d96634d7c11366eb908a168d33aa3c544822fc83e74c104b9275b2ef1cf41375b404a260bbf8fb3d4452af3d0630bb1ec2a01676ba588ae7820727622a6d9df5c93a3ce807d54b79ae007c3d401f8787dc3e235e8b9ae6b1b9279328cb1ca94105434010f15eb07f487f4d5c94f4a5a7
                            90 Issuer Public Key Certificate
                            5AB54FAF4AD810B3CCA4ED42C38E1E768FCA3187ED1BE4196C6779C4633CBE88751889C12B05E10EE87CB198518793FF61E87534F66850E96239B76648429ECED4CC207608D0D2A932DD9E8C4BB0D139C4ECA59E1EF5F4708F72D80DC5B66C45F4566C91B55384DFDEABB55FAA622C6764CC9FB4C4900B6AB2CEC5ABAD9057E2CF63A881BB4EC2A5D96634D7C11366EB908A168D33AA3C544822FC83E74C104B9275B2EF1CF41375B404A260BBF8FB3D4452AF3D0630BB1EC2A01676BA588AE7820727622A6D9DF5C93A3CE807D54B79AE007C3D401F8787DC3E235E8B9AE6B1B9279328CB1CA94105434010F15EB07F487F4D5C94F4A5A7

                            SFI: 2 Record: 2
                            70078f01099f320103
                            8F Certification Authority Public Key Index
                            09
                            9F32 Issuer Public Key Exponent
                            03

                             */
                            byte[] rid = Util.fromHexString("a0 00 00 00 03"); // visa
                            //byte[] mod = Util.fromHexString("BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B");
                            byte[] mod = Util.fromHexString("5AB54FAF4AD810B3CCA4ED42C38E1E768FCA3187ED1BE4196C6779C4633CBE88751889C12B05E10EE87CB198518793FF61E87534F66850E96239B76648429ECED4CC207608D0D2A932DD9E8C4BB0D139C4ECA59E1EF5F4708F72D80DC5B66C45F4566C91B55384DFDEABB55FAA622C6764CC9FB4C4900B6AB2CEC5ABAD9057E2CF63A881BB4EC2A5D96634D7C11366EB908A168D33AA3C544822FC83E74C104B9275B2EF1CF41375B404A260BBF8FB3D4452AF3D0630BB1EC2A01676BA588AE7820727622A6D9DF5C93A3CE807D54B79AE007C3D401F8787DC3E235E8B9AE6B1B9279328CB1CA94105434010F15EB07F487F4D5C94F4A5A7");
                            // 5AB54FAF4AD810B3CCA4ED42C38E1E768FCA3187ED1BE4196C6779C4633CBE88751889C12B05E10EE87CB198518793FF61E87534F66850E96239B76648429ECED4CC207608D0D2A932DD9E8C4BB0D139C4ECA59E1EF5F4708F72D80DC5B66C45F4566C91B55384DFDEABB55FAA622C6764CC9FB4C4900B6AB2CEC5ABAD9057E2CF63A881BB4EC2A5D96634D7C11366EB908A168D33AA3C544822FC83E74C104B9275B2EF1CF41375B404A260BBF8FB3D4452AF3D0630BB1EC2A01676BA588AE7820727622A6D9DF5C93A3CE807D54B79AE007C3D401F8787DC3E235E8B9AE6B1B9279328CB1CA94105434010F15EB07F487F4D5C94F4A5A7
                            byte[] chksum = CA.calculateCAPublicKeyCheckSum(rid, Util.intToByteArray(9), mod, new byte[]{0x03});
                            System.out.println("chkSum: " + Util.prettyPrintHexNoWrap(chksum));

                            System.out.println("IssuerPublicKeyCertificate end");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }


                        byte[] selectAidResponseOk = checkResponse(selectAidResponse);
                        if (selectAidResponseOk != null) {
                            writeToUiAppend(etLog, "03 select AID response length " + selectAidResponseOk.length + " data: " + bytesToHex(selectAidResponseOk));
                            // pretty print of response
                            if (isPrettyPrintResponse)
                                prettyPrintData(etLog, selectAidResponseOk);

                            // extended
                            tsList.addAll(getTagSetFromResponse(selectAidResponseOk, "selectAid " + aidSelectedForAnalyze));

/*
data from MasterCard AAB:

SFI: 1 Record: 1
PAN in this record ??
70759f6c0200019f6206000000000f009f63060000000000fe563442353337353035303030303136303131305e202f5e323430333232313237393433323930303030303030303030303030303030309f6401029f65020f009f660200fe9f6b135375050000160110d24032210000000000000f9f670102
 	9F6C Unknown tag
 	 	0001
 	9F62 Unknown tag
 	 	000000000F00
 	9F63 Unknown tag
 	 	0000000000FE
 	56 Unknown tag
 	 	42353337353035303030303136303131305E202F5E32343033323231323739343332393030303030303030303030303030303030
 	9F64 Unknown tag
 	 	02
 	9F65 Unknown tag
 	 	0F00
 	9F66 Unknown tag
 	 	00FE
 	9F6B Unknown tag
 	 	5375050000160110D24032210000000000000F
 	9F67 Unknown tag
 	 	02

SFI: 2 Record: 1
7081a69f420209785f25032203015f24032403315a0853750500001601105f3401009f0702ffc09f080200028c279f02069f03069f1a0295055f2a029a039c019f37049f35019f45029f4c089f34039f21039f7c148d0c910a8a0295059f37049f4c088e0e000000000000000042031e031f039f0d05b4508400009f0e0500000000009f0f05b4708480005f280202809f4a018257135375050000160110d24032212794329000000f
 	9F4A Static Data Authentication Tag List
 	 	82

SFI: 3 Record: 1
7081909f420209785f25032203015f24032403315a0853750500001601105f3401009f0702ffc08c279f02069f03069f1a0295055f2a029a039c019f37049f35019f45029f4c089f34039f21039f7c148d0c910a8a0295059f37049f4c088e1200000000000000004203440341031e031f039f0d05bc50bc08009f0e0500000000009f0f05bc70bc98005f280202809f4a0182
 	9F4A Static Data Authentication Tag List
 	 	82

SFI: 4 Record: 1
7081b89f4701039f4681b03cada902afb40289fbdfea01950c498191442c1b48234dcaff66bca63cbf821a3121fa808e4275a4e894b154c1874bddb00f16276e92c73c04468253b373f1e6a9a89e2705b4670682d0adff05617a21d7684031a1cdb438e66cd98d591dc376398c8aab4f137a2226122990d9b2b4c72ded6495d637338fefa893ae7fb4eb845f8ec2e260d2385a780f9fda64b3639a9547adad806f78c9bc9f17f9d4c5b26474b9ba03892a754ffdf24df04c702f86
 	9F47 Integrated Circuit Card (ICC) Public Key Exponent
 	 	03
 	9F46 Integrated Circuit Card (ICC) Public Key Certificate
 	 	3CADA902AFB40289FBDFEA01950C498191442C1B48234DCAFF66BCA63CBF821A3121FA808E4275A4E894B154C1874BDDB00F16276E92C73C04468253B373F1E6A9A89E2705B4670682D0ADFF05617A21D7684031A1CDB438E66CD98D591DC376398C8AAB4F137A2226122990D9B2B4C72DED6495D637338FEFA893AE7FB4EB845F8EC2E260D2385A780F9FDA64B3639A9547ADAD806F78C9BC9F17F9D4C5B26474B9BA03892A754FFDF24DF04C702F86

SFI: 4 Record: 2
7081e08f01059f3201039224abfd2ebc115c3796e382be7e9863b92c266ccabc8bd014923024c80563234e8a11710a019081b004cc60769cabe557a9f2d83c7c73f8b177dbf69288e332f151fba10027301bb9a18203ba421bda9c2cc8186b975885523bf6707f287a5e88f0f6cd79a076319c1404fcdd1f4fa011f7219e1bf74e07b25e781d6af017a9404df9fd805b05b76874663ea88515018b2cb6140dc001a998016d28c4af8e49dfcc7d9cee314e72ae0d993b52cae91a5b5c76b0b33e7ac14a7294b59213ca0c50463cfb8b040bb8ac953631b80fa85a698b00228b5ff44223
 	8F Certification Authority Public Key Index
 	 	05
 	9F32 Issuer Public Key Exponent
 	 	03
 	92 Issuer Public Key Remainder
 	 	ABFD2EBC115C3796E382BE7E9863B92C266CCABC8BD014923024C80563234E8A11710A01
 	90 Issuer Public Key Certificate
 	 	04CC60769CABE557A9F2D83C7C73F8B177DBF69288E332F151FBA10027301BB9A18203BA421BDA9C2CC8186B975885523BF6707F287A5E88F0F6CD79A076319C1404FCDD1F4FA011F7219E1BF74E07B25E781D6AF017A9404DF9FD805B05B76874663EA88515018B2CB6140DC001A998016D28C4AF8E49DFCC7D9CEE314E72AE0D993B52CAE91A5B5C76B0B33E7AC14A7294B59213CA0C50463CFB8B040BB8AC953631B80FA85A698B00228B5FF44223

SFI: 4 Record: 3
 	9F46 Integrated Circuit Card (ICC) Public Key Certificate
 	 	AEA2347A69E6D9544DFA891A761833E6E6D3A78D450142DD7C21C131E585448FBC8449FE777F1895CFB18F2983D60EED56466A688D9DA6B3FB6593726251A83132B3F953A71098EEEFCD388BB672AD5A3592D31EA145FDF6F763733BAE482455C7987E96AE6CD8CF9D5CE562E5C80A7A6A083BA85C8EB86DDAC0CA19186554DFE5AB4AEADA5BE92E30C0C16981C516C74203694FD04E2FE3A66BF590BD4BB3085FE80167E98C9745E7E4819E4BD55F2B
 	9F47 Integrated Circuit Card (ICC) Public Key Exponent
 	 	03

data from Visa comd m:
PAN is 4871 7800 8277 0574 Exp 07/25

SFI: 2 Record: 1
7081fb9081f85ab54faf4ad810b3cca4ed42c38e1e768fca3187ed1be4196c6779c4633cbe88751889c12b05e10ee87cb198518793ff61e87534f66850e96239b76648429eced4cc207608d0d2a932dd9e8c4bb0d139c4eca59e1ef5f4708f72d80dc5b66c45f4566c91b55384dfdeabb55faa622c6764cc9fb4c4900b6ab2cec5abad9057e2cf63a881bb4ec2a5d96634d7c11366eb908a168d33aa3c544822fc83e74c104b9275b2ef1cf41375b404a260bbf8fb3d4452af3d0630bb1ec2a01676ba588ae7820727622a6d9df5c93a3ce807d54b79ae007c3d401f8787dc3e235e8b9ae6b1b9279328cb1ca94105434010f15eb07f487f4d5c94f4a5a7
 	90 Issuer Public Key Certificate
 	 	5AB54FAF4AD810B3CCA4ED42C38E1E768FCA3187ED1BE4196C6779C4633CBE88751889C12B05E10EE87CB198518793FF61E87534F66850E96239B76648429ECED4CC207608D0D2A932DD9E8C4BB0D139C4ECA59E1EF5F4708F72D80DC5B66C45F4566C91B55384DFDEABB55FAA622C6764CC9FB4C4900B6AB2CEC5ABAD9057E2CF63A881BB4EC2A5D96634D7C11366EB908A168D33AA3C544822FC83E74C104B9275B2EF1CF41375B404A260BBF8FB3D4452AF3D0630BB1EC2A01676BA588AE7820727622A6D9DF5C93A3CE807D54B79AE007C3D401F8787DC3E235E8B9AE6B1B9279328CB1CA94105434010F15EB07F487F4D5C94F4A5A7

SFI: 2 Record: 2
70078f01099f320103
 	8F Certification Authority Public Key Index
 	 	09
 	9F32 Issuer Public Key Exponent
 	 	03

SFI: 2 Record: 3
7081eb9f4681b02c4b62dfaede136b9bafeddebaaf41e5f4fdc9920b077817de896e6503c69c8f80ece2559cdf721ce1b7b2bc159fe77ec8d6eb45296876fbf4a6bd4bb4a11511ebd80fdf1c7bb8e1f4a2cdb7c4db0cc6f9fda7f6696c30d3846e1b98f4c849b7385f349d280fd92d75774dcbed96a5328f657f7eceb4bfa3ec3f9f39a64414bdbf0f03b15c49cbf0475bfa6a5f2513689c195faea031ae2391998be2028aa1671b380eb19a69a6c454bd2a30d11bc63c9f4701035a0848717800827705745f24032507315f3401015f280202769f070200809f4a01829f6e04207000009f690701000000000000
    9F46 Integrated Circuit Card (ICC) Public Key Certificate
 	 	2C4B62DFAEDE136B9BAFEDDEBAAF41E5F4FDC9920B077817DE896E6503C69C8F80ECE2559CDF721CE1B7B2BC159FE77EC8D6EB45296876FBF4A6BD4BB4A11511EBD80FDF1C7BB8E1F4A2CDB7C4DB0CC6F9FDA7F6696C30D3846E1B98F4C849B7385F349D280FD92D75774DCBED96A5328F657F7ECEB4BFA3EC3F9F39A64414BDBF0F03B15C49CBF0475BFA6A5F2513689C195FAEA031AE2391998BE2028AA1671B380EB19A69A6C454BD2A30D11BC63C
 	9F47 Integrated Circuit Card (ICC) Public Key Exponent
 	 	03
...
 	9F4A Static Data Authentication Tag List
 	 	82
 */

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
                                // this is usually a mastercard
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
                            // from here the processing is equals

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

                                    // extended
                                    tsList.addAll(getTagSetFromResponse(gpoRequestResponseOk, "get processing options"));

                                    /**
                                     * response can be a tag 77 Response Message Template Format 2
                                     * (found with my Visa-, Master- and German Giro-Cards)
                                     * or a tag 80 Response Message Template Format 1
                                     * (found with my American Express card)
                                     */

                                    //

/*
I/System.out: ------------------------------------
Amexco:
I/System.out: 80 12 -- Response Message Template Format 1
I/System.out:       18 00 08 01 01 00 08 03 03 00 08 05 05 00 10 02
I/System.out:       02 00 (BINARY)
I/System.out: ------------------------------------

data = ArrayUtils.subarray(data, 2, data.length);
List<Afl> listAfl = extractAfl(data);


 */

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
                                    String internalAuthHeader = "00880000";
                                    String randomNumberLength = "04";
                                    String randomNumber = "01020304";
                                    String internalAuthTrailer = "00";
                                    String internalAuthString = internalAuthHeader + randomNumberLength + randomNumber + internalAuthTrailer;
                                    //String internalAuthString = "0088000004E153F3E800";
                                    byte[] internalAuthCommand = hexToBytes(internalAuthString);
                                    writeToUiAppend(etLog, "internalAuthCommand: " + internalAuthCommand.length + " data: " + bytesToHex(internalAuthCommand));
                                    byte[] internalAuthResponse = nfc.transceive(internalAuthCommand);
                                    if (internalAuthResponse != null) {
                                        writeToUiAppend(etLog, "internalAuthResponse: " + internalAuthResponse.length + " data: " + bytesToHex(internalAuthResponse));
                                        prettyPrintData(etLog, internalAuthResponse);
                                        // extended
                                        // check response for tags, esp. tag 0x9f4b Signed Dynamic Application Data
                                        tsList.addAll(getTagSetFromResponse(internalAuthResponse, "internal authentication"));
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
                                                if (isPrettyPrintResponse)
                                                    prettyPrintData(etLog, getApplicationCryptoResponseOk);
                                                // extended
                                                // check response for tags, esp. tag 0x9f4b Signed Dynamic Application Data
                                                tsList.addAll(getTagSetFromResponse(getApplicationCryptoResponseOk, "application cryptogram"));
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
                                                if (isPrettyPrintResponse)
                                                    prettyPrintData(etLog, getApplicationCryptoResponseOk);
                                                tsList.addAll(getTagSetFromResponse(getApplicationCryptoResponseOk, "application cryptogram"));
                                            } else {
                                                writeToUiAppend(etLog, "getApplicationCryptoResponse failure");
                                            }
                                        } else {
                                            writeToUiAppend(etLog, "getApplicationCryptoResponse failure");
                                        }
                                    }

                                    // extended
                                    // a single value:
                                    if (applicationTransactionCounter != null) {
                                        TagSet tsAtc = new TagSet(new byte[]{(byte) 0x9f, (byte) 0x36}, "ATC", applicationTransactionCounter, "Binary", "directRead");
                                        tsList.add(tsAtc);
                                    }
                                    // ApplicationCryptoResponse
                                    if (getApplicationCryptoResponseOk != null) {
                                        tsList.addAll(getTagSetFromResponse(getApplicationCryptoResponseOk, "getApplicationCrypto"));
                                    }
                                    writeToUiAppend(etLog, "---- tagSet list ----");
                                    for (int i = 0; i < tsList.size(); i++) {
                                        writeToUiAppend(etLog, "--- tag nr " + i + " ---");
                                        writeToUiAppend(etLog, tsList.get(i).dump());
                                    }

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
 /*
 result visa comd m
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
  */



                                }
                            } else {
                                // we do not need this path
                                writeToUiAppend(etLog, "Found a strange behaviour - get processing options got wrong data to proceed... sorry");
                            }
                        }
                    }
                }
                // print the complete Log
                writeToUiFinal(etLog);
                setLoadingLayoutVisibility(false);
            } catch (IOException e) {
                Log.e(TAG, "IsoDep Error on connecting to card: " + e.getMessage());
                writeToUiFinal(etLog);
                setLoadingLayoutVisibility(false);

                //throw new RuntimeException(e);
            }
            try {
                nfc.close();
            } catch (IOException e) {
                writeToUiFinal(etLog);
                setLoadingLayoutVisibility(false);

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
     * section for some rsa stuff
     */

    private void generateRsaKeyPair() {
        // https://stackoverflow.com/a/31848282/8166854
        // this will generate a RSA keypair with 1408 bits key length and exponent 65537
        // other key length could be 1152
        System.out.println("### generateRsaKeyPair ###");
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1408);
            KeyPair keyPair = keyGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
            System.out.println("Public Modulus: " + BinaryUtils.bytesToHex(publicKey.getModulus().toByteArray()));
            System.out.println("Public Exponent: " + BinaryUtils.bytesToHex(publicKey.getPublicExponent().toByteArray()));
            System.out.println("Private Modulus: " + BinaryUtils.bytesToHex(privateKey.getModulus().toByteArray()));
            System.out.println("Private Private Exponent: " + BinaryUtils.bytesToHex(privateKey.getPrivateExponent().toByteArray()));
            System.out.println("Private Prime Exponent DP : " + BinaryUtils.bytesToHex(privateKey.getPrimeExponentP().toByteArray())); // d mod (p-1)
            System.out.println("Private Prime Exponent DQ: " + BinaryUtils.bytesToHex(privateKey.getPrimeExponentQ().toByteArray())); // d mod (q-1)
            System.out.println("Private Prime P: " + BinaryUtils.bytesToHex(privateKey.getPrimeP().toByteArray())); // P
            System.out.println("Private Prime Q: " + BinaryUtils.bytesToHex(privateKey.getPrimeQ().toByteArray())); // Q
            System.out.println("Private Coefficient PQ : " + BinaryUtils.bytesToHex(privateKey.getCrtCoefficient().toByteArray()));  // PQ

            // key generation with exponent 3
            RSAKeyGenParameterSpec keyGenParameterSpec =
                    new RSAKeyGenParameterSpec(1408, RSAKeyGenParameterSpec.F0);
            KeyPairGenerator keyGen2 = KeyPairGenerator.getInstance("RSA");
            keyGen2.initialize(keyGenParameterSpec);
            KeyPair keyPair2 = keyGen2.generateKeyPair();
            RSAPublicKey publicKey2 = (RSAPublicKey) keyPair2.getPublic();
            RSAPrivateCrtKey privateKey2 = (RSAPrivateCrtKey) keyPair2.getPrivate();
            System.out.println("Public Modulus: " + BinaryUtils.bytesToHex(publicKey2.getModulus().toByteArray()));
            System.out.println("Public Exponent: " + BinaryUtils.bytesToHex(publicKey2.getPublicExponent().toByteArray()));
            System.out.println("Private Modulus: " + BinaryUtils.bytesToHex(privateKey2.getModulus().toByteArray()));
            System.out.println("Private Private Exponent: " + BinaryUtils.bytesToHex(privateKey2.getPrivateExponent().toByteArray()));
            System.out.println("Private Prime Exponent DP : " + BinaryUtils.bytesToHex(privateKey2.getPrimeExponentP().toByteArray())); // d mod (p-1)
            System.out.println("Private Prime Exponent DQ: " + BinaryUtils.bytesToHex(privateKey2.getPrimeExponentQ().toByteArray())); // d mod (q-1)
            System.out.println("Private Prime P: " + BinaryUtils.bytesToHex(privateKey2.getPrimeP().toByteArray())); // P
            System.out.println("Private Prime Q: " + BinaryUtils.bytesToHex(privateKey2.getPrimeQ().toByteArray())); // Q
            System.out.println("Private Coefficient PQ : " + BinaryUtils.bytesToHex(privateKey2.getCrtCoefficient().toByteArray()));  // PQ
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }


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

                            // extended
                            tsList.addAll(getTagSetFromResponse(resultReadRecordOk, "read file from AFL " + "SFI: " + String.format("%02X", sfiOrg) + " REC: " + String.format("%02d", iRecords)));

                            //writeToUiAppend(etLog, "data from AFL " + bytesToHex(tag94BytesListEntry)); // given wrong output for second or third files in multiple records
                            writeToUiAppend(etLog, "data from AFL was: " + bytesToHex(tag94BytesListEntry));
                            writeToUiAppend(etLog, "data from AFL " + "SFI: " + String.format("%02X", sfiOrg) + " REC: " + String.format("%02d", iRecords));

                            byte sfiFile = (byte) (cmd[3] >>> 3);

                            writeToUiAppend(etLog, "data from AFL " + "SFI: " + String.format("%02X", sfiFile) + " REC: " + String.format("%02d", iRecords));
                            writeToUiAppend(etLog, "read result length: " + resultReadRecordOk.length + " data: " + bytesToHex(resultReadRecordOk));
                            // pretty print of response
                            if (isPrettyPrintResponse) {
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
     * section for anonymize the output
     */

    private void anonymizePan() {
        // https://stackoverflow.com/a/2478662/8166854
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Log.i(TAG, "Do you want to anonymize the export data (recommended) ?");
                        //Yes button clicked
                        // search for cleartext PAN in exportString
                        int numberSubstrings = substring_rec(exportString, foundPan);
                        // todo: set anonymized PAN to leength of foundPan
                        exportString = exportString.replaceAll(foundPan, ANONYMIZED_PAN);
                        numberSubstrings = substring_rec(exportString, foundPan);
                        // as the prettyPrint prints a byte array with a blank after each byte we have to search for these occurrences as well
                        String foundPanWithSpace = foundPan.replaceAll("..", "$0 ");
                        numberSubstrings = substring_rec(exportString, foundPanWithSpace);
                        exportString = exportString.replaceAll(foundPanWithSpace, ANONYMIZED_PAN_WITH_SPACE);
                        runAnonymizing = true;
                        writeToUiToast("The export data (mail or file) were anonymized regarding PAN");
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        writeToUiToast("The export data (mail or file) were not anonymized");
                        break;
                }
            }
        };
        final String selectedFolderString = "Do you want to anonymize the export data (recommended) ?";
        AlertDialog.Builder builder = new AlertDialog.Builder(ExtendedReadActivity.this);
        builder.setTitle("ANONYMIZE EXPORT STRING ?");
        builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener).show();
        /*
        If you want to use the "yes" "no" literals of the user's language you can use this
        .setPositiveButton(android.R.string.yes, dialogClickListener)
        .setNegativeButton(android.R.string.no, dialogClickListener)
         */
    }

    /**
     * count the number of substrings in a string recursively
     *
     * @param str complete string
     * @param sub sub string
     * @return number or 0 if nothing found
     */
    private int substring_rec(String str, String sub) {
        if (str.contains(sub)) {
            return 1 + substring_rec(str.replaceFirst(sub, ""), sub);
        }
        return 0;
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
        exportString = textView.getText().toString();
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
                anonymizePan();
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
                etData.setText(sb.toString());

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
                writeToUiAppend(etData, sbV.toString());
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
                Intent intent = new Intent(ExtendedReadActivity.this, FileReaderActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mExportEmulationDataActivity = menu.findItem(R.id.action_activity_export_emulation_data);
        mExportEmulationDataActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(ExtendedReadActivity.this, ExportEmulationDataActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem mViewEmulationDataActivity = menu.findItem(R.id.action_activity_view_emulation_data);
        mViewEmulationDataActivity.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent intent = new Intent(ExtendedReadActivity.this, ViewEmulationDataActivity.class);
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
