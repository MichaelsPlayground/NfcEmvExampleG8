package de.androidcrypto.nfcemvexample.emulate;

import androidx.annotation.NonNull;

/**
 * This class contains the content of a single file on an EMV card
 * No analysis on the data has been made, so the file may contain
 * sensible data like PAN
 */

public class PureFileModel {

    private String aflAddress; // 2 hex encoded bytes aabb, aa = afl sfi bb = afl record
    private String sfi; // sfi on the card
    private String sfiAfl; // sfi in afl notation
    private String record; // record on the card
    private String length; // content length (bytes)
    private String content; // content in hex encoding

    public PureFileModel(@NonNull String aflAddress, @NonNull String sfi, @NonNull String sfiAfl, @NonNull String record, @NonNull String length, @NonNull String content) {
        this.aflAddress = aflAddress;
        this.sfi = sfi;
        this.sfiAfl = sfiAfl;
        this.record = record;
        this.length = length;
        this.content = content;
    }

    public String getAflAddress() {
        return aflAddress;
    }

    public void setAflAddress(@NonNull String aflAddress) {
        this.aflAddress = aflAddress;
    }

    public String getSfi() {
        return sfi;
    }

    public void setSfi(@NonNull String sfi) {
        this.sfi = sfi;
    }

    public String getSfiAfl() {
        return sfiAfl;
    }

    public void setSfiAfl(@NonNull String sfi) {
        this.sfi = sfiAfl;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(@NonNull String record) {
        this.record = record;
    }

    public String getLength() {
        return length;
    }

    public void setLength(@NonNull String length) {
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }
}
