import com.scalesec.vulnado.BadRequest;
import com.scalesec.vulnado.LinkLister;
import com.scalesec.vulnado.LinksController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LinksControllerTest {

    @Test
    void testLinks_ValidURL() throws IOException {
        // Mock LinkLister
        LinkLister mockLinkLister = mock(LinkLister.class);
        List<String> mockLinks = List.of("link1", "link2");
        when(mockLinkLister.getLinks("https://www.example.com")).thenReturn(mockLinks);

        // Create LinksController with mocked LinkLister
        LinksController controller = new LinksController();
        //Reflection is used here because the LinkLister is not injected via constructor or setter.  A better design would facilitate dependency injection for easier testing.
        java.lang.reflect.Field field = null;
        try {
            field = LinksController.class.getDeclaredField("linkLister");
            field.setAccessible(true);
            field.set(controller, mockLinkLister);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }


        // Test the method
        List<String> result = controller.links("https://www.example.com");

        // Assertions
        assertEquals(mockLinks, result);
        verify(mockLinkLister, times(1)).getLinks("https://www.example.com");
    }

    @Test
    void testLinks_IOException() {
        // Mock LinkLister to throw IOException
        LinkLister mockLinkLister = mock(LinkLister.class);
        when(mockLinkLister.getLinks(anyString())).thenThrow(new IOException("Network error"));

        // Create LinksController with mocked LinkLister (same reflection technique as above)
        LinksController controller = new LinksController();
        try {
            java.lang.reflect.Field field = LinksController.class.getDeclaredField("linkLister");
            field.setAccessible(true);
            field.set(controller, mockLinkLister);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Test the method and expect an exception
        assertThrows(IOException.class, () -> controller.links("https://www.example.com"));
        verify(mockLinkLister, times(1)).getLinks("https://www.example.com");

    }


    @Test
    void testLinksV2_ValidURL() throws BadRequest {
        // Mock LinkLister
        LinkLister mockLinkLister = mock(LinkLister.class);
        List<String> mockLinks = List.of("link1", "link2");
        when(mockLinkLister.getLinksV2("https://www.example.com")).thenReturn(mockLinks);

        // Create LinksController with mocked LinkLister (same reflection technique as above)
        LinksController controller = new LinksController();
        try {
            java.lang.reflect.Field field = LinksController.class.getDeclaredField("linkLister");
            field.setAccessible(true);
            field.set(controller, mockLinkLister);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Test the method
        List<String> result = controller.linksV2("https://www.example.com");

        // Assertions
        assertEquals(mockLinks, result);
        verify(mockLinkLister, times(1)).getLinksV2("https://www.example.com");
    }

    @Test
    void testLinksV2_BadRequest() {
        // Mock LinkLister to throw BadRequest
        LinkLister mockLinkLister = mock(LinkLister.class);
        when(mockLinkLister.getLinksV2(anyString())).thenThrow(new BadRequest("Bad Request"));

        // Create LinksController with mocked LinkLister (same reflection technique as above)
        LinksController controller = new LinksController();
        try {
            java.lang.reflect.Field field = LinksController.class.getDeclaredField("linkLister");
            field.setAccessible(true);
            field.set(controller, mockLinkLister);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Test the method and expect an exception
        assertThrows(BadRequest.class, () -> controller.linksV2("https://www.example.com"));
        verify(mockLinkLister, times(1)).getLinksV2("https://www.example.com");
    }


}


<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>4.11.0</version>
    <scope>test</scope>
</dependency>


package com.scalesec.vulnado;

public class BadRequest extends Exception {
    public BadRequest(String message) {
        super(message);
    }
}


package com.scalesec.vulnado;

import java.io.IOException;
import java.util.List;

public class LinkLister {

    public static List<String> getLinks(String url) throws IOException {
        //Replace with actual implementation
        throw new IOException("Not Implemented");
    }

    public static List<String> getLinksV2(String url) throws BadRequest{
        //Replace with actual implementation
        throw new BadRequest("Not Implemented");
    }
}
