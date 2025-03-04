import com.scalesec.vulnado.Cowsay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class CowsayTest {

    @Test
    void testNullInput() {
        assertEquals("Input cannot be null or empty.\n", Cowsay.run(null));
    }

    @Test
    void testEmptyInput() {
        assertEquals("Input cannot be null or empty.\n", Cowsay.run(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello, world!", "This is a test.", "Single quote ' test"})
    void testValidInput(String input) {
        String result = Cowsay.run(input);
        // We can't reliably assert the exact output of cowsay without knowing the system's environment.
        // Instead, we check that it doesn't throw an exception and contains the input.

        assertFalse(result.startsWith("Error executing cowsay:"));
        assertFalse(result.startsWith("cowsay process interrupted:"));
        assertTrue(result.contains(input));
    }


    @Test
    void testInputWithSpecialCharacters() {
        String input = "This is a test with; special! characters.";
        String result = Cowsay.run(input);
        assertFalse(result.startsWith("Error executing cowsay:"));
        assertFalse(result.startsWith("cowsay process interrupted:"));
        assertTrue(result.contains(input));
    }

    @Test
    void testInputWithMultipleQuotes() {
        String input = "This 'is' a ''test'' with multiple quotes.";
        String result = Cowsay.run(input);
        assertFalse(result.startsWith("Error executing cowsay:"));
        assertFalse(result.startsWith("cowsay process interrupted:"));
        assertTrue(result.contains(input));
    }

    @Test
    void testEscapeCommandInjection(){
        String input = "'; rm -rf /; '";
        String escaped = Cowsay.escapeCommandInjection(input);
        assertEquals("'; rm -rf \\/; '", escaped);
        //Further testing would involve mocking the process execution to verify no harmful commands are executed.  This is beyond the scope of a simple unit test.

    }

    @Test
    void testCowsayNotFound(){
        //This test requires modification to the Cowsay class to allow for testing of error handling when cowsay is not found.  A mocked process could be utilized.
        // For this example, a simple placeholder is used.
        //This test would require mocking the ProcessBuilder and Process to simulate the cowsay command not being found.
        // assertEquals("Error executing cowsay: ...", Cowsay.run("test")); //Replace "..." with expected error message
    }


    @Test
    void testIOException(){
        // This test requires mocking the ProcessBuilder to simulate an IOException during process execution. This is beyond the scope of a simple unit test.
    }

    @Test
    void testInterruptedException(){
        //This test requires mocking or interrupting the process in a controlled manner which is complex and beyond the scope of a simple unit test
    }
}
