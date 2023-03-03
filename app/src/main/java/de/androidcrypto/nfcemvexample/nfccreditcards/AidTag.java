package de.androidcrypto.nfcemvexample.nfccreditcards;

public class AidTag {

    private byte[] aidByte;
    private String aidName;

    public AidTag(byte[] aidByte, String aidName) {
        this.aidByte = aidByte;
        this.aidName = aidName;
    }

    public byte[] getAidByte() {
        return aidByte;
    }

    public void setAidByte(byte[] aidByte) {
        this.aidByte = aidByte;
    }

    public String getAidName() {
        return aidName;
    }

    public void setAidName(String aidName) {
        this.aidName = aidName;
    }
}
