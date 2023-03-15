package de.androidcrypto.nfcemvexample.nfccreditcards;

import static de.androidcrypto.nfcemvexample.BinaryUtils.bytesToHex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AidValues {

    private List<AidTag> aidList = new ArrayList<>();

    private final AidTag tNull = setTag(new byte[]{(byte) 0x00}, "Card issuer/type not yet in AID table");
    private final AidTag tVisa = setTag(hexToBytes("A0000000031010"), "VISA credit/debit");
    private final AidTag tVisaVpay = setTag(hexToBytes("A0000000032010"), "Visa International Vpay");
    private final AidTag tVisaVpay2 = setTag(hexToBytes("A0000000032020"), "Visa International Vpay");
    private final AidTag tMasterCard = setTag(hexToBytes("A0000000041010"), "MasterCard");
    private final AidTag tMasterCard2 = setTag(hexToBytes("A0000000043060"), "Maestro (Debit)");
    private final AidTag tMasterCard3 = setTag(hexToBytes("A000000004306001"), "Maestro (Debit)");
    private final AidTag tZka = setTag(hexToBytes("A00000005945430100"), "Zentraler Kreditausschuss (ZKA) Girocard Electronic Cash");
    private final AidTag tEaps = setTag(hexToBytes("A0000003591010028001"), "Euro Alliance of Payment Schemes s.c.r.l. – EAPS Girocard EAPS");
    private final AidTag tZkaAtm = setTag(hexToBytes("D27600002547410100"), "ZKA Girocard ATM");

    // see the complete list here: https://www.eftlab.com/knowledge-base/complete-list-of-application-identifiers-aid

    public AidValues() {
        // empty constructor to fill the emvTagList
    }

    public List<AidTag> getAidList() {
        return aidList;
    }

    public String getAidName(byte[] aidByte) {
        //System.out.println(bytesToHex(aidByte));
        for (int i = 0; i < aidList.size(); i++) {
            AidTag aidTag = aidList.get(i);
            if (Arrays.equals(aidTag.getAidByte(), aidByte)) {
                return aidTag.getAidName();
            }
        }
        return tNull.getAidName(); // default, entry not found
    }

    private AidTag setTag(byte[] aidByte, String aidName) {
        AidTag aidTag = new AidTag(aidByte, aidName);
        aidList.add(aidTag);
        return aidTag;
    }

    /**
     * converts a hex encoded string to a byte array
     * @param str
     * @return
     */
    private static byte[] hexToBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2),
                    16);
        }
        return bytes;
    }
}
