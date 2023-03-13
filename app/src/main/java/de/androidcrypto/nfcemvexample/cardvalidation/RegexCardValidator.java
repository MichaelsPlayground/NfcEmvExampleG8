package de.androidcrypto.nfcemvexample.cardvalidation;

/**
 * Validator for credit card numbers
 * Checks validity and returns card type
 *
 * @author ian.chen
 * Source: https://gist.github.com/icchan/47d83bacc5113db59fbc
 */
public class RegexCardValidator {

    /**
     * Checks if the field is a valid credit card number.
     * @param cardIn The card number to validate.
     * @return Whether the card number is valid.
     */
    public static CardValidationResult isValid(final String cardIn) {
        String card = cardIn.replaceAll("[^0-9]+", ""); // remove all non-numerics
        if ((card == null) || (card.length() < 13) || (card.length() > 19)) {
            return new CardValidationResult(card,"failed length check");
        }

        if (!luhnCheck(card)) {
            return new CardValidationResult(card,"failed luhn check");
        }

        CardCompany cc = CardCompany.gleanCompany(card);
        if (cc == null) return new CardValidationResult(card,"failed card company check");

        return new CardValidationResult(card,cc);
    }

    /**
     * Checks for a valid credit card number.
     * @param cardNumber Credit Card Number.
     * @return Whether the card number passes the luhnCheck.
     */
    protected static boolean luhnCheck(String cardNumber) {
        // number must be validated as 0..9 numeric first!!
        int digits = cardNumber.length();
        int oddOrEven = digits & 1;
        long sum = 0;
        for (int count = 0; count < digits; count++) {
            int digit = 0;
            try {
                digit = Integer.parseInt(cardNumber.charAt(count) + "");
            } catch(NumberFormatException e) {
                return false;
            }

            if (((count & 1) ^ oddOrEven) == 0) { // not
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        return (sum == 0) ? false : (sum % 10 == 0);
    }

    /**
     * Run some tests to show this works
     * @param args
     */
    public static void main(String[] args) {
        String visa = "4444444444444448";
        String master = "5500005555555559";
        String amex = "371449635398431";
        String diners = "36438936438936";
        String discover = "6011016011016011";
        String jcb = "3566003566003566";
        String luhnFail = "1111111111111111";

        String invalid = "abcdabcdabcdabcd";

        printTest(visa);
        printTest(master);
        printTest(amex);
        printTest(diners);
        printTest(discover);
        printTest(jcb);
        printTest(invalid);
        printTest(luhnFail);

    }

    /**
     * Check a card number and print the result
     * @param cardIn
     */
    private static void printTest(String cardIn) {
        CardValidationResult result = RegexCardValidator.isValid(cardIn);
        System.out.println(result.isValid() + " : " + (result.isValid()? result.getCardType().getIssuerName(): "")  + " : " + result.getMessage() );
    }
}
