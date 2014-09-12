package com.github.lawena.vpk;

import java.io.File;

public class JavaVPK {
  /**
   * Main method.
   * 
   * @param args application arguments
   */
  public static void main(String[] args) {
    // header
    System.out.println("JavaVPK");
    System.out.println("(C) Connor Haigh 2014");
    System.out.println();

    if (args.length <= 0) {
      // usage
      System.out.println("Usage:");
      System.out.println("\t" + JavaVPK.INPUT_OPTION + "\t\tSpecify the input VPK file");
      System.out.println("\t" + JavaVPK.OUTPUT_OPTION + "\t\tSpecify the output directory");
      System.out.println("\t" + JavaVPK.VERBOSE_OPTION + "\tToggle verbose output");

      return;
    }

    // parameters
    String input = null;
    String output = null;
    boolean verbose = false;

    try {
      // loop arguments
      for (int argument = 0; argument < args.length; argument++) {
        switch (args[argument]) {
          case JavaVPK.INPUT_OPTION: {
            input = args[++argument];

            break;
          }
          case JavaVPK.OUTPUT_OPTION: {
            output = args[++argument];

            break;
          }
          case JavaVPK.VERBOSE_OPTION: {
            verbose = true;

            break;
          }
        }
      }

      // check arguments
      if (input == null || output == null)
        throw new Exception();
    } catch (Exception exception) {
      // invalid arguments
      System.err.println("Invalid arguments specified");

      return;
    }

    // create files
    File inputFile = new File(input);
    File outputDirectory = new File(output);

    try {
      // create directory
      System.out.println("Creating output directory...");
      outputDirectory.mkdirs();

      // load
      System.out.println("Loading archive...");
      Archive archive = new Archive(inputFile);
      archive.load();

      if (verbose) {
        // details
        System.out.println("\t" + inputFile.getName());
        System.out.println("\tSignature: " + archive.getSignature());
        System.out.println("\tVersion: " + archive.getVersion());
        System.out.println("\tDirectories: " + archive.getDirectories().size());
      }

      // loop directories
      System.out.println("Extracting all entries...");
      for (Directory directory : archive.getDirectories()) {
        if (verbose)
          System.out.println("\t" + directory.getPath());

        // loop entries
        for (Entry entry : directory.getEntries()) {
          if (verbose)
            System.out.println("\t\t" + entry.getFullName());

          try {
            // extract
            File entryDirectory = new File(outputDirectory, directory.getPath());
            File entryFile = new File(outputDirectory, directory.getPathFor(entry));
            entryDirectory.mkdirs();
            entry.extract(entryFile);
          } catch (Exception exception) {
            throw exception;
          }
        }
      }

      // done
      System.out.println("Extracted all entries successfully");
    } catch (Exception exception) {
      // failed
      System.err.println();
      System.err.println("Error during extraction: " + exception.getMessage());
    }
  }

  public static final String INPUT_OPTION = "-input";
  public static final String OUTPUT_OPTION = "-output";
  public static final String VERBOSE_OPTION = "-verbose";
}
