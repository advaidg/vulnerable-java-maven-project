package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class Cowsay {

    private Cowsay() {
        // Private constructor to prevent instantiation
    }

    public static String run(String input) {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder; // Declare locally

        if (input == null || input.isEmpty()) {
            //Handle empty input -  log and return default message
            System.err.println("Error: Input cannot be null or empty.");
            return "No input provided to cowsay.";
        }

        try {
            // Prevent command injection
            List<String> command = Arrays.asList("bash", "-c", "/usr/games/cowsay");
            command.add("'" + escapeShellInput(input) + "'"); //Escape input before adding to command
            processBuilder = new ProcessBuilder(command);


            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error executing cowsay: " + e.getMessage());
            return "Error executing cowsay command."; // Return informative error message.
        } catch (SecurityException e) {
            System.err.println("Security Exception executing cowsay: " + e.getMessage());
            return "Error: Insufficient permissions to execute cowsay."; //Specific error for security exceptions
        }
        return output.toString();
    }

    // Helper function to escape shell metacharacters from user input
    private static String escapeShellInput(String input) {
        return input.replace("'", "'\\''");  //Escape single quotes
    }
}
