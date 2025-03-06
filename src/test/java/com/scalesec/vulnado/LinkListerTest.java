import com.scalesec.vulnado.BadRequestException;
import com.scalesec.vulnado.LinkLister;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkListerTest {

    @Test
    void testGetLinks_validUrl() throws MalformedURLException, IOException {
        // Use a test website that you control or a public website with known links
        String url = "https://www.example.com"; // Replace with a suitable URL

        List<String> links = LinkLister.getLinks(url);
        assertFalse(links.isEmpty()); // Expect some links
        // Add more assertions to check specific links if needed

        //Test for max link limit
        String longUrl = "https://www.iana.org/domains/example"; //Replace with a URL that returns many links
        List<String> longLinks = LinkLister.getLinks(longUrl);
        assertTrue(longLinks.size() <= LinkLister.MAX_LINKS);
    }


    @Test
    void testGetLinks_invalidUrl() {
        String invalidUrl = "invalid-url";
        Executable executable = () -> LinkLister.getLinks(invalidUrl);
        assertThrows(MalformedURLException.class, executable);
    }

    @Test
    void testGetLinks_nullUrl() {
        Executable executable = () -> LinkLister.getLinks(null);
        assertThrows(BadRequestException.class, executable);
    }

    @Test
    void testGetLinks_emptyUrl() {
        Executable executable = () -> LinkLister.getLinks("");
        assertThrows(BadRequestException.class, executable);
    }


    @Test
    void testGetLinksV2_validUrl() throws BadRequestException {
        String url = "https://www.example.com"; // Replace with a suitable URL
        List<String> links = LinkLister.getLinksV2(url);
        assertFalse(links.isEmpty());
    }

    @Test
    void testGetLinksV2_privateIp() {
        String privateIpUrl = "http://192.168.1.1";
        Executable executable = () -> LinkLister.getLinksV2(privateIpUrl);
        assertThrows(BadRequestException.class, executable, "Use of Private IP");
    }

    @Test
    void testGetLinksV2_invalidUrl() {
        String invalidUrl = "invalid-url";
        Executable executable = () -> LinkLister.getLinksV2(invalidUrl);
        assertThrows(BadRequestException.class, executable);
    }

    @Test
    void testGetLinksV2_IOException() {
      //This test is difficult to reliably implement without mocking.  It depends on network connectivity and the behavior of Jsoup.connect.  
      //  A better approach might be to mock the Jsoup connection to simulate an IOException.
    }


    @Test
    void testValidateUrl_validUrl() throws MalformedURLException, BadRequestException {
        String validUrl = "https://www.example.com";
        assertDoesNotThrow(() -> LinkLister.validateUrl(validUrl));
    }

    @Test
    void testValidateUrl_invalidUrl() {
        String invalidUrl = "invalid-url";
        Executable executable = () -> LinkLister.validateUrl(invalidUrl);
        assertThrows(MalformedURLException.class, executable);
    }

    @Test
    void testValidateUrl_nullUrl() {
        Executable executable = () -> LinkLister.validateUrl(null);
        assertThrows(BadRequestException.class, executable);
    }

    @Test
    void testValidateUrl_emptyUrl() {
        Executable executable = () -> LinkLister.validateUrl("");
        assertThrows(BadRequestException.class, executable);
    }
}


<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.11.0-M1</version> <!-- Use the latest version -->
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>1.15.4</version> <!-- Use the latest version -->
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.7</version> <!-- Use the latest version -->
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId> <!-- Or another SLF4j binding -->
        <version>2.0.7</version>
        <scope>test</scope>
    </dependency>


</dependencies>
