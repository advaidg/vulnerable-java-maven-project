package com.scalesec.vulnado;

import org.springframework.boot.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@RestController
@EnableAutoConfiguration
public class LoginController {

  @Value("${app.secret}")
  private String secret;

  @Value("${database.url}")
  private String databaseUrl;

  @Value("${database.username}")
  private String databaseUsername;

  @Value("${database.password}")
  private String databasePassword;

  private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
  private static final int ITERATIONS = 10000;
  private static final int KEY_LENGTH = 512;

  @CrossOrigin(origins = "*")
  @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
  LoginResponse login(@RequestBody LoginRequest input) {
    try (var connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword)) {
      User user = User.fetch(input.username, connection);
      if (verifyPassword(input.password, user.hashedPassword)) {
        return new LoginResponse(user.token(secret));
      } else {
        throw new Unauthorized("Access Denied");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean verifyPassword(String password, String storedHash) {
    try {
      byte[] salt = Base64.getDecoder().decode(storedHash.split(":")[0]);
      byte[] hash = Base64.getDecoder().decode(storedHash.split(":")[1]);

      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
      byte[] testHash = skf.generateSecret(spec).getEncoded();

      return MessageDigest.isEqual(hash, testHash);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }

  private String generateHash(String password) {
    try {
      SecureRandom random = new SecureRandom();
      byte[] salt = new byte[16];
      random.nextBytes(salt);

      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
      byte[] hash = skf.generateSecret(spec).getEncoded();

      return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
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
