package com.scalesec.vulnado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.UUID;

@RestController
@EnableAutoConfiguration
public class LoginController {

    @Value("${app.secret}")
    private String secret;

    @Autowired
    private UserService userService;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    LoginResponse login(@RequestBody LoginRequest input, HttpSession session) {
        // Generate a CSRF token for the session
        String csrfToken = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", csrfToken);

        User user = userService.fetchUser(input.username);
        if (user.isPresent() && user.get().verifyPassword(input.password)) {
            return new LoginResponse(user.get().token(secret), csrfToken);
        } else {
            throw new Unauthorized("Access Denied");
        }
    }
}

class LoginRequest implements Serializable {
    public String username;
    public String password;
}

class LoginResponse implements Serializable {
    public String token;
    public String csrfToken; // Add CSRF token to the response

    public LoginResponse(String token, String csrfToken) {
        this.token = token;
        this.csrfToken = csrfToken;
    }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
    public Unauthorized(String exception) {
        super(exception);
    }
}

// User service to handle user operations
@Service
class UserService {
    @Autowired
    private UserRepository userRepository;

    public Optional<User> fetchUser(String username) {
        return userRepository.findByUsername(username);
    }
}

// User repository interface (replace with your actual implementation)
interface UserRepository {
    Optional<User> findByUsername(String username);
}

// User entity class
class User implements Serializable {
    private String username;
    private String hashedPassword; // Use a secure hashing algorithm

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public boolean verifyPassword(String password) {
        // Implement secure password verification
        // Example: compare the provided password with the hashed password
        // ... 
    }

    public String token(String secret) {
        // Implement token generation using a secure algorithm
        // Example: generate a JWT token
        // ...
        return ""; // Replace with actual token generation
    }
}
