package de.androidcrypto.nfcemvexample.emulate;

import androidx.annotation.NonNull;

public class AidsFull {
    // this is NOT compatible for HceCreditCardEmulator

    private String cardType; // MasterCard, VisaCard, GiroCard
    private String cardName; // individual name given by user
    private String selectPpseCommand;
    private String selectPpseResponse;
    private int numberOfAid;
    //private Aid[] aid;
    private AidFull[] aidFull; // this is new in version 3

    public AidsFull(@NonNull String cardType, @NonNull String cardName, @NonNull String selectPpseCommand, @NonNull String selectPpseResponse, @NonNull int numberOfAid, @NonNull Aid[] aid, @NonNull AidFull[] aidFull) {
        this.cardType = cardType;
        this.cardName = cardName;
        this.selectPpseCommand = selectPpseCommand;
        this.selectPpseResponse = selectPpseResponse;
        this.numberOfAid = numberOfAid;
        //this.aid = aid;
        this.aidFull = aidFull;
    }

    // don't forget to add the aids manually
    public AidsFull(@NonNull String cardType, @NonNull String cardName, @NonNull String selectPpseCommand, @NonNull String selectPpseResponse, @NonNull int numberOfAid) {
        this.cardType = cardType;
        this.cardName = cardName;
        this.selectPpseCommand = selectPpseCommand;
        this.selectPpseResponse = selectPpseResponse;
        this.numberOfAid = numberOfAid;
        //this.aid = new Aid[numberOfAid];
        this.aidFull = new AidFull[numberOfAid];
    }


    public String getCardType() {
        return cardType;
    }

    public void setCardType(@NonNull String cardType) {
        this.cardType = cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(@NonNull String cardName) {
        this.cardName = cardName;
    }

    public String getSelectPpseCommand() {
        return selectPpseCommand;
    }

    public void setSelectPpseCommand(@NonNull String selectAidCommand) {
        this.selectPpseCommand = selectAidCommand;
    }

    public String getSelectPpseResponse() {
        return selectPpseResponse;
    }

    public void setSelectPpseResponse(@NonNull String selectAidResponse) {
        this.selectPpseResponse = selectAidResponse;
    }

    public int getNumberOfAid() {
        return numberOfAid;
    }

    public void setNumberOfAid(@NonNull int numberOfAid) {
        this.numberOfAid = numberOfAid;
    }

    public AidFull[] getAid() {
        return aidFull;
    }

    public void setAid(@NonNull AidFull[] aid) {
        this.aidFull = aid;
    }

    public AidFull[] getAidFull() {
        return aidFull;
    }

    public void setAidFull(AidFull[] aidFull) {
        this.aidFull = aidFull;
    }

    public void setAidEntry(@NonNull AidFull aid, @NonNull int numberOfEntry) {
        this.aidFull[numberOfEntry] = aid;
    }

    public String dumpAids() {
        StringBuilder sb = new StringBuilder();
        sb.append("cardType: ").append(this.cardType).append("\n");
        sb.append("cardName: ").append(this.cardName).append("\n");
        sb.append("selectPpseCommand: ").append(this.selectPpseCommand).append("\n");
        sb.append("selectPpseResponse: ").append(this.selectPpseResponse).append("\n");
        sb.append("numberOfAid: ").append(this.numberOfAid).append("\n");
        if (aidFull != null) {
            for (int i = 0; i < aidFull.length; i++) {
                sb.append("-------- aid --------").append("\n");
                sb.append("aid entry: " + i).append("\n");
                sb.append("aid: ").append(this.aidFull[i].dumpAid()).append("\n");
                // the files in aid dump
                for (int j = 0; j < this.aidFull[i].getNumberOfFiles(); j++) {
                    sb.append("-------- file --------").append("\n");
                    sb.append("file entry: " + j).append("\n");
                    sb.append("file: ").append(this.aidFull[i].getFiles()[j].dumpFilesModel()).append("\n");
                }
                sb.append("aidFull size: ").append(this.aidFull.length).append("\n");
            }
        }
        return sb.toString();
    }
}
