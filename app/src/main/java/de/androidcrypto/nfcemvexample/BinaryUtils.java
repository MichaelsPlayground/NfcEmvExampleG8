package de.androidcrypto.nfcemvexample;

import android.util.Base64;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BinaryUtils {

    /**
     * This class provides service methods for converting binary data
     */

    /**
     * methods:
     * String bytesToHex(byte[] bytes)
     * byte[] hexToBytes(String str)
     * String bytesToHexBlank(byte[] bytes)
     * String formattedHexPrint(byte[] data, int address)
     * String base64Encoding(byte[] input)
     * byte[] base64Decoding(String input)
     * String hexToBase64(String hexString)
     * String base64ToHex(String base64String)
     */

    /**
     * converts a byte array to a hex encoded string
     * @param bytes
     * @return hex encoded string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    /**
     * converts a byte array to a decimal value string
     * @param bytes
     * @return a string with decimal values
     */
    public static String bytesToHexDecimal(byte[] bytes) {
        return Arrays.toString(bytes);
    }


    /**
     * converts a hex encoded string to a byte array
     * @param str
     * @return
     */
    public static byte[] hexToBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2),
                    16);
        }
        return bytes;
    }

    public static byte[] hexBlankToBytes(String str) {
        str = str.replaceAll(" ", "");
        System.out.println("### hexBlankToBytes: " + str);
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2),
                    16);
        }
        return bytes;
    }

    /**
     * converts a byte array to a hex encoded string
     * @param bytes
     * @return hex encoded string with a blank after each value
     */
    public static String bytesToHexBlank(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1)).append(" ");
        return result.toString();
    }

    /**
     * converts a byte array to a formatted string with address, hex encoded values and ascii values
     * @param data
     * @param address int 0 as default, could be start address (offset)
     * @return string like
     */

    public String formattedHexPrint(byte[] data, int address) {
        // get hex address of part
        String hexAddress = formatWithNullsLeft(Integer.toHexString(address), 8) + ":";
        String hexContent = bytesToHexBlank(data);
        // add blanks depending on data length (7 = add 3 blanks, 6 = add 6 blanks
        for (int i = 0; i < (8 - data.length); i++) {
            hexContent += "   ";
        }
        String asciiRowString = "";
        for (int j = 0; j < data.length; j++) {
            // check for maximal characters
            asciiRowString = asciiRowString + returnPrintableChar(data[j], true);
        }
        String hexAscii = (char) 124 + formatWithBlanksRight(asciiRowString, 8);
        return hexAddress + hexContent + hexAscii;
    }

    private static String formatWithNullsLeft(String value, int len) {
        while (value.length() < len) {
            value = "0" + value;
        }
        return value;
    }

    private static String formatWithBlanksRight(String value, int len) {
        while (value.length() < len) {
            value += " ";
        }
        return value;
    }

    private static char returnPrintableChar(byte inputByte, Boolean printDotBool) {
        // ascii-chars from these ranges are printed
        // 48 -  57 = 0-9
        // 65 -  90 = A-Z
        // 97 - 122 = a-z
        // if printDotBool = true then print a dot "." for unprintable chars
        char returnChar = 0;
        if (printDotBool == true) {
            returnChar = 46;
        }
        if ((inputByte >= 48) && (inputByte <= 57)) {
            returnChar = (char) inputByte;
        }
        if ((inputByte >= 65) && (inputByte <= 90)) {
            returnChar = (char) inputByte;
        }
        if ((inputByte >= 97) && (inputByte <= 122)) {
            returnChar = (char) inputByte;
        }
        return returnChar;
    }

    /**
     * converts a byte array to a Base64 encoded string
     * @param input
     * @return Base64 encoded string
     */
    public static String base64Encoding(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }

    /**
     * converts a Base64 encoded string to a byte array
     * @param input
     * @return byte array
     */
    public static byte[] base64Decoding(String input) {
        return Base64.decode(input, Base64.NO_WRAP);
    }

    /**
     * converts a hex encoded string to a Base64 encoded string
     * @param hexString
     * @return Base64 encoded string
     */
    public static String hexToBase64(String hexString) {
        return base64Encoding(hexToBytes(hexString));
    }

    /**
     * converts a Base64 encoded string to hex encoded string
     * @param base64String
     * @return hex encoded string
     */
    public static String base64ToHex(String base64String) {
        return bytesToHex(base64Decoding(base64String));
    }

    // int <-> byte array
    // https://stackoverflow.com/a/7619315/8166854
    public static byte[] intToByteArray(int value) {
        return  ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte[] intToByteArrayV2(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    public static int intFromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    // packing an array of 4 bytes to an int, big endian, minimal parentheses
    // operator precedence: <<, &, |
    // when operators of equal precedence (here bitwise OR) appear in the same expression, they are evaluated from left to right
    public static int intFromByteArrayV2(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    // packing an array of 4 bytes to an int, big endian, clean code
    public static int intFromByteArrayV3(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }

    // be careful when running in loops, calculation is costful
    // https://stackoverflow.com/a/17981098/8166854
    // this works for byte arrays of length 2
    public static int intFromByteArrayV4(byte[] bytes) {
        return new BigInteger(bytes).intValue();
    }

    public static byte[] intToByteArrayV4(byte[] bytes) {
        return new BigInteger(bytes).toByteArray();
    }

}
