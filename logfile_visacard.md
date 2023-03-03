# Logfile Visacard

The data in this document come from a real but outdated creditcard, so I don't mind any security problems.

**VISA debit card from Lloyds Bank**

There are xx steps to read the PAN (primary account number = credit card number) and the card's expiration date:

1) **select PPSE**: this will retrieve all applications and their **Application ID** available on the card
2) **analyze select PPSE respond**: search for tag(s) 0x4F (Application Identifier (AID) - card)
3) **select Application**: select an application by its ID (AID) found in step 2, after this step the card is released for further readings
4) **search for tag 0x9F38 in the response from step 3** The tag 0x9F38 is the **Processing Options Data Object List (PDOL)** that we need for further processing
5) 
6) 
7) **get the processing options**: providing PDOL-data from step 3 & 4 gives the **AFL application file list** where we can read the data
8) 
9) 
10) 
11) **read the files from card**: think of a file directory and the AFL from step 4 lists all files on the card. Read each file and try 
to find the data we want to show (PAN and expiration date)
12) **search in each file for the tag 0x57**: tag 0x57 is the **Track 2 Equivalent Data** that has the PAN and expiration date as data fields.
13) **get PAN and expiration date** from the content in tag 0x57 value.

Expiration date format is YYMM

Soundfiles: https://mobcup.net/ringtone/ping-euf272ye/download/mp3

Below is a full workflow for the steps above. In most cases there are 3 parts for each step:
- the command send to the card in hex encoding
- the response from the card in hex encoding
- the human readable analyze of the response, manually by copy & paste from a TLV decoder website.

For my manual analysis I used the "official" website https://emvlab.org/tlvutils/ 

Second note: the response from the card has 2 additional bytes at the end (0x9000) that indication that the processing was successful. 
For better reading experience I cut them off.

**step 01 select PPSE**

```plaintext
command:  00a404000e325041592e5359532e444446303100
response: 6f2b840e325041592e5359532e4444463031a519bf0c1661144f07a00000000310109f0a080001050100000000
6F File Control Information (FCI) Template
 	84 Dedicated File (DF) Name
 	 	325041592E5359532E4444463031
 	A5 File Control Information (FCI) Proprietary Template
 	 	BF0C File Control Information (FCI) Issuer Discretionary Data
 	 	 	61 Application Template
 	 	 	 	4F Application Identifier (AID) – card
 	 	 	 	 	A0000000031010
 	 	 	 	9F0A Unknown tag
 	 	 	 	 	0001050100000000
```

**step 02 analyze select PPSE respond**
```plaintext
4F Application Identifier (AID) – card
   A0000000031010
```

**step 03 select Application** by using the AID A0000000031010
```plaintext
command:  00a4040007a000000003101000
response: 6f5d8407a0000000031010a5525010564953412044454249542020202020208701029f38189f66049f02069f03069f1a0295055f2a029a039c019f37045f2d02656ebf0c1a9f5a0531082608269f0a080001050100000000bf6304df200180
6F File Control Information (FCI) Template
 	84 Dedicated File (DF) Name
 	 	A0000000031010
 	A5 File Control Information (FCI) Proprietary Template
 	 	50 Application Label
 	 	 	V I S A D E B I T
 	 	87 Application Priority Indicator
 	 	 	02
 	 	9F38 Processing Options Data Object List (PDOL)
 	 	 	9F66049F02069F03069F1A0295055F2A029A039C019F3704
 	 	5F2D Language Preference
 	 	 	e n
 	 	BF0C File Control Information (FCI) Issuer Discretionary Data
 	 	 	9F5A Unknown tag
 	 	 	 	3108260826
 	 	 	9F0A Unknown tag
 	 	 	 	0001050100000000
 	 	 	BF63 Unknown tag
 	 	 	 	DF20 Unknown tag
 	 	 	 	 	80
```

**step 04 search for tag 0x9F38 in the response from step 03**
```plaintext
9F38 Processing Options Data Object List (PDOL)
     9F66049F02069F03069F1A0295055F2A029A039C019F3704
```

Note: for the next step we don't try to fill the PDOL with corret data as requested by the received tag. Instead we are filling the PDOL with 0x00 
but leave the requested length.

**step 05 get the processing options**: the card needs a real "terminal country code" so this field is filled with real data: 
```plaintext
command:  80A80000238321A0000000000000000001000000000000084000000000000840070203008017337000
response: 77478202200057134921828094896752d25022013650000000000f5f3401009f100706040a03a020009f26089f98ecaea782d0739f2701809f3602033c9f6c0216009f6e0420700000
77 Response Message Template Format 2
 	82 Application Interchange Profile
 	 	2000
 	57 Track 2 Equivalent Data
 	 	4921828094896752D25022013650000000000F
 	5F34 Application Primary Account Number (PAN) Sequence Number
 	 	00
 	9F10 Issuer Application Data
 	 	06040A03A02000
 	9F26 Application Cryptogram
 	 	9F98ECAEA782D073
 	9F27 Cryptogram Information Data
 	 	80
 	9F36 Application Transaction Counter (ATC)
 	 	033C
 	9F6C Unknown tag
 	 	1600
 	9F6E Unknown tag
 	 	20700000
```


```plaintext
command: 
response: 

```


```plaintext
command: 
response: 77478202200057134921828094896752d25022013650000000000f5f3401009f100706040a03a020009f26089f98ecaea782d0739f2701809f3602033c9f6c0216009f6e0420700000
77 Response Message Template Format 2
 	82 Application Interchange Profile
 	 	2000
 	57 Track 2 Equivalent Data
 	 	4921828094896752D25022013650000000000F
 	5F34 Application Primary Account Number (PAN) Sequence Number
 	 	00
 	9F10 Issuer Application Data
 	 	06040A03A02000
 	9F26 Application Cryptogram
 	 	9F98ECAEA782D073
 	9F27 Cryptogram Information Data
 	 	80
 	9F36 Application Transaction Counter (ATC)
 	 	033C
 	9F6C Unknown tag
 	 	1600
 	9F6E Unknown tag
 	 	20700000
```


```plaintext
57 Track 2 Equivalent Data
4921828094896752D25022013650000000000F
or in details:
4921828094896752 
                 D 
                   2502 
                        ...
PAN/card number
                 D = separator
                   expiration date in format YYMM
                        other data
```


```plaintext
command: 
response: 

```

