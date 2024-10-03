package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {

    private Cowsay() {
        // Private constructor to prevent instantiation
    }

    public static String run(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty.");
        }
        StringBuilder output = new StringBuilder();
        try (ProcessBuilder processBuilder = new ProcessBuilder();
             BufferedReader reader = new BufferedReader(new InputStreamReader(processBuilder.command("bash", "-c", "/usr/games/cowsay '" + input + "'").start().getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            // Handle the specific exception type
            // Instead of just printing the stack trace, consider logging the error for debugging
            // and throwing a more informative exception to the caller
            throw new RuntimeException("Error running cowsay command.", e);
        }
        return output.toString();
    }
}
