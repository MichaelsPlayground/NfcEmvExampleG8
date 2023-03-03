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

## Permission handling

For some categories we do need permissions (declared in AndroidManifest.xml) and sometimes a 
runtime granting by the user:

AndroidManifest.xml:
```plaintext
...
    <!-- needed for NFC -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.VIBRATE" />
    
...    
    
```
