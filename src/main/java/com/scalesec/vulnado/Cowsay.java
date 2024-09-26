package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {

  /**
   * Prevents instantiation of this utility class.
   */
  private Cowsay() {
  }

  /**
   * Executes the `cowsay` command with the given input and returns the output.
   *
   * @param input The input string to be displayed by `cowsay`.
   * @return The output of the `cowsay` command.
   */
  public static String run(String input) {
    if (input == null || input.isEmpty()) {
      return ""; // Return empty string for empty or null input
    }
    StringBuilder output = new StringBuilder();
    ProcessBuilder processBuilder = new ProcessBuilder(); // Declare locally to avoid potential null issues
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        processBuilder.command("bash", "-c", "/usr/games/cowsay '" + input + "'").start().getInputStream()))) {
      // Safely handle process execution and stream reading using try-with-resources for automatic closing
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    } catch (Exception e) {
      // Log errors instead of printing stacktrace
      System.err.println("Error executing cowsay: " + e.getMessage());
      // Optionally throw a custom exception for specific error handling
      // throw new CowsayExecutionException("Error executing cowsay", e);
    }
    return output.toString();
  }
}
