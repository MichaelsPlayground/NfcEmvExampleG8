# NFC EMV Example G8

G8 = Gradle 8 compatible

Use this app to dump a EMV payment card (e.g. Credit Card or German Girocard) and use this dump file as 
source for the app **HCE CreditCard Emulator G8**.

Soundfiles: https://mobcup.net/ringtone/ping-euf272ye/download/mp3

For prettyPrint: include implementation 'com.github.devnied.emvnfccard:library:3.0.1'

Source: https://github.com/devnied/EMV-NFC-Paycard-Enrollment

written by Julien Millau (devnied)

For TLV-handling (BER-TLV): implementation 'com.payneteasy:ber-tlv:1.0-11'

Source: https://github.com/evsinev/ber-tlv

Screenshot of the running app after startup:
![server_view_after_starting](docs/server00.png?raw=true)

You should always get new newest documentation or specification from the original Website: https://www.emvco.com/specifications/

I have download some specifications and provide them here:

EMV Book 1 (ICC to Terminal Interface): ![Download PDF](docs/books/EMV_v4.3_Book_1_ICC_to_Terminal_Interface_2012060705394541.pdf?raw=true)

EMV Book 2 (Security and Key Management): ![Download PDF](docs/books/EMV_v4.3_Book_2_Security_and_Key_Management_20120607061923900.pdf?raw=true)

EMV Book 3 (Application Specification): ![Download PDF](docs/books/EMV_v4.4_Book_3_Application_Specification.pdf?raw=true)

EMV Book 4 (Other Interfaces): ![Download PDF](docs/books/EMV_v4.4_Book_4_Other_Interfaces.pdf?raw=true)

EMV Level 1 Contactless (Interface Specification) version 3.2: ![Download PDF](docs/books/EMV-Level-1-Contactless-Interface-Specification-V3.2.pdf?raw=true)

EMV Contactless Book A (Architecture and General Rqmtsn) version 2.10: ![Download PDF](docs/books/EMV-Contactless-Book-A-Architecture-and-General-Rqmts-v2.10.pdf?raw=true)

EMV Contactless Book B (Entry Point Specification) version 2.10: ![Download PDF](docs/books/EMV-Contactless-Book-B-Entry-Point-Specification-v2.10.pdf?raw=true)

EMV Contactless Book C see below Kernel 1..8

EMV Contactless Book D (Communication Protocol) version 2.6: ![Download PDF](docs/books/D_EMV_Contactless_Communication_Protocol_v2.6_20160301114325655.pdf?raw=true) 

Kernel 1 (Master- & VisaCard old ?) version 2.6: ![Download PDF](docs/books/C-1_Kernel_1_v2.6_20160512101416661.pdf?raw=true) 

Kernel 2 (MasterCard) version 2.10: ![Download PDF](docs/books/C-2-Kernel-2-v2.10.pdf?raw=true)

Kernel 3 (VisaCard) version 2.10: ![Download PDF](docs/books/C-3-Kernel-3-v2.10.pdf?raw=true)

Kernel 4 (American Express) version 2.10: ![Download PDF](docs/books/C-4-Kernel-4-v2.10.pdf?raw=true)

Kernel 5 (JCB) version 2.10: ![Download PDF](docs/books/C-5_Kernel-5-v2.10.pdf?raw=true)

Kernel 6 (Discover) version 2.6: ![Download PDF](docs/books/C-6_Kernel_6_v2.6_20160512101849195.pdf?raw=true)

Kernel 7 (UnionPay) version 2.9.1: ![Download PDF](docs/books/C-7-Kernel-7-v2-9-1.pdf?raw=true)

Kernel 8 (unknown) version 1.0: ![Download PDF](docs/books/C-8-Kernel-8-V1.0.pdf?raw=true)

Online BER-TLV decoder: https://emvlab.org/tlvutils/

Resource to find codes like Country Code, Currency Code etc: https://github.com/binaryfoo/emv-bertlv/tree/master/src/main/resources

Complete list of APDU responses: https://www.eftlab.com/knowledge-base/complete-list-of-apdu-responses

Complete list of Application Identifiers (AID): https://www.eftlab.com/knowledge-base/complete-list-of-application-identifiers-aid


Additional software: https://github.com/sasc999/javaemvreader

Newer versions: https://github.com/maciejsszmigiero/javaemvreader

Test as add-ons: ApplicationInterchangeProfile.java

## Permission handling

For some categories we do need permissions (declared in AndroidManifest.xml) and sometimes a 
runtime granting by the user:

AndroidManifest.xml:
```plaintext
    <!-- needed for NFC -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.VIBRATE" />
    
```

PPSE (Paypass Payment System Environment)

own answer: https://stackoverflow.com/questions/73519913/apdu-commands-to-get-data-from-nfc-tag-using-isodep

For PAN extraction: https://www.openscdp.org/scripts/tutorial/emv/dda.html

"Application PAN" Length: 10 - PAN (padded to the right with Hex 'F's)

Curated list of AID: https://en.wikipedia.org/wiki/EMV

old Card emulator project: https://web.archive.org/web/20160415031900/http://developer.android.com/samples/CardEmulation/index.html

https://github.com/championswimmer/NFC-host-card-emulation-Android

https://web.archive.org/web/20150906014951/https://developer.android.com/samples/CardReader/index.html

https://github.com/googlearchive/android-CardReader

https://github.com/googlearchive/android-CardEmulation

https://medium.com/the-almanac/how-to-build-a-simple-smart-card-emulator-reader-for-android-7975fae4040f  (Kotlin)

https://github.com/Mohamdaoui/HostCardEmulator (Kotlin)

https://github.com/Mohamdaoui/SmartCardReader (Kotlin)

https://developer.visa.com/identity/user/register

American Express: C-4 Kernel 4 v2.10

decryption of ICC Public Key success
Recovered Data Header:                    106
Recovered Data Header Byte:               6a
Certificate Format:                       4
Application Pan:                          4263540122270050ffff
Certificate Expiration Date:              0625
Certificate Serial Number:                26e44b
Hash Algorithm Indicator                  1
ICC Public Key Algorithm Indicator:       1

https://stackoverflow.com/questions/35881046/get-processing-options-response with Template 1:
```plaintext
GET PROCESSING OPTIONS
According to "EMV Book 3 - Application Specification", Tag 0x80 Format 1 reply for 
GET PROCESSING OPTIONS contained:
x82: Application Interchange Profile (AIP),
x94: Application File Locator (AFL).
Please keep in mind that Tag 0x80 formats are different for different APDU Commands.
Your APDU Data reply with EMV TLV Tag 0x80 Format 1 data contains (2 bytes) with AIP and AFL with 
4 Records (4 bytes each, 16 bytes in total):

I/System.out: 80 12 -- Response Message Template Format 1
I/System.out:       18 00 08 01 01 00 08 03 03 00 08 05 05 00 10 02 02 00 (BINARY)
                 12 = dec 18 total length
                 02 = dec 02 AIP length
                 10 = dec 16 AFL length = 4 AFL blocks 
               AIP: 18 00
               AFL:       08 01 01 00 
                                      08 03 03 00 
                                                  08 05 05 00 
                                                              10 02 02 00
```

Google Wallet infos (https://stackoverflow.com/a/23359247/8166854):
```plaintext
The response to the GET PROCESSING OPTIONS command indicates the following Application Interchange Profile (AIP):

82 Application Interchange Profile
    0000
Google Wallet is basically a MasterCard (EMV contactless kernel 2), so decoding the AIP according to the rules of Kernel 2 results in the following:

Byte 1, b7 = 0: no SDA supported
        b6 = 0: no DDA supported
        b5 = 0: no cardholder verification supported
        b4 = 0: no terminal risk management to be performed
        b3 = 0: no issuer authentication supported
        b2 = 0: no on-device cardholder verification supported
        b1 = 0: no CDA supported
Byte 2, b8 = 0: no EMV mode supported
The important part is byte 2, bit 8: It indicates that your card does not support EMV mode. Hence, your card/Google Wallet is a PayPass card that supports only Mag-Stripe mode. Therefore, you cannot authenticate transactions using GENERATE AC. Instead, you can only let the card generate dynamic card verification codes (CVC3) using COMPUTE CRYPTOGRAPHIC CHECKSUM:

byte[] computeCC = new byte[] {
    (byte)0x80, // CLA = proprietary
    (byte)0x2A, // INS = COMPUTE CRYPTOGRAPHIC CHECKSUM
    (byte)0x8E, // P1
    (byte)0x80, // P2
    (byte)0x04, // Lc
    (byte)0xWW, (byte)0xXX, (byte)0xYY, (byte)0xZZ, // Unpredicatable Number (numeric)
    (byte)0x00, // Le
};
response = isoDep.transceive(computeCC);
Note that the data field of the COMPUTE CRYPTOGRAPHIC CHECKSUM command must be filled with values according to the UDOL (in case there is no UDOL, the default UDOL is 9F6A04, indicating the unpredictable number, numeric).

The unpredictable number (numeric) is a BCD coded number in the range that is defined by the mag-stripe data file (see the AFL). In the past, for Google Wallet, this was a value between 0 and 99 (i.e. WW='00', XX='00', YY='00', ZZ='00'..'99').

UPDATE:

The data read from the card decodes as follows:

70 7c
  9f6c 02    Mag-stripe application version number = Version 1
    00 01
  9f62 06    Track 1 bit map for CVC3
    00 00 00 00 00 38
  9f63 06    Track 1 bit map for UN and ATC
    00 00 00 00 03 c6
  56 29      Track 1 data
    42         ISO/IEC 7813 structure "B" format
    35333936 XXXXXXXX 31XXXX39 XXXXXXXX    PAN (ASCII)
    5e         Field separator "^"
    202f       Cardholder name " /" (empty, see MC requirements)
    5e         Field separator "^"
    31343037   Expiry date "14"/"07"
    313031     Service code "101"
    34303130303030303030303030    Track 1 discretionary data
  9f64 01    Track 1 number of ATC digits
    04
  9f65 02    Track 2 bit map for CVC3
    00 38
  9f66 02    Track 2 bit map for UN and ATC
    03 c6
  9f6b 13    Track 2 data
    5396 XXXX 1XX9 XXXX    PAN (BCD)
    d          Field separator
    1407       Expiry date
    101        Service code
    4010000000000    Track 2 discretionary data
    f          Padding
  9f67 01      Track 2 number of ATC digits
    04
  9f69 0f      UDOL
    9f6a 04      Unpredictable number (numeric)
    9f7e 01      Mobile support indicator
    9f02 06      Amount authorized (numeric)
    5f2a 02      Transaction currency code
    9f1a 02      Terminal country code
So the card does provide a UDOL. Therefore, the COMPUTE CRYPTOGRAPHIC CHECKSUM command has to be adapted accordingly:

byte[] computeCC = new byte[] {
    (byte)0x80, // CLA = proprietary
    (byte)0x2A, // INS = COMPUTE CRYPTOGRAPHIC CHECKSUM
    (byte)0x8E, // P1
    (byte)0x80, // P2
    (byte)0x0F, // Lc
    // 9f6a 04      Unpredictable number (numeric)
    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x12, // two digits according to UN/ATC bit map and number of ATC digits: 6 - 4 = 2
    // 9f7e 01      Mobile support indicator
    (byte)0x00, // no offline PIN required, no mobile support
    // 9f02 06      Amount authorized (numeric)
    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, // 1.00
    // 5f2a 02      Transaction currency code
    (byte)0x09, (byte)0x78, // Euro
    // 9f1a 02      Terminal country code
    (byte)0x00, (byte)0x40, // Austria
    (byte)0x00, // Le
};
response = isoDep.transceive(computeCC);
Share
Edit
Follow
Flag
edited May 4, 2014 at 13:31
answered Apr 29, 2014 at 8:20
Michael Roland's user avatar
Michael Roland
```

## Extended analyzing of the card

article series by Ahmed Hemdan Farghaly:

EMV Application Specification, a closer look...:
https://www.linkedin.com/pulse/emv-application-specification-closer-look-ahmed-hemdan-farghaly/

EMV Application Specification :: Application selection:
https://www.linkedin.com/pulse/emv-application-specification-selection-ahmed-hemdan-farghaly

EMV Application Specification :: 
Initiate Application Process: https://www.linkedin.com/pulse/emv-application-specification-initiate-process-ahmed-hemdan-farghaly

EMV Application Specification :: Read Application Data: 
https://www.linkedin.com/pulse/emv-application-specification-read-data-ahmed-hemdan-farghaly

EMV Application Specification :: Offline Data Authentication (ODA) Part 1:  
https://www.linkedin.com/pulse/emv-application-specification-offline-data-oda-part-farghaly/

EMV Application Specification :: Offline Data Authentication (ODA) Part 2:  
https://www.linkedin.com/pulse/emv-application-specification-offline-data-oda-part-farghaly-1f

EMV Application Specification :: Initiate Application Process: 
https://www.linkedin.com/pulse/emv-application-specification-initiate-process-ahmed-hemdan-farghaly?trk=article-ssr-frontend-pulse_more-articles_related-content-card

EMV Key Management (Karthick Chandrasekar):
https://www.linkedin.com/pulse/emv-key-management-karthick-chandrasekar

EMV Concept - Offline Data Authentication| How an Static Data Authentication Works | SDA |  
Application of Cryptography in Cards & Payments (Sivasailam Sivagnanam): 
https://www.linkedin.com/pulse/emv-concept-offline-data-authentication-how-static-sivasailam

Everything EMV (PIN etc) (Binoy Baby):
https://www.linkedin.com/pulse/cards-payments-101-everything-emv-binoy-baby

Decoding EMV Contactless: https://www.linkedin.com/pulse/decoding-emv-contactless-kenny-shi

John Zweng Android EMV key test: 
https://github.com/johnzweng/android-emv-key-test

Payment Card Tools (e.g. CVM List or Terminal Transaction Qualifiers): https://paymentcardtools.com/emv-tag-decoders/ttq

https://sites.google.com/site/kriengten/smartcard-basic

Overview of complete readings and found PAN + Expiration Date
...
MC Lloyds: SFI 2 REC 1
MC Lloyds: SFI 2 REC 2 Track2 data 
MC AAB: SFI 2 REC 1
MC AAB: SFI 3 REC 1 additional
Visa comdir M: SFI 2 REC 3
Visa Lloyds: SFI 2 REC 6
Visa DKB: SFI 2 REC 4
Girocard Voba M : SFI 1 REC 5
Girocard Voba M : SFI 1 REC 3 Track2 data

comd gk m:
```plaintext

Read by real card terminal

2023.03.29 12:49:28.563 command received: 00a4040007a000000003201000
2023.03.29 12:49:28.565 found an AID in the command foundAid: 4

GPO: real
2023.03.29 12:49:28.732 command received: 
80a8000023832136a04000000000002600000000000000027600000000000978230329000cf80aff00

GPO by own CardReader
80a8000023832127000000000000001000000000000000097800000000000978230301003839303100
               TTQ      Amount       Amout other  TCount   TVR  TrCount Date  Type UN
80a80000238321 36a04000 000000002600 000000000000 0276 0000000000 0978 230329 00 0cf80aff 00
80a80000238321 27000000 000000001000 000000000000 0978 0000000000 0978 230301 00 38393031 00

The card is requesting 9 tags in the PDOL

Tag  Tag Name                        Length Value                Value Real              
-----------------------------------------------------
9f66 Terminal Transaction Qualifiers     4  27 00 00 00 
9f02 Amount, Authorised (Numeric)        6  00 00 00 00 10 00 
9f03 Amount, Other (Numeric)             6  00 00 00 00 00 00 
9f1a Terminal Country Code               2  09 78 
95   Terminal Verification Results (TVR) 5  00 00 00 00 00 
5f2a Transaction Currency Code           2  09 78 
9a   Transaction Date                    3  23 03 01 
9c   Transaction Type                    1  00 
9f37 Unpredictable Number                4  38 39 30 31 
-----------------------------------------------------

Response own was:
776a820220009408200102003001040057134822422508205973006d24122210129613440f5f3401019f10200fa6d2a00040000000000000000000000f0a00000000000021230325c00000029f2608b749192a7b593f629f2701809f360200f89f6c023e009f6e04200000009000
------------------------------------
77 6A -- Response Message Template Format 2
      82 02 -- Application Interchange Profile
            20 00 (BINARY)
      94 08 -- Application File Locator (AFL)
            20 01 02 00 30 01 04 00 (BINARY)
      57 13 -- Track 2 Equivalent Data
            48 22 42 25 08 20 59 73 00 6D 24 12 22 10 12 96
            13 44 0F (BINARY)
      5F 34 01 -- Application Primary Account Number (PAN) Sequence Number
               B7 49 19 2A 7B 59 3F 62 (BINARY)
               00 F8 (BINARY)
      9F 6C 02 -- Mag Stripe Application Version Number (Card)
               3E 00 (BINARY)
      9F 6E 04 -- Visa Low-Value Payment (VLP) Issuer Authorisation Code
               20 00 00 00 (BINARY)
90 00 -- Command successfully executed (OK)
------------------------------------




Response on real was:



```




```

Examples for brute force reading of all files (SFI between 1 to 9 and Record between 1 to 9): 

```plaintext
MC complete Lloyds
I/System.out: NFC tag discovered
I/System.out: TagId: b58fcc6d
I/System.out: TechList found with these entries:
I/System.out: a0000000041010
I/System.out: android.nfc.tech.IsoDep
I/System.out: android.nfc.tech.NfcA
I/System.out: *** Tech ***
I/System.out: Technology IsoDep
I/System.out: try to read a payment card with PPSE
I/System.out: 01 select PPSE
I/System.out: 01 select PPSE command length 20 data: 00a404000e325041592e5359532e444446303100
I/System.out: 01 select PPSE response length 51 data: 6f2f840e325041592e5359532e4444463031a51dbf0c1a61184f07a0000000041010500a4d6173746572436172648701019000
I/System.out: 02 analyze select PPSE response and search for tag 0x4F (applications on card)
I/System.out: Found tag 0x4F 1 times:
I/System.out: application Id (AID): a0000000041010
I/System.out: ************************************
I/System.out: SFI: 1
I/System.out: Record: 1
I/System.out: 706a9f6c0200019f620600000000000e9f63060000000007f0562942353138373931303331313233363637375e202f5e31373131323031303430303030303030303030309f6401049f6502000e9f660207f09f6b135187910311236677d17112010400000000000f9f670104
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 1
I/System.out: 7081815f25031409015f24031711309f07023d005a0851879103112366775f3401049f0d0500000400009f0e05b4708000009f0f0500000480005f280208268e0e000000000000000042031e031f039f4a01828c219f02069f03069f1a0295055f2a029a039c019f37049f35019f45029f4c089f34038d0c910a8a0295059f37049f4c08
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 2
I/System.out: 701a57135187910311236677d17112013369738100553f9f08020002
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 1
I/System.out: 7081e08f01059f3201039224f2235c27dea9e81221a1eaf9040628e27950708a387694eee0a5304202b52236a5e159a99081b07945cb9f6776b7fb188ba542ea469c53ba91eccbb0202902fb9c7011eb9e1f7bad06a329240ec8e69b90f61c752248d5e36e14075bef1df7fd518d8a7db3a9dfde119237a1e95a9fb1645bc39936393632725c1ab7a2bfd92c47746bba7b3f7f99acd1317a8f9af50397a7ed9ff3b52d4d8b1b05be5b140efab84f28db4ab560728144ee75e91c909ba12124b717d7283c6f007c5639a94519767114f28e56e2909c664f99dc01e3ec8190390ac3a385
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 2
I/System.out: 70039301ff
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 1
I/System.out: 70049f470103
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 2
I/System.out: 7081b49f4681b04622d517f70ac589ca80c872d683c40de548cc4937192a87662b2ddf3479c809e8f7a9aabc35cb9ca294d1415f495f41dd3109f05cff541f5f546a344debe5b85649b7e02717f166954be264586831c30e7d5982d3d5d0f3e0de67a369be8d86e63d85519ef07b1f6ddbbecc2bfffae696608e49d1a627a642200fc2a32063608b5901e687571337f675d86b8a2693623fbcb0216731cb860dd4605684d4f96e7b48e7ff41126ce673ba2f16fb68868a
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: reading complete
```

MC AAB
```plaintext
I/System.out: SFI: 1
I/System.out: Record: 1
I/System.out: 70759f6c0200019f6206000000000f009f63060000000000fe563442353337353035303030303136303131305e202f5e323430333232313237393433323930303030303030303030303030303030309f6401029f65020f009f660200fe9f6b135375050000160110d24032210000000000000f9f670102
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 1
I/System.out: 7081a69f420209785f25032203015f24032403315a0853750500001601105f3401009f0702ffc09f080200028c279f02069f03069f1a0295055f2a029a039c019f37049f35019f45029f4c089f34039f21039f7c148d0c910a8a0295059f37049f4c088e0e000000000000000042031e031f039f0d05b4508400009f0e0500000000009f0f05b4708480005f280202809f4a018257135375050000160110d24032212794329000000f
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 1
I/System.out: 7081909f420209785f25032203015f24032403315a0853750500001601105f3401009f0702ffc08c279f02069f03069f1a0295055f2a029a039c019f37049f35019f45029f4c089f34039f21039f7c148d0c910a8a0295059f37049f4c088e1200000000000000004203440341031e031f039f0d05bc50bc08009f0e0500000000009f0f05bc70bc98005f280202809f4a0182
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 1
I/System.out: 7081b89f4701039f4681b03cada902afb40289fbdfea01950c498191442c1b48234dcaff66bca63cbf821a3121fa808e4275a4e894b154c1874bddb00f16276e92c73c04468253b373f1e6a9a89e2705b4670682d0adff05617a21d7684031a1cdb438e66cd98d591dc376398c8aab4f137a2226122990d9b2b4c72ded6495d637338fefa893ae7fb4eb845f8ec2e260d2385a780f9fda64b3639a9547adad806f78c9bc9f17f9d4c5b26474b9ba03892a754ffdf24df04c702f86
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 2
I/System.out: 7081e08f01059f3201039224abfd2ebc115c3796e382be7e9863b92c266ccabc8bd014923024c80563234e8a11710a019081b004cc60769cabe557a9f2d83c7c73f8b177dbf69288e332f151fba10027301bb9a18203ba421bda9c2cc8186b975885523bf6707f287a5e88f0f6cd79a076319c1404fcdd1f4fa011f7219e1bf74e07b25e781d6af017a9404df9fd805b05b76874663ea88515018b2cb6140dc001a998016d28c4af8e49dfcc7d9cee314e72ae0d993b52cae91a5b5c76b0b33e7ac14a7294b59213ca0c50463cfb8b040bb8ac953631b80fa85a698b00228b5ff44223
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 3
I/System.out: 7081b89f4681b0aea2347a69e6d9544dfa891a761833e6e6d3a78d450142dd7c21c131e585448fbc8449fe777f1895cfb18f2983d60eed56466a688d9da6b3fb6593726251a83132b3f953a71098eeefcd388bb672ad5a3592d31ea145fdf6f763733bae482455c7987e96ae6cd8cf9d5ce562e5c80a7a6a083ba85c8eb86ddac0ca19186554dfe5ab4aeada5be92e30c0c16981c516c74203694fd04e2fe3a66bf590bd4bb3085fe80167e98c9745e7e4819e4bd55f2b9f470103
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: reading complete
```


Visa comd M
```plaintext
I/System.out: SFI: 1
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 1
I/System.out: 7081fb9081f85ab54faf4ad810b3cca4ed42c38e1e768fca3187ed1be4196c6779c4633cbe88751889c12b05e10ee87cb198518793ff61e87534f66850e96239b76648429eced4cc207608d0d2a932dd9e8c4bb0d139c4eca59e1ef5f4708f72d80dc5b66c45f4566c91b55384dfdeabb55faa622c6764cc9fb4c4900b6ab2cec5abad9057e2cf63a881bb4ec2a5d96634d7c11366eb908a168d33aa3c544822fc83e74c104b9275b2ef1cf41375b404a260bbf8fb3d4452af3d0630bb1ec2a01676ba588ae7820727622a6d9df5c93a3ce807d54b79ae007c3d401f8787dc3e235e8b9ae6b1b9279328cb1ca94105434010f15eb07f487f4d5c94f4a5a7
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 2
I/System.out: 70078f01099f320103
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 3
I/System.out: 7081eb9f4681b02c4b62dfaede136b9bafeddebaaf41e5f4fdc9920b077817de896e6503c69c8f80ece2559cdf721ce1b7b2bc159fe77ec8d6eb45296876fbf4a6bd4bb4a11511ebd80fdf1c7bb8e1f4a2cdb7c4db0cc6f9fda7f6696c30d3846e1b98f4c849b7385f349d280fd92d75774dcbed96a5328f657f7eceb4bfa3ec3f9f39a64414bdbf0f03b15c49cbf0475bfa6a5f2513689c195faea031ae2391998be2028aa1671b380eb19a69a6c454bd2a30d11bc63c9f4701035a0848717800827705745f24032507315f3401015f280202769f070200809f4a01829f6e04207000009f690701000000000000
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 5
I/System.out: 70155f280202765f3401019f070200809f6e0420700000
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: reading complete
```

Visa Lloyds
```plaintext
I/System.out: SFI: 1
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 3
I/System.out: 7081fb9081f830f056de40a950bec2a870c59d5462222605a8f31cdef39a0537c7c175115e352ad0c55470fce5737c4e769897623e01401da73e01644bb0b491aa1aadb27fc360c0089f7c2e52a64e96a3f8a59f76e49aa6dd9a6792644f2b0b513b1a1a93b98a3cc19f0bec45e9f8edd70f893a8cafb21b62f3b8f15983775f14fd16cb36a19120e5a5068ef9f05ffaea4e714d80f134a298d167a65a92f6f57963db94ab5d3967f6675b3609a0fceb5fbb70f07cfdeab1352c6a34d6be737aa74848f3f56932f08b51f54aa3040f1ace4a0ced38684df900a395c5cd88562eb2af8d35601210c20d6c3425dcd813b9b358d1356d52a8ebd8fb5a19915d
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 4
I/System.out: 70078f01099f320103
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 5
I/System.out: 7081b49f4681b047461ffca14b5dfdc209569c8a14f17644251aa3f4abea251262134b920982f0250741f96fccb40800293054c0d89824ba7ac44ee7bab06fa157fccf7e52d3c64b4d8acd41b9774b801519ed6fec827ec2ec29f8991167c453776559a4a06fd98c4b9bd1548a65af2f56002a836bdf9a040a9253e653584c92833c3d1aa8e08c4de9cda1026044f80f39a9326a57496598987a6b3e18a5f56a8bdede752870e8793776db9d325ccd9c7ca5db33c28f04
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 6
I/System.out: 70249f4701035a0849218280948967525f3401005f24032502285f280208269f6e0420700000
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: reading complete
```

Visa DKB
```plaintext
I/System.out: SFI: 1
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 2
I/System.out: 7081fb9081f88893cf85a81325ab8da6a4196eb5787291db7205f61b172b26deb867da427f1d0e438e86400aea81a0f2826b250da618108389bdabe2a75c0168a28bb97645158b57ca8faa1d38d7a56e0a4171ec0d5e048d048dd98106bcadb3b5cac80485ff9c0fc970b4ea95d557fb9dd065bf75eb06f51df5a2c20479058ede6c8a376d9bfbf0c05b9e2b5aac1ec5982e2a9d861573e892da87b68357306e88cb054ab0090e01670a73d23fa239f4ae1283110fca40d46edc6c8021d15b3c147251b3c5e754f0fa9d82b7934ed34a12ef3d0a66c0c2a26a32e9722b10653516b356440aa8eece8d1d023829394adc2f9309ff60fc5baf51c0b24690be
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 3
I/System.out: 70079f3201038f0109
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 4
I/System.out: 7081eb9f4681b07e3b33a489fb75a23643407d2ebf48a808957165aa538d681213d71495b577086e63a24e847ed29d2ceba4bb3b1784361221287607ace4b8bfce09dd8364d4709293ed52b528623472fb6157094b12367534d7cf5c20b810058c817fb87c130111ee53c3855fd2b2a95449d03795541ea7c6ef942b0b069bfa7caa5d0ec6db0e428f18d03adcf7f92fb7e5516403adc629f3ffbd6900a1f308fbe5d28cba795c6c62d7573333abed15ad00a4da4ba8a99f4701035a0849300050250039855f24032609305f280202765f3401009f0702c0809f4a01829f6e04207000009f690701000000000000
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: reading complete
```

Girocard Voba M
```plaintext
I/System.out: SFI: 1
I/System.out: Record: 1
I/System.out: 70059f08020002
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 2
I/System.out: 70059f08020002
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 3
I/System.out: 703d8c1b9f02069f03069f1a0295055f2a029a039c019f37049f35019f34038d0991108a0295059f370457136726428902046846007d21122010254828156f
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 4
I/System.out: 700c8e0a00000000000000000203
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 5
I/System.out: 70385f24032112315a0a6726428902046846007f5f3401025f280202809f0702ffc09f0d05fc40a480009f0e0500101800009f0f05fc40a49800
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 6
I/System.out: 70188e0c0000000000000000440302039f4a01829f49039f3704
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 7
I/System.out: 701c8e0c00000000000000001f0302039f080200029f6c02ffff9f4a0182
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 8
I/System.out: 701a8e0a000000000000000002039f080200029f6c02ffff9f4a0182
I/System.out: -----------------------
I/System.out: SFI: 1
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 2
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 1
I/System.out: 7081e28f01059081b078cdb2c84b435325ec4478fd6f0f9f0dd61210a78c791adcb22c85fb0095db3a540658569a1c0d35a48d1fd9c2dba83ed941fcb3f2cfe56c943bfa0f8d25f0896284006cbdc10821cf0f0f6ec033332f8eb52c1acad9c52221a27dd23aba70c27c547aece994c7dc5c4d5f1b28529a803340cc249caf6bcb3614d071de141f89a1f4a545c5598395864474514e42c7f1edbeedef27b9a50eeb81ed5762a0af36505ee084703dfd168ec6f02245077d8b9f3201039224b0568adf146b092492be46e5d57d920b026be8e734264cf34710483a0af52d46790f01ab0000
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 3
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 1
I/System.out: 70339f47030100019f480a757271487e0b220c81cb0000000000000000000000000000000000000000000000000000000000000000
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 2
I/System.out: 7081b49f4681b06658bded357473566956e7d4e9d7220e906b0c7efd1c342040890d24a8ba157ee3da3822f6649cc30f5df0566f453b43f3966655701c2b1df35c93fbba27ca0a1b9261f8e9454d9f51f58ef4f0ebaa7271f5b1d282a2ce46761c7d4c30849945c872b9857066cc7a35d6e488c14fcf574b337156ba666a4bd4acec84b4388d0a7618cee7a896959a012645ae9f7bc2a918d3e4141bca42983a002d394f2b710afb7dcf9c0d7f50b92524534dc1993d06
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 3
I/System.out: 7081b49f4681b02e9419171b0065fc462339ec45cfbe2b8358167ba3a73fd8e1290ea31ead5617652724d8bc308c0348d1ddaaea25d337668ba0b5db2bdc398027c067d30adcdd089b200cd4f930d34f406bcc43bb029d30c775e40a96e4639d66e2c22f368986d65639c5e3699ca21ad99b130b23c321f8184a18fca59ae3a0c5629cfea8ef89cc9438d59adc7ebf6dba32b6f538058f359ef1a5ccb4af7a850ab7bae2eb878d2a2156e94776d96dd16a5c19713279c5
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 4
I/System.out: 7081b49f4681b087df5942ee89317aea2e53d477ab272794375e9025b0447b304f52e07f54494bea054076a0fd22faf4ee85cfd06ae61c44e0bf1c0156b1c0f287312e1c9460c0b93fac7bdd88a6cf286daeeab5d81310ff49b9d80f4b905261429b44a2c0e3b876ee8825fbb6ff3aef14a645983e886a61a7acde252698868b74033bbecee902050196579b2df75bfe070a14a45ce710c5e782da9ecd20d21db77352461b031ad83d9137615b8a63aca55900619a7a9c
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 4
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 5
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 6
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 7
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 8
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 1
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 2
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 3
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 4
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 5
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 6
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 7
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 8
I/System.out: NULL
I/System.out: -----------------------
I/System.out: SFI: 9
I/System.out: Record: 9
I/System.out: NULL
I/System.out: -----------------------
I/System.out: reading complete
```


