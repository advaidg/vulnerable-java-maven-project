package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {

  // Private constructor to prevent instantiation
  private Cowsay() {
  }

  public static String run(String input) {
    StringBuilder output = new StringBuilder();

    try {
      // Validate input to prevent command injection
      if (input == null || input.isEmpty()) {
        throw new IllegalArgumentException("Input string cannot be empty.");
      }
      // Sanitize input using escaping
      String sanitizedInput = input.replaceAll("'", "\\'"); 

      ProcessBuilder processBuilder = new ProcessBuilder();
      // Use a secure command construction method
      processBuilder.command("bash", "-c", "/usr/games/cowsay '" + sanitizedInput + "'");

      Process process = processBuilder.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
        }
      }
    } catch (IllegalArgumentException e) {
      // Handle specific exception: provide a clear and informative error message
      System.err.println("Error: " + e.getMessage());
    } catch (Exception e) {
      // Log the exception with details for debugging
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }

    return output.toString();
  }
}
