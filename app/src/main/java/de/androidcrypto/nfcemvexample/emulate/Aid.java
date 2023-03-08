package de.androidcrypto.nfcemvexample.emulate;

import androidx.annotation.NonNull;

public class Aid {

    private String aid;
    private String aidName;
    private String selectAidCommand;
    private String selectAidResponse;
    private String getProcessingOptionsCommand;
    private String getProcessingOptionsResponse;
    private int checkFirstBytesGetProcessingOptions;
    private String panFoundInTrack2Data;
    private String panFoundInFiles;
    private int numberOfFiles;
    private String afl;
    private FilesModel[] files;

    public Aid(@NonNull String aid, @NonNull String aidName, @NonNull String selectAidCommand, @NonNull String selectAidResponse, @NonNull String getProcessingOptionsCommand, @NonNull String getProcessingOptionsResponse, @NonNull int checkFirstBytesGetProcessingOptions, @NonNull String panFoundInTrack2Data, @NonNull String panFoundInFiles, @NonNull int numberOfFiles, @NonNull String afl, FilesModel[] files) {
        this.aid = aid;
        this.aidName = aidName;
        this.selectAidCommand = selectAidCommand;
        this.selectAidResponse = selectAidResponse;
        this.getProcessingOptionsCommand = getProcessingOptionsCommand;
        this.getProcessingOptionsResponse = getProcessingOptionsResponse;
        this.checkFirstBytesGetProcessingOptions = checkFirstBytesGetProcessingOptions;
        this.panFoundInTrack2Data = panFoundInTrack2Data;
        this.panFoundInFiles = panFoundInFiles;
        this.numberOfFiles = numberOfFiles;
        this.afl = afl;
        this.files = files;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(@NonNull String aid) {
        this.aid = aid;
    }

    public String getAidName() {
        return aidName;
    }

    public void setAidName(@NonNull String aidName) {
        this.aidName = aidName;
    }

    public String getSelectAidCommand() {
        return selectAidCommand;
    }

    public void setSelectAidCommand(String selectAidCommand) {
        this.selectAidCommand = selectAidCommand;
    }

    public String getSelectAidResponse() {
        return selectAidResponse;
    }

    public void setSelectAidResponse(String selectAidResponse) {
        this.selectAidResponse = selectAidResponse;
    }

    public String getGetProcessingOptionsCommand() {
        return getProcessingOptionsCommand;
    }

    public void setGetProcessingOptionsCommand(@NonNull String getProcessingOptionsCommand) {
        this.getProcessingOptionsCommand = getProcessingOptionsCommand;
    }

    public String getGetProcessingOptionsResponse() {
        return getProcessingOptionsResponse;
    }

    public void setGetProcessingOptionsResponse(@NonNull String getProcessingOptionsResponse) {
        this.getProcessingOptionsResponse = getProcessingOptionsResponse;
    }

    public int getCheckFirstBytesGetProcessingOptions() {
        return checkFirstBytesGetProcessingOptions;
    }

    public void setCheckFirstBytesGetProcessingOptions(@NonNull int checkFirstBytesGetProcessingOptions) {
        this.checkFirstBytesGetProcessingOptions = checkFirstBytesGetProcessingOptions;
    }

    public String getPanFoundInTrack2Data() {
        return panFoundInTrack2Data;
    }

    public void setPanFoundInTrack2Data(@NonNull String panFoundInTrack2Data) {
        this.panFoundInTrack2Data = panFoundInTrack2Data;
    }

    public String getPanFoundInFiles() {
        return panFoundInFiles;
    }

    public void setPanFoundInFiles(@NonNull String panFoundInFiles) {
        this.panFoundInFiles = panFoundInFiles;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(@NonNull int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public String getAfl() {
        return afl;
    }

    public void setAfl(String afl) {
        this.afl = afl;
    }

    public FilesModel[] getFiles() {
        return files;
    }

    public void setFiles(FilesModel[] files) {
        this.files = files;
    }
}
