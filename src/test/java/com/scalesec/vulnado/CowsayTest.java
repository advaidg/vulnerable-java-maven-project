package com.scalesec.vulnado;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.assertThrows;

import static org.junit.jupiter.api.Assertions.*;

class CowsayTest {

    @Test
    void run_validInput_returnsExpectedOutput() {
        String input = "Hello, world!";
        String expectedOutput =  "  ________________________\n" +
                                " /  _  _  _  _  _  _  _  \\\n" +
                                "(  (_) (_) (_) (_) (_) )\n" +
                                " \\   _   _   _   _   _   /\n" +
                                "  `-'`-'`-'`-'`-'`-'`-'`\n" +
                                "                \\   ^__^\n" +
                                "                \\  (oo)\\_______\n" +
                                "                 (__)\\       )\\/\\\n" +
                                "                    ||----w |   \\  \\  \\n" +
                                "                    ||     ||\n" +
                                "                    ||_____||\n" +
                                "Hello, world!";

        String actualOutput = Cowsay.run(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void run_nullInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Cowsay.run(null));
    }

    @Test
    void run_emptyInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Cowsay.run(""));
    }
}
