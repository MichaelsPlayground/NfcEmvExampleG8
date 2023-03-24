package de.androidcrypto.nfcemvexample.johnzweng;


import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHexNpe;

import androidx.annotation.NonNull;

/**
 * The class decrypts the following keys and data:
 * 1) retrieve the Issuer Public Key  using the Issuer Public Key Certificate and the CA Public Key
 * 2) retrieve the Issuer ICC Public Key using the Issuer ICC Public Key Certificate and the Issuer Public Key
 * 3) decrypt the Signed Dynamic Application Data using the Issuer ICC Public Key
 */

public class DecryptUtils {

    /**
     * decryption of the Issuer Public Key Certificate
     * @param caPublicKeyExponent        *1)
     * @param caPublicKeyModulus         *1)
     * @param issuerPublicKeyCertificate *2) tag 0x90
     * @param issuerPublicKeyRemainder   *3) tag 0x92
     * @param issuerPublicKeyExponent    *2) tag 0x9f32
     * @return the recovered Issuer Public Key
     *
     * Notes: *1) get it from https://www.eftlab.co.uk/knowledge-base/list-of-ca-public-keys
     *        *2) get it from the card
     *        *3) get it from the card [optional]
     */

    public static EmvKeyReader.RecoveredIssuerPublicKey retrieveIssuerPublicKey(@NonNull byte[] caPublicKeyExponent, @NonNull byte[] caPublicKeyModulus, @NonNull byte[] issuerPublicKeyCertificate, byte[] issuerPublicKeyRemainder, @NonNull byte[] issuerPublicKeyExponent) {
        EmvKeyReader emvKeyReader = new EmvKeyReader();
        IssuerIccPublicKeyNew issuerIccPublicKey;
        try {
            //issuerIccPublicKeyNew = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyMc05Exponent, caPublicKeyMc05Modulus, tag90_IssuerPublicKeyCertificate, tag92_IssuerPublicKeyRemainder, tag9f32_IssuerPublicKeyExponent);
            issuerIccPublicKey = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyExponent, caPublicKeyModulus, issuerPublicKeyCertificate, issuerPublicKeyRemainder, issuerPublicKeyExponent);
        } catch (EmvParsingException e) {
            // throw new RuntimeException(e);
            return null;
        }
        EmvKeyReader.RecoveredIssuerPublicKey recoveredIssuerPublicKeyParsed;
        try {
            recoveredIssuerPublicKeyParsed = emvKeyReader.parseIssuerPublicKeyCert(issuerIccPublicKey.getRecoveredBytes(), caPublicKeyModulus.length);
        } catch (EmvParsingException e) {
            // throw new RuntimeException(e);
            return null;
        }
        return recoveredIssuerPublicKeyParsed;
    }


    public static void retrieveIccPublicKey(@NonNull byte[] issuerPublicKeyExponent, @NonNull byte[] issuerPublicKeyModulus, @NonNull byte[] iccPublicKeyCertificate, byte[] iccPublicKeyRemainder, @NonNull byte[] iccPublicKeyExponent) {

    }
}
