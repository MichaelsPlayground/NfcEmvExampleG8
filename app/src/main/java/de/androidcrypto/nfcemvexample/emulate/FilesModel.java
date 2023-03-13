package de.androidcrypto.nfcemvexample.emulate;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;

import androidx.annotation.NonNull;

public class FilesModel {

    private String addressAfl; // eg "0801" or "1002"
    private int sfi; // the pure sfi number on file, e.g. 1 or 2 or 3
    private int record;
    private int dataLength;
    private String content; // hex encoded data
    private int offlineAuth; // files included in offline transaction, usually 0

    public FilesModel(@NonNull String addressAfl, @NonNull int sfi, @NonNull int record, @NonNull int dataLength, @NonNull String content, @NonNull int offlineAuth) {
        this.addressAfl = addressAfl;
        this.sfi = sfi;
        this.record = record;
        this.dataLength = dataLength;
        this.content = content;
        this.offlineAuth = offlineAuth;
    }

    public String getAddressAfl() {
        return addressAfl;
    }

    public void setAddressAfl(@NonNull String addressAfl) {
        this.addressAfl = addressAfl;
    }

    public int getSfi() {
        return sfi;
    }

    public void setSfi(int sfi) {
        this.sfi = sfi;
    }

    public int getRecord() {
        return record;
    }

    public void setRecord(int record) {
        this.record = record;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public int getOfflineAuth() {
        return offlineAuth;
    }

    public void setOfflineAuth(int offlineAuth) {
        this.offlineAuth = offlineAuth;
    }

    public String dumpFilesModel() {
        StringBuilder sb = new StringBuilder();
        sb.append("addressAfl: ").append(this.addressAfl).append("\n");
        sb.append("sfi: ").append(this.sfi).append("\n");
        sb.append("record: ").append(this.record).append("\n");
        sb.append("dataLength: ").append(this.dataLength).append("\n");
        sb.append("content: ").append(this.content).append("\n");
        sb.append("offlineAuth: ").append(this.offlineAuth);
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
