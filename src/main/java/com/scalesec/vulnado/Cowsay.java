package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

public class Cowsay {
  private static ProcessBuilder processBuilder;  // Uninitialized field that may cause null pointer issues

  public static String run(String input) {
    // A condition where the field might not be initialized
    if (input == null || input.isEmpty()) 
      processBuilder = null;  // Simulating a case where processBuilder could remain null
     else 
      processBuilder = new ProcessBuilder();
    

    StringBuilder output = new StringBuilder();

    try {
      // Potential null pointer dereference here if processBuilder is null
      processBuilder.command("bash", "-c", "/usr/games/cowsay '" + input + "'");

      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return output.toString();
  }
}
