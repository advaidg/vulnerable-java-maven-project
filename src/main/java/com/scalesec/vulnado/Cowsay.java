package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public abstract class Cowsay {

  private Cowsay() {
  }

  public static String run(String input) {
    if (input == null || input.isEmpty()) {
      return "";
    }

    StringBuilder output = new StringBuilder();

    try {
      ProcessBuilder processBuilder = new ProcessBuilder();
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
