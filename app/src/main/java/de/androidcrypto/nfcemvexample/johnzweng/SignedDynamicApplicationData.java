package de.androidcrypto.nfcemvexample.johnzweng;

// written by AndroidCrypto

import static de.androidcrypto.nfcemvexample.StringUtils.fillTrimRight;

import java.math.BigInteger;
import java.util.Arrays;

public class SignedDynamicApplicationData {

    byte[] recoveredDataHeader;
    byte[] signedDataFormat;
    byte[] hashAlgorithmIndicator;
    byte[] iccDynamicDataLength;
    byte[] iccDynamicData;
    byte[] padPattern;
    byte[] hashResult;
    byte[] recoveredDataTrailer;
    byte[] recoveredBytes;

    public SignedDynamicApplicationData(byte[] recoveredBytes) {
        this.recoveredBytes = recoveredBytes;
        parseData();
    }

    private void parseData() {
        int dataLength = recoveredBytes.length;
        System.out.println("#*# dataLength: " + dataLength);
        if (dataLength < 27) return;
        int totalLength = 0;
        recoveredDataHeader = Arrays.copyOfRange(recoveredBytes, totalLength, totalLength + 1);
        totalLength ++;
        signedDataFormat = Arrays.copyOfRange(recoveredBytes, totalLength, totalLength + 1);
        totalLength ++;
        hashAlgorithmIndicator = Arrays.copyOfRange(recoveredBytes, totalLength, totalLength + 1);
        totalLength ++;
        iccDynamicDataLength = Arrays.copyOfRange(recoveredBytes, totalLength, totalLength + 1);
        totalLength ++;
        int iccDynamicDataLengthInt = new BigInteger(1, iccDynamicDataLength).intValue();
        iccDynamicData = Arrays.copyOfRange(recoveredBytes, totalLength, totalLength + iccDynamicDataLengthInt);
        totalLength += iccDynamicDataLengthInt;
        // length of padPattern = 94 ?
        // end is dataLength - 20 - 1 - 1 iccDynamicDataLength = 106, end is
        int dynamicDataEnd = dataLength - 22;
        padPattern = Arrays.copyOfRange(recoveredBytes, totalLength, dynamicDataEnd);
        totalLength = dynamicDataEnd + 1;
        hashResult = Arrays.copyOfRange(recoveredBytes, totalLength, totalLength + 20);
        recoveredDataTrailer = Arrays.copyOfRange(recoveredBytes, dataLength - 1, dataLength);
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(fillTrimRight("Recovered Data Header Byte: ", 30)).append(bytesToHexNpe(recoveredDataHeader)).append("\n");
        sb.append(fillTrimRight("Signed Data Format: ", 30)).append(bytesToHexNpe(signedDataFormat)).append("\n");
        sb.append(fillTrimRight("Hash Algorithm Indicator", 30)).append(bytesToHexNpe(hashAlgorithmIndicator)).append("\n");
        sb.append(fillTrimRight("ICC Dynamic Data Length: ", 30)).append(bytesToHexNpe(iccDynamicDataLength)).append("\n");
        sb.append(fillTrimRight("ICC Dynamic Data: ", 30)).append(bytesToHexNpe(iccDynamicData)).append("\n");
        sb.append(fillTrimRight("Pad Pattern: ", 30)).append(bytesToHexNpe(padPattern)).append("\n");
        sb.append(fillTrimRight("Hash Result : ", 30)).append(bytesToHexNpe(hashResult)).append("\n");
        sb.append(fillTrimRight("Data Trailer Byte: ", 30)).append(bytesToHexNpe(recoveredDataTrailer)).append("\n");
        return sb.toString();
    }

    /**
     * converts a byte array to a hex encoded string
     *
     * @param bytes
     * @return hex encoded string
     */
    public static String bytesToHexNpe(byte[] bytes) {
        if (bytes != null) {
            StringBuffer result = new StringBuffer();
            for (byte b : bytes)
                result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            return result.toString();
        } else {
            return "";
        }
    }
}
