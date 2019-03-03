package ru.ifmo.rain.ilina.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

@SuppressWarnings("Duplicates")
public class Walk {
    public static void main(String[] args) {
        if (args == null) {
            System.out.println("Args is null");
            return;
        }
        if (args.length < 2) {
            System.out.println("Not enough args");
            return;
        }
        if (args[0] == null) {
            System.out.println("Wrong input file");
            return;
        }
        if (args[1] == null) {
            System.out.println("Wrong output file");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get(args[0]), Charset.forName("UTF-8"))) {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(args[1]), Charset.forName("UTF-8"))) {
                try {
                    String filePath = reader.readLine();
                    while (filePath != null) {
                        int fileHash;
                        try {
                            fileHash = FNVHash.getFNVByBlocks(Paths.get(filePath));
                        } catch (IOException ex) {
                            fileHash = 0;
                        }
                        try {
                            writer.write(String.format("%08x %s%n", fileHash, filePath));
                        } catch (InvalidPathException | IOException ex) {
                            System.out.println(String.format("Writing to %s crashed", args[1]));
                        }
                        filePath = reader.readLine();
                    }
                } catch (IOException ex) {
                    System.out.println(String.format("Reading to %s crashed", args[0]));
                }
            } catch (SecurityException ex) {
                System.out.println("Have no access to file: " + args[1]);
            } catch (InvalidPathException ex) {
                System.out.println(String.format("Path %s is incorrect", args[1]));
            } catch (NoSuchFileException ex) {
                System.err.println(String.format("File %s doesn't exist", args[1]));
            } catch (IOException ex) {
                System.out.println("Can't open/close: " + args[1]);
            }
        } catch (SecurityException ex) {
            System.out.println("Have no access to file: " + args[0]);
        } catch (InvalidPathException ex) {
            System.out.println(String.format("Path %s is incorrect", args[0]));
        } catch (NoSuchFileException ex) {
            System.err.println(String.format("File %s doesn't exist", args[0]));
        } catch (IOException ex) {
            System.out.println("Can't open/close: " + args[0]);
        }
    }
}
