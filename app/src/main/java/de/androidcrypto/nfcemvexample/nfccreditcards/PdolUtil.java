package de.androidcrypto.nfcemvexample.nfccreditcards;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;

import android.nfc.tech.IsoDep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PdolUtil {

    private final IsoDep nfc;
    private final List<String> pdolList = new ArrayList<>();
    private final int maximumPdolEntries = 20;
    private boolean settingResult = setPdolEntries(maximumPdolEntries);
    private final String pdolWithCountryCode =   "80A80000238321A0000000000000000001000000000000084000000000000840070203008017337000";
    private final String pdolWithCountryCodeNew ="80A800002383B620C00000000000000001000000000000084000000000000840070203008017337000";
    String c                                    ="80A80000158313C080000000000000100001240124823DDE7A0100";
    private final String pdolWithCountryCode2 = "80A8000012831B7604000000000010000000038393031000";
    private final String pdolWithCountryCode3 = "80a800001283100000000000000000000000000000000000";
/*
Request :80 A8 00 00 12 83 10 B6 60 40 00 00 00 00 01 00 00 00 00 38 39 30 31 00
Tag 9F 66: Terminal Transaction Qualifiers : B6 60 40 00
Tag 9F 02: Transaction Amount :              00 00 00 01 00 00
Tag 5F 2A: Transaction Currency Code :       03 56
Tag 9F 37: Unpredictable Number :            38 39 30 31
 */

/*
Commd Visa 9f66049f02069f03069f1a0295055f2a029a039c019f3704
  9f66 04 Terminal Transaction Qualifiers : B6 60 40 00
  9f02 06 Transaction Amount :              00 00 00 01 00 00
  9f03 06 Amount, Other (Numeric) always:   00 00 00 00 00 00
  9f1a 02 Terminal Country Code :           08 26              UK
  95   05 Terminal Verificat.Results alway: 00 00 00 00 00
  5f2a 02 Transaction Currency Code :       08 26
  9a   03 Transaction Date :                23 03 03
  9c   01 Transaction Type :                00
  9f37 04 Unpredictable Number :            38 39 30 31
  Totl 33 (0x21)
 */
    private final String pdolVisaComdirect = "80A80000238321B6604000000000010000000000000000082600000000000826230303003839303100";
    //private final String pdolVisaComdire = "80A80000238321A0000000000000000001000000000000084000000000000840070203008017337000";

/*
Lloyd Visa 9f66049f02069f03069f1a0295055f2a029a039c019f3704
 */

    public PdolUtil(IsoDep nfc) {
        this.nfc = nfc;
    }

    /**
     * getGpo means get the processing options
     * @param preferredLength
     * @return
     */

    public byte[] getGpo(int preferredLength) {
        System.out.println("*** preferredLength: " + preferredLength);
        String pdol;
        if (preferredLength <= pdolList.size() && preferredLength > -1) {
            //System.out.println("++ preferredLength ++");
            // first test with the preferredLength, then test all
            pdol = pdolList.get(preferredLength);
            byte[] pdolCommand = hexToBytes(pdol);
            System.out.println("***pdolCommand: " + bytesToHex(pdolCommand));
            try {
                byte[] pdolResult = nfc.transceive(pdolCommand);
                System.out.println("***pdolResult: " + bytesToHex(pdolResult));
                byte[] pdolResultOk = checkResponse(pdolResult);
                if (pdolResultOk != null) return pdolResultOk;
            } catch (IOException e) {
                //throw new RuntimeException(e);
                return null;
            }
        }
        // at this point the preferredLength pdol was not working or the preferredLength was larger than the predefined lengths
        // now loop through the predefined pdol's
        //System.out.println("++ loop through predefinedPdol list with " + pdolList.size() + " entries ++");
        for (int i = 0; i < pdolList.size(); i++) {
            pdol = pdolList.get(i);
            byte[] pdolCommand = hexToBytes(pdol);
            System.out.println("++ pdolCommand: " + bytesToHex(pdolCommand));
            try {
                byte[] pdolResult = nfc.transceive(pdolCommand);
                /*
                if (pdolResult != null) {
                    System.out.println("pdolResult: " + bytesToHex(pdolResult));
                } else {
                    System.out.println("pdolResult is NULL");
                }
                */
                byte[] pdolResultOk = checkResponse(pdolResult);
                if (pdolResultOk != null) return pdolResultOk;
            } catch (IOException e) {
                //throw new RuntimeException(e);
                return null;
            }
        }
        // at this point none of the predefined pdol is working
        //System.out.println("++ predefinedPdols do not work, return null ++");
        return null;
    }

    public String getPdolWithCountryCode() {
        return pdolWithCountryCode;
    }
    public String getPdolVisaComdirect() {
        return pdolVisaComdirect;
    }
    public String getPdolWithCountryCode2() {
        return pdolWithCountryCode2;
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

    private boolean setPdolEntries(int maximum) {
        if (maximum > 100) return false;
        // this is a sample string for a pdol entry with value 8:
        // 80A800000a8308000000000000000000
        for (int entry = 0; entry < maximum; entry ++) {
            StringBuilder sb = new StringBuilder();
            sb.append("80A80000");
            sb.append(String.format("%02X", (entry + 2)));
            sb.append("83");
            sb.append(String.format("%02X", (entry)));
            for (int i = 0; i < entry; i++) {
                sb.append("00");
            }
            sb.append("00"); // trailing "00"
            pdolList.add(sb.toString());
        }
        return true;
    }
    private String setPdolEntry(String pdolEntry) {
        pdolList.add(pdolEntry);
        return pdolEntry;
    }
}
