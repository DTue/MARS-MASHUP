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

public class Music_v2 extends CustomAssembly{
    @Override
    public String getName(){
        return "Music_(Ver 2)";
    }

    @Override
    public String getDescription(){
        return "Stimulate an audio engineering tool";
    }

    @Override
    protected void populate()
    {
        SystemIO.printString("Welcome DJ for the day!\n");
        SystemIO.printString("~~~Let's make some tunes~~~\n");

        //BASIC INSTRUCTIONS

        //BASIC_INSTRUCTIONS: R_FORMAT

         //add / boost(add) => worked
         instructionList.add(
            new BasicInstruction("bst $t1 ,$t2, $t3", 
            "Addition with overflow : set $t1 to ($t2 plus $t3)",
            BasicInstructionFormat.R_FORMAT, 
            "000000 sssss ttttt fffff 00000 000010",
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

        //subtract / attenuate(att) => worked
        instructionList.add(
         new BasicInstruction("att $t1 ,$t2, $t3", 
         "Subtraction with overflow: set $t1 to ($t2 minus $t3)",
            BasicInstructionFormat.R_FORMAT, 
            "000000 sssss ttttt fffff 00000 000011",
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

        //mult(multiple) - atk/ attack => worked
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
                                                            
                    RegisterFile.updateRegister(33, (int) (prod >> 32));
                    RegisterFile.updateRegister(34, (int) ((prod << 32) >> 32));
                 }
             }));


        //div(division) - decay(dcy) => worked
        instructionList.add(
            new BasicInstruction("dcy $t0, $t1",
            "Decay - Division : divides $t0 by $t1 and store within HI|LO",
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

                    RegisterFile.updateRegister(33, remain);
                    RegisterFile.updateRegister(34, (int) quo);
                 }
            }));

        //mflo(move from low)- flat(flt): remainder => worked
        instructionList.add(
                new BasicInstruction("flt $t1", 
            	 "flat(♭) Move from LO register : Set $t1 to contents of LO",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 fffff 00000 000110",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int reg = operands[0];
                     int loVal = RegisterFile.getValue(34);
                     RegisterFile.updateRegister(reg, loVal);
                  }
               }));

       //mfhi(move from high) - sharp(shrp): quotient => worked
       instructionList.add(
                new BasicInstruction("shrp $t1", 
            	 "sharp(♯) Move from HI register : Set $t1 to contents of HI",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 ffffff 00000 010000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                    int operands[] = statement.getOperands();
                    int reg = operands[0];
                    int hiVal = RegisterFile.getValue(33);
                    RegisterFile.updateRegister(reg, hiVal);
                  }
               }));

            
        //BASIC_INSTRUCTIONS: J_FORMAT

        //j(jump) / (hip) hop: get to a target address
        instructionList.add(
                new BasicInstruction("h target", 
            	 "(Hip) Hop unconditionally : Jump to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "000010 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     SystemIO.printString("Alright! Let's hop. \n");
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processJump(
                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2)));            
                  }
               }));


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

        //BASIC_INSTRUCTIONS: I_FORMAT

        //addi - bsti(boosti)
        instructionList.add(
              new BasicInstruction("bsti $t1 ,$t2, -100", 
              "Addition immediate with overflow: set  $t1 to ($t2 plus signed 16-bit immediate)",
              BasicInstructionFormat.I_FORMAT, 
             "001000 sssss fffff tttttttttttttttt",
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

        //store-word(sw) / record(rec)
        instructionList.add(
            new BasicInstruction("rec $t1, -100($t2)",
            "Record word: set contents of $t1 into effective memory word address",
            BasicInstructionFormat.I_FORMAT,
            "101011 sssss fffff tttttttttttttttt",
            new SimulationCode()
            {
                public void simulate(ProgramStatement statement) throws ProcessingException
                {
                    int [] operands = statement.getOperands();
                    int base = RegisterFile.getValue(operands[2]);
                    int offset = operands[1] << 16 >> 16;
                    int address = base + offset;
                    try
                    {
                        Globals.memory.setWord(address, RegisterFile.getValue(operands[0]));
                    }
                    catch(AddressErrorException e)
                    {
                        throw new ProcessingException(statement, e); 
                    }
                }
            }));

        //load-word(lw)/ sample(smp)
        instructionList.add(
            new BasicInstruction("smp $t1, -100($t2)",
            "Sample word: sample(get) memory int your register",
            BasicInstructionFormat.I_FORMAT,
            "100011 sssss fffff tttttttttttttttt",
            new SimulationCode() 
                {
                    public void simulate(ProgramStatement statement) throws ProcessingException
                    {
                         int operands[] = statement.getOperands();
                         int base = RegisterFile.getValue(operands[2]);
                         int offset = operands[1] << 16 >> 16;
                         int address = base + offset;
                         
                        try
                        {
                            RegisterFile.updateRegister(operands[0],
                                Globals.memory.getWord(address)); 
                        }
                        catch(AddressErrorException e)
                        {
                            throw new ProcessingException(statement, e); 
                        }
                    }
                })); 


        //beq(branch equal) - csn(consonance)
        instructionList.add(
                new BasicInstruction("csn $t1,$t2,label",
                "Branch if equal : Branch to statement at label's address if $t1 and $t2 are equal",
            	 BasicInstructionFormat.I_BRANCH_FORMAT,
                "000100 sssss fffff tttttttttttttttt",
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
                "000101 sssss fffff tttttttttttttttt",
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


     //CREATIVE INSTRUCTIONS

    //CREATIVE_______R-FORMAT
     instructionList.add(
            new BasicInstruction("cf $t1, $t2", "swap two register values $t1 and $t2",
            BasicInstructionFormat.R_FORMAT,
             "001010 sssss ttttt fffff 00000 000111",
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

     //mut - muting:set all values to 0
        instructionList.add(
            new BasicInstruction("mut $t1, $t2, $t3", "set $t1, $t2, $t3 to 0",
            BasicInstructionFormat.R_FORMAT,
             "001010 sssss ttttt fffff 00000 000010",
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
             "001010 sssss ttttt fffff 00000 000110",
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
            

        //dj: get a random track
       instructionList.add(
                new BasicInstruction("dj",
            	 "Ready for a suprise? Let's mix some tracks",
                BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 000010", 
                new SimulationCode()
               {
                  public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random random = new Random();
                     int track = random.nextInt(6);
                     SystemIO.printString("Random track: " + track + "\n");
                     RegisterFile.updateRegister(2, track);

                     switch (RegisterFile.getValue(2)){ 
                        case 0:
                           SystemIO.printString("SAIROI by Khoi Vu\n");
                           break;
                        case 1:
                           SystemIO.printString("EM by Binz!\n");
                           break;
                        case 2:
                           SystemIO.printString("Versace On The Floor By Bruno Mars\n");
                           break;
                        case 3:
                           SystemIO.printString("Ghost by Justin Bieber\n");
                           break;
                        case 4:
                           SystemIO.printString("Everytime by Ariana Grande\n");
                           break;
                        case 5:
                           SystemIO.printString("Paris by The Chainsmoker\n");
                           break;
                        case 6:
                           SystemIO.printString("Moonlight by Kali Uchis\n");
                           break;
                        case 7:
                            SystemIO.printString("The Dress by Dijon\n");
                        default:
                           SystemIO.printString("System out of tracks!\n");
                    }
                }
            }));


        //xmas: get a random track
       instructionList.add(
                new BasicInstruction("xmas",
            	 "Let's mix xmas tracks",
                BasicInstructionFormat.R_FORMAT,
                "000000 00000 00000 00000 00000 000011", 
                new SimulationCode()
               {
                  public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     Random random = new Random();
                     int track = random.nextInt(6);
                     SystemIO.printString("Xmas Track: " + track + "\n");
                     RegisterFile.updateRegister(2, track);

                     switch (RegisterFile.getValue(2)){ 
                        case 0:
                           SystemIO.printString("Jingle Bell\n");
                           break;
                        case 1:
                           SystemIO.printString("White Christmas\n");
                           break;
                        case 2:
                           SystemIO.printString("A Holly Jolly\n");
                           break;
                        case 3:
                           SystemIO.printString("We Wish You A Merry Christmas\n");
                           break;
                        case 4:
                           SystemIO.printString("Santa Tell Me\n");
                           break;
                        case 5:
                           SystemIO.printString("Santa Baby\n");
                           break;
                        case 6:
                           SystemIO.printString("Snowman\n");
                           break;
                        case 7:
                            SystemIO.printString("Silent Night\n");
                        default:
                           SystemIO.printString("System out of tracks!\n");
                    }
                }
            }));

     //sync - release: set value the same for all registers
        instructionList.add(
            new BasicInstruction("sync $t3, $t1, $t2", "set St1 and $t2 same as its destination, $t3",
            BasicInstructionFormat.R_FORMAT,
             "001010 sssss ttttt fffff 00000 001000",
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

             
    //CREATIVE_______I-FORMAT
    //CREATIVE_______J-FORMAT


    }
    
}
