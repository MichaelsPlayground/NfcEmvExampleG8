package de.androidcrypto.nfcemvexample.extended;

import java.io.Serializable;

public class TagNameValue implements Serializable {

    /**
     * This is a new class for collecting all tags from parsing an APDU response.
     * As an emv card have more than one tag this class is storing the
     * data for just one tag, all tags are collected in TagNameValues
     * author: androidcrypto
     */

    private static final long serialVersionUID = -5687519700883492228L;

    /**
     * section for variables
     */

    // general
    private final String modelVersion = "1"; // 1 = original version

    // data
    private byte[] tagBytes;
    private byte[] tagRawEncodedLengthBytes;
    private byte[] tagValueBytes;
    private String tagName;
    private String tagDescription;
    private String tagValueType;

    /**
     * section for constructor / init
     */

    public TagNameValue() {
    }

    /**
     * section for setter & getter
     */

    public String getModelVersion() {
        return modelVersion;
    }

    public byte[] getTagBytes() {
        return tagBytes;
    }

    public void setTagBytes(byte[] tagBytes) {
        this.tagBytes = tagBytes;
    }

    public byte[] getTagRawEncodedLengthBytes() {
        return tagRawEncodedLengthBytes;
    }

    public void setTagRawEncodedLengthBytes(byte[] tagRawEncodedLengthBytes) {
        this.tagRawEncodedLengthBytes = tagRawEncodedLengthBytes;
    }

    public byte[] getTagValueBytes() {
        return tagValueBytes;
    }

    public void setTagValueBytes(byte[] tagValueBytes) {
        this.tagValueBytes = tagValueBytes;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagDescription() {
        return tagDescription;
    }

    public void setTagDescription(String tagDescription) {
        this.tagDescription = tagDescription;
    }

    public String getTagValueType() {
        return tagValueType;
    }

    public void setTagValueType(String tagValueType) {
        this.tagValueType = tagValueType;
    }
}