package com.scalesec.vulnado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.io.Serializable;
import java.util.Optional;

@RestController
@SpringBootApplication
public class LoginController {

    @Value("${app.secret}")
    private String secret;

    @Autowired
    private UserService userService;

    @CrossOrigin(origins = "*")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest input) {
        Optional<User> user = userService.getUserByUsername(input.username);
        if (user.isPresent() && user.get().getHashedPassword().equals(Postgres.md5(input.password))) {
            return new LoginResponse(user.get().token(secret));
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
    public LoginResponse(String msg) { this.token = msg; }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
    public Unauthorized(String exception) {
        super(exception);
    }
}
