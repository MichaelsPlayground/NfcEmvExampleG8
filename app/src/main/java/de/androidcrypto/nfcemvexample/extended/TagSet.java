package de.androidcrypto.nfcemvexample.extended;

import androidx.annotation.NonNull;

import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;

/**
 * to run this class you need to use the dependency
 * implementation 'com.github.devnied.emvnfccard:library:3.0.1'
 */

public class TagSet {

    private byte[] tag;
    private String tagName;
    private byte[] tagValue;
    //private TagValueTypeEnum tagValueType;
    private String tagValueType;
    private String tagFound;

    //public TagSet(@NonNull byte[] tag, @NonNull String tagName, @NonNull byte[] tagValue, @NonNull TagValueTypeEnum tagValueType, @NonNull String tagFound) {
    public TagSet(@NonNull byte[] tag, @NonNull String tagName, @NonNull byte[] tagValue, @NonNull String tagValueType, @NonNull String tagFound) {
        this.tag = tag;
        this.tagName = tagName;
        this.tagValue = tagValue;
        this.tagValueType = tagValueType;
        this.tagFound = tagFound;
    }

    public byte[] getTag() {
        return tag;
    }

    public void setTag(@NonNull byte[] tag) {
        this.tag = tag;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(@NonNull String tagName) {
        this.tagName = tagName;
    }

    public byte[] getTagValue() {
        return tagValue;
    }

    public void setTagValue(@NonNull byte[] tagValue) {
        this.tagValue = tagValue;
    }

    //public TagValueTypeEnum getTagValueType() {
    public String getTagValueType() {
        return tagValueType;
    }

    //public void setTagValueType(@NonNull TagValueTypeEnum tagValueType) {
    public void setTagValueType(@NonNull String tagValueType) {
        this.tagValueType = tagValueType;
    }

    public String getTagFound() {
        return tagFound;
    }

    public void setTagFound(@NonNull String tagFound) {
        this.tagFound = tagFound;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("tag: ").append(bytesToHex(tag)).append("\n");
        sb.append("tagName: ").append(tagName).append("\n");
        sb.append("tagValue: ").append(bytesToHex(tagValue)).append("\n");
        sb.append("tagValueType: ").append(tagValueType).append("\n");
        sb.append("tagFound: ").append(tagFound).append("\n");
        return sb.toString();
    }

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
}
