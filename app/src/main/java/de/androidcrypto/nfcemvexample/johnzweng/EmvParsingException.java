package de.androidcrypto.nfcemvexample.johnzweng;

/**
 * Parsing Exception
 *
 * @author Johannes Zweng (johannes@zweng.at) on 23.10.17.
 * source: https://github.com/johnzweng/android-emv-key-test/blob/master/app/src/main/java/at/zweng/emv/utils/EmvParsingException.java
 */
public class EmvParsingException extends Exception {
    public EmvParsingException() {
    }

    public EmvParsingException(String message) {
        super(message);
    }

    public EmvParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}