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

        if (input == null || input.isEmpty()) {
            return "Input cannot be null or empty.\n"; // Handle empty input gracefully
        }

        //Using List for command to prevent command injection
        List<String> command = Arrays.asList("bash", "-c", "/usr/games/cowsay");
        command.add("'" + escapeCommandInjection(input) + "'");


        try (ProcessBuilder processBuilder = new ProcessBuilder(command);
             Process process = processBuilder.start();
             BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            //Error handling for process exit code.
            int exitCode = process.waitFor();
            if(exitCode != 0){
                throw new IOException("cowsay process exited with error code: " + exitCode);
            }

        } catch (IOException e) {
            return "Error executing cowsay: " + e.getMessage() + "\n"; // More informative error message
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "cowsay process interrupted: " + e.getMessage() + "\n";
        }

        return output.toString();
    }
    
    //Function to escape special characters to prevent command injection
    private static String escapeCommandInjection(String input){
        return input.replace("'", "'\\''");
    }
}
