# Android examples

This is a collection of my examples for Android (Java) in these categories:

- internal storage management
- external storage management
- image management
- encryption
- shared preferences
- encrypted shared preferences
- NFC (real device needed)
- Material edittext
- Material switch

Soundfiles: https://mobcup.net/ringtone/ping-euf272ye/download/mp3

For prettyPrint: include implementation 'com.github.devnied.emvnfccard:library:3.0.1'

## Permission handling

For some categories we do need permissions (declared in AndroidManifest.xml) and sometimes a 
runtime granting by the user:

AndroidManifest.xml:
```plaintext
    <!-- needed for NFC -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.VIBRATE" />
    
```

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


