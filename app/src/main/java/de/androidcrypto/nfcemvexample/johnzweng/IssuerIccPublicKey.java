package de.androidcrypto.nfcemvexample.johnzweng;

import java.math.BigInteger;
import java.util.Date;

public class IssuerIccPublicKey {

    /*
    return new IssuerIccPublicKey(new BigInteger(1, publicKeyExponent),
                new BigInteger(1, concatenateModulus(cert.leftMostPubKeyDigits, remainder)),
                publicKeyCertificate, expirationDate);
     */

    private BigInteger publicExponent;
    private BigInteger modulus;
    private byte[] emvCertificate;
    private Date expirationDate;

    public IssuerIccPublicKey(BigInteger publicExponent, BigInteger modulus, byte[] emvCertificate, Date expirationDate) {
        this.publicExponent = publicExponent;
        this.modulus = modulus;
        this.emvCertificate = emvCertificate;
        this.expirationDate = expirationDate;
    }

    public BigInteger getPublicExponent() {
        return publicExponent;
    }

    public void setPublicExponent(BigInteger publicExponent) {
        this.publicExponent = publicExponent;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public void setModulus(BigInteger modulus) {
        this.modulus = modulus;
    }

    public byte[] getEmvCertificate() {
        return emvCertificate;
    }

    public void setEmvCertificate(byte[] emvCertificate) {
        this.emvCertificate = emvCertificate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
