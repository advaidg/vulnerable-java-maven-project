package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class User {
    private static final String SQL_FETCH_USER = "SELECT user_id, username, password FROM users WHERE username = ? LIMIT 1";
    private static final SecretKey JWT_SECRET_KEY = Keys.hmacShaKeyFor("YOUR_SECRET_KEY".getBytes());

    private String id;
    private String username;
    private String hashedPassword;

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

    public String token() {
        return Jwts.builder()
                .setSubject(this.username)
                .signWith(JWT_SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static void assertAuth(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(JWT_SECRET_KEY)
                    .build();
            parser.parseClaimsJws(token);
        } catch (Exception e) {
            throw new Unauthorized(e.getMessage());
        }
    }

    public static User fetch(String username) {
        try (Connection cxn = Postgres.connection();
             Statement stmt = cxn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(SQL_FETCH_USER, username)) {
                if (rs.next()) {
                    return new User(
                            rs.getString("user_id"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            // Log the exception
            // For example, using SLF4j logger
            // logger.error("Error fetching user: {}", e.getMessage(), e);
            // Throw a more specific exception if necessary
            throw new RuntimeException("Error fetching user", e);
        }
        return null;
    }
}
