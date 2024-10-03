package com.scalesec.vulnado;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;

@WebMvcTest(CowController.class)
public class CowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Cowsay cowsay;

    @Test
    public void testCowsay() throws Exception {
        String input = "I love Linux!";
        String expectedOutput = "  ________\n" +
                " /        \\ \n" +
                "|          |\n" +
                "|  I love  |\n" +
                "|   Linux!  |\n" +
                "\\  ______  /\n" +
                " \\________/";

        when(cowsay.run(input)).thenReturn(expectedOutput);

        mockMvc.perform(MockMvcRequestBuilders.get("/cowsay")
                        .param("input", input)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(expectedOutput));
    }

    @Test
    public void testCowsayWithDefaultInput() throws Exception {
        String expectedOutput = "  ________\n" +
                " /        \\ \n" +
                "|          |\n" +
                "| I love  |\n" +
                "|  Linux!  |\n" +
                "\\  ______  /\n" +
                " \\________/";

        when(cowsay.run("I love Linux!")).thenReturn(expectedOutput);

        mockMvc.perform(MockMvcRequestBuilders.get("/cowsay")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(expectedOutput));
    }
}
