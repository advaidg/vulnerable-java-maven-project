import com.scalesec.vulnado.CowController;
import com.scalesec.vulnado.Cowsay; // Assuming this class exists and is accessible
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

public class CowControllerTest {

    @Test
    void testCowsayDefaultInput() {
        CowController controller = new CowController();
        String result = controller.cowsay(""); // Test with empty input, relying on default
        assertEquals("  _____________________________\n"+
                     " /                             \\\n"+
                     "| I love Linux!                 |\n"+
                     "\\_____________________________/\n"+
                     "        \\   ^__^\n"+
                     "         \\  (oo)\\_______\n"+
                     "            (__)\\       )\\/\n"+
                     "                ||----w |\n"+
                     "                ||     ||", result);

    }

    @Test
    void testCowsayCustomInput() {
        CowController controller = new CowController();
        String input = "Hello, world!";
        String result = controller.cowsay(input);
        assertEquals("  _________________________\n"+
                     " /                           \\\n"+
                     "| Hello, world!              |\n"+
                     "\\___________________________/\n"+
                     "        \\   ^__^\n"+
                     "         \\  (oo)\\_______\n"+
                     "            (__)\\       )\\/\n"+
                     "                ||----w |\n"+
                     "                ||     ||", result);
    }


    @Test
    void testCowsayWithSpecialCharacters() {
        CowController controller = new CowController();
        String input = "This is a test with <>&\"' characters.";
        String result = controller.cowsay(input);
        //Assertion will depend on how Cowsay handles special characters.  Adjust expected output accordingly.
        assertNotNull(result); //At minimum, ensure it doesn't throw an exception.

    }

    @Test
    void testCowsayLongInput(){
        CowController controller = new CowController();
        String longInput = "This is a very long string to test how Cowsay handles long inputs.  It should truncate or wrap appropriately.";
        String result = controller.cowsay(longInput);
        //Assertion will depend on how Cowsay handles long inputs.  Adjust expected output accordingly.  May need to check length or substring.
        assertNotNull(result);
    }


    //Integration test - requires a running Spring Boot application.  This is more complex and outside the scope of a simple unit test.
    //@Test
    void integrationTestCowsayEndpoint(){
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/cowsay?input=Testing%20the%20endpoint"; //Replace with your application URL and port
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        //Further assertions on the response body can be added based on expected output.
        assertNotNull(response.getBody());
    }

}


<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
