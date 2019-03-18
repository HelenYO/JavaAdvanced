package ru.ifmo.rain.ilina.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

//@SuppressWarnings("Duplicates")
public class RecursiveWalk {

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
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8))) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8))) {
                    try {
                        String path;
                        while ((path = br.readLine()) != null) {
                            Path path1;
                            try {
                                path1 = Paths.get(path);
                                try {
                                    Files.walkFileTree(path1, new SimpleFileVisitor<>(){
                                        @Override
                                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                                            try {
                                                bw.write(String.format("%08x", FNVHash.getFNVByBlocks(file)) + " " + file);
                                                bw.newLine();
                                            } catch (IOException e) {
                                                System.err.println("Error occurred during writing into output file: " + e.getMessage());
                                            }
                                            return CONTINUE;
                                        }
                                    });
                                } catch (IOException e) {
                                    System.err.println("Error occurred during scanning directory \"" + path1 + "\", error: " + e.getMessage());
                                    try {
                                        bw.write(String.format("%08x", 0) + " " + path);
                                        bw.newLine();
                                    } catch (IOException r) {
                                        System.err.println("Error occurred during writing output file: " + r.getMessage());
                                    }
                                }
                            } catch (InvalidPathException e) {
                                System.err.println("Invalid path: " + path);
                                try {
                                    bw.write(String.format("%08x", 0) + " " + path);
                                    bw.newLine();
                                } catch (IOException r) {
                                    System.err.println("Error during writing output file: " + e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error occurred during reading input file: " + e.getMessage());
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("The file exists but is a directory rather than a regular file, " +
                            "does not exist but cannot be created, " +
                            "or cannot be opened for any other reason: " + args[1]);
                } catch (IOException e) {
                    System.err.println("Error occurred: " + e.getMessage());
                }
            } catch (FileNotFoundException e) {
                System.err.println("File is not found: " + args[0]);
            } catch (IOException e) {
                System.err.println("Error occurred: " + e.getMessage());
            }

    }
}