package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Optional;

public class Cowsay {

  private static ProcessBuilder processBuilder = new ProcessBuilder();  // Safe Initialization
  
  public static String run(String input) {
    
    // Use Optional to handle null input more explicitly
    Optional<String> optInput = Optional.ofNullable(input);
    String validatedInput = optInput.filter(i -> !i.isEmpty()).orElse(null);
    
    // If input is invalid (null or empty), return a default message without causing errors
    if (validatedInput == null) {
      return "Invalid input. Please provide a valid message.";
    }

    StringBuilder output = new StringBuilder();

    try {
      // Safe command injection protection by avoiding direct String concatenation
      processBuilder.command("/usr/games/cowsay", validatedInput);  

      Process process = processBuilder.start();
      
      // Use try-with-resources to handle potential resource leaks
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
          String line;
          while ((line = reader.readLine()) != null) {
              output.append(line).append("\n");
          }
      }

    } catch (IOException e) {
      // Stack trace for simplicity, should be replaced by a proper logging framework per enterprise standards
      e.printStackTrace();
    }

    return output.toString();
  }
}
