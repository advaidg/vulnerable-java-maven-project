package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class User {

    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private final String id;
    private final String username;
    private final String hashedPassword;

    public User(String id, String username, String hashedPassword) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }


    public String token(String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setSubject(this.username)
                .signWith(key)
                .compact();
    }

    public static Optional<Claims> assertAuth(String secret, String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            return Optional.of(Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody());
        } catch (SignatureException e) {
            logger.error("Invalid token signature: {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error during token verification: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static Optional<User> fetch(String un) {
        try (Connection cxn = Postgres.connection();
             PreparedStatement stmt = cxn.prepareStatement("SELECT * FROM users WHERE username = ? LIMIT 1")) {

            stmt.setString(1, un);
            logger.info("Executing query: {}", stmt); //Log the prepared statement, not the vulnerable query.
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    return Optional.of(new User(userId, username, password));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Database error during user fetch: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }


    public static String generateCsrfToken() {
        return UUID.randomUUID().toString();
    }

    //Example usage of CSRF token
    public void protectedAction(String csrfTokenFromSession, String csrfTokenFromForm){
        if(!csrfTokenFromSession.equals(csrfTokenFromForm)){
            throw new SecurityException("CSRF token mismatch");
        }
        // Perform action
    }

}
