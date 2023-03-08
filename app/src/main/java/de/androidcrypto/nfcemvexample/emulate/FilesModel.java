package de.androidcrypto.nfcemvexample.emulate;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;

import androidx.annotation.NonNull;

public class FilesModel {

    private String address;
    private String content;
    private String sfi;
    private String record;
    private String length;
    private String offlineAuth;
    private String containsTrack2Data; // 'true' or 'false'
    private String panT2D;
    private String exptDateT2D;
    String track2Data;
    private String containsPan; // 'true' or 'false'
    private String pan;
    private String expDate;

    public FilesModel(@NonNull String address, @NonNull String content, @NonNull String sfi, @NonNull String record, @NonNull String length, @NonNull String offlineAuth, @NonNull String containsTrack2Data, @NonNull String track2Data, @NonNull String panT2D, @NonNull String expDateT2D, @NonNull String containsPan, @NonNull String pan, @NonNull String expDate) {
        this.address = address;
        this.content = content;
        this.sfi = sfi;
        this.record = record;
        this.length = length;
        this.offlineAuth = offlineAuth;
        this.containsTrack2Data = containsTrack2Data;
        this.panT2D = panT2D;
        this.exptDateT2D = expDateT2D;
        this.track2Data = track2Data;
        this.containsPan = containsPan;
        this.pan = pan;
        this.expDate = expDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(@NonNull String address) {
        this.address = address;
    }

    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public String getSfi() {
        return sfi;
    }

    public void setSfi(String sfi) {
        this.sfi = sfi;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getOfflineAuth() {
        return offlineAuth;
    }

    public void setOfflineAuth(String offlineAuth) {
        this.offlineAuth = offlineAuth;
    }

    public String getContainsTrack2Data() {
        return containsTrack2Data;
    }

    public void setContainsTrack2Data(String containsTrack2Data) {
        this.containsTrack2Data = containsTrack2Data;
    }

    public String getPanT2D() {
        return panT2D;
    }

    public void setPanT2D(String panT2D) {
        this.panT2D = panT2D;
    }

    public String getExptDateT2D() {
        return exptDateT2D;
    }

    public void setExptDateT2D(String exptDateT2D) {
        this.exptDateT2D = exptDateT2D;
    }

    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public String getContainsPan() {
        return containsPan;
    }

    public void setContainsPan(String containsPan) {
        this.containsPan = containsPan;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }
}
