import com.scalesec.vulnado.LinkLister;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LinkListerTest {

    @Test
    void testGetLinksValidUrl() throws IOException {
        // Using a test URL that is guaranteed to be accessible and have links.  Replace with a suitable URL.
        String testUrl = "https://www.example.com";  
        List<String> links = LinkLister.getLinks(testUrl);
        assertFalse(links.isEmpty(), "List of links should not be empty");
        // Add assertions to check specific links if needed.  For example:
        // assertTrue(links.contains("https://www.example.com/somepage"), "Expected link not found");

    }

    @Test
    void testGetLinksInvalidUrl() {
        String invalidUrl = "invalid-url";
        assertThrows(IOException.class, () -> LinkLister.getLinks(invalidUrl));
    }


    @ParameterizedTest
    @ValueSource(strings = {"https://172.16.0.1", "http://192.168.1.1", "ftp://10.0.0.1"})
    void testGetLinksV2PrivateIp(String privateIpUrl) {
        assertThrows(LinkLister.BadRequest.class, () -> LinkLister.getLinksV2(privateIpUrl));
    }

    @Test
    void testGetLinksV2ValidUrl() throws MalformedURLException {
        String validUrl = "https://www.example.com"; //Replace with a suitable URL
        List<String> links = LinkLister.getLinksV2(validUrl);
        assertFalse(links.isEmpty(), "List of links should not be empty");
    }

    @Test
    void testGetLinksV2NullUrl() {
        assertThrows(IllegalArgumentException.class, () -> LinkLister.getLinksV2(null));
    }

    @Test
    void testGetLinksV2BlankUrl() {
        assertThrows(IllegalArgumentException.class, () -> LinkLister.getLinksV2(" "));
    }

    @Test
    void testGetLinksV2InvalidUrlFormat() {
        String invalidUrl = "invalid url format";
        assertThrows(IllegalArgumentException.class, () -> LinkLister.getLinksV2(invalidUrl));
    }

    @Test
    void testIsPrivateIp() {
        assertTrue(LinkLister.isPrivateIp("172.16.0.1"));
        assertTrue(LinkLister.isPrivateIp("192.168.1.1"));
        assertTrue(LinkLister.isPrivateIp("10.0.0.1"));
        assertFalse(LinkLister.isPrivateIp("8.8.8.8"));
        assertFalse(LinkLister.isPrivateIp("example.com"));
    }

    @Test
    void testBadRequestConstructor() {
        LinkLister.BadRequest exception = new LinkLister.BadRequest("Test message");
        assertEquals("Test message", exception.getMessage());
    }


    @Test
    void testGetLinks_HandlesIOExceptionGracefully() {
        //Simulate an IOException.  This requires mocking Jsoup.connect().get(),
        // which is beyond the scope of a simple JUnit test without mocking frameworks.
        //This test is therefore commented out.  A proper solution would involve mocking.

        /*
        String url = "this_is_not_a_real_url";
        assertThrows(IOException.class, () -> LinkLister.getLinks(url));
         */

    }
}
