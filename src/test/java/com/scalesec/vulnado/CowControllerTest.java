import com.scalesec.vulnado.CowController;
import com.scalesec.vulnado.Cowsay;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

public class CowControllerTest {

    @Test
    void testCowsayDefault() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/cowsay", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("I love Linux!"));
    }


    @Test
    void testCowsayCustomInput() {
        RestTemplate restTemplate = new RestTemplate();
        String customInput = "Hello, world!";
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/cowsay?input=" + customInput, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(customInput));

    }

    @Test
    void testCowsayEmptyInput() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/cowsay?input=", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        //  The behavior with empty input might vary depending on Cowsay implementation. Adjust assertion accordingly.
        assertTrue(response.getBody().length() > 0); // Expecting some output, even if empty input.

    }

    @Test
    void testCowsaySpecialCharacters() {
        RestTemplate restTemplate = new RestTemplate();
        String specialInput = "<>\"';/\\"; // Test with various special characters
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/cowsay?input=" + specialInput, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(specialInput)); // or adjust assertion based on Cowsay's handling

    }


    @Test
    void testCowsayLongInput() {
        RestTemplate restTemplate = new RestTemplate();
        String longInput = "This is a very long string to test the cowsay function with a significantly long input string.";
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/cowsay?input=" + longInput, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(longInput));
    }


    //  Add more tests as needed to cover edge cases and different inputs.  Consider testing for exceptions if any are thrown by Cowsay.


    //This requires a mock Cowsay class  or running the application to test against a real server.
    //Below is an example with mocking

    /*@Test
    void testCowsayMocking(){
        CowController controller = new CowController();
        //mock Cowsay.run()
        when(Cowsay.run("test")).thenReturn("Mocked Cowsay Output");
        String result = controller.cowsay("test");
        assertEquals("Mocked Cowsay Output", result);
    }*/

}
