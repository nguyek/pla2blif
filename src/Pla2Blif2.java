import java.io.*;
import java.util.*;

/**
 * Kevin Nguyen
 * Converts .pla files to .blif files
 * Requires .all pla files to be in a folder named 'pla'
 * Generates .blif files into a folder named 'blif' 
 * This class converts all pla files to .blif files with NO compression
 * No compression means no '-' substitutions for redundancies.
 * 
 * Must be careful of any hidden files like DS_Store. This program expects
 * input folder, pla, to be full of .pla files only!
 *
 * 
 */
public class Pla2Blif2 {

   public static void main(String[] args) throws FileNotFoundException {
      File directory = new File("pla");
      File[] contents = directory.listFiles();
      
      
      // Looping through all .pla files to create .blif files
      for (int i = 0; i < contents.length; i++) {
         File plaFile = new File(contents[i].getPath()); // directory
         Scanner console = new Scanner(plaFile);      // console to scan each file
         
         // Getting indices for file renaming later
         String path = contents[i].getPath();
         int slashIndex = path.indexOf("/")+1;
         int dotIndex = path.indexOf(".");
         
         // Adjancy lists
         String[] inputBits = new String[Integer.MAX_VALUE / 10000];
         String[] outputBits = new String[Integer.MAX_VALUE / 10000];
         
         // Variables for formatting
         int numDots = 0;
         String head = path.substring(slashIndex, dotIndex); // filename
         String[] inputs = null;
         String[] outputs = null;
         int numInputs = 0;
         int numOutputs = 0;
         
         int indexOfAdjArray = 0; // Where I am in the adjacency arrays when
                                  // adding
         
         // Begin parsing file and setting variables
         while (console.hasNextLine()) {
            String line = console.nextLine();
            if (line.length() > 0) {
               if (line.charAt(0) == '#') {
                  continue; // don't care about file header
               }
            
               if (line.charAt(0) == '.') {
                  if (line.contains(".ilb")) {
                     inputs = line.split(" ");
                     numInputs = inputs.length - 1;
                  } else if (line.contains(".ob")) {
                     outputs = line.split(" ");
                     numOutputs = outputs.length - 1;
                  }
                  continue; 
               }
            }
            
            // Parsing the meat of the file
            String[] bitValues = null;
            if (line.contains("\t")) {
               bitValues = line.split("\t"); // Some files are delimited
            } else {                         // by tabs instead of spaces
               bitValues = line.split(" ");  // so have to check.
            }
            if (bitValues.length > 1) {
               inputBits[indexOfAdjArray] = bitValues[0]; // saving input bits
               outputBits[indexOfAdjArray] = bitValues[1]; // saving outputbits
               indexOfAdjArray++; // incrementing location
            }
   
        }
        inputBits[indexOfAdjArray] = null; // null terminating the large array
        outputBits[indexOfAdjArray] = null; 
        
        // Renaming variables from file
        renameVars(inputs, true);
        renameVars(outputs, false);
        
        // Create blif file and close inputstream
        createAndPrintBlif(head, inputs, outputs, indexOfAdjArray, numOutputs, inputBits, outputBits);              
        console.close(); 
         
      }           
   }
      
      
      
    /**
     * Creates and prints the BLIF file to the 'blif' folder.
     * PRE: 'blif' folder must be created, and be in same directory of this file.
     *       head       : file name
     *       inputs     : input variables [i0, i1, i2 ....]
     *       output     : output variables[o0, o1, o2 ....]
     *       numEntries : number of inputs
     */
    public static void createAndPrintBlif(String head, String[] inputs, String[] outputs,
                                          int numEntries, int numOutputs, String[] inputBits,
                                          String[] outputBits) throws FileNotFoundException {
        // Beginning to print .blif files
         PrintWriter printer = null;
         try {
         
            // Printing header of blif file
            printer = new PrintWriter("blif/" + head + ".blif", "UTF-8");
            printer.println("# Kevin");
            printer.println(".model " + head);
            printer.print(".inputs ");
            for (int a = 1; a < inputs.length-1; a++) {
               printer.print(inputs[a] + " ");
            }
            printer.print(inputs[inputs.length-1]);
            printer.print('\n');
            printer.print(".outputs ");
            for (int a = 1; a < outputs.length-1; a++) {
               printer.print(outputs[a] + " ");
            }
            printer.print(outputs[outputs.length-1]);
            printer.print('\n');
            
            
            
            // For each output o0, o1, o2 .... 
            for (int col = 0; col < numOutputs; col++) {
               // Prints input and output formatting
               printer.print(".names ");
               for (int a = 1; a < inputs.length; a++) {
                  printer.print(inputs[a] + " ");  
               }
               
               printer.print(outputs[col+1]);
               printer.println();
               
               // Loop to go through all elements in the list <rows>
               for (int a = 0; a < numEntries; a++) {
                  String outputLine = outputBits[a];
                  if (!outputLine.isEmpty()) {
                     if (outputLine.charAt(col) == '1') {
                        printer.println(inputBits[a] + " 1");
                     }                
                  }
              
               } 
            
            }
            printer.println(".end"); 
         } catch (UnsupportedEncodingException e) { 
            return;
         } finally {
            printer.close(); 
         } 
   
    }
   
   /* Renames the inputs and outputs of all pla files to be standard:
    *   inputs named as i(number)
    *   ouputs named as o(number)
    * xput is either in-put or out-put
    * isInput is true if xput represents inputs, false if output
    */
   private static void renameVars(String[] xput, boolean isInput) {
      char c = 'i';
      if (!isInput)
         c = 'o';
      for (int i = 0; i < xput.length; i++) {
         xput[i] = c + "" + i;
      }
   }
   
}