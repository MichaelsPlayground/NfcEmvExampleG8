package de.androidcrypto.nfcemvexample;

public class StringUtils {

    public static String fillTrimRight(String string, int length) {
        int stringLength = string.length();
        if (stringLength > length) {
            return string.substring(0, length);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 0; i < (length - stringLength); i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

}
