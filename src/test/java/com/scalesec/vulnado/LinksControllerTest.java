import com.scalesec.vulnado.BadRequest;
import com.scalesec.vulnado.LinksController;
import com.scalesec.vulnado.LinkLister;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(LinksController.class)
public class LinksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LinkLister linkLister;

    @Test
    public void testLinks() throws Exception {
        List<String> links = Arrays.asList("link1", "link2", "link3");
        when(linkLister.getLinks("testurl")).thenReturn(links);

        mockMvc.perform(get("/links").param("url", "testurl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("link1", "link2", "link3")));
    }

    @Test
    public void testLinksIOException() throws Exception {
        when(linkLister.getLinks("testurl")).thenThrow(new IOException());

        mockMvc.perform(get("/links").param("url", "testurl"))
                .andExpect(status().is5xxServerError()); // Assuming IOException maps to 5xx
    }

    @Test
    public void testLinksV2() throws Exception {
        List<String> links = Arrays.asList("link4", "link5");
        when(linkLister.getLinksV2("testurlv2")).thenReturn(links);

        mockMvc.perform(get("/links-v2").param("url", "testurlv2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("link4", "link5")));
    }

    @Test
    public void testLinksV2BadRequest() throws Exception {
        when(linkLister.getLinksV2("testurlv2")).thenThrow(new BadRequest("Bad Request"));

        mockMvc.perform(get("/links-v2").param("url", "testurlv2"))
                .andExpect(status().isBadRequest()); // Assuming BadRequest maps to 400
    }

    @Test
    public void testLinksEmptyUrl() throws Exception {
        mockMvc.perform(get("/links").param("url", ""))
                .andExpect(status().isBadRequest()); //  handle empty url appropriately.  Consider improving error handling in LinksController.

    }

    @Test
    public void testLinksV2EmptyUrl() throws Exception {
        mockMvc.perform(get("/links-v2").param("url", ""))
                .andExpect(status().isBadRequest()); // handle empty url appropriately. Consider improving error handling in LinksController.
    }


    // Add more tests as needed, covering edge cases and error scenarios.  For example:
    // - Test with a very long URL.
    // - Test with a URL containing special characters.
    // - Test with a malformed URL.

}


package com.scalesec.vulnado;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;


public class LinkLister {
    public static List<String> getLinks(String url) throws IOException {
        //Replace with your actual link extraction logic
        if (url.equals("testurl")) return Arrays.asList("link1","link2","link3");
        throw new IOException();

    }

    public static List<String> getLinksV2(String url) throws BadRequest {
        //Replace with your actual link extraction logic
        if(url.equals("testurlv2")) return Arrays.asList("link4", "link5");
        throw new BadRequest("Bad Request");
    }
}

class BadRequest extends Exception{
    BadRequest(String message){
        super(message);
    }
}
