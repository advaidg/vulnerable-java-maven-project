package com.scalesec.vulnado;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(LinksController.class)
public class LinksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LinkLister linkLister;

    @Test
    void testLinks() throws Exception {
        List<String> expectedLinks = Arrays.asList("https://www.example.com", "https://www.google.com");
        when(linkLister.getLinks("https://www.example.org")).thenReturn(expectedLinks);

        mockMvc.perform(MockMvcRequestBuilders.get("/links")
                        .param("url", "https://www.example.org")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[{\"link\":\"https://www.example.com\"},{\"link\":\"https://www.google.com\"}]"));
    }

    @Test
    void testLinksV2() throws Exception {
        List<String> expectedLinks = Arrays.asList("https://www.example.com", "https://www.google.com");
        when(linkLister.getLinksV2("https://www.example.org")).thenReturn(expectedLinks);

        mockMvc.perform(MockMvcRequestBuilders.get("/links-v2")
                        .param("url", "https://www.example.org")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[{\"link\":\"https://www.example.com\"},{\"link\":\"https://www.google.com\"}]"));
    }

    @Test
    void testLinksV2BadRequest() throws Exception {
        when(linkLister.getLinksV2("https://www.example.org")).thenThrow(new BadRequest("Invalid URL"));

        mockMvc.perform(MockMvcRequestBuilders.get("/links-v2")
                        .param("url", "https://www.example.org")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
