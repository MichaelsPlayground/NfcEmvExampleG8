package de.androidcrypto.nfcemvexample.extended;

import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.exception.TlvException;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.Service;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.TrackUtils;

import net.sf.scuba.tlv.TLVInputStream;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import fr.devnied.bitlib.BytesUtils;

/**
 * This class parses the respond of a command to an EMV card
 * The class will return a List of tags that can easily printed
 * or searched
 */


public class TagListParser {

    private static List<TagNameValue> tagListData = new ArrayList<TagNameValue>();
    private static String tagListDump = "";

    public static String getTagListDump() {
        return tagListDump;
    }

    public static List<TagNameValue> parseRespond (byte[] respond) {
        tagListData = new ArrayList<TagNameValue>();
        tagListDump = "";

        tagListDump = parseAndPrintApduRespond(respond, tagListData);

        return tagListData;
    }

    private static String parseAndPrintApduRespond(byte[] apduResponse, List<TagNameValue> tagList) {
        String output = "";
        tagApduResponse(apduResponse, 0, tagList);
        int tagListSize = tagList.size();
        output += "\n" + "== tagListSize: " + tagListSize;
        for (int i = 0; i < tagListSize; i++) {
            TagNameValue tag = tagList.get(i);
            output += "\n" + "== tagNameValue " + i + "\n" +
                    printTagNameValue(tag);
        }
        return output;
    }

    private static void tagApduResponse(final byte[] data, final int indentLength, List<TagNameValue> tagList) {
        TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(data));
        try {
            while (stream.available() > 0) {
                if (stream.available() == 2) {
                    stream.mark(0);
                    byte[] value = new byte[2];
                    try {
                        stream.read(value);
                    } catch (IOException e) {
                        System.out.println("IOException: " + e);
                    }
                    SwEnum sw = SwEnum.getSW(value);
                    if (sw != null) {
                        continue;
                    }
                    stream.reset();
                }
                TLV tlv = TlvUtil.getNextTLV(stream);

                if (tlv == null) {
                    System.out.println("ERROR: TLV format error");
                    break;
                }
                byte[] tagBytes = tlv.getTagBytes();
                byte[] lengthBytes = tlv.getRawEncodedLengthBytes();
                byte[] valueBytes = tlv.getValueBytes();
                ITag tag = tlv.getTag();
                TagNameValue tagNameValue = new TagNameValue();
                tagNameValue.setTagBytes(tlv.getTagBytes());
                tagNameValue.setTagName(tlv.getTag().getName());
                tagNameValue.setTagRawEncodedLengthBytes(tlv.getRawEncodedLengthBytes());
                tagNameValue.setTagValueBytes(tlv.getValueBytes());
                TagValueTypeEnum tagValueTypeEnum = tag.getTagValueType();
                tagNameValue.setTagValueType(tagValueTypeEnum.name());
                tagNameValue.setTagDescription(tag.getDescription());
                tagList.add(tagNameValue);

                int extraIndent = (lengthBytes.length + tagBytes.length) * 3;

                if (tag.isConstructed()) {
                    // Recursion
                    tagApduResponse(valueBytes, indentLength + extraIndent, tagList);
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e);
        } catch (TlvException exce) {
            System.out.println("ERROR TlvException: " + exce);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private static String printTagNameValue(TagNameValue tag) {
        String output = "";
        output = output + "tag: " + BytesUtils.bytesToString(tag.getTagBytes()) + "\n" +
                "tagname: " + tag.getTagName() + "\n" +
                "tag value length: " + BytesUtils.bytesToString(tag.getTagRawEncodedLengthBytes()) + "\n";
        String tagValueType = tag.getTagValueType();
        if (tagValueType == "TEXT") {
            output = output + "tag value bytes: " + BytesUtils.bytesToString(tag.getTagValueBytes()) +
                    " (= " + new String(tag.getTagValueBytes()) + ")\n";
        } else {
            output = output + "tag value bytes: " + BytesUtils.bytesToString(tag.getTagValueBytes()) + "\n";
        }
        output = output + "tag description: " + tag.getTagDescription() + "\n" +
                "tag value type: " + tagValueType + "\n";

        return output;
    }

    /**
     * this is the advanced version of printTableTags - if there is a TagValueType of TEXT the
     * output line is repeated with a byte array to string conversion
     *
     * @param tagList
     * @return
     */
    public static String printTableTagsText(List<TagNameValue> tagList) {
        StringBuilder buf = new StringBuilder();
        buf.append("Tag   Name                            Value\n");
        buf.append("--------------------------------------------------------------\n");
        int tagListSize = tagList.size();
        for (int i = 0; i < tagListSize; i++) {
            TagNameValue tag = tagList.get(i);
            boolean isTagValueTypeText = (tag.getTagValueType().equals(TagValueTypeEnum.TEXT.toString()));
            buf.append(rightpad(BytesUtils.bytesToString(tag.getTagBytes()), 5));
            buf.append(" ");
            buf.append(rightpad(tag.getTagName(), 31));
            buf.append(" ");
            buf.append(rightpad(BytesUtils.bytesToStringNoSpace(tag.getTagValueBytes(), false), 25));
            buf.append("\n");
            // if the type is TEXT repeat the line with byte to string converted data
            if (isTagValueTypeText) {
                buf.append(rightpad(BytesUtils.bytesToString(tag.getTagBytes()), 5));
                buf.append(" ");
                buf.append(rightpad(tag.getTagName(), 31));
                buf.append(" ");
                if (tag.getTagValueBytes() != null) {
                    buf.append(rightpad(new String(tag.getTagValueBytes()), 25));
                } else {
                    buf.append(rightpad("-empty-", 25));
                }
                buf.append("\n");
            }
        }
        return buf.toString();
    }

    public static String printTableTags(List<TagNameValue> tagList) {
        StringBuilder buf = new StringBuilder();
        buf.append("Tag   Name                            Value\n");
        buf.append("--------------------------------------------------------------------\n");
        int tagListSize = tagList.size();
        for (int i = 0; i < tagListSize; i++) {
            TagNameValue tag = tagList.get(i);
            buf.append(rightpad(BytesUtils.bytesToString(tag.getTagBytes()), 5));
            buf.append(" ");
            buf.append(rightpad(tag.getTagName(), 36));
            buf.append(" ");
            buf.append(rightpad(BytesUtils.bytesToStringNoSpace(tag.getTagValueBytes(), false), 25));
            buf.append("\n");
        }
        return buf.toString();
    }

    // This code will have exactly the given amount of characters; filled with spaces or truncated
    // on the left side
    // source: https://stackoverflow.com/a/38110257/8166854
    private static String leftpad(String text, int length) {
        return String.format("%" + length + "." + length + "s", text);
    }

    private static String rightpad(String text, int length) {
        return String.format("%-" + length + "." + length + "s", text);
    }

    /**
     * this method finds the TNV in TNV-List by the tag bytes
     */

    // find a tag in the tag list
    public static TagNameValue findTnv(byte[] tagBytes, List<TagNameValue> tnvs) {
        for (TagNameValue tnv : tnvs) {
            if (Arrays.equals(tnv.getTagBytes(), tagBytes)) {
            //if (tnv.getTagBytes().equals(tagBytes)) {
                return tnv;
            }
        }
        return null;
    }

    /**
     * This function builds a new tag for TNV
     * @param tagBytes
     * @param tagName
     * @param tvtEnum
     * @param tagValueBytes
     * @return
     */

    public static TagNameValue tagBuild(byte[] tagBytes, String tagName, TagValueTypeEnum tvtEnum, byte[] tagValueBytes) {
        TagNameValue tnv = new TagNameValue();
        tnv.setTagBytes(tagBytes);
        tnv.setTagName(tagName);
        tnv.setTagValueType(tvtEnum.toString());
        tnv.setTagValueBytes(tagValueBytes);
        return tnv;
    }

    /**
     * This function builds a new tag for TNV if the data is boolean
     * The value will be 0x00 for FALSE and 0x01 for TRUE
     * @param tagBytes
     * @param tagName
     * @param tvtEnum
     * @param valueBoolean
     * @return
     */

    public static TagNameValue tagBuildBoolean(byte[] tagBytes, String tagName, TagValueTypeEnum tvtEnum, boolean valueBoolean) {
        TagNameValue tnv = new TagNameValue();
        tnv.setTagBytes(tagBytes);
        tnv.setTagName(tagName);
        tnv.setTagValueType(tvtEnum.toString());
        if (valueBoolean) {
            tnv.setTagValueBytes(new byte[]{(byte) 0x01}); // true = 1
        } else {
            tnv.setTagValueBytes(new byte[]{(byte) 0x00}); // false = 0
        }
        return tnv;
    }

    public static List<TagNameValue> getTrack2EquivalentData(List<TagNameValue> tlvList, byte[] tagBytes) {
        List<TagNameValue> tagListNew = new ArrayList<>();
        TagNameValue emvTrack2EquivalentData = TagListParser.findTnv(tagBytes, tlvList);
        if (emvTrack2EquivalentData != null) {
            byte[] t2edByte = emvTrack2EquivalentData.getTagValueBytes();
            EmvTrack2 emvTrack2 = TrackUtils.extractTrack2EquivalentData(t2edByte);
            String cardNumber = emvTrack2.getCardNumber();
            Date expireDate = emvTrack2.getExpireDate();
            String expireDateString = DateUtils.getFormattedDateYyyy_Mm(expireDate);

            Service service = emvTrack2.getService();
            String service1Interchange = service.getServiceCode1().getInterchange();
            String service1Technology = service.getServiceCode1().getTechnology();
            String service2AuthorizationProcessing = service.getServiceCode2().getAuthorizationProcessing();
            String service3GetAllowedServices = service.getServiceCode3().getAllowedServices();
            String service3PinRequirements = service.getServiceCode3().getPinRequirements();

            // build new tags
            TagNameValue tnvNew = new TagNameValue();
            tnvNew = tagBuild(new byte[]{(byte) 0xff, 0x21}, "Track2 list raw data", TagValueTypeEnum.BINARY, t2edByte);
            tagListNew.add(tnvNew);
            // for credit cards the pan is allways even (8 bytes = 16 digits)
            // some other cards like German's girocard may get a checknumber at the end so the card number string is odd
            if (cardNumber.length() % 2 != 0) {
                cardNumber = cardNumber + "0"; // for odd card numbers
            }
            // some tag are doubled to show them under the known tag and the new tag
            tnvNew = tagBuild(new byte[]{(byte) 0x5a}, "Track2 PAN", TagValueTypeEnum.BINARY, BytesUtils.fromString(cardNumber));
            tagListNew.add(tnvNew);
            tnvNew = tagBuild(new byte[]{(byte) 0xff, 0x22}, "Track2 PAN", TagValueTypeEnum.BINARY, BytesUtils.fromString(cardNumber));
            tagListNew.add(tnvNew);
            // we should not write in tag 5f 24 as the format my differ to previous found date
            // tnvNew = tagBuild(new byte[]{(byte) 0x5f, 0x24}, "Track2 ExpireDate", TagValueTypeEnum.TEXT, expireDateString.getBytes(StandardCharsets.UTF_8));
            // tagListNew.add(tnvNew);
            tnvNew = tagBuild(new byte[]{(byte) 0xff, 0x23}, "Track2 ExpireDate", TagValueTypeEnum.TEXT, expireDateString.getBytes(StandardCharsets.UTF_8));
            tagListNew.add(tnvNew);
            tnvNew = tagBuild(new byte[]{(byte) 0xff, 0x24}, "Track2 Service1 Interchange", TagValueTypeEnum.TEXT, service1Interchange.getBytes(StandardCharsets.UTF_8));
            tagListNew.add(tnvNew);
            tnvNew = tagBuild(new byte[]{(byte) 0xff, 0x25}, "Track2 Service1 Technology", TagValueTypeEnum.TEXT, service1Technology.getBytes(StandardCharsets.UTF_8));
            tagListNew.add(tnvNew);
            tnvNew = tagBuild(new byte[]{(byte) 0xff, 0x26}, "Track2 Service2 AuthorizationProcessing", TagValueTypeEnum.TEXT, service2AuthorizationProcessing.getBytes(StandardCharsets.UTF_8));
            tagListNew.add(tnvNew);
            tnvNew = tagBuild(new byte[]{(byte) 0xff, 0x27}, "Track2 Service3 GetAllowedServices", TagValueTypeEnum.TEXT, service3GetAllowedServices.getBytes(StandardCharsets.UTF_8));
            tagListNew.add(tnvNew);
            tnvNew = tagBuild(new byte[]{(byte) 0xff, 0x28}, "Track2 Service3 PinRequirements", TagValueTypeEnum.TEXT, service3PinRequirements.getBytes(StandardCharsets.UTF_8));
            tagListNew.add(tnvNew);
            return tagListNew;
        } else {
            return null;
        }
    }
}
