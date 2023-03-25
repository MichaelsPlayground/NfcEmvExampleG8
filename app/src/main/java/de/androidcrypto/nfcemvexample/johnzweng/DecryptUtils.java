package de.androidcrypto.nfcemvexample.johnzweng;


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

    public static EmvKeyReader.RecoveredIssuerPublicKey retrieveIssuerPublicKey(byte[] caPublicKeyExponent, byte[] caPublicKeyModulus, byte[] issuerPublicKeyCertificate, byte[] issuerPublicKeyRemainder, byte[] issuerPublicKeyExponent) {
        if ((caPublicKeyExponent == null) | (caPublicKeyModulus == null) | (issuerPublicKeyCertificate == null) | (issuerPublicKeyExponent == null)) return null;
        EmvKeyReader emvKeyReader = new EmvKeyReader();
        IssuerIccPublicKeyNew issuerPublicKey; // don't mind about IssuerIccPublicKey name, it is just a class to hold the issuer and icc public key
        try {
            //issuerIccPublicKeyNew = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyMc05Exponent, caPublicKeyMc05Modulus, tag90_IssuerPublicKeyCertificate, tag92_IssuerPublicKeyRemainder, tag9f32_IssuerPublicKeyExponent);
            issuerPublicKey = emvKeyReader.parseIssuerPublicKeyNew(caPublicKeyExponent, caPublicKeyModulus, issuerPublicKeyCertificate, issuerPublicKeyRemainder, issuerPublicKeyExponent);
        } catch (EmvParsingException e) {
            // throw new RuntimeException(e);
            return null;
        }
        EmvKeyReader.RecoveredIssuerPublicKey recoveredIssuerPublicKeyParsed;
        try {
            recoveredIssuerPublicKeyParsed = emvKeyReader.parseIssuerPublicKeyCert(issuerPublicKey.getRecoveredBytes(), caPublicKeyModulus.length);
        } catch (EmvParsingException e) {
            // throw new RuntimeException(e);
            return null;
        }
        return recoveredIssuerPublicKeyParsed;
    }

    /**
     * decryption of the ICC Public Key Certificate
     * @param issuerPublicKeyExponent  *1)
     * @param issuerPublicKeyModulus   *1)
     * @param iccPublicKeyCertificate  *2) tag 0x9f46
     * @param iccPublicKeyRemainder    *3) tag 0x9f48
     * @param iccPublicKeyExponent     *2) tag 0x9f47
     * @return the recovered Issuer Public Key
     *
     * Notes: *1) get it from RecoveredIssuerPublicKey retrieveIssuerPublicKey
     *        *2) get it from the card
     *        *3) get it from the card [optional]
     */

    public static EmvKeyReader.RecoveredIccPublicKey retrieveIccPublicKey(byte[] issuerPublicKeyExponent, byte[] issuerPublicKeyModulus, byte[] iccPublicKeyCertificate, byte[] iccPublicKeyRemainder, byte[] iccPublicKeyExponent) {
        if ((issuerPublicKeyExponent == null) | (issuerPublicKeyModulus == null) | (iccPublicKeyCertificate == null) | (iccPublicKeyExponent == null)) return null;
        EmvKeyReader emvKeyReader = new EmvKeyReader();
        IssuerIccPublicKeyNew issuerIccPublicKey;
        try {
            //issuerIccPublicKey2New = emvKeyReader.parseIccPublicKeyNew(tag9f32_IssuerPublicKeyExponent, recoveredIssuerPublicKeyParsed.getLeftMostPubKeyDigits(), tag9f46_IccPublicKeyCertificate, tag92_IssuerPublicKeyRemainder, tag9f47_IccPublicKeyExponent);
            issuerIccPublicKey = emvKeyReader.parseIccPublicKeyNew(issuerPublicKeyExponent, issuerPublicKeyModulus, iccPublicKeyCertificate, iccPublicKeyRemainder, iccPublicKeyExponent);
        } catch (EmvParsingException e) {
            // throw new RuntimeException(e);
            return null;
        }

        EmvKeyReader.RecoveredIccPublicKey recoveredIccPublicKey;
        try {
            recoveredIccPublicKey = emvKeyReader.parseIccPublicKeyCert(issuerIccPublicKey.getRecoveredBytes(), issuerPublicKeyModulus.length);
        } catch (EmvParsingException e) {
            //throw new RuntimeException(e);
            return null;
        }
        return recoveredIccPublicKey;
    }

    /**
     * decrypt data with ICC Public Key
     * @param iccPublicKeyExponent                     *1)
     * @param iccPublicKeyModulus                      *1)
     * @param data, eg. SignedDynamicApplicationData   *2)
     * @return the decrypted data
     *
     * Notes: *1) get it from RecoveredIccPublicKey retrieveIccPublicKey
     *        *2) get it from the card
     */
    public static byte[] decryptDataWithIccPublicKey(byte[] iccPublicKeyExponent, byte[] iccPublicKeyModulus, byte[] data) {
        try {
            if ((iccPublicKeyExponent == null) | (iccPublicKeyModulus == null) | (data == null))
                return null;
            return EmvKeyReader.calculateRSA(data, iccPublicKeyExponent, iccPublicKeyModulus);
        } catch (EmvParsingException e) {
            // throw new RuntimeException(e);
            return null;
        }
    }


    /**
     * Either just returns leftmost digits or concatenated with remainder
     * just a dependency from EmvKeyReader
     * @param leftMostDigits
     * @param remainder
     * @return
     */
    public static byte[] concatenateModulus(byte[] leftMostDigits, byte[] remainder) {
        return EmvKeyReader.concatenateModulus(leftMostDigits, remainder);
    }

}
