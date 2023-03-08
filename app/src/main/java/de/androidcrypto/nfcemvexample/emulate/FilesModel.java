package de.androidcrypto.nfcemvexample.emulate;

import androidx.annotation.NonNull;

public class FilesModel {

    private String address;
    private String content;

    public FilesModel(@NonNull String address, @NonNull String content) {
        this.address = address;
        this.content = content;
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
}
