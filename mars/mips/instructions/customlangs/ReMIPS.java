    package mars.mips.instructions.customlangs;
    import mars.simulator.*;
    import mars.mips.hardware.*;
    import mars.mips.instructions.syscalls.*;
    import mars.*;
    import mars.util.*;
    import java.util.*;
    import java.io.*;
    import mars.mips.instructions.*;
    import java.util.Random;


    public class ReMIPS extends CustomAssembly{

        static int temrCount = 0;

        @Override
        public String getName(){
            return "Re: Starting MIPS in Another World from Zero";
        }

        @Override
        public String getDescription(){
            return "A Re: Zero Themed Assembly Language";
        }


        @Override
        protected void populate(){
            // dc label (same as j label)
            instructionList.add(
                            new BasicInstruction("dc label",
                                "Door Crossing : Unconditional teleport to an existing label",
                                BasicInstructionFormat.J_FORMAT,
                                "000010 ffffffffffffffffffffffffff",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();

                                        Globals.instructionSet.processJump(
                                            ((RegisterFile.getProgramCounter() & 0xF0000000)
                                                | (operands[0] << 2)));
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("wbod $t0, 0($t1)",
                            "Write Book of the Dead: Have the contents of your life written into memory.",
                            BasicInstructionFormat.I_FORMAT,
                            "101011 sssss fffff tttttttttttttttt",
                            new SimulationCode(){
                                public void simulate(ProgramStatement statement) throws ProcessingException{
                                    int operands[] = statement.getOperands();
                                    
                                    int value = RegisterFile.getValue(operands[0]);
                                    int base = RegisterFile.getValue(operands[2]);
                                    int offset = operands[1] << 16 >> 16;
                                    int address = base + offset;

                                    try{
                                        Globals.memory.setWord(address, value);

                                    }
                                    catch(AddressErrorException e){
                                        throw new ProcessingException(statement, e);
                                    }
                                }
                            }
                            )
            );

            instructionList.add(
                            new BasicInstruction("rbod $t0, 0($t1)",
                                "Read Book of the Dead : Store the conents from memory stored into you (your register)",
                                BasicInstructionFormat.I_FORMAT,
                                "100011 sssss fffff tttttttttttttttt",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int base = RegisterFile.getValue(operands[2]);
                                        int offset = operands[1] << 16 >> 16;
                                        int address = base + offset;

                                        try{
                                            int value = Globals.memory.getWord(address);
                                            RegisterFile.updateRegister(operands[0], value);
                                        }
                                        catch(AddressErrorException e){
                                            throw new ProcessingException(statement, e);
                                        }
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("add $t0, $t1, $t2",
                                "Addition : set $t0 to ($t1 plus $t2)",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 sssss ttttt fffff 00000 100000",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int num1 = RegisterFile.getValue(operands[1]);
                                        int num2 = RegisterFile.getValue(operands[2]);
                                        int sum = num1 + num2;
                                        if ((num1 >= 0 && num2 >= 0 && sum < 0)
                                        || (num1 < 0 && num2 < 0 && sum >= 0)){
                                            throw new  ProcessingException(statement,
                                                "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION
                                            );
                                        }
                                        RegisterFile.updateRegister(operands[0], sum);
                                    }
                                }
                            )
            );
            instructionList.add(
                            new BasicInstruction("sub $t0, $t1, $t2",
                                "Subtraction : set $t0 to ($t1 minus $t2)",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 sssss ttttt fffff 00000 100010",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int num1 = RegisterFile.getValue(operands[1]);
                                        int num2 = RegisterFile.getValue(operands[2]);
                                        int dif = num1 - num2;
                                        if ((num1 >= 0 && num2 < 0 && dif < 0)
                                        || (num1 < 0 && num2 >= 0 && dif >= 0)){
                                            throw new  ProcessingException(statement,
                                                "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION
                                            );
                                        }
                                        RegisterFile.updateRegister(operands[0], dif);
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("mult $t0, $t1",
                                "Multiplication : multiplies $t0 with $t1 and store within HI|LO",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 sssss fffff 00000 00000 011000",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int num1 = RegisterFile.getValue(operands[0]);
                                        int num2 = RegisterFile.getValue(operands[1]);
                                        long prod = (long)num1 * (long)num2;
                                        
                                        // Register 33 is HIGH and 34 is LOW
                                        RegisterFile.updateRegister(33, (int) (prod >> 32));
                                        RegisterFile.updateRegister(34, (int) ((prod << 32) >> 32));
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("div $t0, $t1",
                                "Division : divides $t0 by $t1 and store within HI|LO",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 sssss fffff 00000 00000 011010",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int num1 = RegisterFile.getValue(operands[0]);
                                        int num2 = RegisterFile.getValue(operands[1]);

                                        if (num2 == 0){
                                            throw new ProcessingException(statement, 
                                                "division by 0",
                                                Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION
                                            );
                                        }
                                        int quo = num1 / num2;
                                        int remain = num1 % num2;
                                        
                                        // Register 33 is HIGH and 34 is LOW
                                        RegisterFile.updateRegister(33, remain);
                                        RegisterFile.updateRegister(34, (int) quo);
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("bmol $t0",
                                "Bless Me Od Laguna : Copies the value in HI to $t0",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 00000 00000 fffff 00000 010000",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int reg = operands[0];
                                        int hiVal = RegisterFile.getValue(33);

                                        RegisterFile.updateRegister(reg, hiVal);

                                    }
                                }
                            )
            );
            instructionList.add(
                            new BasicInstruction("tmol $t0",
                                "Trade with Me Od Laguna : Copies the value in LO to $t0",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 00000 00000 fffff 00000 010010",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int reg = operands[0];
                                        int loVal = RegisterFile.getValue(34);
                                        RegisterFile.updateRegister(reg, loVal);

                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("addi $t0, $t1, 100",
                                "Add Immediate : set $t0 to ($t1 plus 100)",
                                BasicInstructionFormat.I_FORMAT,
                                "001000 sssss fffff tttttttttttttttt",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        
                                        int num1 = RegisterFile.getValue(operands[1]);
                                        int num2 = operands[2] << 16 >> 16;
                                        int sum = num1 + num2;

                                        if ((num1 >= 0 && num2 >= 0 && sum < 0)
                                        || (num1 < 0 && num2 < 0 && sum >= 0)){
                                            throw new  ProcessingException(statement,
                                                "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION
                                            );
                                        }
                                        RegisterFile.updateRegister(operands[0], sum);
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("temr label",
                                "Territory Expansion Matrix Redefinition : Saves the address of the next instruction and jumps in label",
                                BasicInstructionFormat.J_FORMAT,
                                "000011 ffffffffffffffffffffffffff",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();

                                        int rtrnAddr = RegisterFile.getProgramCounter() + 4;
                                        RegisterFile.updateRegister(31, rtrnAddr);
                                        int target = operands[0];
                                        int jAddr = (RegisterFile.getProgramCounter()
                                        & 0xF0000000) | (target << 2);

                                        Globals.instructionSet.processJump(jAddr);
                                        ++temrCount;
                                    }
                                }
                            )
            );
            
            instructionList.add(
                            new BasicInstruction("lrbd",
                                "Loveless Return by Death : returns to address contained in register",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 11111 00000 00000 00000 001000",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        

                                        
                                        int tAddr = RegisterFile.getValue(31);
                                        

                                        Globals.instructionSet.processJump(tAddr);
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("bh $t0",
                                "Bowl Hunter : Splits $t0 in half",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 00000 00000 fffff 00000 000000",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int reg = operands[0];
                                        int val = RegisterFile.getValue(reg);
                                        int quo = val / 2;
                                        RegisterFile.updateRegister(reg, quo);
                                        
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("fsh",
                                "Following Star's Habit : Outputs the amount of times temr was used",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 00000 00000 00000 00000 000010",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        
                                        String tOut = Integer.toString(temrCount) + "\n";
                                        SystemIO.printString(tOut);
                                        
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("osmk $t0",
                                "Ol Shamak : Makes general-purpose register read-only",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 00000 00000 fffff 00000 000011",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        
                                        int reg = operands[0];
                                        RegisterFile.osmkReg(reg);
                                        
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("rosmk $t0",
                                "Release Ol Shamak : Releases $t0 from the read-only seal",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 00000 00000 fffff 00000 000100",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        
                                        int reg = operands[0];
                                        RegisterFile.rOsmkRg(reg);
                                        
                                    }
                                }
                            )
            );
            
            instructionList.add(
                            new BasicInstruction("asmk $t0",
                                "Al Shamak : Transfers $t0's content to a random general-purpose register",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 00000 00000 fffff 00000 000110",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        Random rand = new Random();
                                        int srcReg = operands[0];
                                        int value = RegisterFile.getValue(srcReg);
                                        
                                        int destReg = rand.nextInt(32);
                                        while (destReg == 0 || destReg == srcReg
                                            || RegisterFile.isOsmked(destReg)){
                                                destReg = rand.nextInt(32);
                                        }
                                        RegisterFile.updateRegister(destReg, value);
                                        RegisterFile.updateRegister(srcReg, Integer.MIN_VALUE);
                                        
                                        
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("gr",
                                "Great Reset : Restarts program execution to the very beginning, restoring registers to original state",
                                BasicInstructionFormat.J_FORMAT,
                                "000001 00000000000000000000000000",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        
                                        RegisterFile.resetRegisters();
                                        int beginning = RegisterFile.getInitialProgramCounter();
                                        Globals.instructionSet.processJump(beginning);
                                        
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("mm label",
                                "Metia Message : Outputs a message stored within the label",
                                BasicInstructionFormat.I_BRANCH_FORMAT,
                                "000110 00000 00000 ffffffffffffffff",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        char ch = 0;
                                        String label = statement.getOriginalTokenList().get(1).getValue();

                                        int byteAddr = Globals.program.getLocalSymbolTable().getAddressLocalOrGlobal(label);
                                        
                                        try{
                                            ch = (char)Globals.memory.getByte(byteAddr);
                                            while (ch != 0){
                                                SystemIO.printString(Character.toString(ch));
                                                ++byteAddr;
                                                ch = (char) Globals.memory.getByte(byteAddr);
                                            }
                                        }
                                        catch(AddressErrorException e){
                                            throw new ProcessingException(statement, e);
                                        }
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("clss $t0, $t1",
                                "Cor Leonis Second Shift : Evenly distributes the values between $t0 and $t1 to make them as even as possible",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 sssss 00000 fffff 00000 000111",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        int srcReg = operands[1];
                                        int destReg = operands[0];
                                        int num1 = RegisterFile.getValue(srcReg);
                                        int num2 = RegisterFile.getValue(destReg);
                                        int sum = num1 + num2;

                                        int destVal = sum / 2;
                                        RegisterFile.updateRegister(destReg, destVal);
                                        RegisterFile.updateRegister(srcReg, sum - destVal);
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("ip $t0",
                                "Invisible Providence : Subtracts 50 from $t0",
                                BasicInstructionFormat.R_FORMAT,
                                "000000 00000 00000 fffff 00000 001001",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        int operands[] = statement.getOperands();
                                        
                                        int destReg = operands[0];
                                        int value = RegisterFile.getValue(destReg);
                                        RegisterFile.updateRegister(destReg, value - 50);
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("subn 100",
                                "Subaru Navigation : Finds which general-purpose register holds the given value and prints its name or \"Not Found\"",
                                BasicInstructionFormat.J_FORMAT,
                                "000111 ffffffffffffffffffffffffff",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        String text = statement.getOriginalTokenList().get(1).getValue();
                                        int target;
                                        try{
                                            target = Binary.stringToInt(text);
                                        }
                                        catch(NumberFormatException e){
                                            throw new ProcessingException(statement, "subn immediate is not a valid number");
                                        }

                                        Register regs[] = RegisterFile.getRegisters();
                                        int idx = -1;
                                        for (int x = 0; x < regs.length; ++x){
                                            int regNum = regs[x].getNumber();
                                            if (RegisterFile.getValue(regNum) == target){
                                                idx = x;
                                                break;
                                            }
                                        }

                                        if (idx < 0){
                                            SystemIO.printString("Not found.\n");
                                        }
                                        else{
                                            String regName = regs[idx].getName();
                                            SystemIO.printString("Value found in register " + regName + ".\n");
                                        }
                                        
                                    }
                                }
                            )
            );

            instructionList.add(
                            new BasicInstruction("pd",
                                "Perma Death : Terminates program execution unconditionally",
                                BasicInstructionFormat.J_FORMAT,
                                "001001 ffffffffffffffffffffffffff",
                                new SimulationCode(){
                                    public void simulate(ProgramStatement statement) throws ProcessingException{
                                        
                                        SystemIO.printString("You've died and did not have Return by Death.\n");
                                        throw new ProcessingException();
                                    }
                                }
                            )
            );
        }
    }
