package de.androidcrypto.nfcemvexample.extended;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * code by ozkansari/EncryptSample.java
 * source: https://gist.github.com/ozkansari/65fa143b2018bb7c2efaf44e76d89e62
 */

public class PinEncryptSample1 {

    private static final String KEY = "AB1C11111111111AAAAAAADDDDD11111";
    private static final int PIN_LENGTH = 6;

    public static void main(String[] args) throws Exception {

        TripleDES td = new TripleDES(KEY, PIN_LENGTH);

        String pan = "4111111111111111";
        String pin = "123456";

        String encrypted = td.encrypt(pan, pin);
        System.out.println("encrypted: " + encrypted);

        String decrypted = td.decrypt(pan, encrypted);
        System.out.println("decrypted    : " + decrypted);
    }
}

class TripleDES {

    private static final String ALGORITHM = "TripleDES";
    private static final String MODE = "ECB";
    private static final String PADDING = "NoPadding";

    /** algorithm/mode/padding */
    private static final String TRANSFORMATION = ALGORITHM+"/"+MODE+"/"+PADDING;

    private final String key;
    private int pinLength;

    TripleDES(String key, int pinLength) {
        this.key = key;
        this.pinLength = pinLength;
    }

    public String encrypt(String pan, String pinClear) throws Exception {

        if(pinClear.length() != pinLength) {
            System.out.println("Incorrect PIN length given. Please fix! pinClear.size() " + "!= " + " pinLength : " + pinClear.length() + " !=" + pinLength);
        }

        String pinEncoded = encodePinBlockAsHex(pan, pinClear);
        byte[] tmp = h2b(this.key);
        byte[] key = new byte[24];
        System.arraycopy(tmp, 0, key, 0, 16);
        System.arraycopy(tmp, 0, key, 16, 8);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM));
        byte[] plaintext = cipher.doFinal(h2b(pinEncoded));
        return b2h(plaintext);
    }

    public String decrypt(String pan, String encryptedPin) throws Exception {
        byte[] tmp = h2b(this.key);
        byte[] key = new byte[24];
        System.arraycopy(tmp, 0, key, 0, 16);
        System.arraycopy(tmp, 0, key, 16, 8);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM));
        byte[] plaintext = cipher.doFinal(h2b(encryptedPin));
        String pinEncoded = b2h(plaintext);
        return decodePinBlock(pan, pinEncoded);
    }

    public String decodePinBlock(String pan, String pinEncoded) throws Exception {
        pan = pan.substring(pan.length() - 12 - 1, pan.length() - 1);
        String paddingPAN = "0000".concat(pan);
        byte[] pinBlock = xorBytes(h2b(paddingPAN), h2b(pinEncoded));
        return b2h(pinBlock).substring(2, pinLength+2);
    }

    public String encodePinBlockAsHex(String pan, String pin) throws Exception {
        pan = pan.substring(pan.length() - 12 - 1, pan.length() - 1);
        String paddingPAN = "0000".concat(pan);

        String Fs = "FFFFFFFFFFFFFFFF";
        String paddingPIN = "0" + pin.length() + pin + Fs.substring(2 + pin.length(), Fs.length());

        byte[] pinBlock = xorBytes(h2b(paddingPAN), h2b(paddingPIN));

        return b2h(pinBlock);
    }

    private static byte[] xorBytes(byte[] a, byte[] b) throws Exception {
        if (a.length != b.length) {
            throw new Exception();
        }
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            int r = 0;
            r = a[i] ^ b[i];
            r &= 0xFF;
            result[i] = (byte) r;
        }
        return result;
    }

    private static byte[] h2b(String hex) {
        if ((hex.length() & 0x01) == 0x01)
            throw new IllegalArgumentException();
        byte[] bytes = new byte[hex.length() / 2];
        for (int idx = 0; idx < bytes.length; ++idx) {
            int hi = Character.digit((int) hex.charAt(idx * 2), 16);
            int lo = Character.digit((int) hex.charAt(idx * 2 + 1), 16);
            if ((hi < 0) || (lo < 0))
                throw new IllegalArgumentException();
            bytes[idx] = (byte) ((hi << 4) | lo);
        }
        return bytes;
    }

    private static String b2h(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int idx = 0; idx < bytes.length; ++idx) {
            int hi = (bytes[idx] & 0xF0) >>> 4;
            int lo = (bytes[idx] & 0x0F);
            hex[idx * 2] = (char) (hi < 10 ? '0' + hi : 'A' - 10 + hi);
            hex[idx * 2 + 1] = (char) (lo < 10 ? '0' + lo : 'A' - 10 + lo);
        }
        return new String(hex);
    }
}

/*
IN: 1234
PAN: 1111222233334444
PADDING (Fs): FFFFFFFFFFFFFFFF

(1) PAN Padded :
0000122223333444

(2) PIN Padded:
041234FFFFFFFFFF

PAN Padded XOR PIN Padded (In Bytes):
041226DDDCCCCBBB

Clear PIN Block:
041226DDDCCCCBBB

(1) Creating Padding PAN: "0000".concat( pan.substring(pan.length() - 12 - 1, pan.length() - 1) ) ;
(2) Creating Padding PIN: "0"+pin.length()+pin + Fs.substring(2+pin.length(), Fs.length())

Yukarıda anlatıldığı şekilde Clear PIN Block oluşturulduktan sonra, ZPK ile şifreleniyor. Buradaki şifrelemede 3DES kullanılıyor.

** Ek Bilgi: ZPK’yı müşteriye gönderirken de ZPK’yı ZMK ile şifreleyerek ( ZPK under ZMK ) gönderiyoruz. ZMK müşteride de olduğundan bunu açıp ZPK’yı elde edebiliyor.

Clear PIN Block’un ZPK ile şifrelenmesine örnek vermek gerekirse:

Clear PIN Block:
041226DDDCCCCBBB

ZPK:
AB1C11111111111AAAAAAADDDDD11111

(PIN Block) ZPK :
5A8E559FCABC189B
 */
/*
jluizsc commented on Oct 17, 2022 •
You are the man!!!
This is amazing!!!
I just modified the Pinlock decode to get automatic length, based on this site https://emvlab.org/pinblock/
explaiden here https://www.linkedin.com/pulse/pinblock-explained-iftekharul-haque/

public String decodePinBlock(String pan, String pinEncoded) throws Exception {
    pan = pan.substring(pan.length() - 12 - 1, pan.length() - 1);
    String paddingPAN = "0000".concat(pan);
    byte[] pinBlock = xorBytes(h2b(paddingPAN), h2b(pinEncoded));
    //return b2h(pinBlock).substring(2, pinLength+2);
    String result=b2h(pinBlock);
    int lenClear =Integer.parseInt(result.substring(1,2)) ;
    return result.substring(2, lenClear+2);
}
So... pinLength is used to encrypt only !!!

thank you so much.
 */