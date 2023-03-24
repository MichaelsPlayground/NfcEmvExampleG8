package de.androidcrypto.nfcemvexample;

import de.androidcrypto.nfcemvexample.sasc.CVMList;

/**
 * This class has all methods to run cryptographic on emv cards
 * Some of the methods are taken from Johannes Zweng's EmvKeyReader.java:
 * https://github.com/johnzweng/android-emv-key-test/blob/master/app/src/main/java/at/zweng/emv/keys/EmvKeyReader.java
 * Some methods are modified to be suited to this program, e.g. this app doesn't read the CA-keys from a file but from
 * a service class that hold some of the keys needed for my tests.
 * Other codes are from the javaemvreader-project from sasc99
 * As the original repository seems to be abounded I took my codes from
 * https://github.com/maciejsszmigiero/javaemvreader
 */

public class EmvCryptoModules {

    /**
     * dumps the 1 byte tag 0x9f27
     * @param cid
     * @return a String
     */
    public static String dumpCryptogramInformationData(byte cid) {
        // source: https://github.com/maciejsszmigiero/javaemvreader/blob/master/src/main/java/sasc/emv/CryptogramInformationData.java
        switch (cid & 0xC0) {
            case 0x00:
                return "AAC";
            case 0x40:
                return "TC";
            case 0x80:
                return "ARQC";
            default: // 0xC0
                return "RFU";
        }
    }

    /**
     * dumps the content of tag 0x8e = Cardholder Verification Method list
     * @param data
     * @return
     */
    public static String dumpCvmList(byte[] data) {
        // source: https://github.com/maciejsszmigiero/javaemvreader/blob/master/src/main/java/sasc/emv/CVMList.java
        // sample 00 00 00 00 00 00 00 00 42 03 1E 03 1F 03
        data = BinaryUtils.hexBlankToBytes("00 00 00 00 00 00 00 00 42 03 1E 03 1F 03");
        CVMList cvmList = new CVMList(data);
        return cvmList.toString();
    }
}







