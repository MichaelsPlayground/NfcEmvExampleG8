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
    private final String pdolWithCountryCode = "80A80000238321A0000000000000000001000000000000084000000000000840070203008017337000";

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
