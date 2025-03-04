package com.scalesec.vulnado;

import org.springframework.boot.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.codec.digest.DigestUtils; //For constant-time comparison

//This is a placeholder. Replace with your actual datasource configuration.
//@Configuration
//@EnableAutoConfiguration
//public class DatabaseConfig {
//    @Bean
//    public DataSource dataSource() {
//        BasicDataSource dataSource = new BasicDataSource();
//        //Configure your datasource here.  Use a connection pool like HikariCP for better performance.
//        return dataSource;
//    }
//}


@RestController
@EnableAutoConfiguration
public class LoginController {

    private final String secret;
    private final DataSource dataSource; // Dependency injection for DataSource

    @Autowired
    public LoginController(@Value("${app.secret}") String secret, DataSource dataSource) {
        this.secret = secret;
        this.dataSource = dataSource;
    }


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public LoginResponse login(@RequestBody LoginRequest input) {
        //Input Validation
        if (input.username == null || input.username.isEmpty() || input.password == null || input.password.isEmpty()) {
            throw new BadRequest("Username and password are required.");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {

            statement.setString(1, input.username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                User user = mapResultSetToUser(resultSet); //Helper method

                // Constant-time comparison
                if (constantTimeCompare(DigestUtils.md5Hex(input.password), user.getHashedPassword())) {
                    return new LoginResponse(user.token(secret));
                } else {
                    throw new Unauthorized("Access Denied");
                }
            } else {
                throw new Unauthorized("Access Denied");
            }
        } catch (SQLException e) {
            throw new InternalServerError("Database error: " + e.getMessage());
        }
    }

    // Helper method to map ResultSet to User object.  Adapt to your User class structure.
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException{
        return new User(resultSet.getString("username"), resultSet.getString("hashedPassword"), resultSet.getString("token"));
    }


    // Constant-time comparison to prevent timing attacks
    private boolean constantTimeCompare(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}

class LoginRequest implements Serializable {
    public String username;
    public String password;
}

class LoginResponse implements Serializable {
    public String token;

    public LoginResponse(String msg) {
        this.token = msg;
    }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
    public Unauthorized(String exception) {
        super(exception);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequest extends RuntimeException{
    public BadRequest(String exception){
        super(exception);
    }
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class InternalServerError extends RuntimeException{
    public InternalServerError(String exception){
        super(exception);
    }
}

class User implements Serializable {
    private final String username;
    private final String hashedPassword;
    private final String token;


    public User(String username, String hashedPassword, String token){
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.token = token;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String token(String secret) {
        //  Token generation logic (replace with your actual implementation)
        return "token_" + username; // Replace with proper token generation using the secret.
    }
}

// Removed Postgres class -  Database interaction handled by DataSource and PreparedStatement


