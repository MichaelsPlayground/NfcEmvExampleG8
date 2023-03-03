package de.androidcrypto.nfcemvexample.nfccreditcards;

public class EmvTag {

    private byte[] tagByte;
    private String tagName;
    private boolean tagIsBinary;

    public EmvTag(byte[] tagByte, String tagName, boolean tagIsBinary) {
        this.tagByte = tagByte;
        this.tagName = tagName;
        this.tagIsBinary = tagIsBinary;
    }

    public byte[] getTagByte() {
        return tagByte;
    }

    public void setTagByte(byte[] tagByte) {
        this.tagByte = tagByte;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public boolean isTagIsBinary() {
        return tagIsBinary;
    }

    public void setTagIsBinary(boolean tagIsBinary) {
        this.tagIsBinary = tagIsBinary;
    }
}
