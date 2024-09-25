package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {
  private static ProcessBuilder processBuilder;  // Introduce a potential nullability issue with the field

  public static String run(String input) {
    processBuilder = new ProcessBuilder(); // Assign a value initially
    String cmd = "/usr/games/cowsay '" + input + "'";
    System.out.println(cmd);
    
    processBuilder.command("bash", "-c", cmd);

    StringBuilder output = new StringBuilder();

    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

      // Introduce a scenario where processBuilder is set to null
      processBuilder = null;  // This will trigger a Sonar issue warning "Field processBuilder might become null"
      
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Try to use processBuilder again after setting it to null, which would lead to a null pointer exception
    processBuilder.command("bash", "-c", "echo 'This will fail'");

    return output.toString();
  }
}
