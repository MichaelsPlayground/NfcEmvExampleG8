package de.androidcrypto.nfcemvexample.nfccreditcards;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;

import android.nfc.tech.IsoDep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuessPdol {
    
    private final IsoDep nfc;
    private List<String> pdolList = new ArrayList<>();
    private final String pdol00 = setPdolEntry("80A8000002830000");
    private final String pdol01 = setPdolEntry("80A800000383010000");
    private final String pdol02 = setPdolEntry("80A80000048302000000");
    private final String pdol03 = setPdolEntry("80A8000005830300000000");
    private final String pdol04 = setPdolEntry("80A800000683040000000000");
    private final String pdol05 = setPdolEntry("80A80000078305000000000000");
    private final String pdol06 = setPdolEntry("80A8000008830600000000000000");
    private final String pdol07 = setPdolEntry("80A800000983070000000000000000");
    private final String pdol08 = setPdolEntry("80A800000a8308000000000000000000"); // this command does work with GiroCards

    private final String pdol09 = setPdolEntry("80A800000b830900000000000000000000");
    private final String pdol10 = setPdolEntry("80A800000c830a0000000000000000000000");
    private final String pdol11 = setPdolEntry("80A800000d830b000000000000000000000000");
    private final String pdol12 = setPdolEntry("80A800000e830c00000000000000000000000000");
    private final String pdol13 = setPdolEntry("80A800000f830d0000000000000000000000000000");
    private final String pdol14 = setPdolEntry("80A8000010830e000000000000000000000000000000");
    private final String pdol15 = setPdolEntry("80A8000011830f00000000000000000000000000000000");
    private final String pdol16 = setPdolEntry("80A800001283100000000000000000000000000000000000");
    private final String pdol17 = setPdolEntry("80A80000138311000000000000000000000000000000000000");
    private final String pdol18 = setPdolEntry("80A8000014831200000000000000000000000000000000000000");
    private final String pdol19 = setPdolEntry("80A800001583130000000000000000000000000000000000000000");
    private final String pdol20 = setPdolEntry("80A80000168314000000000000000000000000000000000000000000");

    public GuessPdol(IsoDep nfc) {
        this.nfc = nfc;
    }

    public byte[] getPdol(int preferredLength) {
        String pdol;
        if (preferredLength <= pdolList.size() && preferredLength > -1) {
            //System.out.println("++ preferredLength ++");
            // first test with the preferredLength, then test all
            pdol = pdolList.get(preferredLength);
            byte[] pdolCommand = hexToBytes(pdol);
            try {
                byte[] pdolResult = nfc.transceive(pdolCommand);
                byte[] pdolResultOk = checkResponse(pdolResult);
                if (pdolResultOk != null) return pdolResultOk;
            } catch (IOException e) {
                //throw new RuntimeException(e);
                return null;
            }
        }
        // at this point the preferredLength pdol was not working or the preferredLength was larger than the predefined lengths
        // now loop through the predefined pdol's
        System.out.println("++ loop through predefinedPdol list with " + pdolList.size() + " entries ++");
        for (int i = 0; i < pdolList.size(); i++) {
            pdol = pdolList.get(i);
            byte[] pdolCommand = hexToBytes(pdol);
            //System.out.println("++ pdolCommand: " + bytesToHex(pdolCommand));
            try {
                byte[] pdolResult = nfc.transceive(pdolCommand);
                if (pdolResult != null) {
                    System.out.println("pdolResult: " + bytesToHex(pdolResult));
                } else {
                    System.out.println("pdolResult is NULL");
                }
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

    private byte[] checkResponse(byte[] data) {
        if (data.length < 5) return null; // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x9000) {
            return null;
        } else {
            return Arrays.copyOfRange(data, 0, data.length - 2);
        }
    }

    private String setPdolEntry(String pdolEntry) {
        pdolList.add(pdolEntry);
        return pdolEntry;
    }
}
