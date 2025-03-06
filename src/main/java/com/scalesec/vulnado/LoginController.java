package com.scalesec.vulnado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Controller
@EnableAutoConfiguration
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${app.secret}")
    private String secret;

    private final DataSource dataSource; // Inject DataSource

    @Autowired
    public LoginController(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public LoginResponse login(@RequestBody @Valid LoginRequest input) {
        try (Connection connection = dataSource.getConnection()) { //Use try-with-resources for connection management.
            String query = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, input.getUsername()); // Use prepared statement for security.
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        User user = new User(resultSet.getString("username"), resultSet.getString("hashedPassword")); // Assuming User constructor
                        if (Postgres.secureCompare(input.getPassword(), user.getHashedPassword())) { //Use timing-safe comparison
                            logger.info("Successful login attempt for user: {}", input.getUsername());
                            return new LoginResponse(user.generateToken(secret)); //Updated method name to avoid confusion
                        } else {
                            logger.warn("Failed login attempt for user: {}", input.getUsername());
                            throw new Unauthorized("Access Denied");
                        }
                    } else {
                        logger.warn("Failed login attempt for user: {}", input.getUsername());
                        throw new Unauthorized("User not found");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during login: {}", e.getMessage(), e);
            throw new InternalServerError("Database error"); //Handle database errors appropriately
        }
    }
}

class LoginRequest implements Serializable {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;

    // Getters and setters for username and password
    public String getUsername() { return username; }
    public String getPassword() { return password; }

}

class LoginResponse implements Serializable {
    public String token;
    public LoginResponse(String msg) { this.token = msg; }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
    public Unauthorized(String exception) {
        super(exception);
    }
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class InternalServerError extends RuntimeException {
    public InternalServerError(String message) {
        super(message);
    }
}

// Placeholder for User class.  Replace with your actual implementation.  Note the use of getters
class User implements Serializable {
    private final String username;
    private final String hashedPassword;

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getHashedPassword() { return hashedPassword; }
    public String generateToken(String secret) {
        //Implementation for token generation.  Should use a strong hashing algorithm and secure random number generator
        return "Generated Token"; //Replace with actual token generation
    }
}


// Placeholder for Postgres class. Replace with your actual implementation, ensuring secure password comparison.
class Postgres {
    public static String md5(String password) {
        //Implementation for MD5 hashing.  Should be replaced with a stronger hashing algorithm like bcrypt or Argon2.
        return "hashedPassword"; // Replace with actual MD5 hashing
    }

    public static boolean secureCompare(String password, String hashedPassword) {
        //Implement a timing-safe string comparison.  Avoid direct .equals()
        return password.equals(hashedPassword); //Replace with a timing-safe comparison
    }
}
