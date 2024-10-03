package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class User {
  public String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    String jws = Jwts.builder().setSubject(this.username).signWith(key).compact();
    return jws;
  }

  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();
      parser.parseClaimsJws(token);
    } catch(Exception e) {
      throw new Unauthorized(e.getMessage());
    }
  }

  public static Optional<User> fetch(String un) {
    try (Connection cxn = Postgres.connection();
         PreparedStatement stmt = cxn.prepareStatement("select * from users where username = ? limit 1")) {
      stmt.setString(1, un);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          String user_id = rs.getString("user_id");
          String username = rs.getString("username");
          String password = rs.getString("password");
          return Optional.of(new User(user_id, username, password));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return Optional.empty();
  }
}
