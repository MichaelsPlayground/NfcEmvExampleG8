# Log Entry

```plaintext
code for DKB - not working: 
                                /**
                                 * step xx code start read log entry
                                 */
                                // this manually forced for DKB Girocard:
                                writeToUiAppend(stepSeparatorString);
                                writeToUiAppend("DKB log file entries 19 0A");
                                byte[] tag94BytesListEntry = hexToBytes("25010A00");
                                byte sfiOrg = tag94BytesListEntry[0];
                                byte rec1 = tag94BytesListEntry[1];
                                byte recL = tag94BytesListEntry[2];
                                byte offl = tag94BytesListEntry[3]; // offline authorization
                                int sfiNew = (byte) sfiOrg | 0x04; // add 4 = set bit 3
                                int numberOfRecordsToRead = (byteToInt(recL) - byteToInt(rec1) + 1);
                                writeToUiAppend("for SFI " + byteToHex(sfiOrg) + " we read " + numberOfRecordsToRead + (numberOfRecordsToRead == 1 ? " record" : " records"));
                                // read records
                                byte[] readRecordResponse = new byte[0];
                                for (int iRecord = (int) rec1; iRecord <= (int) recL; iRecord++) {
                                    byte[] cmd = hexToBytes("00B2000400");
                                    cmd[2] = (byte) (iRecord & 0x0FF);
                                    cmd[3] |= (byte) (sfiNew & 0x0FF);
                                    writeToUiAppend("readRecord  command length: " + cmd.length + " data: " + bytesToHex(cmd));
                                    readRecordResponse = nfc.transceive(cmd);
                                    byte[] readRecordResponseTag5a = null;
                                    byte[] readRecordResponseTag5f24 = null;
                                    if (readRecordResponse != null) {
                                        writeToUiAppend("readRecord response length: " + readRecordResponse.length + " data: " + bytesToHex(readRecordResponse));
                                        writeToUiAppend(prettyPrintDataToString(readRecordResponse));
                                        System.out.println("readRecord response length: " + readRecordResponse.length + " data: " + bytesToHex(readRecordResponse));
                                        System.out.println(prettyPrintDataToString(readRecordResponse));

                                    } else {
                                        writeToUiAppend("readRecord response was NULL");
                                    }
                                }
```


Log Entry found in EMV 4.3 Book 3

https://mvallim.github.io/emv-qrcode/docs/EMV_v4.3_Book_3_Application_Specification_20120607062110791.pdf
pages (170/171) 185 + 186

```plaintext
DKB card, selectAidResponse:
6f45840aa0000003591010028001a53750086769726f636172649f38069f02069f1d045f2d046465656ebf0c1a9f4d02190a9f6e07028000003030009f0a0800010501000000009000
6F 45 -- File Control Information (FCI) Template
      84 0A -- Dedicated File (DF) Name
            A0 00 00 03 59 10 10 02 80 01 (BINARY)
      A5 37 -- File Control Information (FCI) Proprietary Template
            50 08 -- Application Label
                  67 69 72 6F 63 61 72 64 (=girocard)
            9F 38 06 -- Processing Options Data Object List (PDOL)
                     9F 02 06 -- Amount, Authorised (Numeric)
                     9F 1D 04 -- Terminal Risk Management Data
            5F 2D 04 -- Language Preference
                     64 65 65 6E (=deen)
            BF 0C 1A -- File Control Information (FCI) Issuer Discretionary Data
                     9F 4D 02 -- Log Entry
                              19 0A (BINARY)
                     9F 6E 07 -- Visa Low-Value Payment (VLP) Issuer Authorisation Code
                              02 80 00 00 30 30 00 (BINARY)
                     9F 0A 08 -- [UNKNOWN TAG]
                              00 01 05 01 00 00 00 00 (BINARY)
```

```plaintext
DKB log: 
a0000003591010028001: 19 0A
d27600002547410100:   19 0A

logFormat: 9f02065f2a029a039f52059f36029f2701ca019505
  9f02 06 amount
  5f2a 02 Transaction Currency Code
  9a   03 Transaction Date YYMMDD
  9f52 05 ???
  9f36 02 Application Transaction Counter (ATC) 
  9f27 01 Cryptogram Information Data 
          ca019505



To get the Transaction Log information, the two following data elements are
used: Log Entry and Log Format.
Table 44 describes the format of the Log Entry data element (tag '9F4D'):
Byte Format Length Value
1 b 1 SFI containing the cyclic transaction log file
2 b 1 Maximum number of records in the transaction log
file
Table 44: Log Entry
Devices that read the transaction log use the Log Entry data element to
determine the location (SFI) and the maximum number of transaction log
records.
The SFI shall be in the range 11 to 30.
The Transaction Log records shall be accessible using the READ RECORD
command as specified in section 6.5.11. The file is a cyclic file as defined in
ISO/IEC 7816-4. Record #1 is the most recent transaction. Record #2 is the next
prior transaction, etc.
The Transaction Log records shall not be designated in the Application File
Locator. Each record is a concatenation of the values identified in the Log Format
data element. The records in the file shall not contain the Application
Elementary File (AEF) Data Template (tag '70').
The Log Format and the Transaction Log records shall remain accessible when
the application is blocked.
To read the transaction log information, the special device uses the following
steps:
• Perform Application Selection and retrieve the Log Entry data element
located in the FCI Issuer Discretionary Data. If the Log Entry data element is
not present, the application does not support the Transaction Log function.
• Issue a GET DATA command to retrieve the Log Format data element.
• Issue READ RECORD commands to read the Transaction Log records.

D5 Example
Note that the following data elements are shown for example purposes only.
A Log Entry data element equal to '0F14' indicates that the transaction log file is
located in SFI 15 ('0F') and contains a maximum of 20 records ('14').
A Log Format data element equal to '9A039F21035F2A029F02069F4E149F3602'
indicates that the transaction log records have the following content:
Data Content Tag Length
Transaction Date '9A' 3
Transaction Time '9F21' 3
Transaction Currency Code '5F2A' 2
Amount, Authorised '9F02' 6
Merchant Name and Location '9F4E' 20
Application Transaction Counter '9F36' 2
Table 45: Example of Log Format
In Table 45, lengths and tags are shown for clarity. They do not appear in the log
record which is the concatenation of values (no TLV coding).
Data elements listed in the Log Format may come from the terminal and the
card. Terminal data elements such as Merchant Name and Location might have
been passed to the card in the PDOL or CDOL data.

```

```plaintext
7081fb9f4681b05db8d140820b5368d4c79ddd2d60d49bcd530acaf391a4a05e1cc7ae051d9fab12a03daa4a1b9bf7a66d2ac3f86ea0ed8b93e1af74ad6d27dfd7932a28015dcf67eaf11cfa5d049dd913369a828e12e6d6b106829cf30e94b7910cc77f885062adb73aac1c1ee636653af378d51f7b38431097f433feb2f7bc396ad3e39e2e21706bb7bb3d333a1254797e856fc5999e5eec9c97ffebcd049cb7c888293320cd5c15cc819e52800d8c7cd74c6cd019a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009000
70 81
 0x81 = 129 decimal 
  
  fb9f 46 81b05db8d140820b5368d4c79ddd2d60d49bcd530acaf391a4a05e1cc7ae051d9fab12a03daa4a1b9bf7a66d2ac3f86ea0ed8b93e1af74ad6d27dfd7932a28015dcf67eaf11c
     0x46 = 70 dec
    fa5d049dd913369a828e12e6d6b106829cf30e94b7910cc77f885062adb73aac1c1ee636653af378d51f7b38431097f433feb2f7bc396ad3e39e2e21706bb7bb3d333a1254797e856fc5999e5eec9c97ffebcd049cb7c888293320cd5c15cc819e52800d8c7cd74c6cd019a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009000
```

