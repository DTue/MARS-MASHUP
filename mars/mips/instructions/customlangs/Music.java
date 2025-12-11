package mars.mips.instructions.customlangs;

public class Music {

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

public class Music extends CustomAssembly{

    @Override
    public String getName(){
        return "Music";
    }

    @Override
    public String getDescription(){
        return "Stimulate an audio engineering tool";
    }

    @Override
    protected void populate()
    {

        //BASIC INSTRUCTIONS

        //BASIC_INSTRUCTIONS: R_FORMAT
        
        //load-word(lw)/ sample(smp)
        instructionList.add(
            new BasicInstruction("smp $t1, -100($t2)",
            "Sample word: set $t1 to contents of effective memory word address",
            BasicInstructionFormat.I_FORMAT,
            "100011 sssss ttttt iiiiiiiiiiiiiiiiii",
            new SimulationCode() 
                {
                    public void simulate(ProgramStatement statement) throws ProcessingException
                    {
                        int [] operands = statement.getOperands();
                        try
                        {
                            RegisterFile.updateRegister(operands[0],
                                Globals.memory.getWord(
                                RegisterFile.getValue(operands[2]) + operands[1])); 
                        }
                        catch(AddressErrorException e)
                        {
                            throw new ProcessingException(statement, e); 
                        }
                    }
                })); 

        //store-word(sw) / record(rec)
        instructionList.add(
            new BasicInstruction("rec $t1, -100($t2)",
            "Record word: set contents of $t1 into effective memory word address",
            BasicInstructionFormat.I_FORMAT,
            "101011 sssss ttttt iiiiiiiiiiiiiiiiii",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int [] operands = statement.getOperands();
                    try
                    {
                        Globals.memory.setWord(
                            RegisterFile.getValue(operands[2] + operands[1]),
                            RegisterFile.getValue(operands[0]));
                    }
                    catch(AddressErrorException e)
                    {
                        throw new ProcessingException(statement, e); 
                    }
                }
            }));


        //add / boost(add)
         instructionList.add(
            new BasicInstruction("bst $t1 ,$t2, $t3", 
            "Addition with overflow : set $t1 to ($t2 plus $t3)",
            BasicInstructionFormat.R_FORMAT, 
            "000000 sssss ttttt ddddd 00000 000010",
            new SimulationCode() 
            {
            public void simulate(ProgramStatement statement) throws ProcessingException
            {
                int [] operands = statement.getOperands();
                int reg1 = RegisterFile.getValue(operands[1]); 
                int reg2 = RegisterFile.getValue(operands[2]); 
                int sum = reg1 + reg2; 
            
            //Error Handle
            if((reg1 >= 0 && reg2 >= 0 && sum < 0) || (reg1 < 0 && reg2 < 0 && sum >= 0))
            {
                throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
            }
            RegisterFile.updateRegister(operands[0], sum);
            }
        }));


        //subtract / attenuate(att)
        instructionList.add(
         new BasicInstruction("att $t1 ,$t2, $t3", 
         "Subtraction with overflow: set $t1 to ($t2 minus $t3)",
            BasicInstructionFormat.R_FORMAT, 
            "000000 sssss ttttt ddddd 00000 000011",
            new SimulationCode() 
        {
            public void simulate(ProgramStatement statement) throws ProcessingException
            {
                int [] operands = statement.getOperands();
                int reg1 = RegisterFile.getValue(operands[1]); 
                int reg2 = RegisterFile.getValue(operands[2]); 
                int difference = reg1 - reg2; 
            
            if ((reg1 >= 0 && reg2 < 0 && difference < 0)
                        || (reg1 < 0 && reg2 >= 0 && difference >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
           
            RegisterFile.updateRegister(operands[0], difference);
            }
        }));


        //mul(multiple) - atk/ attack
        instructionList.add(
                new BasicInstruction("atk $t1,$t2,$t3",
            	 "Attack(Multiply) without overflow  : Set HI to high-order 32 bits, LO and $t1 to low-order 32 bits of the product of $t2 and $t3 (use mfhi to access HI, mflo to access LO)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt ddddd 00000 000100",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     long product = (long) RegisterFile.getValue(operands[1])
                                    * (long) RegisterFile.getValue(operands[2]);

                     
                     RegisterFile.updateRegister(operands[0], (int) ((product << 32) >> 32));
                     RegisterFile.updateRegister(33, (int) (product >> 32));
                     RegisterFile.updateRegister(34, (int) ((product << 32) >> 32));
                  }
               }));

        //div(division) - decay(dcy)
        instructionList.add(
                new BasicInstruction("dcy $t1,$t2",
            	 "Decay(Divide) with overflow : Divide $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt 00000 00000 000101",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     if (RegisterFile.getValue(operands[1]) == 0)
                     {
                        return;
                     }
                        RegisterFile.updateRegister(33,
                        RegisterFile.getValue(operands[0])
                        % RegisterFile.getValue(operands[1]));
                        RegisterFile.updateRegister(34,
                        RegisterFile.getValue(operands[0])
                        / RegisterFile.getValue(operands[1]));
                  }
               }));

        //mflo(move from low) - sharp(shrp): quotient
        instructionList.add(
                new BasicInstruction("shrp $t1", 
            	 "sharp(♯) Move from LO register : Set $t1 to contents of LO (see multiply and divide operations)",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 ddddd 00000 000110",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0],
                        RegisterFile.getValue(34));
                  }
               }));

       //mfhi(move from high) - flat(flt): remainder
       instructionList.add(
                new BasicInstruction("flt $t1", 
            	 "flat(♭)Move from HI register : Set $t1 to contents of HI (see multiply and divide operations)",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 ddddd 00000 010000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0],
                        RegisterFile.getValue(33));
                  }
               }));


       //BASIC_INSTRUCTIONS: J_FORMAT

        //j(jump) / hop: get to a target address
        instructionList.add(
                new BasicInstruction("h target", 
            	 "Hop unconditionally : Jump to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "000010 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processJump(
                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2)));            
                  }
               }));

        //syscall - release: call system
        instructionList.add(
                new BasicInstruction("release", 
            	 "Issue a release call : Execute the system call specified by value in $v0",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 001100",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Globals.instructionSet.findAndSimulateSyscall(RegisterFile.getValue(2),statement);
                  }
               }));

        //BASIC_INSTRUCTIONS: I_FORMAT

        //addi - bsti(boosti)
        instructionList.add(
              new BasicInstruction("bsti $t1 ,$t2, -100", 
              "Addition immediate with overflow: set  $t1 to ($t2 plus signed 16-bit immediate)",
              BasicInstructionFormat.I_FORMAT, 
             "000001 sssss ttttt iiiiiiiiiiiiiiiiii",
             new SimulationCode() 
             {
                  public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                int [] operands = statement.getOperands();
                int reg1 = RegisterFile.getValue(operands[1]); 
                int reg2 = operands[2] << 16 >> 16; 
                int sum = reg1 + reg2; 
            
            //Error Handle
                 if((reg1 >= 0 && reg2 >= 0 && sum < 0) || (reg1 < 0 && reg2 < 0 && sum >= 0))
                 {
                throw new ProcessingException(statement,
                                    "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                    }
                 RegisterFile.updateRegister(operands[0], sum);
                  }
             }));


        //beq(branch equal) - csn(consonance)
        instructionList.add(
                new BasicInstruction("csn $t1,$t2,label",
                "Branch if equal : Branch to statement at label's address if $t1 and $t2 are equal",
            	 BasicInstructionFormat.I_BRANCH_FORMAT,
                "000100 sssss ttttt iiiiiiiiiiiiiiiiii",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                  
                     if (RegisterFile.getValue(operands[0])
                        == RegisterFile.getValue(operands[1]))
                     {
                        Globals.instructionSet.processBranch(operands[2]);
                     }
                  }
               }));


        //bne(branch not equal) - dsn (dissonance)
        instructionList.add(
                new BasicInstruction("dsn $t1,$t2,label",
                "Branch if not equal : Branch to statement at label's address if $t1 and $t2 are not equal",
            	 BasicInstructionFormat.I_BRANCH_FORMAT,
                "000101 sssss ttttt iiiiiiiiiiiiiiiiii",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     if (RegisterFile.getValue(operands[0])
                        != RegisterFile.getValue(operands[1]))
                     {
                        Globals.instructionSet.processBranch(operands[2]);
                     }
                  }
               }));

         //la
         instructionList.add(
                new BasicInstruction("la $t1,label",
            	 "Load Address",
                BasicInstructionFormat.I_FORMAT,
                "001011 sssss ttttt iiiiiiiiiiiiiiiiii",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0], operands[2]);
                  }
               }));

         

    //CREATIVE INSTRUCTIONS

    //CREATIVE_______J-FORMAT

       //jal - jump and return -> rvb / reverb
        instructionList.add(
                new BasicInstruction("rvb target",
                "Jump and link : Set $ra to Program Counter (return address) then jump to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "001001 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processReturnAddress(31);
                     Globals.instructionSet.processJump(
                        (RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2));
                  }
               }));

        //CREATIVE_______R-FORMAT

        //crossfade - cf: swap values between registers
        instructionList.add(
            new BasicInstruction("cf $t1, $t2", "swap two register values $t1 and $t2",
            BasicInstructionFormat.R_FORMAT,
             "001010 sssss ttttt 00000 00000 000000",
             new SimulationCode()
             {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int [] operands = statement.getOperands();
                    int a = RegisterFile.getValue(operands[0]);
                    int b = RegisterFile.getValue(operands[1]);

                    RegisterFile.updateRegister(operands[0], b); 
                    RegisterFile.updateRegister(operands[1], a); 
                }
             }));

        //sync - release: set value the same for all registers
        instructionList.add(
            new BasicInstruction("sync $t3, $t1, $t2", "set St1 and $t2 same as its destination, $t3",
            BasicInstructionFormat.R_FORMAT,
             "001010 sssss ttttt ddddd 00000 000001",
             new SimulationCode()
             {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int [] operands = statement.getOperands();
                    int value =  RegisterFile.getValue(operands[0]);
                    RegisterFile.updateRegister(operands[1], value);
                    RegisterFile.updateRegister(operands[2], value); 
                }
             }));


        //mut - muting:set all values to 0
        instructionList.add(
            new BasicInstruction("mut $t1, $t2, $t3", "set $t1, $t2, $t3 to 0",
            BasicInstructionFormat.R_FORMAT,
             "001010 sssss ttttt ddddd 00000 000010",
             new SimulationCode()
             {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int [] operands = statement.getOperands();
                    RegisterFile.updateRegister(operands[0], 0); 
                    RegisterFile.updateRegister(operands[1], 0); 
                    RegisterFile.updateRegister(operands[2], 0); 
                }
             }));
    
        //blend: find an average between two registers
        instructionList.add(
            new BasicInstruction("bl $t3, $t2, $t1", "Find an average between two values, $t1 and $t2 to store in $t3",
            BasicInstructionFormat.R_FORMAT,
             "001010 sssss ttttt ddddd 00000 000110",
             new SimulationCode()
             {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
               
                    int [] operands = statement.getOperands();
                    int reg1 = RegisterFile.getValue(operands[1]);
                    int reg2 = RegisterFile.getValue(operands[2]);
                    int average = (reg1 + reg2) / 2; 
                    RegisterFile.updateRegister(operands[0], average); 
                }
             }));
    }
} 

    
}
