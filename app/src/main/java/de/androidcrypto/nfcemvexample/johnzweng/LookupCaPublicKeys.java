package de.androidcrypto.nfcemvexample.johnzweng;

import static de.androidcrypto.nfcemvexample.BinaryUtils.hexToBytes;

import java.util.Arrays;

/**
 * This class just provides the CA Public Keys for several Credit/Payment Cards
 * tag8f_CertificationAuthorityPublicKeyIndex
 */


public class LookupCaPublicKeys {

    // manual table

    // American Express
    private static final byte[] caPublicKeyAmex10Rid = hexToBytes("A000000025");
    private static final byte[] caPublicKeyAmex10Index = hexToBytes("10");
    private static final byte[] caPublicKeyAmex10Modulus = hexToBytes(("CF98DFEDB3D3727965EE7797723355E0751C81D2D3DF4D18EBAB9FB9D49F38C8C4A826B99DC9DEA3F01043D4BF22AC3550E2962A59639B1332156422F788B9C16D40135EFD1BA94147750575E636B6EBC618734C91C1D1BF3EDC2A46A43901668E0FFC136774080E888044F6A1E65DC9AAA8928DACBEB0DB55EA3514686C6A732CEF55EE27CF877F110652694A0E3484C855D882AE191674E25C296205BBB599455176FDD7BBC549F27BA5FE35336F7E29E68D783973199436633C67EE5A680F05160ED12D1665EC83D1997F10FD05BBDBF9433E8F797AEE3E9F02A34228ACE927ABE62B8B9281AD08D3DF5C7379685045D7BA5FCDE58637"));
    private static final byte[] caPublicKeyAmex10Exponent = hexToBytes(("03"));

    // MasterCard
    private static final byte[] caPublicKeyMc05Rid = hexToBytes("A000000004");
    private static final byte[] caPublicKeyMc05Index = hexToBytes("05");
    private static final byte[] caPublicKeyMc05Modulus = hexToBytes("B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597");
    private static final byte[] caPublicKeyMc05Exponent = hexToBytes(("03"));

    private static final byte[] caPublicKeyMc06Rid = hexToBytes("A000000004");
    private static final byte[] caPublicKeyMc06Index = hexToBytes("06");
    private static final byte[] caPublicKeyMc06Modulus = hexToBytes("CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747F");
    private static final byte[] caPublicKeyMc06Exponent = hexToBytes(("03"));

    // VisaCard

    private static final byte[] caPublicKeyVisa06Rid = hexToBytes("A000000003");
    private static final byte[] caPublicKeyVisa06Index = hexToBytes("06");
    private static final byte[] caPublicKeyVisa06Modulus = hexToBytes("F934FC032BE59B609A9A649E04446F1B365D1D23A1E6574E490170527EDF32F398326159B39B63D07E95E6276D7FCBB786925182BC0667FBD8F6566B361CA41A38DDF227091B87FA4F47BAC780AC47E15A6A0FB65393EB3473E8D193A07EB579");
    private static final byte[] caPublicKeyVisa06Exponent = hexToBytes(("03"));

    private static final byte[] caPublicKeyVisa08Rid = hexToBytes("A000000003");
    private static final byte[] caPublicKeyVisa08Index = hexToBytes("08");
    private static final byte[] caPublicKeyVisa08Modulus = hexToBytes("9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41");
    private static final byte[] caPublicKeyVisa08Exponent = hexToBytes(("03"));

    private static final byte[] caPublicKeyVisa09Rid = hexToBytes("A000000003");
    private static final byte[] caPublicKeyVisa09Index = hexToBytes("09");
    private static final byte[] caPublicKeyVisa09Modulus = hexToBytes("9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41");
    private static final byte[] caPublicKeyVisa09Exponent = hexToBytes(("03"));

    // missing: D276000025	ZKA Girocard ATM
    // missing: A000000359  Euro Alliance of Payment Schemes s.c.r.l. – EAPS Girocard EAPS
    // missing: A000000059  Zentraler Kreditausschuss (ZKA) Girocard Electronic Cash
    // missing:

    /**
     * get the Certification Authority Public Key for an AID-Key Index combination
     * @param aid
     * @param caPublicKeyIndex - get it from tag 0x8f CertificationAuthorityPublicKeyIndex
     * @return the key as array of byte[]:
     * [0] = modulus
     * [1] = exponent
     *
     * Note: the data is hardcoded for my test cards, append data if you need newer or other keys from
     * https://www.eftlab.co.uk/knowledge-base/list-of-ca-public-keys
     */
    public static byte[][] getCaPublicKey(byte[] aid, byte[] caPublicKeyIndex) {
        byte[][] caKey = new byte[2][];
        // the CA Public Keys are based on the RID = the first 5 bytes from the AID
        byte[] rid = Arrays.copyOf(aid, 5);
        // this is a manual table lookup, for "production" you should consider a file based mode
        if ((Arrays.equals(rid, caPublicKeyAmex10Rid) && (Arrays.equals(caPublicKeyIndex, caPublicKeyAmex10Index)))) {
            caKey[0] = caPublicKeyAmex10Modulus;
            caKey[1] = caPublicKeyAmex10Exponent;
        }
        if ((Arrays.equals(rid, caPublicKeyMc05Rid)  && (Arrays.equals(caPublicKeyIndex, caPublicKeyMc05Index)))){
            caKey[0] = caPublicKeyMc05Modulus;
            caKey[1] = caPublicKeyMc05Exponent;
        }
        if ((Arrays.equals(rid, caPublicKeyMc06Rid)  && (Arrays.equals(caPublicKeyIndex, caPublicKeyMc06Index)))){
            caKey[0] = caPublicKeyMc06Modulus;
            caKey[1] = caPublicKeyMc06Exponent;
        }
        if ((Arrays.equals(rid, caPublicKeyVisa06Rid)  && (Arrays.equals(caPublicKeyIndex, caPublicKeyVisa06Index)))){
            caKey[0] = caPublicKeyVisa06Modulus;
            caKey[1] = caPublicKeyVisa06Exponent;
        }
        if ((Arrays.equals(rid, caPublicKeyVisa08Rid)  && (Arrays.equals(caPublicKeyIndex, caPublicKeyVisa08Index)))){
            caKey[0] = caPublicKeyVisa08Modulus;
            caKey[1] = caPublicKeyVisa08Exponent;
        }
        if ((Arrays.equals(rid, caPublicKeyVisa09Rid)  && (Arrays.equals(caPublicKeyIndex, caPublicKeyVisa09Index)))){
            caKey[0] = caPublicKeyVisa09Modulus;
            caKey[1] = caPublicKeyVisa09Exponent;
        }
        return caKey;
    }



}
