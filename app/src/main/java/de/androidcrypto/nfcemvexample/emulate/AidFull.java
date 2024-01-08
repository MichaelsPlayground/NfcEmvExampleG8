package de.androidcrypto.nfcemvexample.emulate;

import androidx.annotation.NonNull;

public class AidFull {
    // this is NOT compatible for HceCreditCardEmulator
    private String aid;
    private String aidName;
    private String selectAidCommand;
    private String selectAidResponse;
    private String getProcessingOptionsCommand;
    private String getProcessingOptionsResponse;
    private int checkFirstBytesGetProcessingOptions;
    private String panFound;
    private String expirationDateFound;
    private int numberOfFiles;
    private String afl;
    // new in version 2
    private String applicationTransactionCounter;
    private String leftPinTryCounter;
    private String lastOnlineATCRegister;
    private String logFormat;
    private String getInternalAuthenticationCommand;
    private String getInternalAuthenticationResponse;
    private String getApplicationCryptogramCommand;
    private String getApplicationCryptogramResponse;
    private FilesModel[] files; // files read by analyzing the GPO
    // new in version 3
    private int numberOfFullFiles;
    private FilesModel[] fullFiles; // files read by brute force

    // constructor version 3 = AidFull
    public AidFull(@NonNull String aid, @NonNull String aidName, @NonNull String selectAidCommand, @NonNull String selectAidResponse, @NonNull String getProcessingOptionsCommand,
                   @NonNull String getProcessingOptionsResponse, int checkFirstBytesGetProcessingOptions, @NonNull String panFound,
                   @NonNull String expirationDateFound, int numberOfFiles, @NonNull String afl, @NonNull String applicationTransactionCounter, @NonNull String leftPinTryCounter,
                   @NonNull String lastOnlineATCRegister, @NonNull String logFormat, @NonNull String getInternalAuthenticationCommand,
                   @NonNull String getInternalAuthenticationResponse, @NonNull String getApplicationCryptogramCommand, @NonNull String getApplicationCryptogramResponse,
                   int numberOfFullFiles) {
        this.aid = aid;
        this.aidName = aidName;
        this.selectAidCommand = selectAidCommand;
        this.selectAidResponse = selectAidResponse;
        this.getProcessingOptionsCommand = getProcessingOptionsCommand;
        this.getProcessingOptionsResponse = getProcessingOptionsResponse;
        this.checkFirstBytesGetProcessingOptions = checkFirstBytesGetProcessingOptions;
        this.panFound = panFound;
        this.expirationDateFound = expirationDateFound;
        this.numberOfFiles = numberOfFiles;
        this.afl = afl;
        this.applicationTransactionCounter = applicationTransactionCounter;
        this.leftPinTryCounter = leftPinTryCounter;
        this.lastOnlineATCRegister = lastOnlineATCRegister;
        this.logFormat = logFormat;
        this.getInternalAuthenticationCommand = getInternalAuthenticationCommand;
        this.getInternalAuthenticationResponse = getInternalAuthenticationResponse;
        this.getApplicationCryptogramCommand = getApplicationCryptogramCommand;
        this.getApplicationCryptogramResponse = getApplicationCryptogramResponse;
        this.files = new FilesModel[numberOfFiles];
        // new in version 3 = AidDump
        this.numberOfFullFiles = numberOfFullFiles;
        this.fullFiles = new FilesModel[numberOfFullFiles];
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

    public void setCheckFirstBytesGetProcessingOptions(int checkFirstBytesGetProcessingOptions) {
        this.checkFirstBytesGetProcessingOptions = checkFirstBytesGetProcessingOptions;
    }

    public String getPanFound() {
        return panFound;
    }

    public void setPanFound(@NonNull String panFound) {
        this.panFound = panFound;
    }

    public String getExpirationDateFound() {
        return expirationDateFound;
    }

    public void setExpirationDateFound(@NonNull String expirationDateFound) {
        this.expirationDateFound = expirationDateFound;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
        this.files = new FilesModel[numberOfFiles];
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
    public void setFile(@NonNull int entry, @NonNull FilesModel filesModel) {
        files[entry] = filesModel;
    }

    public String getApplicationTransactionCounter() {
        return applicationTransactionCounter;
    }

    public void setApplicationTransactionCounter(@NonNull String applicationTransactionCounter) {
        this.applicationTransactionCounter = applicationTransactionCounter;
    }

    public String getLeftPinTryCounter() {
        return leftPinTryCounter;
    }

    public void setLeftPinTryCounter(@NonNull String leftPinTryCounter) {
        this.leftPinTryCounter = leftPinTryCounter;
    }

    public String getLastOnlineATCRegister() {
        return lastOnlineATCRegister;
    }

    public void setLastOnlineATCRegister(@NonNull String lastOnlineATCRegister) {
        this.lastOnlineATCRegister = lastOnlineATCRegister;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public void setLogFormat(@NonNull String logFormat) {
        this.logFormat = logFormat;
    }

    public String getGetInternalAuthenticationCommand() {
        return getInternalAuthenticationCommand;
    }

    public void setGetInternalAuthenticationCommand(@NonNull String getInternalAuthenticationCommand) {
        this.getInternalAuthenticationCommand = getInternalAuthenticationCommand;
    }

    public String getGetInternalAuthenticationResponse() {
        return getInternalAuthenticationResponse;
    }

    public void setGetInternalAuthenticationResponse(@NonNull String getInternalAuthenticationResponse) {
        this.getInternalAuthenticationResponse = getInternalAuthenticationResponse;
    }

    public String getGetApplicationCryptogramCommand() {
        return getApplicationCryptogramCommand;
    }

    public void setGetApplicationCryptogramCommand(@NonNull String getApplicationCryptogramCommand) {
        this.getApplicationCryptogramCommand = getApplicationCryptogramCommand;
    }

    public String getGetApplicationCryptogramResponse() {
        return getApplicationCryptogramResponse;
    }

    public void setGetApplicationCryptogramResponse(@NonNull String getApplicationCryptogramResponse) {
        this.getApplicationCryptogramResponse = getApplicationCryptogramResponse;
    }

    public int getNumberOfFullFiles() {
        return numberOfFullFiles;
    }

    public void setNumberOfFullFiles(int numberOfFullFiles) {
        this.numberOfFullFiles = numberOfFullFiles;
    }

    public void setFullFile(@NonNull int entry, @NonNull FilesModel filesModel) {
        fullFiles[entry] = filesModel;
    }

    public FilesModel[] getFullFiles() {
        return fullFiles;
    }

    public void setFullFiles(FilesModel[] fullFiles) {
        this.fullFiles = fullFiles;
    }

    public String dumpAid() {
        StringBuilder sb = new StringBuilder();
        sb.append("aid: ").append(this.aid).append("\n");
        sb.append("aidName: ").append(this.aidName).append("\n");
        sb.append("selectAidCommand: ").append(this.selectAidCommand).append("\n");
        sb.append("selectAidResponse: ").append(this.selectAidResponse).append("\n");
        sb.append("getProcessingOptionsCommand: ").append(this.getProcessingOptionsCommand).append("\n");
        sb.append("getProcessingOptionsResponse: ").append(this.getProcessingOptionsResponse).append("\n");
        sb.append("checkFirstBytesGetProcessingOptions: ").append(this.checkFirstBytesGetProcessingOptions).append("\n");
        sb.append("panFound: ").append(this.panFound).append("\n");
        sb.append("expirationDateFound: ").append(this.expirationDateFound).append("\n");
        sb.append("afl: ").append(this.afl).append("\n");
        sb.append("applicationTransactionCounter: ").append(this.applicationTransactionCounter).append("\n");
        sb.append("leftPinTryCounter: ").append(this.leftPinTryCounter).append("\n");
        sb.append("lastOnlineATCRegister: ").append(this.lastOnlineATCRegister).append("\n");
        sb.append("logFormat: ").append(this.logFormat).append("\n");
        sb.append("getInternalAuthenticationCommand: ").append(this.getInternalAuthenticationCommand).append("\n");
        sb.append("getInternalAuthenticationResponse: ").append(this.getInternalAuthenticationResponse).append("\n");
        sb.append("getApplicationCryptogramCommand: ").append(this.getApplicationCryptogramCommand).append("\n");
        sb.append("getApplicationCryptogramResponse: ").append(this.getApplicationCryptogramResponse).append("\n");
        sb.append("numberOfFiles: ").append(this.numberOfFiles).append("\n");
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                // todo get the files dump
                sb.append("files: ").append(this.files[i].dumpFilesModel());
            }
        }
        // new in version 3 = AidDump
        sb.append("numberOfFullFiles: ").append(this.numberOfFullFiles).append("\n");
        // here we don't print the complete files
        return sb.toString();
    }
}
