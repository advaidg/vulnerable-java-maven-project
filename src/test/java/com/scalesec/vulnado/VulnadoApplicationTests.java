package com.scalesec.vulnado;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
class VulnadoApplicationTests {

    @Test
    void contextLoads() {
        //Example test:  This assumes VulnadoApplication has a method getGreeting()
        // Replace with an actual test relevant to your application
        VulnadoApplication app = new VulnadoApplication(); // Assuming VulnadoApplication is a Spring Boot application class
        assertNotNull(app.getGreeting()); //Replace with a meaningful assertion.

    }


    //Add other tests here as needed.

}
