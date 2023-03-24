package de.androidcrypto.nfcemvexample;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;
import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHexNpe;
import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;
import static de.androidcrypto.nfcemvexample.BinaryUtils.intToByteArrayV4;

import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import org.apache.commons.lang3.ArrayUtils;

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
import de.androidcrypto.nfcemvexample.nfccreditcards.DolValues;
import de.androidcrypto.nfcemvexample.nfccreditcards.ModuleInfo;
import de.androidcrypto.nfcemvexample.sasc.ApplicationInterchangeProfile;

public class EmvModules {

    /**
     * This class holds the modules for reading an EMV/Payment/CreditCard:
     * moduleSelectPpse
     * moduleSelectAid
     *
     */

    /**
     * module for selecting PPSE
     *
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
                mi = new ModuleInfo(command, responseOk, true, tsList, dataList, responseString);
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
     *
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
                    mi = new ModuleInfo(command, responseOk, true, tsList, dataList, responseString);
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
     *
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
    public static ModuleInfo checkForAflInGpoResponse(byte[] gpoResponse) {
        ModuleInfo mi;
        List<TagSet> tsLst = new ArrayList<>();
        List<byte[]> aflList = new ArrayList<>();
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvs = parser.parse(gpoResponse);
        // search for tag 0x94 Application File Locator (AFL) in tag 77 Response Message Template Format 2
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
            mi = new ModuleInfo(null, gpoResponse, true, null, aflList, "Template 2");
            return mi;
        } else {
            // return an empty AFL list
        }
        // search for tag 0x80 = Response Message Template Format 1
        BerTlv tag80 = tlvs.find(new BerTag(0x80));
        if (tag80 != null) {
            byte[] dataTemp = tag80.getBytesValue();
            // first 2 bytes are AIP, followed by xx AFL bytes
            byte[] tag82_AIP = ArrayUtils.subarray(dataTemp, 0, 2);
            byte[] tag94_AFL = ArrayUtils.subarray(dataTemp, 2, dataTemp.length);
            if (tag94_AFL != null) {
                TagSet ts82 = new TagSet(new byte[]{(byte) 0x82}, "Application Interchange Profile (AIP)", tag82_AIP, TagValueTypeEnum.BINARY.toString(), "getProcessingOptions Template 1");
                tsLst.add(ts82);
                TagSet ts94 = new TagSet(new byte[]{(byte) 0x94}, "Application File Locator (AFL)", tag94_AFL, TagValueTypeEnum.BINARY.toString(), "getProcessingOptions Template 1");
                tsLst.add(ts94);
                List<byte[]> tag94BytesList = divideArray(tag94_AFL, 4);
                aflList.addAll(tag94BytesList);
                mi = new ModuleInfo(null, gpoResponse, true, tsLst, aflList, "Template 1");
                return mi;
            }
        }
        return null;
    }

    /**
     * checks that a tag 0x94 Application File Locator (AFL) is available in gpoResponse
     *
     * @param gpoResponse
     * @return the list with afl entries (each of 4 byte)
     * if no afl was found it returns an empty list
     */
    public static List<byte[]> checkForAflInGpoResponseOrg(byte[] gpoResponse) {
        List<byte[]> aflList = new ArrayList<>();
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvs = parser.parse(gpoResponse);
        // search for tag 0x94 Application File Locator (AFL) in tag 77 Response Message Template Format 2
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
     *
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

/*
offline authorization
EMV Book 4.1 Book 3 page 78:
The AFL determines the files and records to be used for processing a transaction. The use of the AFL is described in section 10.2.
The data objects listed in Table 25 are used by the offline data authentication algorithm and, when present,
should be located in the first record referenced by the AFL.4
Tag '8F' Value Certification Authority Public Key Index
Tag '90' Value Issuer Public Key Certificate
Table 25: Data Objects Used by the Offline Data Authentication Algorithm

page 95:
The fourth byte indicates the number of records involved in offline data authentication starting with the record number coded
in the second byte. The fourth byte may range from zero to the value of the third byte less the value of the second byte plus 1.

 */

    /**
     * reads a single entry from an AFL entry (just one record)
     *
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
     * section for single read commands
     * overview: https://github.com/sasc999/javaemvreader/blob/master/src/main/java/sasc/emv/EMVAPDUCommands.java
     */

    /**
     * reads the single data elements "application transaction counter", "left pin try counter",
     * "last online ATC register" and "logFormat"
     * @param nfc
     * @return
     */
    public static ModuleInfo readSingleDataElements(IsoDep nfc) {
        ModuleInfo mi;
        boolean result = false;
        List<TagSet> tsLst = new ArrayList<>();
        List<byte[]> dataLst = new ArrayList<>();
        ModuleInfo miAtc = getApplicationTransactionCounter(nfc);
        String atcString = "00000";
        ModuleInfo miLeftPinTryCounter = getLeftPinTryCounterCounter(nfc);
        ModuleInfo miLastOnlineAtcRegister = getLastOnlineATCRegister(nfc);
        ModuleInfo miLogFormat = getLogFormat(nfc);
        if (miAtc.isSuccess()) {
            tsLst.addAll(miAtc.getTsList());
            dataLst.addAll(miAtc.getDataList());
            result = true;
            atcString = miAtc.getPrettyPrint();
        }
        if (miLeftPinTryCounter.isSuccess()) {
            tsLst.addAll(miLeftPinTryCounter.getTsList());
            dataLst.addAll(miLeftPinTryCounter.getDataList());
            result = true;
        }
        if (miLastOnlineAtcRegister.isSuccess()) {
            tsLst.addAll(miLastOnlineAtcRegister.getTsList());
            dataLst.addAll(miLastOnlineAtcRegister.getDataList());
            result = true;
        }
        if (miLogFormat.isSuccess()) {
            tsLst.addAll(miLogFormat.getTsList());
            dataLst.addAll(miLogFormat.getDataList());
            result = true;
        }
        mi = new ModuleInfo(new byte[0], new byte[0], result, tsLst, dataLst, atcString);
        return mi;
    }

    // Get the data of ATC(Application Transaction Counter, tag '9F36')), template 77 or 80
    private static ModuleInfo getApplicationTransactionCounter(IsoDep nfc) {
        ModuleInfo mi;
        // we do need empty lists because we need a complete list after all reads
        TagSet tsEmpty = new TagSet(new byte[]{(byte) 0x00}, "empty", new byte[0], TagValueTypeEnum.TEXT.toString(), "read single data elements");
        byte[] dsEmpty = new byte[0];
        List<TagSet> tsLstEmpty = new ArrayList<>();
        tsLstEmpty.add(tsEmpty);
        List<byte[]> dataLstEmpty = new ArrayList<>();
        dataLstEmpty.add(dsEmpty);

        List<TagSet> tsLst = new ArrayList<>();
        List<byte[]> dataLst = new ArrayList<>();
        byte[] command = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x36, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(command);
        } catch (IOException e) {
            //System.out.println("* getApplicationTransactionCounter failed");
            mi = new ModuleInfo(command, null, false, tsLstEmpty, dataLstEmpty, null);
            return mi;
        }
        // e.g. visa returns 9f360200459000
        // e.g. visa returns 9f36020045 9000
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            mi = new ModuleInfo(command, result, false, tsLstEmpty, dataLstEmpty, null);
            return mi;
        } else {
            byte[] tagValue = getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x36);
            TagSet ts = new TagSet(new byte[]{(byte) 0x9F, (byte) 0x36}, "application transaction counter (ATC)", tagValue, TagValueTypeEnum.BINARY.toString(), "read single data elements");
            tsLst.add(ts);
            dataLst.add(resultOk);
            mi = new ModuleInfo(command, result, true, tsLst, dataLst, String.valueOf(BinaryUtils.intFromByteArrayV4(tagValue)));
            return mi;
        }
    }

    private static ModuleInfo getLeftPinTryCounterCounter(IsoDep nfc) {
        ModuleInfo mi;
        // we do need empty lists because we need a complete list after all reads
        TagSet tsEmpty = new TagSet(new byte[]{(byte) 0x00}, "empty", new byte[0], TagValueTypeEnum.TEXT.toString(), "read single data elements");
        byte[] dsEmpty = new byte[0];
        List<TagSet> tsLstEmpty = new ArrayList<>();
        tsLstEmpty.add(tsEmpty);
        List<byte[]> dataLstEmpty = new ArrayList<>();
        dataLstEmpty.add(dsEmpty);

        List<TagSet> tsLst = new ArrayList<>();
        List<byte[]> dataLst = new ArrayList<>();
        byte[] command = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x17, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(command);
        } catch (IOException e) {
            //System.out.println("* getApplicationTransactionCounter failed");
            mi = new ModuleInfo(command, null, false, tsLstEmpty, dataLstEmpty, null);
            return mi;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            mi = new ModuleInfo(command, result, false, tsLstEmpty, dataLstEmpty, null);
            return mi;
        } else {
            byte[] tagValue = getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x17);
            TagSet ts = new TagSet(new byte[]{(byte) 0x9F, (byte) 0x17}, "left PIN try counter", tagValue, TagValueTypeEnum.BINARY.toString(), "read single data elements");
            tsLst.add(ts);
            dataLst.add(resultOk);
            mi = new ModuleInfo(command, result, true, tsLst, dataLst, String.valueOf(BinaryUtils.intFromByteArrayV4(tagValue)));
            return mi;
        }
    }

    private static ModuleInfo getLastOnlineATCRegister(IsoDep nfc) {
        ModuleInfo mi;
        // we do need empty lists because we need a complete list after all reads
        TagSet tsEmpty = new TagSet(new byte[]{(byte) 0x00}, "empty", new byte[0], TagValueTypeEnum.TEXT.toString(), "read single data elements");
        byte[] dsEmpty = new byte[0];
        List<TagSet> tsLstEmpty = new ArrayList<>();
        tsLstEmpty.add(tsEmpty);
        List<byte[]> dataLstEmpty = new ArrayList<>();
        dataLstEmpty.add(dsEmpty);

        List<TagSet> tsLst = new ArrayList<>();
        List<byte[]> dataLst = new ArrayList<>();
        byte[] command = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x13, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(command);
        } catch (IOException e) {
            //System.out.println("* getApplicationTransactionCounter failed");
            mi = new ModuleInfo(command, null, false, tsLstEmpty, dataLstEmpty, null);
            return mi;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            mi = new ModuleInfo(command, result, false, tsLstEmpty, dataLstEmpty, null);
            return mi;
        } else {
            byte[] tagValue = getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x13);
            TagSet ts = new TagSet(new byte[]{(byte) 0x9F, (byte) 0x13}, "last online ATC register", tagValue, TagValueTypeEnum.BINARY.toString(), "read single data elements");
            tsLst.add(ts);
            dataLst.add(resultOk);
            mi = new ModuleInfo(command, result, true, tsLst, dataLst, String.valueOf(BinaryUtils.intFromByteArrayV4(tagValue)));
            return mi;
        }
    }

    private static ModuleInfo getLogFormat(IsoDep nfc) {
        ModuleInfo mi;
        // we do need empty lists because we need a complete list after all reads
        TagSet tsEmpty = new TagSet(new byte[]{(byte) 0x00}, "empty", new byte[0], TagValueTypeEnum.TEXT.toString(), "read single data elements");
        byte[] dsEmpty = new byte[0];
        List<TagSet> tsLstEmpty = new ArrayList<>();
        tsLstEmpty.add(tsEmpty);
        List<byte[]> dataLstEmpty = new ArrayList<>();
        dataLstEmpty.add(dsEmpty);

        List<TagSet> tsLst = new ArrayList<>();
        List<byte[]> dataLst = new ArrayList<>();
        byte[] command = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x4F, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(command);
        } catch (IOException e) {
            //System.out.println("* getApplicationTransactionCounter failed");
            mi = new ModuleInfo(command, null, false, tsLstEmpty, dataLstEmpty, null);
            return mi;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            mi = new ModuleInfo(command, result, false, tsLstEmpty, dataLstEmpty, null);
            return mi;
        } else {
            byte[] tagValue = getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x4f);
            TagSet ts = new TagSet(new byte[]{(byte) 0x9F, (byte) 0x4F}, "log format", tagValue, TagValueTypeEnum.BINARY.toString(), "read single data elements");
            tsLst.add(ts);
            dataLst.add(resultOk);
            mi = new ModuleInfo(command, result, true, tsLst, dataLst, bytesToHex(tagValue));
            return mi;
        }
    }

    private static byte[] getApplicationTransactionCounterOrg(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x36, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            //System.out.println("* getApplicationTransactionCounter failed");
            return null;
        }
        // e.g. visa returns 9f360200459000
        // e.g. visa returns 9f36020045 9000
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            return null;
        } else {
            return getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x36);
        }
    }

    private static byte[] getPinTryCounterOrg(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x17, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* getPinTryCounterCounter failed");
            return null;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            return null;
        } else {
            return getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x17);
        }
    }

    private static byte[] getLastOnlineATCRegisterOrg(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x13, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* getLastOnlineATCRegister failed");
            return null;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            return null;
        } else {
            return getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x13);
        }
    }

    private static byte[] getLogFormatOrg(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x4F, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* getLastOnlineATCRegister failed");
            return null;
        }
        byte[] resultOk = checkResponse(result);
        if (resultOk == null) {
            return null;
        } else {
            return getTagValueFromResult(resultOk, (byte) 0x9f, (byte) 0x4F);
        }
    }

    /**
     * section for internal authentication
     */

    public static ModuleInfo getInternalAuthentication(IsoDep nfc, @NonNull byte[] unpredictionalNumber4Bytes) {
        ModuleInfo mi;
        byte[] command;
        byte[] response = new byte[0];
        byte[] responseOk = null;
        // check for correct length of data
        if (unpredictionalNumber4Bytes.length != 4) {
            return null;
        }

        String internalAuthHeader = "00880000";
        String randomNumberLength = "04";
        String randomNumber = "01020304";
        String internalAuthTrailer = "00";
        String internalAuthString = internalAuthHeader + randomNumberLength + randomNumber + internalAuthTrailer;
        command = hexToBytes(internalAuthString);
        try {
            response = nfc.transceive(command);
        } catch (IOException e) {
            mi = new ModuleInfo(command, null, false, null, null, null);
            return mi;
        }
        if (response != null) {
            System.out.println("**** internalAuthResponse: " + bytesToHexNpe(response));
            responseOk = checkResponse(response);
            if (responseOk != null) {
                String responseString = TlvUtil.prettyPrintAPDUResponse(responseOk);
                List<TagSet> tsList = new ArrayList<>();
                tsList.addAll(getTagSetFromResponse(responseOk, "getInternalAuthentication"));
                mi = new ModuleInfo(command, responseOk, true, tsList, null, responseString);
                return mi;
            } else {
                mi = new ModuleInfo(command, response, false, null, null, null);
                return mi;
            }
        } else {
            return null;
        }

    }

    /**
     * section for get application cryptogram
     */

    /**
     * get application cryptogram
     * @param nfc
     * @param tag8cCdol1 - the tag 0x8c = CDOL1 can be found while reading any response
     * @return ModuleInfo with the application cryptogram
     */
    public static ModuleInfo getApplicationCryptogram (@NonNull IsoDep nfc, byte[] tag8cCdol1) {
        ModuleInfo mi;
        byte[] command;
        byte[] response = new byte[0];
        byte[] responseOk = null;
        if (tag8cCdol1.length > 1) {
            command = getAppCryptoCommandFromCdol(tag8cCdol1);
            try {
                response = nfc.transceive(command);
            } catch (IOException e) {
                mi = new ModuleInfo(command, null, false, null, null, null);
                return mi;
            }
            if (response != null) {
                responseOk = checkResponse(response);
                if (responseOk != null) {
                    List<TagSet> tsList = new ArrayList<>();
                    BerTlvParser parser = new BerTlvParser();
                    BerTlvs tlvs = parser.parse(responseOk);
                    // search for tag 0x9f26 Application Cryptogram in tag 77 Response Message Template Format 2
                    BerTlv tag94 = tlvs.find(new BerTag(0x9f, (byte) 0x26));
                    if (tag94 != null) {
                        String responseString = TlvUtil.prettyPrintAPDUResponse(responseOk);
                        tsList.addAll(getTagSetFromResponse(responseOk, "getApplicationCryptogram"));
                        mi = new ModuleInfo(command, responseOk, true, tsList, null, responseString);
                        return mi;
                    }
                    // search for tag 0x80 = Response Message Template Format 1
                    BerTlv tag80 = tlvs.find(new BerTag(0x80));
                    if (tag80 != null) {
                        return dumpApplicationCryptoResponseMessageTemplate1(command, responseOk);
                    }
                } else {
                    mi = new ModuleInfo(command, response, false, null, null, null);
                    return mi;          }
            } else {
                mi = new ModuleInfo(command, response, false, null, null, null);
                return mi;
            }
        } else {
            // no cdol1 found
            return null;
        }
        return null;
    }

    /**
     * takes the CDOL1 list from any response (file reading) and returns the getApplicationCrypto command
     *
     * @param cdol
     * @return
     */
    private static byte[] getAppCryptoCommandFromCdol(@NonNull byte[] cdol) {
        // get the tags in a list
        List<TagAndLength> tagAndLength = TlvUtil.parseTagAndLength(cdol);
        int tagAndLengthSize = tagAndLength.size();
        if (tagAndLengthSize < 1) {
            // there are no cdols in the list
            // returning an empty cdolCommand
            String constructedGetAcCommandString = "80AE8000" + "00" + "" + "00";
            return hexToBytes(constructedGetAcCommandString);
        }
        int valueOfTagSum = 0; // total length
        StringBuilder sb = new StringBuilder(); // takes the default values of the tags
        DolValues dolValues = new DolValues();
        for (int i = 0; i < tagAndLengthSize; i++) {
            // get a single tag
            TagAndLength tal = tagAndLength.get(i); // eg 9F0206
            byte[] tagToSearch = tal.getTag().getTagBytes(); // gives the tag 9F02
            int lengthOfTag = tal.getLength(); // 2
            valueOfTagSum += tal.getLength(); // add it to the sum
            // now we are trying to find a default value
            byte[] defaultValue = dolValues.getDolValue(tagToSearch);
            byte[] usedValue = new byte[0];
            if (defaultValue != null) {
                if (defaultValue.length > lengthOfTag) {
                    // cut it to correct length
                    usedValue = Arrays.copyOfRange(defaultValue, 0, lengthOfTag);
                } else if (defaultValue.length < lengthOfTag) {
                    // increase length
                    usedValue = new byte[lengthOfTag];
                    System.arraycopy(defaultValue, 0, usedValue, 0, defaultValue.length);
                } else {
                    // correct length
                    usedValue = defaultValue.clone();
                }
            } else {
                // defaultValue is null means the tag was not found in our tags database for default values
                usedValue = new byte[lengthOfTag];
            }
            // now usedValue does have the correct length
            sb.append(bytesToHex(usedValue));
        }
        String constructedGetAcString = sb.toString();
        String tagLength2d = bytesToHex(intToByteArrayV4(valueOfTagSum)); // length value
        String constructedGetAcCommandString = "80AE8000" + tagLength2d + constructedGetAcString + "00";
        return hexToBytes(constructedGetAcCommandString);
    }

    /**
     * dumps the data from getApplicationCryptogram if response
     * is tag 0x80 Response Message Template Format 1
     * @param applicationCryptoResponseOk
     * @return a dump string
     */
    public static ModuleInfo dumpApplicationCryptoResponseMessageTemplate1(byte[] applicationCryptoCommand,
            byte[] applicationCryptoResponseOk) {
        ModuleInfo mi;
        List<TagSet> tsLst = new ArrayList<>();
        // check that response is a 0x80 Response Message Template Format 1
        byte[] respHeader = Arrays.copyOfRange(applicationCryptoResponseOk, 0, 2);
        if (Arrays.equals(respHeader, new byte[]{(byte) 0x80, (byte) 0x12})) {
            byte[] resp9F27 = Arrays.copyOfRange(applicationCryptoResponseOk, 2, 3);
            byte[] resp9F36 = Arrays.copyOfRange(applicationCryptoResponseOk, 3, 5);
            byte[] resp9F26 = Arrays.copyOfRange(applicationCryptoResponseOk, 5, 13);
            byte[] resp9F10 = Arrays.copyOfRange(applicationCryptoResponseOk, 14, 21);
            TagSet ts9F27 = new TagSet(new byte[]{(byte) 0x9f, (byte) 0x27}, "Cryptogram Information Data (CID)", resp9F27, TagValueTypeEnum.BINARY.toString(), "getApplicationCryptogram Template 1");
            tsLst.add(ts9F27);
            TagSet ts9F36 = new TagSet(new byte[]{(byte) 0x9f, (byte) 0x6}, "Application Transaction Counter (ATC)", resp9F36, TagValueTypeEnum.BINARY.toString(), "getApplicationCryptogram Template 1");
            tsLst.add(ts9F36);
            TagSet ts9F26 = new TagSet(new byte[]{(byte) 0x9f, (byte) 0x26}, "Application Cryptogram", resp9F26, TagValueTypeEnum.BINARY.toString(), "getApplicationCryptogram Template 1");
            tsLst.add(ts9F26);
            TagSet ts9F10 = new TagSet(new byte[]{(byte) 0x9f, (byte) 0x10}, "Issuer Application Data (IAD)", resp9F10, TagValueTypeEnum.BINARY.toString(), "getApplicationCryptogram Template 1");
            tsLst.add(ts9F10);

            StringBuilder sb = new StringBuilder();
            sb.append("80 12 -- Response Message Template Format 1").append("\n");
            sb.append("- tag 0x9F27 length 01\n  Cryptogram Information Data (CID)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F27)).append("\n");
            sb.append("- tag 0x9F36 length 02\n  Appl. Transaction Counter (ATC)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F36)).append("\n");
            sb.append("- tag 0x9F26 length 08\n  Application Cryptogram\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F26)).append("\n");
            sb.append("- tag 0x9F10 length 07\n  Issuer Application Data (IAD)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F10)).append("\n");
            mi = new ModuleInfo(applicationCryptoCommand, applicationCryptoResponseOk, true, tsLst, null, sb.toString());
            return mi;
        } else {
            return null;
        }
        /*
                                    https://stackoverflow.com/a/35892602/8166854
                                    if response is tag 0x80 Response Message Template Format 1
                                    - x9F27:  # EMV, Cryptogram Information Data (CID)
                                        val: "80" # Cryptogram Information Data (CID).
                                        # 10______ - bits 8-7, ARQC
                                        # _____000 - bits 3-1 (Reason/Advice/Referral Code), No information given
                                    + x9F36: "0001" # EMV, Application Transaction Counter (ATC)
                                    + x9F26: "0102030405060708" # EMV, Cryptogram, Application
                                    + x9F10: "06010A03A40000" # EMV, Issuer Application Data (IAD)
                                    8012
                                        80
                                          000e
                                              03ab88079529a75c
                                                              06590203a00000
                                     */
    }

    /**
     * dumps the data from getApplicationCryptogram if response
     * is tag 0x80 Response Message Template Format 1
     * @param applicationCryptoResponseOk
     * @return a dump string
     */
    public static String dumpApplicationCryptoResponseMessageTemplate1Org(
            byte[] applicationCryptoResponseOk) {
        // check that response is a 0x80 Response Message Template Format 1
        byte[] respHeader = Arrays.copyOfRange(applicationCryptoResponseOk, 0, 2);
        if (Arrays.equals(respHeader, new byte[]{(byte) 0x80, (byte) 0x12})) {
            byte[] resp9F27 = Arrays.copyOfRange(applicationCryptoResponseOk, 2, 3);
            byte[] resp9F36 = Arrays.copyOfRange(applicationCryptoResponseOk, 3, 5);
            byte[] resp9F26 = Arrays.copyOfRange(applicationCryptoResponseOk, 5, 13);
            byte[] resp9F10 = Arrays.copyOfRange(applicationCryptoResponseOk, 14, 21);
            StringBuilder sb = new StringBuilder();
            sb.append("80 12 -- Response Message Template Format 1").append("\n");
            sb.append("- tag 0x9F27 length 01\n  Cryptogram Information Data (CID)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F27)).append("\n");
            sb.append("- tag 0x9F36 length 02\n  Appl. Transaction Counter (ATC)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F36)).append("\n");
            sb.append("- tag 0x9F26 length 08\n  Application Cryptogram\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F26)).append("\n");
            sb.append("- tag 0x9F10 length 07\n  Issuer Application Data (IAD)\n  - ").append(BinaryUtils.bytesToHexBlank(resp9F10)).append("\n");
            return sb.toString();
        } else {
            return "";
        }
        /*
                                    https://stackoverflow.com/a/35892602/8166854
                                    if response is tag 0x80 Response Message Template Format 1
                                    - x9F27:  # EMV, Cryptogram Information Data (CID)
                                        val: "80" # Cryptogram Information Data (CID).
                                        # 10______ - bits 8-7, ARQC
                                        # _____000 - bits 3-1 (Reason/Advice/Referral Code), No information given
                                    + x9F36: "0001" # EMV, Application Transaction Counter (ATC)
                                    + x9F26: "0102030405060708" # EMV, Cryptogram, Application
                                    + x9F10: "06010A03A40000" # EMV, Issuer Application Data (IAD)

                                    8012
                                        80
                                          000e
                                              03ab88079529a75c
                                                              06590203a00000
                                     */
    }

    public static String dumpAip(byte[] data) {
        if (data == null) return "";
        if (data.length != 2) return "";
        return new ApplicationInterchangeProfile(data[0], data[1]).toString();
    }

    /**
     * section for getting the tags from read responses
     */

    public static byte[] getTagValueFromList(@NonNull List<TagSet> tagSetList, @NonNull byte[] tag) {
        int size = tagSetList.size();
        for (int i = 0; i < size; i++) {
            TagSet tagSet = tagSetList.get(i);
            if (Arrays.equals(tag, tagSet.getTag())) {
                return tagSet.getTagValue();
            }
        }
        return null;
    }

    /**
     * gets the byte value of a tag from transceive response
     *
     * @param data
     * @param search
     * @return
     */
    private static byte[] getTagValueFromResult(byte[] data, byte... search) {
        int argumentsLength = search.length;
        if (argumentsLength < 1) return null;
        if (argumentsLength > 2) return null;
        if (data.length > 253) return null;
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvDatas = parser.parse(data);
        BerTlv tag;
        if (argumentsLength == 1) {
            tag = tlvDatas.find(new BerTag(search[0]));
        } else {
            tag = tlvDatas.find(new BerTag(search[0], search[1]));
        }
        byte[] tagBytes;
        if (tag == null) {
            return null;
        } else {
            return tag.getBytesValue();
        }
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
     * remove all trailing 0xF's trailing in the 16 bytes length field tag 0x5a = PAN
     * PAN is padded with 'F'
     *
     * @param input
     * @return
     */
    public static String removeTrailingF(String input) {
        int index;
        for (index = input.length() - 1; index >= 0; index--) {
            if (input.charAt(index) != 'f') {
                break;
            }
        }
        return input.substring(0, index + 1);
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
     *
     * @param data
     * @return the data without 0x9000 at the end
     * null if response is not OK
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
