package de.androidcrypto.nfcemvexample.emulate;

import androidx.annotation.NonNull;

public class Aids {

    private String cardType; // MasterCard, VisaCard, GiroCard
    private String cardName; // individual name given by user
    private String selectAidCommand;
    private String selectAidResponse;
    private int numberOfAid;
    private Aid[] aid;

    public Aids(@NonNull String cardType, @NonNull String cardName, @NonNull String selectAidCommand, @NonNull String selectAidResponse, @NonNull int numberOfAid, @NonNull Aid[] aid) {
        this.cardType = cardType;
        this.cardName = cardName;
        this.selectAidCommand = selectAidCommand;
        this.selectAidResponse = selectAidResponse;
        this.numberOfAid = numberOfAid;
        this.aid = aid;
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

    public String getSelectAidCommand() {
        return selectAidCommand;
    }

    public void setSelectAidCommand(@NonNull String selectAidCommand) {
        this.selectAidCommand = selectAidCommand;
    }

    public String getSelectAidResponse() {
        return selectAidResponse;
    }

    public void setSelectAidResponse(@NonNull String selectAidResponse) {
        this.selectAidResponse = selectAidResponse;
    }

    public int getNumberOfAid() {
        return numberOfAid;
    }

    public void setNumberOfAid(@NonNull int numberOfAid) {
        this.numberOfAid = numberOfAid;
    }

    public Aid[] getAid() {
        return aid;
    }

    public void setAid(@NonNull Aid[] aid) {
        this.aid = aid;
    }
}
