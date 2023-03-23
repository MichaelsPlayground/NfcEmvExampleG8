package de.androidcrypto.nfcemvexample.nfccreditcards;

import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.nfcemvexample.extended.TagSet;

/**
 * encapsulates the information for each module in emv workflow (e.g. selectPpse, readRecord...)
 */

public class ModuleInfo {

    private byte[] command;
    private byte[] response;
    private boolean success;
    private List<TagSet> tsList = new ArrayList<>(); // holds the tags found during reading
    private List<byte[]> dataList = new ArrayList<>(); // holds the data found during reading
    private String prettyPrint;

    public ModuleInfo(byte[] command, byte[] response, boolean success, List<TagSet> tsList, List<byte[]> dataList, String prettyPrint) {
        this.command = command;
        this.response = response;
        this.success = success;
        this.tsList = tsList;
        this.dataList = dataList;
        this.prettyPrint = prettyPrint;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<TagSet> getTsList() {
        return tsList;
    }

    public void setTsList(List<TagSet> tsList) {
        this.tsList = tsList;
    }

    public List<byte[]> getDataList() {
        return dataList;
    }

    public void setDataList(List<byte[]> dataList) {
        this.dataList = dataList;
    }

    public String getPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(String prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public String dumpTsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("---- tagSet list ----").append("\n");
        for (int i = 0; i < tsList.size(); i++) {
            sb.append("--- tag nr ").append(i).append(" ---\"").append("\n");
            sb.append(tsList.get(i).dump());
        }
        return sb.toString();
    }
}
