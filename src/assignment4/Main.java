package assignment4;
/* CRITTERS Main.java
 * EE422C Project 4 submission by
 * Replace <...> with your actual data.
 * Zachary Williams
 * zw3622
 * 15470
 * <Student2 Name>
 * <Student2 EID>
 * <Student2 5-digit Unique No.>
 * Slip days used: <0>
 * Fall 2016
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;


/*
 * Usage: java <pkgname>.Main <input file> test
 * input file is optional.  If input file is specified, the word 'test' is optional.
 * May not use 'test' argument without specifying input file.
 */
public class Main {

    static Scanner kb;	// scanner connected to keyboard input, or input file
    private static String inputFile;	// input file, used instead of keyboard input if specified
    static ByteArrayOutputStream testOutputString;	// if test specified, holds all console output
    private static String myPackage;	// package of Critter file.  Critter cannot be in default pkg.
    private static boolean DEBUG = false; // Use it or not, as you wish!
    static PrintStream old = System.out;	// if you want to restore output to console


    // Gets the package name.  The usage assumes that Critter and its subclasses are all in the same package.
    static {
        myPackage = Critter.class.getPackage().toString().split(" ")[1];
    }

    /**
     * Main method.
     * @param args args can be empty.  If not empty, provide two parameters -- the first is a file name, 
     * and the second is test (for test output, where all output to be directed to a String), or nothing.
     */
    public static void main(String[] args) { 
        if (args.length != 0) {
            try {
                inputFile = args[0];
                kb = new Scanner(new File(inputFile));			
            } catch (FileNotFoundException e) {
                System.out.println("USAGE: java Main OR java Main <input file> <test output>");
                e.printStackTrace();
            } catch (NullPointerException e) {
                System.out.println("USAGE: java Main OR java Main <input file>  <test output>");
            }
            if (args.length >= 2) {
                if (args[1].equals("test")) { // if the word "test" is the second argument to java
                    // Create a stream to hold the output
                    testOutputString = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(testOutputString);
                    // Save the old System.out.
                    old = System.out;
                    // Tell Java to use the special stream; all console output will be redirected here from now
                    System.setOut(ps);
                }
            }
        } else { // if no arguments to main
            kb = new Scanner(System.in); // use keyboard and console
        }

        /* Do not alter the code above for your submission. */
        /* Write your code below. */

        String in = "";
        List<String> commands = new ArrayList<>();
        while (!in.equals("quit")){
            in = kb.nextLine();
            String [] command = in.split("\\s+", -2);
            switch (command[0]){
                case "show":
                    if (command.length != 1) {
                        System.out.println("error processing: " + errorToString(command));
                        break;
                    }
                    Critter.displayWorld();
                    break;

                case "make":
                    if (command.length == 2) {
                        try {
                            Critter.makeCritter(command[1]);
                        } catch (InvalidCritterException e) {
                            System.out.println("error processing: " + errorToString(command));
                        }
                    } else if (command.length == 3) {
                        try{
                            int count = Integer.parseInt(command[2]);
                            if (count <=0 ){
                                System.out.println("error processing: " + errorToString(command));
                                break;
                            }
                            for (int i=0; i<count; i++){
                                Critter.makeCritter(command[1]);
                            }
                        } catch (NumberFormatException | InvalidCritterException e){
                            System.out.println("error processing: " + errorToString(command));
                        }
                    } else{
                        System.out.println("error processing: " + errorToString(command));
                    }
                    break;

                case "step":
                    if (command.length ==1){
                        Critter.worldTimeStep();
                    } else if (command.length == 2){
                        try {
                            int count = Integer.parseInt(command[1]);
                            if (count <=0 ){
                                System.out.println("error processing: " + errorToString(command));
                                break;
                            }
                            for (int i=0; i<count; i++){
                                Critter.worldTimeStep();
                            }
                        } catch (NumberFormatException e){
                            System.out.println("error processing: " + errorToString(command));
                        }
                    } else{
                        System.out.println("error processing: " + errorToString(command));
                    }
                    break;

                case "clear":
                    if (command.length == 1) {
                        Critter.clearWorld();
                    } else{
                        System.out.println("error processing: " + errorToString(command));
                    }
                    break;

                case "stats":
                    if (command.length == 2){
                        try{
                            String myPackage = Critter.class.getPackage().toString().split(" ")[1];
                            Class myClass = Class.forName(myPackage + "." + command[1]);
                            List<Critter> critterList = new ArrayList<>();
                            critterList = Critter.getInstances(command[1]);
                            myClass.getMethod("runStats", List.class).invoke(null, critterList);
                        } catch (InvalidCritterException | ClassNotFoundException | NoSuchMethodException |
                                IllegalAccessException | InvocationTargetException e){
                            System.out.println("error processing: " + errorToString(command));
                        }
                    }
                    break;

                case "seed":
                    if (command.length == 2){
                        try{
                            int result = Integer.parseInt(command[1]);
                            Critter.setSeed(result);
                        } catch (NumberFormatException e){
                            System.out.println("error processing: " + errorToString(command));
                        }
                    } else {
                        System.out.println("error processing: " + errorToString(command));
                    }
                    break;

                default:
                    if (command[0].equals("quit") && command.length == 1){
                        continue;
                    } else {
                        System.out.println("invalid command: " + errorToString(command));
                    }
            }
        }
        System.out.println();

        /* Write your code above */
        System.out.flush();

    }

    private static String errorToString (String[] commands){
        StringBuilder stringBuilder = new StringBuilder();
        for (String string: commands){
            stringBuilder.append(string);
            stringBuilder.append(" ");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
