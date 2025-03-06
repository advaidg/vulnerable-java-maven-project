import com.scalesec.vulnado.Cowsay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class CowsayTest {

    @Test
    void testNullInput() {
        String result = Cowsay.run(null);
        assertEquals("No input provided to cowsay.", result);
    }

    @Test
    void testEmptyInput() {
        String result = Cowsay.run("");
        assertEquals("No input provided to cowsay.", result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello, world!", "This is a test.", "It's working!"})
    void testValidInput(String input) {
        String result = Cowsay.run(input);
        // Cannot reliably assert the exact output of cowsay without knowing the specific system's cowsay implementation
        // Instead, we check that it's not an error message and contains the input
        assertFalse(result.startsWith("Error"));
        assertTrue(result.contains(input));
    }


    @Test
    void testInputWithSingleQuote() {
        String input = "This is a test with a 'single quote'.";
        String result = Cowsay.run(input);
        assertFalse(result.startsWith("Error"));
        assertTrue(result.contains(input));
    }

    @Test
    void testInputWithMultipleSingleQuotes() {
        String input = "This is a test with multiple 'single' 'quotes'.";
        String result = Cowsay.run(input);
        assertFalse(result.startsWith("Error"));
        assertTrue(result.contains(input));

    }

    @Test
    void testEscapingSingleQuotes(){
        String input = "'; rm -rf /; '"; //Malicious input attempting command injection.
        String result = Cowsay.run(input);
        assertFalse(result.startsWith("Error"));
        assertTrue(result.contains(input)); //The escape function should prevent command execution

    }


    //This test is highly system dependent and may fail on some systems. Consider removing or adjusting based on your needs.
    @Test
    void testNonExistentCowsay() {
        //Simulate a case where /usr/games/cowsay doesn't exist.  Requires modification to Cowsay class to be useful.
        //This test would require mocking or a different approach to reliably test this scenario without relying on the system's /usr/games/cowsay.
       // String result = Cowsay.run("test");
       // assertTrue(result.startsWith("Error"));

    }


    @Test
    void testSecurityException(){
        //This test requires a way to artificially trigger a SecurityException.  This is highly system and context dependent.
        //Consider using mocking or another strategy that simulates security restrictions.
       // String result = Cowsay.run("test");
       // assertTrue(result.startsWith("Error: Insufficient permissions to execute cowsay."));

    }


}
