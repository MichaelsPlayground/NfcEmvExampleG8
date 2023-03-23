package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;

import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.nfcemvexample.emulate.PureFileModel;
import de.androidcrypto.nfcemvexample.emulate.PureFilesModel;
import de.androidcrypto.nfcemvexample.extended.TagListParser;
import de.androidcrypto.nfcemvexample.extended.TagNameValue;
import de.androidcrypto.nfcemvexample.extended.TagSet;
import de.androidcrypto.nfcemvexample.nfccreditcards.ModuleInfo;

public class EmvModules {

    /**
     * This class holds the modules for reading an EMV/Payment/CreditCard:
     * moduleSelectPpse
     * moduleSelectAid
     *
     */

    /**
     * module for selecting PPSE
     * @param nfc
     * @return ModuleInfo
     */
    public static ModuleInfo moduleSelectPpse(IsoDep nfc) {
        ModuleInfo mi;
        byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PPSE
        byte[] command = selectApdu(PPSE);
        byte[] response = new byte[0];
        try {
            response = nfc.transceive(command);
            boolean selectPpseNotAllowed = responseNotAllowed(response);
            if (selectPpseNotAllowed) {
                mi = new ModuleInfo(command, response, false, null, null, null);
                return mi;
            }
            byte[] responseOk = checkResponse(response);
            if (responseOk != null) {
                String responseString = TlvUtil.prettyPrintAPDUResponse(responseOk);
                List<TagSet> tsList = new ArrayList<>();
                tsList.addAll(getTagSetFromResponse(responseOk, "selectPpse"));
                List<byte[]> dataList = new ArrayList<>(); // will return the found aid's in tag 0x4f
                BerTlvParser parser = new BerTlvParser();
                BerTlvs tlv4Fs = parser.parse(responseOk);
                // by searching for tag 4f
                List<BerTlv> tag4fList = tlv4Fs.findAll(new BerTag(0x4F));
                if (tag4fList.size() < 1) {
                    mi = new ModuleInfo(command, responseOk, true, tsList, null, responseString);
                    return mi;
                }
                //ArrayList<byte[]> aidList = new ArrayList<>();
                for (int i4f = 0; i4f < tag4fList.size(); i4f++) {
                    BerTlv tlv4f = tag4fList.get(i4f);
                    byte[] tlv4fBytes = tlv4f.getBytesValue();
                    dataList.add(tlv4fBytes);
                }
                mi = new ModuleInfo(command, response, true, tsList, dataList, responseString);
                return mi;
            } else {
                // if (responseOk != null) {
                mi = new ModuleInfo(command, response, false, null, null, null);
                return mi;
            }
        } catch (IOException e) {
            mi = new ModuleInfo(command, null, false, null, null, null);
            return mi;
        }
    }

    /**
     * module for selecting AID
     * @param nfc
     * @param aid
     * @return ModuleInfo
     */
    public static ModuleInfo moduleSelectAid(IsoDep nfc, byte[] aid) {
        ModuleInfo mi;
        byte[] command = selectApdu(aid);
        byte[] response = new byte[0];
        try {
            response = nfc.transceive(command);
            boolean selectAidNotAllowed = responseNotAllowed(response);
            if (selectAidNotAllowed) {
                mi = new ModuleInfo(command, response, false, null, null, null);
                return mi;
            }
            byte[] responseOk = checkResponse(response);
            if (responseOk != null) {
                String responseString = TlvUtil.prettyPrintAPDUResponse(responseOk);
                List<TagSet> tsList = new ArrayList<>();
                tsList.addAll(getTagSetFromResponse(responseOk, "selectAid"));
                List<byte[]> dataList = new ArrayList<>(); // will return the found PDOL in tag 0x9F38
                BerTlvParser parser = new BerTlvParser();
                BerTlvs tlvs = parser.parse(responseOk);
                // searching for tag 0x9f38
                BerTlv tag9f38 = tlvs.find(new BerTag(0x9F, 0x38));
                if (tag9f38 == null) {
                    // return mi with empty dataList = null
                    mi = new ModuleInfo(command, response, true, tsList, null, responseString);
                } else {
                    byte[] pdolValue = tag9f38.getBytesValue();
                    dataList.add(pdolValue); // just one entry
                    mi = new ModuleInfo(command, response, true, tsList, dataList, responseString);
                }
                return mi;
            } else {
                // if (responseOk != null) {
                mi = new ModuleInfo(command, response, false, null, null, null);
                return mi;
            }
        } catch (IOException e) {
            mi = new ModuleInfo(command, null, false, null, null, null);
            return mi;
        }
    }

    /**
     * module for get the processing options (GPO)
     * @param nfc
     * @param commandRequestGpo
     * @return ModuleInfo
     */
    public static ModuleInfo moduleGetProcessingOptions(IsoDep nfc, byte[] commandRequestGpo) {
        ModuleInfo mi;
        byte[] response = new byte[0];
        try {
            response = nfc.transceive(commandRequestGpo);
            boolean gpoNotAllowed = responseNotAllowed(response);
            if (gpoNotAllowed) {
                mi = new ModuleInfo(commandRequestGpo, response, false, null, null, null);
                return mi;
            }
            byte[] responseOk = checkResponse(response);
            if (responseOk != null) {
                String responseString = TlvUtil.prettyPrintAPDUResponse(responseOk);
                List<TagSet> tsList = new ArrayList<>();
                tsList.addAll(getTagSetFromResponse(responseOk, "getProcessingOptions"));
                mi = new ModuleInfo(commandRequestGpo, response, true, tsList, null, responseString);
                return mi;
            } else {
                // if (responseOk != null) {
                mi = new ModuleInfo(commandRequestGpo, response, false, null, null, null);
                return mi;
            }
        } catch (IOException e) {
            mi = new ModuleInfo(commandRequestGpo, null, false, null, null, null);
            return mi;
        }
    }

    /**
     * checks if a pan is included in a response
     * checks for the following tags
     * tag 0x57   Track 2 Equivalent Data
     * tag 0x5a   Application Primary Account Number (PAN)
     * tag 0x5f24 Application Expiration Date
     * not included:
     * tag 0x56   Track 1 equivalent data (only on MagStripe)
     * tag 0x9f6b Track 2 Equivalent Data (only on MagStripe)
     *
     * @param response could be from getProcessingOptionsResponse or readFile
     * @return a string pan + "_" + expirationDate, eg. 1234567890123456_2605 or 123456789012345_260514
     * if no pan was found it returns "_"
     */
    public static String checkForPanInResponse(byte[] response) {
        String pan = "";
        String expirationDate = "";
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvs = parser.parse(response);
        // 57 Track 2 Equivalent Data field as well
        // 5a = Application Primary Account Number (PAN)
        // 5F34 = Application Primary Account Number (PAN) Sequence Number
        // 5F25  = Application Effective Date (card valid from)
        // 5F24 = Application Expiration Date
        // search for track 2 equivalent data
        BerTlv tag57 = tlvs.find(new BerTag(0x57));
        if (tag57 != null) {
            byte[] tag57Bytes = tag57.getBytesValue();
            String track2DataString = bytesToHex(tag57Bytes);
            int posSeparator = track2DataString.toUpperCase().indexOf("D");
            pan = track2DataString.substring(0, posSeparator);
            expirationDate = track2DataString.substring((posSeparator + 1), (posSeparator + 5));
            return pan + "_" + expirationDate;
        }
        // search for tag 0x5a = pan
        BerTlv tag5a = tlvs.find(new BerTag(0x5a));
        if (tag5a != null) {
            byte[] tag5aBytes = tag5a.getBytesValue();
            pan = bytesToHex(tag5aBytes);
        }
        // search for tag 0x5f24 = expiration date
        BerTlv tag5f24 = tlvs.find(new BerTag(0x5f, 0x24));
        if (tag5f24 != null) {
            byte[] tag5f24Bytes = tag5f24.getBytesValue();
            expirationDate = bytesToHex(tag5f24Bytes);
        }
        return pan + "_" + expirationDate;
    }

    /**
     * checks that a tag 0x94 Application File Locator (AFL) is available in gpoResponse
     *
     * @param gpoResponse
     * @return the list with afl entries (each of 4 byte)
     * if no afl was found it returns an empty list
     */
    public static List<byte[]> checkForAflInGpoResponse(byte[] gpoResponse) {
        List<byte[]> aflList = new ArrayList<>();
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvs = parser.parse(gpoResponse);
        // search for tag 0x94 Application File Locator (AFL)
        BerTlv tag94 = tlvs.find(new BerTag(0x94));
        if (tag94 != null) {
            byte[] tag94Bytes = tag94.getBytesValue();
            // split array by 4 bytes
            List<byte[]> tag94BytesList = divideArray(tag94Bytes, 4);
            aflList.addAll(tag94BytesList);
            /*
            for (int i = 0; i < tag94BytesList.size(); i++) {
                aflList.add(tag94BytesList.get(i));
            }
             */
        } else {
            // return an empty AFL list
        }
        return aflList;
    }

    private static List<byte[]> divideArray(byte[] source, int chunksize) {
        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }
        return result;
    }

    /**
     * section for reading files from card
     */

    /**
     * reads one AFL entry, could be more than one record
     * @param nfc
     * @param aflEntry 4 bytes
     * @return List<ModuleInfo>
     */

    public static List<ModuleInfo> moduleReadAflEntry(IsoDep nfc, byte[] aflEntry) {
        List<ModuleInfo> mis = new ArrayList<>();
        byte sfiOrg = aflEntry[0];
        byte rec1 = aflEntry[1];
        byte recL = aflEntry[2];
        byte offl = aflEntry[3]; // offline authorization
        // todo work with offline authorization
        int sfiNew = (byte) sfiOrg | 0x04; // add 4 = set bit 3
        for (int iRecords = (int) rec1; iRecords <= (int) recL; iRecords++) {
            ModuleInfo mi = readSingleAflEntry(nfc, sfiNew, iRecords);
            mis.add(mi);
        }
        return mis;
    }

    /**
     * reads a single entry from an AFL entry (just one record)
     * @param nfc
     * @param sfiNew
     * @param record
     * @return ModuleInfo with command, response and prettyPrint, NO dataList
     */
    private static ModuleInfo readSingleAflEntry(IsoDep nfc, int sfiNew, int record) {
        ModuleInfo mi;
        byte[] command = hexToBytes("00B2000400");
        byte[] response = new byte[0];
        command[2] = (byte) (record & 0x0FF);
        command[3] |= (byte) (sfiNew & 0x0FF);
        try {
            response = nfc.transceive(command);
            byte[] responseOk = checkResponse(response);
            if (responseOk != null) {
                String responseString = TlvUtil.prettyPrintAPDUResponse(responseOk);
                List<TagSet> tsList = new ArrayList<>();
                tsList.addAll(getTagSetFromResponse(responseOk, "readFile"));
                mi = new ModuleInfo(command, responseOk, true, tsList, null, responseString);
                return mi;
            } else {
                mi = new ModuleInfo(command, response, false, null, null, null);
                return mi;
            }
        } catch (IOException e) {
            mi = new ModuleInfo(command, response, false, null, null, null);
            return mi;
        }
    }


    /*
    public static PureFilesModel moduleReadAflEntry(IsoDep nfc, byte[] aflEntry) {
        PureFileModel pf;
        PureFilesModel pfs;
        //List<ModuleInfo> mis = new ArrayList<>();
        byte[] response = new byte[0];
        try {
            byte sfiOrg = aflEntry[0];
            byte rec1 = aflEntry[1];
            byte recL = aflEntry[2];
            byte offl = aflEntry[3]; // offline authorization
            //writeToUiAppend(etLog, "sfiOrg: " + sfiOrg + " rec1: " + ((int) rec1) + " recL: " + ((int) recL));
            int sfiNew = (byte) sfiOrg | 0x04; // add 4 = set bit 3
            // read records
            //byte[] resultReadRecord = new byte[0];
            for (int iRecords = (int) rec1; iRecords <= (int) recL; iRecords++) {
                byte[] command = hexToBytes("00B2000400");
                command[2] = (byte) (iRecords & 0x0FF);
                command[3] |= (byte) (sfiNew & 0x0FF);
                response = nfc.transceive(command);
                if (response != null) {
                    pf = new PureFileModel()

                } else {
                    // if (response != null) {
                    // no entry
                    //mi = new ModuleInfo(command, response, false, null, null, null);
                    //mis.add(mi);
                }



            }


            boolean gpoNotAllowed = responseNotAllowed(response);
            if (gpoNotAllowed) {
                mi = new ModuleInfo(commandRequestGpo, response, false, null, null, null);
                return mi;
            }
            byte[] responseOk = checkResponse(response);
            if (responseOk != null) {
                String responseString = TlvUtil.prettyPrintAPDUResponse(responseOk);
                List<TagSet> tsList = new ArrayList<>();
                tsList.addAll(getTagSetFromResponse(responseOk, "getProcessingOptions"));
                mi = new ModuleInfo(commandRequestGpo, response, true, tsList, null, responseString);
                return mi;
            } else {
                // if (responseOk != null) {
                mi = new ModuleInfo(commandRequestGpo, response, false, null, null, null);
                return mi;
            }
        } catch (IOException e) {
            mi = new ModuleInfo(commandRequestGpo, null, false, null, null, null);
            return mi;
        }
    }
    */

/*
    public static byte[] readFileAflEntryOrg(IsoDep nfc, byte[] aflEntry) {
        byte sfiOrg = aflEntry[0];
        byte rec1 = aflEntry[1];
        byte recL = aflEntry[2];
        byte offl = aflEntry[3]; // offline authorization
        //writeToUiAppend(etLog, "sfiOrg: " + sfiOrg + " rec1: " + ((int) rec1) + " recL: " + ((int) recL));
        int sfiNew = (byte) sfiOrg | 0x04; // add 4 = set bit 3
        // read records
        byte[] resultReadRecord = new byte[0];

        for (int iRecords = (int) rec1; iRecords <= (int) recL; iRecords++) {
            //System.out.println("** for loop start " + (int) rec1 + " to " + (int) recL + " iRecords: " + iRecords);

            //System.out.println("*#* readRecord iRecords: " + iRecords);
            byte[] cmd = hexToBytes("00B2000400");
            cmd[2] = (byte) (iRecords & 0x0FF);
            cmd[3] |= (byte) (sfiNew & 0x0FF);
            //writeToUiAppend(etLog, "");
            //writeToUiAppend(etLog, "read command length: " + cmd.length + " data: " + bytesToHex(cmd));

            try {
                resultReadRecord = nfc.transceive(cmd);
                //writeToUiAppend(etLog, "readRecordCommand length: " + cmd.length + " data: " + bytesToHex(cmd));
                byte[] resultReadRecordOk = checkResponse(resultReadRecord);
                if (resultReadRecordOk != null) {
                    //writeToUiAppend(etLog, "data from AFL " + bytesToHex(tag94BytesListEntry)); // given wrong output for second or third files in multiple records
                    writeToUiAppend(etLog, "data from AFL was: " + bytesToHex(tag94BytesListEntry));
                    writeToUiAppend(etLog, "data from AFL " + "SFI: " + String.format("%02X", sfiOrg) + " REC: " + String.format("%02d", iRecords));

                    byte sfiFile = (byte) (cmd[3] >>> 3);

                    writeToUiAppend(etLog, "data from AFL " + "SFI: " + String.format("%02X", sfiFile) + " REC: " + String.format("%02d", iRecords));
                    writeToUiAppend(etLog, "read result length: " + resultReadRecordOk.length + " data: " + bytesToHex(resultReadRecordOk));
                    // pretty print of response
                    if (isPrettyPrintResponse) {
                        prettyPrintData(etLog, resultReadRecordOk);
                    }
                    // this is the shortened one
                    try {
                        BerTlvs tlvsAfl = parser.parse(resultReadRecordOk);
                        // todo there could be a 57 Track 2 Equivalent Data field as well
                        // 5a = Application Primary Account Number (PAN)
                        // 5F34 = Application Primary Account Number (PAN) Sequence Number
                        // 5F25  = Application Effective Date (card valid from)
                        // 5F24 = Application Expiration Date
                        BerTlv tag5a = tlvsAfl.find(new BerTag(0x5a));
                        if (tag5a != null) {
                            byte[] tag5aBytes = tag5a.getBytesValue();
                            pan = removeTrailingF(bytesToHex(tag5aBytes));
                            Log.e(TAG, "found tag 0x5A PAN: " + pan);
                        }
                        BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                        if (tag5f24 != null) {
                            byte[] tag5f24Bytes = tag5f24.getBytesValue();
                            expirationDate = bytesToHex(tag5f24Bytes);
                        } else {
                            // System.out.println("record: " + iRecords + " Tag 5F24 not found");
                        }

                        findTag0x8c(tlvsAfl);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        //System.out.println("ERROR: ArrayOutOfBoundsException: " + e.getMessage());
                    }
                } else {
                    //writeToUiAppend(etLog, "ERROR: read record failed, result: " + bytesToHex(resultReadRecord));
                    resultReadRecord = new byte[0];
                }
            } catch (IOException e) {
                //throw new RuntimeException(e);
                //return "";
            }
        }

    }

 */


    /**
     * reads a single file (sector) of an EMV card
     * source: https://stackoverflow.com/a/38999989/8166854 answered Aug 17, 2016
     * by Michael Roland
     *
     * @param nfc
     * @param sector is the real place, not the data from AFL
     * @param record
     * @return
     */
    private static byte[] readFileSectorFormat(IsoDep nfc, int sector, int record) {
        byte[] cmd = new byte[]{(byte) 0x00, (byte) 0xB2, (byte) 0x00, (byte) 0x04, (byte) 0x00};
        // calculate byte 3 = cmd[3] |= (byte) ((sfi << 3) & 0x0F8);
        //byte cmd3 = (byte) ((sfi << 3) & 0x0F8);
        cmd[2] = (byte) (record & 0x0FF);
        cmd[3] |= (byte) ((sector << 3) & 0x0F8);
        //cmd[3] |= cmd3;
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            // do nothing
        }
        return checkResponse(result);
    }

    private static int convertSfiToSector(int sfi) {
        byte sector = (byte) ((sfi << 3) & 0x0F8);
        return sector;
    }

    private static String convertSfiRecordToAfl(int sfi, int record) {
        byte sfiOld = (byte) (sfi & 0xFF);
        byte sfiAfl = (byte) ((sfiOld << 3) & 0x0F8);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X", sfiAfl));
        sb.append(String.format("%02X", record));
        return sb.toString();
    }

    private static String convertSfiToSfiAfl(int sfi) {
        byte sfiOld = (byte) (sfi & 0xFF);
        byte sfiAfl = (byte) ((sfiOld << 3) & 0x0F8);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X", sfiAfl));
        return sb.toString();
    }


    /**
     * section for getting the tags from read responses
     */

    private static byte[] getTagValueFromList(@NonNull List<TagSet> tagSetList, @NonNull byte[] tag) {
        int size = tagSetList.size();
        for (int i = 0; i < size; i++) {
            TagSet tagSet = tagSetList.get(i);
            if (Arrays.equals(tag, tagSet.getTag())) {
                return tagSet.getTagValue();
            }
        }
        return null;
    }

    private static List<TagSet> getTagSetFromResponse(@NonNull byte[] data, @NonNull String tagsFound) {
        List<TagSet> tagsSet = new ArrayList<>();
        List<TagNameValue> parsedTags = TagListParser.parseRespond(data);
        int parsedTagsSize = parsedTags.size();
        for (int i = 0; i < parsedTagsSize; i++) {
            TagNameValue parsedTag = parsedTags.get(i);
            byte[] eTag = parsedTag.getTagBytes();
            String eTagName = parsedTag.getTagName();
            byte[] eTagValue = parsedTag.getTagValueBytes();
            String eTagValueType = parsedTag.getTagValueType();
            TagSet tagSet = new TagSet(eTag, eTagName, eTagValue, eTagValueType, tagsFound);
            tagsSet.add(tagSet);
        }
        return tagsSet;
    }

    /**
     * general methods
     */

    // https://stackoverflow.com/a/51338700/8166854
    private static byte[] selectApdu(byte[] aid) {
        byte[] commandApdu = new byte[6 + aid.length];
        commandApdu[0] = (byte) 0x00;  // CLA
        commandApdu[1] = (byte) 0xA4;  // INS
        commandApdu[2] = (byte) 0x04;  // P1
        commandApdu[3] = (byte) 0x00;  // P2
        commandApdu[4] = (byte) (aid.length & 0x0FF);       // Lc
        System.arraycopy(aid, 0, commandApdu, 5, aid.length);
        commandApdu[commandApdu.length - 1] = (byte) 0x00;  // Le
        return commandApdu;
    }

    private static boolean responseNotAllowed(byte[] data) {
        byte[] RESULT_FAILUE = hexToBytes("6a82");
        if (Arrays.equals(data, RESULT_FAILUE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * checks if the response is longer than 3 bytes and ends with 0x9000
     * @param data
     * @return the data without 0x9000 at the end
     *         null if response is not OK
     */
    private static byte[] checkResponse(byte[] data) {
        //if (data.length < 5) return null; // not ok
        if (data.length < 5) {
            //System.out.println("checkResponse: data length " + data.length);
            return null;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x9000) {
            //System.out.println("status: " + status);
            return null;
        } else {
            //System.out.println("will return: " + bytesToHex(Arrays.copyOfRange(data, 0, data.length - 2)));
            return Arrays.copyOfRange(data, 0, data.length - 2);
        }
    }
}
