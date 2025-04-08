import java.util.HashMap;

public class TwosComplimentConversion {

    /**
     * Accepts an array of characters representing the bits in a two's complement number
     * and returns the decimal equivalent.
     * precondition: This method requires that the maximum length of the parameter array is 16.
     * postcondition: The value returned is the decimal equivalent of the two's complement
     * parameter.
     * The parameter array is unchanged.
     * @param theBits an array representing the bits in a two's complement number
     * @throws IllegalArgumentException if the length of the parameter array > 16.
     * @return the decimal equivalent of the two's complement parameter
     */
    public static int convert2sCompToDecimal(final char[] theBits) {
        int result = 0;
        HashMap<Integer, Integer> binaryMap = new HashMap<>();

        if(theBits.length > 16) {
            throw new IllegalArgumentException("The array length is greater than 16");
        }


        for(int i = 0; i < theBits.length; i++){
            binaryMap.put(i, (int)Math.pow(2, i));
        }

        for(int i = theBits.length - 1; i > -1; i--){
            if(theBits[0] == 1){
                result = -binaryMap.get(theBits.length - 1);
            }
            if(theBits[i] == 1) {
                result += binaryMap.get(i);
            }
        }




        return result;
    }
// Add a comment here indicating which algorithm you implemented.
/**
 * Accepts a decimal parameter and returns an array of characters
 * representing the bits in the 16 bit two's complement equivalent.
 * precondition: This method requires that the two's complement equivalent
 * won't require more than 16 bits
 * postcondition: The returned array represents the 16 bit two's complement equivalent
 * of the decimal parameter.
 * @param theDecimal the decimal number to convert to two's complement
 * @throws IllegalArgumentException if the parameter cannot be represented in 16 bits.
 * @return a 16 bit two's complement equivalent of the decimal parameter
 */ public static char[] convertDecimalTo2sComp(final int theDecimal) {
     return null;
    }
// Add a comment here indicating which algorithm you implemented.

    public static void main(String[] args) {
     char[] binary = new char[6];
     binary[0] = 1;
     binary[1] = 1;
     binary[2] = 1;
     binary[3] = 1;
     binary[4] = 1;
     binary[5] = 1;

        System.out.println(convert2sCompToDecimal(binary));
    }
}