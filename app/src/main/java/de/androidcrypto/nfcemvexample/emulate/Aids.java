package de.androidcrypto.nfcemvexample.emulate;

import androidx.annotation.NonNull;

public class Aids {

    private String cardType; // MasterCard, VisaCard, GiroCard
    private String cardName; // individual name given by user
    private String selectPpseCommand;
    private String selectPpseResponse;
    private int numberOfAid;
    private Aid[] aid;

    public Aids(@NonNull String cardType, @NonNull String cardName, @NonNull String selectPpseCommand, @NonNull String selectPpseResponse, @NonNull int numberOfAid, @NonNull Aid[] aid) {
        this.cardType = cardType;
        this.cardName = cardName;
        this.selectPpseCommand = selectPpseCommand;
        this.selectPpseResponse = selectPpseResponse;
        this.numberOfAid = numberOfAid;
        this.aid = aid;
    }

    // don't forget to add the aids manually
    public Aids(@NonNull String cardType, @NonNull String cardName, @NonNull String selectPpseCommand, @NonNull String selectPpseResponse, @NonNull int numberOfAid) {
        this.cardType = cardType;
        this.cardName = cardName;
        this.selectPpseCommand = selectPpseCommand;
        this.selectPpseResponse = selectPpseResponse;
        this.numberOfAid = numberOfAid;
        this.aid = new Aid[numberOfAid];
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

    public Aid[] getAid() {
        return aid;
    }

    public void setAid(@NonNull Aid[] aid) {
        this.aid = aid;
    }

    public void setAidEntry(@NonNull Aid aid, @NonNull int numberOfEntry) {
        this.aid[numberOfEntry] = aid;
    }

    /*
    private String cardType; // MasterCard, VisaCard, GiroCard
    private String cardName; // individual name given by user
    private String selectPpseCommand;
    private String selectPpseResponse;
    private int numberOfAid;
    private Aid[] aid;
     */

    public String dumpAids() {
        StringBuilder sb = new StringBuilder();
        sb.append("cardType: ").append(this.cardType).append("\n");
        sb.append("cardName: ").append(this.cardName).append("\n");
        sb.append("selectPpseCommand: ").append(this.selectPpseCommand).append("\n");
        sb.append("selectPpseResponse: ").append(this.selectPpseResponse).append("\n");
        sb.append("numberOfAid: ").append(this.numberOfAid).append("\n");
        for (int i = 0; i < aid.length; i++) {
            // todo get the aid dump
            sb.append("--------");
            sb.append("aid entry: " + i);
            sb.append("aid: ").append(this.aid[i].dumpAid());
        }
        return sb.toString();
    }
}
