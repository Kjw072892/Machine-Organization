package simulator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The Computer class is composed of registers, memory, PC, IR, and CC.
 * The Computer can execute a program based on the instructions in memory.
 *  
 * @author Kassie Whitney (kjw0728)
 * @version 5.31.25
 */
public class Computer {

	/**
	 * The max memory allocation
	 */
	private final static int MAX_MEMORY = 50;

	/**
	 * Max number of usable registers
	 */
	private final static int MAX_REGISTERS = 8;

	/**
	 * Max number of bits in an instruction
	 */
	private final static int MAX_BITS = 16;

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
	 * Destination Register
	 */
	private BitString dr;

	/**
	 * Source Register
	 */
	private BitString sr;

    /**
	 * Source Register 1
	 */
	private BitString sr1;

	/**
	 * Source Register 2
	 */
	private BitString sr2;

	/**
	 * Immediate Value (5-bits)
	 */
	private BitString imm5;

	/**
	 * 9-Bit PC offset
	 */
	private BitString pos9;


    /**
	 * HashMap, with the values being of a Runnable interface, allows for scalability for
	 * future implementation of the computer class
	 * <P>
	 * Key: Opcode Decimal
	 * <p>
	 * Value: of type Runnable-Interface with a .run() method call
	 */
	private final HashMap<Integer, Runnable> instruction = new HashMap<>();

	/**
	 * HashMap, with the values being of a Runnable interface, allows for scalability for
	 * future implementation of the computer class
	 * <P>
	 * Key: TRAP Operand Decimal
	 * <p>
	 * Value: of type Runnable-Interface with a .run() method call
	 */
	private final HashMap<Integer, Runnable> trapInstr = new HashMap<>();

	{
		instruction.put(0, this::executeBranch);
		instruction.put(1, this::executeAdd);
		instruction.put(2, this::executeLoad);
		instruction.put(3, this::executeStore);
        instruction.put(4, this::executeJSR);
		instruction.put(5, this::executeAnd);
	  //instruction.put(6, this::executeLDR);
	    instruction.put(7, this::executeSTR);
		instruction.put(9, this::executeNot);
	  //instruction.put(10, this::executeLDI);
	  //instruction.put(11, this::executeSTI);
        instruction.put(12, this::executeRET);
		instruction.put(14, this::executeLEA);

		int GETC = 0x20;
		int OUT = 0x21;
		int PUTS = 0x22;
		int IN = 0x23;
		//int PUT_SP = 0x24;
        trapInstr.put(OUT, this::executeTrapOut);
		trapInstr.put(GETC, this::executeTrapGetC);
		trapInstr.put(PUTS, this::executeTrapPutS);
		trapInstr.put(IN, this::executeTrapIn);
//		trapInstr.put(PUT_SP, this::executeTrapPutSP);
	}

	/**
	 * Initialize all memory addresses to 0, registers to 0 to 7
	 * PC, IR to 16-bits 0's and CC to 000.
	 */
	public Computer() {
		mPC = new BitString();
		mPC.setUnsignedValue(0); //creates a PC with [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]

		mIR = new BitString();
		mIR.setUnsignedValue(0); //creates an IR with [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]

		mCC = new BitString();
		mCC.setBits(new char[]{'0', '1', '0'}); //LC-3 defaults to CC: Z on startup
		// [[1,0,0] -> negative], [[0,1,0] -> zero], [[0,0,1] -> positive]

		mRegisters = new BitString[MAX_REGISTERS];
		for (int i = 0; i < MAX_REGISTERS; i++) {
			mRegisters[i] = new BitString();
			mRegisters[i].setUnsignedValue(i);
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
	 * Loads a 16-bit word into memory at the given address.
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

		for(int i = 0; i < theInstruction.length; i++) {
			final BitString instruction = new BitString();
			theInstruction[i] = theInstruction[i].replace("_", "");
			theInstruction[i] = theInstruction[i].replace(" ", "");
			theInstruction[i] = theInstruction[i].replace("-","");
			instruction.setBits(theInstruction[i].toCharArray());
			loadWord(i, instruction);
		}
	}
	
	/**
	 * <P>
	 * op nzp PcOffset9
	 * </P>
	 * <P>
	 * 0000 000 000000000
	 * </P>
	 * The condition codes specified by bits [11:9] are tested.
	 * If bit [11] is 1, N is tested; if bit [11] is 0, N is not tested.
	 * If bit [10] is 1, Z is tested, etc.
	 * If any of the condition codes tested is 1, the program branches to the memory location specified by
	 * adding the sign-extended PcOffset9 field to the incremented PC.
	 */
	private void executeBranch() {

		BitString nzp = getIR().substring(4, 3); // Gets substring for NZP
		pos9 = getOperand("pos9"); // 9-bit pc offset

		int decOffSet = pos9.get2sCompValue();
		int currentPC = getPC().getUnsignedValue();
		boolean unconditionalBranch = Arrays.equals(nzp.getBits(), new char[]{'1','1','1'});
		boolean conditionalBranch = nzp.getBits()[0] == '1' && getCC().getBits()[0] == '1'
				|| nzp.getBits()[1] == '1' && getCC().getBits()[1] == '1'
				|| nzp.getBits()[2] == '1' && getCC().getBits()[2] == '1';

		if (currentPC + decOffSet < 0 || currentPC + decOffSet > 49) {
				throw new OutOfMemoryError("Your BR 9-bit offset falls outside the "
						+ "scope of your program! Please readjust your 9-bit offset at PC "
						+ getPC().getUnsignedValue());
		}

		// Only branches if the condition code matches with the NZP
		if (conditionalBranch) {

			mPC.set2sCompValue(decOffSet + currentPC);

		// Ensures that the BR instruction executes regardless of the CC value
			// (even if CC is undefined)
		} else if (unconditionalBranch) {

			mPC.set2sCompValue(decOffSet + currentPC);
		}

	}

	/**
	 * Jumps to a subroutine at the PC + 11-bit offset
	 */
	private void executeJSR() {
		BitString pos11 = getIR().substring(5,11);

		if (pos11.get2sCompValue() + getPC().getUnsignedValue() > MAX_MEMORY
				|| pos11.get2sCompValue() + getPC().getUnsignedValue() < 0) {
			throw new OutOfMemoryError("Your BR 11-bit offset falls outside the "
					+ "scope of your program! Please readjust your 11-bit offset at PC "
					+ getPC().getUnsignedValue());
		}

		//Stores the current PC into R7
		mRegisters[7].setUnsignedValue(getPC().getUnsignedValue());

		//Sets PC to the PC + 11-bit Offset
		mPC.setUnsignedValue(mPC.getUnsignedValue() + pos11.get2sCompValue());
	}
	
	/**
	 * OP dr sr1 sr2
	 * 0001 000 000 0 00 000
	 * OR
	 * op dr sr1 imm5
	 * 0001 000 000 1 00000
	 * If bit [5] is 0, the second source operand is obtained from SR2.
	 * If bit [5] is 1, the second source operand is obtained by sign-extending the imm5 field to 16 bits.
	 * In both cases, the second source operand is added to the contents of SR1 and the
	 * result stored in DR. The condition codes are set, based on whether the result is
	 * negative, zero, or positive.
	 */
	private void executeAdd() {

		dr = getOperand("dr");
		sr1 =  getOperand("sr1");

		boolean isImmediate = getIR().substring(10,1).getUnsignedValue() == 1;

		if (isImmediate) {
			imm5 =  getOperand("imm5");

			int totalWithImm5 = getRegisters()[sr1.getUnsignedValue()].get2sCompValue()
				+ imm5.get2sCompValue();

			mRegisters[dr.getUnsignedValue()].set2sCompValue(totalWithImm5);

		} else {
			sr2 =  getOperand("sr2");

			boolean isSource2 = Arrays.equals(getIR().substring(10, 3).getBits(),
					new char[]{'0','0','0'});

			//Throws exception if the bits [5:3] are not zeros
			if (!isSource2) {
				throw new UnsupportedOperationException("Incorrect bits at [5:3] on ADD " +
						"instruction on PC " + getPC().getUnsignedValue());
			}

			int totalWithSr2 = getRegisters()[sr2.getUnsignedValue()].get2sCompValue() +
				getRegisters()[sr1.getUnsignedValue()].get2sCompValue();

			mRegisters[dr.getUnsignedValue()].set2sCompValue(totalWithSr2);
		}

		setCC(getRegisters()[dr.getUnsignedValue()].get2sCompValue());
	}
	
	/**
	 * Performs the load operation by placing the data from PC
	 * + PC offset9 bits [8:0]
	 * into DR - bits [11:9]
	 * then sets CC.
	 */
	private void executeLoad() {
		dr = getOperand("dr");
		pos9 = getOperand("pos9");

		int targetPc = pos9.get2sCompValue() + getPC().getUnsignedValue();

		if (targetPc > MAX_MEMORY) {
			throw new OutOfMemoryError("Your LD 9-bit offset falls outside the "
						+ "scope of your program! Please readjust your 9-bit offset at PC "
						+ getPC().getUnsignedValue());
		}

		mRegisters[dr.getUnsignedValue()].setBits(getMemory()[targetPc].getBits());

		setCC(getRegisters()[dr.getUnsignedValue()].get2sCompValue());

	}
	
	/**
	 * Store the contents of the register specified by SR
	 * to in the memory location whose address is computed by sign-extending bits [8:0] to 16 bits
	 * and adding this value to the incremented PC.
	 */
	private void executeStore() {

        sr = getOperand("sr");
		pos9 = getOperand("pos9");

		if (pos9.get2sCompValue() + mPC.getUnsignedValue() > MAX_MEMORY
				|| pos9.get2sCompValue() + mPC.getUnsignedValue() < 0) {

			throw new OutOfMemoryError("Your ST 9-bit offset falls outside the "
						+ "scope of your program! Please readjust your 9-bit offset at PC "
						+ getPC().getUnsignedValue());
		}

		//Getting the value from the source register
		char[] srArr = getRegisters()[sr.getUnsignedValue()].getBits();

		//Storing the bits found in sr1 into the memory location via offset + PC
		mMemory[mPC.getUnsignedValue() + pos9.get2sCompValue()].setBits(srArr);
	}

	/**
	 * Store the contents of the register specified by SR
	 * into the memory address stored in the BaseR register + the 6-bit PC Offset
	 * <P>
	 * 0111-SR-BaseR-Offset6
	 */
	private void executeSTR() {
		sr = getOperand("sr");
		BitString br = getOperand("br"); //The base register that has the memory address
		BitString pos6 = getOperand("pos6");//6-bit offset
		int locationIndex = br.getUnsignedValue() + pos6.get2sCompValue();

		mMemory[locationIndex].setBits(sr.getBits());
	}
	
	/**
	 * op   dr  sr1      sr2
	 * <p>
	 * 0101 000 000 0 00 000
	 * <p>
	 * OR
	 * <p>
	 * op   dr  sr1   imm5
	 * <p>
	 * 0101 000 000 1 00000
	 * <p>
	 * If bit [5] is 0, the second source operand is obtained from SR2.
	 * If bit [5] is 1, the second source operand is obtained by sign-extending the imm5 field to 16 bits.
	 * In either case, the second source operand and the contents of SR1 are bitwise ANDed
	 * and the result stored in DR.
	 * The condition codes are set, based on whether the binary value produced, taken as a 2’s complement integer,
	 * is negative, zero, or positive.
	 */
	private void executeAnd() {

		dr = getOperand("dr");
		sr1 = getOperand("sr1");
		sr2 = getOperand("sr2");
		imm5 = getOperand("imm5");

		boolean isImmediate = getIR().substring(10,1).getUnsignedValue() == 1;

		char[] destR = new char[MAX_BITS];
		char[] sourceReg1 = getRegisters()[sr1.getUnsignedValue()].getBits();

		//Checks if the two values are '1's
		for(int i = 0; i < destR.length; i++) {
			if (isImmediate) {
				char[] immediate = signExt(imm5.getBits());
				if (sourceReg1[i] == '1' && immediate[i] == '1') {
					destR[i] = '1';
				} else {
					destR[i] = '0';
				}

			} else {
				char[] sourceReg2 = getRegisters()[sr2.getUnsignedValue()].getBits();
				if (sourceReg1[i] == '1' && sourceReg2[i] == '1') {
					destR[i] = '1';
				} else {
					destR[i] = '0';
				}
			}
		}

		//sets the values in destR into the destination register
		mRegisters[dr.getUnsignedValue()].setBits(destR);

		//sets condition code
		setCC(getRegisters()[dr.getUnsignedValue()].get2sCompValue());
	}

	/**
	 * Loads into R0 the effective address at the offset + PC
	 */
	private void executeLEA() {
		dr = getOperand("dr");
		pos9 = getOperand("pos9");

		//Loads into the destination register the effective address at the 9-bit Offset + PC
		mRegisters[dr.getUnsignedValue()].setUnsignedValue(mPC.getUnsignedValue()
				+ pos9.getUnsignedValue());
	}


	/**
	 * Performs not operation by using the data from the source register (bits[8:6]) 
	 * and inverting and storing in the destination register (bits[11:9]).
	 * Then sets CC.
	 */
	private void executeNot() {
		dr = getOperand("dr");
		sr1 = getOperand("sr1");

		mRegisters[dr.getUnsignedValue()] = getRegisters()[sr1.getUnsignedValue()];
		mRegisters[dr.getUnsignedValue()].invert();

		//set Condition Code
		setCC(mRegisters[dr.getUnsignedValue()].get2sCompValue());

	}

	/**
	 * Retrieves the stored PC from R7 and sets the current PC back to it
	 */
	private void executeRET() {
		mPC.setUnsignedValue(mRegisters[7].getUnsignedValue());
	}
	
	/**
	 * Executes the trap operation by checking the vector (bits [7:0]
	 * <P>
	 * Vector x20 - GETC
	 * <P>
	 * Vector x21 - OUT
	 * <P>
	 * Vector x22 - PUTS
	 * <P>
	 * Vector x23 - IN
	 * <P>
	 * Vector x25 - HALT
	 * 
	 * @return true if this Trap is a HALT command; false otherwise.
	 */
	private boolean executeTrap() {
		BitString vector = getOperand("trap");
		int decVector = vector.getUnsignedValue();// The vector operand
		int trapHalt = 0x25; // The halt vector
		boolean halt = trapHalt == decVector; // return true if vector operand is HALT

		if (!trapInstr.containsKey(decVector) && decVector != trapHalt) {
			throw new UnsupportedOperationException("This TRAP vector is not currently supported!");
		}

		if (!halt) {
			trapInstr.get(decVector).run();
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
		int trap = 15; //Decimal value for TRAP opCode
		boolean halt = false;

		while(!halt) {
			// Fetch the next instruction
			mIR = getMemory()[mPC.getUnsignedValue()];

			// increment the PC
			mPC.addOne();

			// Decode the instruction's first 4 bits to figure out the opcode
			opCodeStr = getIR().substring(0, 4);
			opCode = opCodeStr.getUnsignedValue();

			//What instruction is this?
			if (!instruction.containsKey(opCode) && opCode != trap) {
				throw new UnsupportedOperationException("Illegal opCode: "
						+ Arrays.toString(opCodeStr.getBits())+ " @ PC: "
						+ getPC().getUnsignedValue());
			}

			if (opCode == trap){
				halt = executeTrap();

			} else {
				instruction.get(opCode).run();
			}
		}
	}

	/**
	 * Displays the computer's state
	 */
	public void display() {
		System.out.println();
		System.out.print("PC ");
		getPC().display(true);
		System.out.print("   ");

		System.out.print("IR ");
		getIR().display(true);
		System.out.print("   ");

		System.out.print("CC ");
		getCC().display(true);
		System.out.println("   ");
		for(int i = 0; i < MAX_REGISTERS; i++) {
			System.out.printf("R%d ", i);
			getRegisters()[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();
		for(int i = 0; i < MAX_MEMORY; i++) {
			System.out.printf("%3d ", i);
			getMemory()[i].display(true);
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
	 * Adjusts the Condition Code
	 * @param arg The signed integer representation that would be used to change the condition
	 *        	  code.
	 */
	private void setCC(final int arg){

		if (arg < 0) {
			char[] negativeCC = {'1','0','0'};
			mCC.setBits(negativeCC);

		} else if (arg > 0) {
			char[] positiveCC = {'0','0','1'};
			mCC.setBits(positiveCC);

		} else {
			char[] zeroCC = {'0','1','0'};
			mCC.setBits(zeroCC);
		}

	}

	/**
	 * Input the operand you want and the output will be a BitString object of that operand.
	 *
	 * @param operands 'sr', 'sr1','sr2', 'imm5', 'nzp', 'dr', 'pos9', 'pos6','trap', 'br'
	 *
	 * @return Substring of the BitString object for the operand
	 */
	private BitString getOperand(final String operands){
		BitString result;

		switch(operands.toLowerCase()){
			case "nzp" -> result = mIR.substring(3,3);
			case "dr", "sr" -> result = mIR.substring(4,3);
            case "sr1", "br" -> result = mIR.substring(7,3);
			case "pos9" -> result = mIR.substring(7,9);
			case "trap" -> result = mIR.substring(8,8);
			case "pos6" -> result = mIR.substring(10, 6);
			case "imm5" -> result = mIR.substring(11,5);
			case "sr2" -> result = mIR.substring(13,3);
			default -> throw new UnsupportedOperationException("Invalid operand");
		}

		return result;
	}

	/**
	 * Sign extension for PC offsets, and immediate values
	 *
	 * @param operand char[] that needs a sign extension to 16-bits
	 */
	private char[] signExt(final char[] operand) {

		char[] result = new char[MAX_BITS];

		int resultIndex = result.length - 1;

		int arrIndex = operand.length - 1;

		for(int i = 0; i < result.length; i++) {
			if (i < operand.length ) {

				result[resultIndex - i] = operand[arrIndex - i];
			} else if (operand[0] == '1'){

				result[resultIndex - i] = '1';
			} else {

				result[resultIndex - i] = '0';
			}
		}

		return result;
	}

	/**
	 * Displays the character from mRegister[0]
	 */
	private void executeTrapOut(){
		char ascii = (char) getRegisters()[0].getUnsignedValue();
		System.out.print(ascii);
	}

	/**
	 * Echos the first character from the user input
	 */
	private void executeTrapGetC(){
		System.out.print("> ");
		Scanner sc = new Scanner(System.in);
		char input = sc.next().charAt(0);
		mRegisters[0].setUnsignedValue(input);
	}

	/**
	 * Echos the first character value from the user input and stores the value into R0
	 */
	private void executeTrapIn(){
		System.out.print("Input a character: ");
		Scanner sc = new Scanner(System.in);
		char input = sc.next().charAt(0);//only stores the first character into input
		System.out.print(input);
		mRegisters[0].setUnsignedValue(input);
    }

	/**
	 * Outputs the string into the console starting from the memory location stored in R0 and
	 * traverses until the memory location has 0x0000
	 */

	private void executeTrapPutS(){
		//Gets the stored memory location from R0 from LEA
		int MemoryLocation = getRegisters()[0].getUnsignedValue();
		char nullAscii = 0x0000;
		char output = (char) mMemory[MemoryLocation].getUnsignedValue();
		int incrementer = 1;

		while(output != nullAscii) {
			System.out.print(output);
			output = (char) mMemory[MemoryLocation + incrementer].getUnsignedValue();
			incrementer++;
		}
	}
}
