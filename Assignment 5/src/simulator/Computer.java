package simulator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * The Computer class is composed of registers, memory, PC, IR, and CC.
 * The Computer can execute a program based on the the instructions in memory.
 *  
 * @author mmuppa
 * @author acfowler
 * @version 4.1
 */
public class Computer {

	/**
	 * The max memory allocation
	 */
	private final static int MAX_MEMORY = 50;

	/**
	 * max number of usable registers
	 */
	private final static int MAX_REGISTERS = 8;

	/**
	 * An array of size 8 where index 0 is R0 and index 7 is R7
	 */
	private final BitString[] mRegisters;

	/**
	 * An array of size 50 where index 0 is 0x3000 and 49 is 0x3032
	 */
	private final BitString[] mMemory;

	/**
	 * The program counter. Stores the memory address of the next instruction
	 */
	private final BitString mPC;

	/**
	 * Stores the current instruction
	 */
	private BitString mIR;

	/**
	 * A 3-bit array representing the condition code where
	 * [1,0,0] is negative, [0,1,0] is zero, [0,0,1] is positive
	 */
	private final BitString mCC;

	/**
	 * Initialize all memory addresses to 0, registers to 0 to 7
	 * PC, IR to 16 bit 0s and CC to 000.
	 */

	public Computer() {
		mPC = new BitString();
		mPC.setUnsignedValue(0); //creates a PC with [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]

		mIR = new BitString();
		mIR.setUnsignedValue(0); //creates an IR with [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]

		mCC = new BitString();
		mCC.setBits(new char[]{'0', '0', '0'}); //creates a Condition Code of [0,0,0]
		// [1,0,0] -> negative, [0,1,0] -> zero, [0,0,1] -> positive

		mRegisters = new BitString[MAX_REGISTERS];
		for (int i = 0; i < MAX_REGISTERS; i++) {
			mRegisters[i] = new BitString();
			mRegisters[i].setUnsignedValue(0);
		}
		//[R0, R1, R2, R3, R4, R5, R6, R7] the elements represents the registers

		//An array with 50 slots where each empty element represents an unused memory location
		mMemory = new BitString[MAX_MEMORY];
		for (int i = 0; i < MAX_MEMORY; i++) {
			mMemory[i] = new BitString();
			mMemory[i].setUnsignedValue(0);
		}
	}
	
	// The public accessor methods shown below are useful for unit testing.
	// Do NOT add public mutator methods (setters)!
	
	/**
	 * @return the registers
	 */
	public BitString[] getRegisters() {
		return copyBitStringArray(mRegisters);
	}

	/**
	 * @return the memory
	 */
	public BitString[] getMemory() {
		return copyBitStringArray(mMemory);
	}

	/**
	 * @return the PC
	 */
	public BitString getPC() {
		return mPC.copy();
	}

	/**
	 * @return the IR
	 */
	public BitString getIR() {
		return mIR.copy();
	}

	/**
	 * @return the CC
	 */
	public BitString getCC() {
		return mCC.copy();
	}
	
	/**
	 * Safely copies a BitString array.
	 * @param theArray the array to copy.
	 * @return a copy of theArray.
	 */
	private BitString[] copyBitStringArray(final BitString[] theArray) {
		BitString[] bitStrings = new BitString[theArray.length];
		Arrays.setAll(bitStrings, n -> bitStrings[n] = theArray[n].copy());
		return bitStrings;
	}

	/**
	 * Loads a 16 bit word into memory at the given address. 
	 * @param address memory address
	 * @param word data or instruction or address to be loaded into memory
	 */
	private void loadWord(int address, BitString word) {
		if (address < 0 || address >= MAX_MEMORY) {
			throw new IllegalArgumentException("Invalid address");
		}
		mMemory[address] = word;
	}
	
	/**
	 * Loads a machine code program, as Strings.
	 * @param theInstruction the Strings that contain the instructions or data.
	 */
	public void loadMachineCode(final String ... theInstruction) {
		if (theInstruction.length == 0 || theInstruction.length >= MAX_MEMORY) {
			throw new IllegalArgumentException("Invalid words");
		}
		for (int i = 0; i < theInstruction.length; i++) {
			final BitString instruction = new BitString();
			instruction.setBits(theInstruction[i].toCharArray());
			loadWord(i, instruction);
		}
	}
	
	
	
	
	
	// The next 6 methods are used to execute the required instructions:
	// BR, ADD, LD, ST, AND, NOT, TRAP
	
	/**
	 * op   nzp pc9offset
	 * 0000 000 000000000
	 * 
	 * The condition codes specified by bits [11:9] are tested.
	 * If bit [11] is 1, N is tested; if bit [11] is 0, N is not tested.
	 * If bit [10] is 1, Z is tested, etc.
	 * If any of the condition codes tested is 1, the program branches to the memory location specified by
	 * adding the sign-extended PCoffset9 field to the incremented PC.
	 */
	public void executeBranch() {
		
		// implement the BR instruction here

		//[15:12] -> 0's; [11:9] -> nzp; [8:0] -> PC Offset 9

		BitString nzp = mIR.substring(4, 3);
		BitString offSet = mIR.substring(7, 9);

		if(Arrays.equals(getCC().getBits(), nzp.getBits())) {
			int decOffSet = offSet.get2sCompValue();
			int decMPC = mPC.getUnsignedValue();

			if(decMPC + decOffSet < 0 || decMPC + decOffSet > 49) {
				throw new IllegalArgumentException("Your BR 9-bit offset falls outside the " +
						"scope of" +
						" your program! Please readjust your 9-bit offset!");
			}

			mPC.set2sCompValue(decMPC + decOffSet);
		}

	}
	
	/**
	 * op   dr  sr1      sr2
	 * 0001 000 000 0 00 000
	 * <p>
	 * OR
	 * <p>
	 * op   dr  sr1   imm5
	 * 0001 000 000 1 00000
	 * <p>
	 * If bit [5] is 0, the second source operand is obtained from SR2.
	 * If bit [5] is 1, the second source operand is obtained by sign-extending the imm5 field to 16 bits.
	 * In both cases, the second source operand is added to the contents of SR1 and the
	 * result stored in DR. The condition codes are set, based on whether the result is
	 * negative, zero, or positive.
	 */
	public void executeAdd() {

		BitString dr = getOperand("dr");
		BitString sr1 =  getOperand("sr1");
		BitString sr2 =  getOperand("sr2");
		BitString immediate =  getOperand("imm");

		boolean isImmediate = mIR.substring(10,1).getUnsignedValue() == 1;

		int totalWithImm = mRegisters[sr1.getUnsignedValue()].get2sCompValue()
				+ immediate.get2sCompValue();

		int totalWithSr2 = mRegisters[sr2.getUnsignedValue()].get2sCompValue() +
				mRegisters[sr1.getUnsignedValue()].get2sCompValue();

		if(isImmediate) {
			mRegisters[dr.getUnsignedValue()].set2sCompValue(totalWithImm);


		} else {
			mRegisters[dr.getUnsignedValue()].set2sCompValue(totalWithSr2);
		}

		setCC(mRegisters[dr.getUnsignedValue()].get2sCompValue());

	}
	
	/**
	 * Performs the load operation by placing the data from PC
	 * + PC offset9 bits [8:0]
	 * into DR - bits [11:9]
	 * then sets CC.
	 */
	public void executeLoad() {
		System.out.println("LD");  // remove this print statement
		
		// implement the LD instruction here

	}
	
	/**
	 * Store the contents of the register specified by SR
	 * in the memory location whose address is computed by sign-extending bits [8:0] to 16 bits
	 * and adding this value to the incremented PC.
	 */
	public void executeStore() {
		System.out.println("ST");  // remove this print statement
		
		// implement the ST instruction here

	}
	
	/**
	 * op   dr  sr1      sr2
	 * 0101 000 000 0 00 000
	 * <p>
	 * OR
	 * <p>
	 * op   dr  sr1   imm5
	 * 0101 000 000 1 00000
	 * <p>
	 * If bit [5] is 0, the second source operand is obtained from SR2.
	 * If bit [5] is 1, the second source operand is obtained by sign-extending the imm5 field to 16 bits.
	 * In either case, the second source operand and the contents of SR1 are bitwise ANDed
	 * and the result stored in DR.
	 * The condition codes are set, based on whether the binary value produced, taken as a 2’s complement integer,
	 * is negative, zero, or positive.
	 */
	public void executeAnd() {
		//TODO: Fix this, need to figure out how to check individual bits in the destination
		// two registers.
		BitString dr = getOperand("dr");
		BitString sr1 = getOperand("sr1");
		BitString sr2 = getOperand("sr2");
		BitString immediate = getOperand("imm");
		boolean isImmediate = mIR.substring(10,1).getUnsignedValue() == 1;

		char[] destChar = new char[getOperand("dr").getBits().length];
		char[] sr1Char = sr1.getBits();
		char[] sr2Char = sr2.getBits();
		char[] immChar = immediate.getBits();

		if(isImmediate) {
			for(int i = 0; i < destChar.length; i++) {
				if (sr1Char[i] == '1' && immChar[i] == '1') {
					destChar[i] = '1';
				} else {
					destChar[i] = '0';
				}
			}


		}




	}



	/**
	 * Performs not operation by using the data from the source register (bits[8:6]) 
	 * and inverting and storing in the destination register (bits[11:9]).
	 * Then sets CC.
	 */
	public void executeNot() {
		BitString dest = mIR.substring(4, 3);
		BitString sr = mIR.substring(7, 3);


		//R(n) <- not R(m)
		mRegisters[dest.getUnsignedValue()] = mRegisters[sr.getUnsignedValue()].copy();
		mRegisters[dest.getUnsignedValue()].invert();

		//set Condition Code
		setCC(mRegisters[dest.getUnsignedValue()].get2sCompValue());

	}
	
	/**
	 * Executes the trap operation by checking the vector (bits [7:0]
	 * 
	 * vector x21 - OUT
	 * vector x25 - HALT
	 * 
	 * @return true if this Trap is a HALT command; false otherwise.
	 */
	public boolean executeTrap() {
		boolean halt = false;

		// implement the TRAP instruction here
		BitString vector = mIR.substring(8,8);
		char[] haltBits = {'0','0','1','0','0','1','0','1'};
		char[] outBits = {'0','0','1','0','0','0','0','1'};

		if(Arrays.equals(vector.getBits(), haltBits)) {
			halt = true;

		} else if(Arrays.equals(vector.getBits(), outBits)) {
			char ascii = (char) mRegisters[0].getUnsignedValue();
			System.out.println("Output -> " + ascii);

		}

		return halt;
	}
	
	

	/**
	 * This method will execute all the instructions starting at address 0 
	 * until a HALT instruction is encountered. 
	 */
	public void execute() {
		BitString opCodeStr;
		int opCode;
		boolean halt = false;

		while (!halt) {
			// Fetch the next instruction
			mIR = mMemory[mPC.getUnsignedValue()];
			// increment the PC
			mPC.addOne();

			// Decode the instruction's first 4 bits 
			// to figure out the opcode
			opCodeStr = mIR.substring(0, 4);
			opCode = opCodeStr.getUnsignedValue();

			// What instruction is this?
			if (opCode == 0) { // BR
				executeBranch();
			} else if (opCode == 1) {  // ADD    0001
				executeAdd();
//			} else if (opCode == 2) {  // LD     0010
//				executeLoad();
//			} else if (opCode == 3) {  // ST     0011
//				executeStore();
			} else if (opCode == 5) {  // AND    0101
				executeAnd();
			} else if (opCode == 9) {  // NOT    1001
				executeNot();
			} else if (opCode == 15) { // TRAP   1111
				halt = executeTrap();
			} else {
				throw new UnsupportedOperationException("Illegal opCode: " + opCode);
			}
		}
	}

	/**
	 * Displays the computer's state
	 */
	public void display() {
		System.out.println();
		System.out.print("PC ");
		mPC.display(true);
		System.out.print("   ");

		System.out.print("IR ");
		mIR.display(true);
		System.out.print("   ");

		System.out.print("CC ");
		mCC.display(true);
		System.out.println("   ");
		for (int i = 0; i < MAX_REGISTERS; i++) {
			System.out.printf("R%d ", i);
			mRegisters[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();
		for (int i = 0; i < MAX_MEMORY; i++) {
			System.out.printf("%3d ", i);
			mMemory[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();
		System.out.println();
	}

	/**
	 * Adjusts the CC
	 * @param equ The integer representation that would be used to change the condition code.
	 */
	private void setCC(final int equ){
		char[] negativeCC = {'1','0','0'};
		char[] positiveCC = {'0','0','1'};
		char[] zeroCC = {'0','0','0'};

		if(equ < 0) {
			mCC.setBits(negativeCC);

		}else if(equ > 0) {
			mCC.setBits(positiveCC);

		}else {
			mCC.setBits(zeroCC);
		}

	}

	/**
	 * Input the operand you want and the output will be a BitString object of that operand.
	 *
	 * @param operands (sr1, sr2, dr, imm)
	 * @return BitString object of that register
	 */
	private BitString getOperand(final String operands){
		BitString result;

		switch(operands){
			case "dr" -> result = mIR.substring(4,3);
			case "sr1" -> result = mIR.substring(7,3);
			case "sr2" -> result = mIR.substring(13,3);
			case "imm" -> result = mIR.substring(11,5);
			default -> throw new IllegalArgumentException("Invalid operand");
		}

		return result;
	}
}
