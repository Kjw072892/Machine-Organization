import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Test;

/**
 * Unit tests of the methods in the Convert class.
 *
 * 
 * @author Alan Fowler
 * @version 1.3
 * @apiNote  Refactored the Test class to be compatible with intellij file systems.
 */
public class ConvertTest {

    // Binary to Decimal Tests

    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompPositive0with1Bit() {
        final char[] data = {'0'}; 
        assertEquals(0, Convert.convert2sCompToDecimal(data));
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompPositive0with16Bits() {
        final char[] data = {'0', '0', '0', '0', '0', '0', '0', '0',
                             '0', '0', '0', '0', '0', '0', '0', '0'};
        assertEquals(0, Convert.convert2sCompToDecimal(data));
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompPositive1() {
        final char[] data = {'0', '1'};
        assertEquals(1, Convert.convert2sCompToDecimal(data));
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompNegative1with1bit() {
        final char[] data = {'1'};
        assertEquals(-1, Convert.convert2sCompToDecimal(data));
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompNegative1with16Bits() {
        final char[] data = {'1', '1', '1', '1', '1', '1', '1', '1',
                             '1', '1', '1', '1', '1', '1', '1', '1'};
        assertEquals(-1, Convert.convert2sCompToDecimal(data));
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */

    @Test
    public void test2sCompMaxPos16BitValue() {
        final char[] data = {'0', '1', '1', '1', '1', '1', '1', '1',
                             '1', '1', '1', '1', '1', '1', '1', '1'};
        assertEquals(32767, Convert.convert2sCompToDecimal(data));
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompMinNeg16BitValue() {
        final char[] data = {'1', '0', '0', '0', '0', '0', '0', '0',
                             '0', '0', '0', '0', '0', '0', '0', '0'};
        assertEquals(-32768, Convert.convert2sCompToDecimal(data));
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompDoesNotChangePosParameter() {
        final char[] data = {'0', '1', '1', '1', '1', '1', '1', '1',
                             '1', '1', '1', '1', '1', '1', '1', '1'};
        final char[] dataClone = data.clone();
        Convert.convert2sCompToDecimal(data);
        assertArrayEquals(data, dataClone);
        
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompDoesNotChangeNegParameter() {
        final char[] data = {'1', '0', '0', '0', '0', '0', '0', '0',
                             '0', '0', '0', '0', '0', '0', '0', '0'};
        int decimal = Convert.convert2sCompToDecimal(data);
        final char[] dataClone = Convert.convertDecimalTo2sComp(decimal);
        assertArrayEquals(data, dataClone);

        //I did see that the test was testing to check if the clone was equal to the data
        // without actually doing anything to either the clone or data since originally
        // dataClone = data.clone(); and the test was checking if dataClone and data were
        // equal. So I changed this test method so that it would run the data through the
        // decimal converter and then through the binary converter to check if the data
        // remained consistent.
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompIllegalPos17BitValue() {
        final char[] data = {'0', '1', '1', '1', '1', '1', '1', '1',
                             '1', '1', '1', '1', '1', '1', '1', '1', '1'};
        
        assertThrows(IllegalArgumentException.class, () ->
                Convert.convert2sCompToDecimal(data));
    }
    
    /**
     * Test method for {@link Convert#convert2sCompToDecimal(char[])}.
     */
    @Test
    public void test2sCompMinIllegalNeg17BitValue() {
        final char[] data = {'1', '0', '0', '0', '0', '0', '0', '0',
                             '0', '0', '0', '0', '0', '0', '0', '0', '0'};
        assertThrows(IllegalArgumentException.class, () ->
            Convert.convert2sCompToDecimal(data)
        );
    }
    

    // Decimal to Binary
    
    /**
     * Test method for {@link Convert#convertDecimalTo2sComp(int)}.
     */
    @Test
    public void testConvertDecimalTo2sCompZero() {
        final int data = 0;
        final char[] expected =
            {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
        final char[] result = Convert.convertDecimalTo2sComp(data);
        assertArrayEquals(expected, result);
    }
    
    /**
     * Test method for {@link Convert#convertDecimalTo2sComp(int)}.
     */
    @Test
    public void testConvertDecimalTo2sCompPositive1() {
        final int data = 1;
        final char[] expected =
            {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1'};
        final char[] result = Convert.convertDecimalTo2sComp(data);
        assertArrayEquals(expected, result);
    }
    
    /**
     * Test method for {@link Convert#convertDecimalTo2sComp(int)}.
     */
    @Test
    public void testConvertDecimalTo2sCompPositiveMAX() {
        final int data = 32767;
        final char[] expected =
            {'0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1'};
        final char[] result = Convert.convertDecimalTo2sComp(data);
        assertArrayEquals(expected, result);
    }
    
    /**
     * Test method for {@link Convert#convertDecimalTo2sComp(int)}.
     */
    @Test
    public void testConvertDecimalTo2sCompNegative1() {
        final int data = -1;
        final char[] expected =
            {'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1'};
        final char[] result = Convert.convertDecimalTo2sComp(data);
        assertArrayEquals(expected, result);
    }
    
    /**
     * Test method for {@link Convert#convertDecimalTo2sComp(int)}.
     */
    @Test
    public void testConvertDecimalTo2sCompNegativeMAX() {
        final int data = -32768;
        final char[] expected =
            {'1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
        final char[] result = Convert.convertDecimalTo2sComp(data);
        assertArrayEquals(expected, result);
    }
    
    /**
     * Test method for {@link Convert#convertDecimalTo2sComp(int)}.
     */
    @Test
    public void testConvertDecimalTo2sCompIllegalPositiveMAX() {
        final int data = 32768;
        assertThrows(IllegalArgumentException.class, () ->
            Convert.convertDecimalTo2sComp(data)
        );
    }
    
    /**
     * Test method for {@link Convert#convertDecimalTo2sComp(int)}.
     */
    @Test
    public void testConvertDecimalTo2sCompIllegalNegativeMAX() {
        final int data = -32769;
        assertThrows(IllegalArgumentException.class, () ->
                Convert.convertDecimalTo2sComp(data)
        );
    }


}
