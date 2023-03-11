package de.androidcrypto.nfcemvexample.emulate;

import androidx.annotation.NonNull;

/**
 * This class contains the results of a complete file reading.
 * No analyzis on the data has been made, so the file may contain
 * sensible data like PAN
 */

public class PureFilesModel {

    private String name; // name given by the extractor
    private int numberOfRecords = 0; // default
    //private PureFileModel[] pureFiles = new PureFileModel[496];
    private PureFileModel[] pureFiles;

    public PureFilesModel(@NonNull String name, @NonNull int numberOfRecords, @NonNull PureFileModel[] pureFiles) {
        this.name = name;
        this.numberOfRecords = numberOfRecords;
        this.pureFiles = pureFiles;
    }

    // don't forget to add the number of records and the pureFile
    public PureFilesModel(@NonNull String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(@NonNull int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
        this.pureFiles = new PureFileModel[numberOfRecords]; // reassign array
    }

    public PureFileModel[] getPureFiles() {
        return pureFiles;
    }

    public void setPureFiles(@NonNull PureFileModel[] pureFiles) {
        this.pureFiles = pureFiles;
    }

    public void setPureFile(@NonNull int entry, @NonNull PureFileModel pureFileModel) {
        pureFiles[entry] = pureFileModel;
    }

}
