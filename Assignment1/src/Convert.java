/**
 * Converts 2's complement binary up to 16-bits into a signed decimal.
 * Also converts a signed decimal into 2's complement binary.
 * @author Kassie Whitney
 * @version 4/10/25
 */

public class Convert {

    /**
     * Accepts an array of characters representing the bits in a two's complement number
     * and returns the decimal equivalent.
     * Precondition: This method requires that the maximum length of the parameter array is 16.
     * Post-condition: The value returned is the decimal equivalent of the two's complement
     * parameter.
     * The parameter array is unchanged.
     * @param theBits an array representing the bits in a two's complement number
     * @throws IllegalArgumentException if the length of the parameter array > 16.
     * @return the decimal equivalent of the two's complement parameter
     * @apiNote Time Complexity: O(n)
     */
    public static int convert2sCompToDecimal(final char[] theBits) {
        int result = 0;

        //Checks if the most significant bit is a 1.
        boolean isNegative = theBits[0] == '1';

        if(theBits.length > 16) {
            throw new IllegalArgumentException("The array length is greater than 16");
        }

        for(int i = 0; i < theBits.length; i++) {
            if(isNegative) {

                //the result begins at the most significant bit decimal value and turns it
                // negative.
                result = -1 * (int)Math.pow(2, theBits.length - 1);
                isNegative = false;
                continue;
            }
            if(theBits[i] == '1') {

                // Adds the binary value to the result when i = 0 through to i = 15; the
                // binary value is:
                // 2^(theBit.length - (0 + 1)) [starting value: 2^15].
                // 2^(theBit.length - (theBit.length - 1 + 1) [Last value: 2^0];

               result += (int) Math.pow(2, theBits.length - (i + 1));
            }
        }

        return result;
    }
    // Method: Add powers of 2
    //
    //The algorithm that I used is taking the highest bit value and subtract any true bit
    // value from it if the highest bit value is a 1 using the Math.pow(2,i) where I is the
    // index where its element is 1.
    // If the highest bit value is 0, then add up all the bit value using the Math.pow(2, i)
    // where i is the index of the array where its element is a 1.

   /**
    * Accepts a decimal parameter and returns an array of characters
    * representing the bits in the 16-bit two's complement equivalent.
    * Precondition: This method requires that the two's complement equivalent
    * won't require more than 16-bits.
    * Post-condition: The returned array represents the 16-bit two's complement equivalent
    * of the decimal parameter.
    * @param theDecimal the decimal number to convert to two's complement
    * @throws IllegalArgumentException if the parameter cannot be represented in 16 bits.
    * @return a 16-bit two's complement equivalent of the decimal parameter.
    * @apiNote Time Complexity: O(n).
    */
    public static char[] convertDecimalTo2sComp(final int theDecimal) {
        int tempDecimal = theDecimal;

        int bitWidth = 16;

        boolean isOdd = theDecimal % 2 == 1 || theDecimal % 2 == -1;

        char[] binary = new char[bitWidth];

        int maxBinDecimal = (int) Math.pow(2, (bitWidth - 1));

        //checks if the theDecimal is at most 16-bits.
        if (theDecimal > ((int) Math.pow(2, (bitWidth - 1))) - 1 //32767
                || theDecimal < -1 * (int) Math.pow(2, (bitWidth - 1))) { //-32768
            throw new IllegalArgumentException("theDecimal exceeds the limit!");
        }

        //Checks if theDecimal is positive.
        if(theDecimal > -1) {

            //Adds a 1 to the last bit to indicate an odd number was passed.
            if (isOdd) {
                binary[15] = '1';
                tempDecimal -= 1;
            }

            for(int i = 0; i < binary.length; i++) {
                //Checks if the next binary decimal value
                // (maxBinDecimal) is less than theDecimal.
                if (maxBinDecimal > 1 && tempDecimal >= maxBinDecimal) {

                    //Appends '1' to the binary array from left to right to denote negative
                    // 2's complement binary.
                    binary[(bitWidth - 1) - log2(maxBinDecimal)] = '1';
                    tempDecimal -= maxBinDecimal;
                }
                //Gives us the next binary decimal value.
                maxBinDecimal /= 2;
            }


        } else {

            if (isOdd) {
                binary[15] = '1';
                tempDecimal -= 1;
            }

            // Appends '1' to the binary array at the bit position if the bit decimal value,
            // at the bit current bit position, was used
            // to subtract from tempDecimal (theDecimal).
            for(int i = 0; i < binary.length; i++) {

                if(maxBinDecimal >= (-1 * tempDecimal)) {
                    binary[i] = '1';
                }

                if(maxBinDecimal/2  < (-1 * tempDecimal)) {

                    break;
                }

                maxBinDecimal /= 2;

            }
            tempDecimal *= -1;

            if(maxBinDecimal > tempDecimal) {
                tempDecimal = maxBinDecimal - tempDecimal;
            }
            while (maxBinDecimal > 1) {

                if(maxBinDecimal <= tempDecimal) {
                    tempDecimal -= maxBinDecimal;
                    binary[(bitWidth - 1) - log2(maxBinDecimal)] = '1';
                }

                maxBinDecimal /= 2;

            }
        }

        //Converts char null characters into 0's.
        for (int i = 0; i < binary.length; i++) {
                if(binary[i] == 0){
                    binary[i] = '0';
                }
        }

        return binary;
    }

    // Method: Subtract Powers of 2.
    //
    // The algorithm that I used is,
    // if the decimal is positive, I find the closest binary value
    // that is still less than the Decimal.
    // Then I subtract that value from the Decimal and
    // place a 1 at the position of which the binary value was used.
    // So if 32 (2^5) was used to
    // subtract from 61, then I put 1 in the binary position for 32 (2^5).
    // The algorithm that I used for when the Decimal is negative, insert a 1 for every binary
    // value that is greater than the absolute value of the Decimal.
    // I then subtract the
    // Decimal from the lowest binary value that is greater than the Decimal.
    // I then check
    // the next lowest binary value to see if it's smaller than the Decimal, if it is, I
    // subtract that binary value from the Decimal and repeat the process.

    /**
     * Converts log base 10 to log base 2 to get the exponent value for power 2 decimal value.
     * @param x the decimal value you want to extract the exponent value from.
     * @return the exponent value for power 2.
     */
    private static int log2(final int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    private static int bitWidth(final int theDecimal) {
        int tempDecimal = theDecimal;
        int bitWidth = 1;
        if (tempDecimal%2 == 1) {
                tempDecimal -= 1;
            }
        while (tempDecimal < -1) {


        }
        return bitWidth;
    }

    public static void main(String[] args) {

        System.out.println(convertDecimalTo2sComp(126)); //0000 0000 0111 1110
        System.out.println(convertDecimalTo2sComp(31));  //0000 0000 0001 1111
        System.out.println(convertDecimalTo2sComp(32156));//0111 1101 1001 1100
        System.out.println(convertDecimalTo2sComp(-74)); //1111 1111 1011 0110
        System.out.println(convertDecimalTo2sComp(-12354)); //1100 1111 1011 1110
        System.out.println(convertDecimalTo2sComp(-32175)); //1000 0010 0101 0001
        System.out.println(convert2sCompToDecimal(convertDecimalTo2sComp(32767)));
        //32767

    }
}