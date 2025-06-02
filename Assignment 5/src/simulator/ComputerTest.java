package simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



/**
 * Junit test for the Computer class
 *
 * @author Kassie Whitney (kjw0728)
 * @version 5.31.25
 */
class ComputerTest {
	
	// An instance of the Computer class to use in the tests.
	private Computer myComputer;

	@BeforeEach
	void setUp() {
		myComputer = new Computer();
	}

	/*
	 * NOTE:
	 * Programs in unit tests should ideally have one instruction per line
	 * with a comment for each line.
	 */

	/**
	 * Test method for {@link simulator.Computer#executeBranch()}.
	 */
	@Test
	void testExecuteBranchNZP() {

		//The expected condition code
		char[] ccExpected = myComputer.getCC().getBits();

		String[] programNzp = {
				"0000 111 0 0000 0001",//BR pc + 1 (pc = 1)
				"1111 0000 0010 0101",//TRAP HALT PC = 2
				"1111 0000 0010 0101"//TRAP HALT PC = 3
		};

		myComputer.loadMachineCode(programNzp);
		myComputer.execute();
		//myComputer.display();

		assertAll("Testing BRnzp instruction",
				() -> assertEquals(3, myComputer.getPC().getUnsignedValue(),
						"PC should have been 3. But your PC is at "
								+ myComputer.getPC().getUnsignedValue() + "!"),

				//Tests if condition code changed because of the BR instruction
				() -> assertArrayEquals(ccExpected, myComputer.getCC().getBits(),
						"Your Condition Code was updated unexpectedly!")
		);
	}

	/**
	 * Test method for {@link simulator.Computer#executeBranch()}.
	 */
	@Test
	void testExecuteBranchNegative() {

		//The expected condition code
		char[] ccExpected = myComputer.getCC().getBits();
		ccExpected[0] = '1';

		String[] programNegative = {
				"1001 000 000 111111",//Not R0
				"0000 100 0 0000 0001",//BR pc + 1 (pc = 4)
				"1111 0000 0010 0101",//TRAP HALT PC = 3
				"1111 0000 0010 0101"//TRAP HALT PC = 4
		};

		myComputer.loadMachineCode(programNegative);
		myComputer.execute();
		//myComputer.display();


		assertAll("Testing BRn instruction",
				() -> assertEquals(4, myComputer.getPC().getUnsignedValue(),
						"PC should have been 4. The PC offset is wrong!"),

				//Tests if condition code changed because of the BR instruction
				() -> assertArrayEquals(ccExpected, myComputer.getCC().getBits(),
						"Your Condition Code was updated unexpectedly!")
		);
	}

	/**
	 * Test method for {@link simulator.Computer#executeBranch()}.
	 */
	@Test
	void testExecuteBranchPositive() {

		//The expected condition code
		char[] ccExpected = myComputer.getCC().getBits();
		ccExpected[2] = '1';

		String[] programPositive = {
				"1001 110 110 111111",//Not R6
				"1001 110 110 111111",//Not R6
				"0000 001 0 0000 0001",//BR pc + 1 (pc = 5)
				"1111 0000 0010 0101",//TRAP HALT PC = 4
				"1111 0000 0010 0101"//TRAP HALT PC = 5
		};

		myComputer.loadMachineCode(programPositive);
		myComputer.execute();
		//myComputer.display();

		assertAll("Testing BRp instruction",
				() -> assertEquals(5, myComputer.getPC().getUnsignedValue(),
						"PC should have been 5. The PC offset is wrong!"),

				//Tests if condition code changed because of the BR instruction
				() -> assertArrayEquals(ccExpected, myComputer.getCC().getBits(),
						"Your Condition Code was updated unexpectedly!")
		);
	}
	/**
	 * Test method for {@link simulator.Computer#executeBranch()}.
	 */
	@Test
	void testExecuteBranchZero() {

		//The expected condition code
		char[] ccExpected = myComputer.getCC().getBits();
		ccExpected[1] = '1';

		String[] programZero = {
				"1001 000 000 111111",//Not R0
				"1001 000 000 111111",//Not R0
				"0000 010 0 0000 0001",//BR pc + 1 (pc = 5)
				"1111 0000 0010 0101",//TRAP HALT PC = 4
				"1111 0000 0010 0101"//TRAP HALT PC = 5
		};

		myComputer.loadMachineCode(programZero);
		myComputer.execute();
		//myComputer.display();

		assertAll("Testing BRz instruction",
				() -> assertEquals(5, myComputer.getPC().getUnsignedValue(),
						"PC should have been 5. The PC offset is wrong!"),

				//Tests if condition code changed because of the BR instruction
				() -> assertArrayEquals(ccExpected, myComputer.getCC().getBits(),
						"Your Condition Code was updated unexpectedly!")
		);
	}

	/**
	 * Test method for {@link simulator.Computer#executeLoad()}.
	 */
	@Test
	void testExecuteLoad() {

		//The expected condition code
		char[] ccExpected = myComputer.getCC().getBits();

		ccExpected[0] = '1';
		ccExpected[1] = '0';

		String[] program = {
				"0010 000 0 0000 0001",//R0 <- -39
				"1111 0000 0010 0101",//TRAP HALT
				"1111 1111 1101 1001",//-39
				"0000 0000 0011 1111",//x3f
		};

		myComputer.loadMachineCode(program);
		myComputer.execute();
		//myComputer.display();

		assertAll("Testing LD instruction",
				() -> assertEquals(-39, myComputer.getRegisters()[0].get2sCompValue(),
						"Register 0 should have had -39 loaded into it! But got "
								+ myComputer.getRegisters()[0].get2sCompValue() + " " + "instead!"),

				//Tests if condition code changed properly
				() -> assertArrayEquals(ccExpected, myComputer.getCC().getBits(),
						"Your Condition Code was not updated properly!")
		);
	}
	
	/**
	 * Test method for {@link simulator.Computer#executeStore()}.
	 */
	@Test
	void testExecuteStore() {

		//The expected condition code
		char[] ccDefault = myComputer.getCC().getBits();

		String[] program = {
				"0011 111 0 0000 0001",//Memory[2] <- R7 (7)
				"1111 0000 0010 0101",//TRAP HALT PC = 2
				"0000 0000 0011 1001",//x39 (57) Memory[2]
		};



		myComputer.loadMachineCode(program);
		myComputer.execute();
		//myComputer.display();

		assertAll("Testing ST instruction",
				() -> assertEquals(7, myComputer.getMemory()[2].get2sCompValue(),
						"Memory location 2 should have had 7 loaded into it! But got"
								+ " 57 instead!"),

				//Checks if the condition code was changed because of the ST instruction
				() -> assertArrayEquals(ccDefault, myComputer.getCC().getBits(),
						"Your Condition Code was updated unexpectedly!")
		);
	}

	/**
	 * Test method for {@link simulator.Computer#executeAnd()}.
	 */
	@Test
	void testExecuteAnd() {

		//The expected condition code
		char[] expectedCC = new char[]{'0','0','1'};

		String[] program = {
				"0101 000 110 0 00 111",//R0 <- R6 & R7
				"1111 0000 0010 0101",//TRAP HALT PC = 2
		};

		myComputer.loadMachineCode(program);
		myComputer.execute();
		//myComputer.display();

		assertAll("Testing AND instruction",
				() -> assertEquals(6, myComputer.getRegisters()[0].get2sCompValue(),
						"Your AND instruction is incorrect!"),

				//Tests if condition code was set properly
				() -> assertArrayEquals(expectedCC, myComputer.getCC().getBits(),
						"Your Condition Code did not update correctly!")
		);
	}

	/**
	 * Test method for {@link simulator.Computer#executeNot()}.
	 */
	@Test
	void testExecuteNot5() {
		// NOTE: R5 contains #5 initially when the Computer is instantiated
		// So, iF we execute R4 <- NOT R5, then R4 should contain 1111 1111 1111 1010    (-6)
		// AND CC should be 100
		
		String[] program = {
			"1001 100 101 111111",    // R4 <- NOT R5
			"1111 0000 0010 0101"     // TRAP - vector x25 - HALT
		};
		
		myComputer.loadMachineCode(program);
		myComputer.execute();
		//myComputer.display();
		
		assertEquals(-6, myComputer.getRegisters()[4].get2sCompValue());
		
		// Check that CC was set correctly
		BitString expectedCC = new BitString();
		expectedCC.setBits("100".toCharArray());
		assertEquals(expectedCC.get2sCompValue(), myComputer.getCC().get2sCompValue());

	}
	
	/**
	 * Test method for {@link simulator.Computer#executeAdd()}. <br>
	 * Computes 2 + 2. R0 <- R2 + R2
	 */
	@Test
	void testExecuteAddR2PlusR2() {
		
		String[] program = {
				"0001 000 010 0 00 010",  // R0 <- R2 + R2 (#4)
		     	"1111 0000 0010 0101" // HALT
		};
		
		myComputer.loadMachineCode(program);
		myComputer.execute();
		//myComputer.display();

		assertEquals(4, myComputer.getRegisters()[0].get2sCompValue());
		
		// Check that CC was set correctly
		BitString expectedCC = new BitString();
		expectedCC.setBits("001".toCharArray());
		assertEquals(expectedCC.get2sCompValue(), myComputer.getCC().get2sCompValue());
	}
	
	/**
	 * Test method for {@link simulator.Computer#executeAdd()}. <br>
	 * Computes 2 + 3. R0 <- R2 + #3
	 */
	@Test
	void testExecuteAddR2PlusImm3() {
		
		String[] program = {
				"0001 000 010 1 00011",  // R0 <- R2 + #3
		     	"1111 0000 0010 0101"  // HALT
		};
		
		myComputer.loadMachineCode(program);
		myComputer.execute();
		//myComputer.display();

		assertEquals(5, myComputer.getRegisters()[0].get2sCompValue());
		
		// Check that CC was set correctly
		BitString expectedCC = new BitString();
		expectedCC.setBits("001".toCharArray());
		assertEquals(expectedCC.get2sCompValue(), myComputer.getCC().get2sCompValue());
	}
	
	/**
	 * Test method for {@link simulator.Computer#executeAdd()}. <br>
	 * Computes 2 - 3. R0 <- R2 + #-3
	 */
	@Test
	void testExecuteAddR2PlusImmNeg3() {
		
		String[] program = {
				"0001 000 010 1 11101",  // R0 <- R2 + #-3
		     	"1111 0000 0010 0101" // HALT
		};
		
		myComputer.loadMachineCode(program);
		myComputer.execute();
		//myComputer.display();

		assertEquals(-1, myComputer.getRegisters()[0].get2sCompValue());
		
		// Check that CC was set correctly
		BitString expectedCC = new BitString();
		expectedCC.setBits("100".toCharArray());
		assertEquals(expectedCC.get2sCompValue(), myComputer.getCC().get2sCompValue());
	}

}
