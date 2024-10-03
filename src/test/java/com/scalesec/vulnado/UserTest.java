package com.scalesec.vulnado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserTest {

  private static final String SECRET = "secret";
  private static final String USERNAME = "testuser";
  private static final String PASSWORD = "password";
  private static final String ID = "1";

  private User user;

  @BeforeEach
  void setUp() {
    user = new User(ID, USERNAME, PASSWORD);
  }

  @Test
  void testToken() {
    String token = user.token(SECRET);
    // Verify token is not empty
    assertFalse(token.isEmpty());
    // Verify token can be parsed
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(SECRET.getBytes())
        .build()
        .parseClaimsJws(token)
        .getBody();
    assertEquals(claims.getSubject(), USERNAME);
  }

  @Test
  void testAssertAuthSuccess() {
    String token = user.token(SECRET);
    User.assertAuth(SECRET, token);
  }

  @Test
  void testAssertAuthFailure() {
    String token = user.token("wrongSecret");
    assertThrows(Unauthorized.class, () -> User.assertAuth(SECRET, token));
  }

  @Test
  void testFetchSuccess() throws SQLException {
    // Mock database connection and result set
    Connection mockConnection = Mockito.mock(Connection.class);
    PreparedStatement mockStatement = Mockito.mock(PreparedStatement.class);
    ResultSet mockResultSet = Mockito.mock(ResultSet.class);

    // Configure mock objects to return expected values
    Mockito.when(mockConnection.prepareStatement("select * from users where username = ? limit 1"))
        .thenReturn(mockStatement);
    Mockito.when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    Mockito.when(mockResultSet.next()).thenReturn(true);
    Mockito.when(mockResultSet.getString("user_id")).thenReturn(ID);
    Mockito.when(mockResultSet.getString("username")).thenReturn(USERNAME);
    Mockito.when(mockResultSet.getString("password")).thenReturn(PASSWORD);

    // Call the fetch method and verify the result
    Optional<User> fetchedUser = User.fetch(USERNAME);
    assertTrue(fetchedUser.isPresent());
    assertEquals(fetchedUser.get().id, ID);
    assertEquals(fetchedUser.get().username, USERNAME);
    assertEquals(fetchedUser.get().hashedPassword, PASSWORD);
  }

  @Test
  void testFetchFailure() throws SQLException {
    // Mock database connection and result set
    Connection mockConnection = Mockito.mock(Connection.class);
    PreparedStatement mockStatement = Mockito.mock(PreparedStatement.class);
    ResultSet mockResultSet = Mockito.mock(ResultSet.class);

    // Configure mock objects to return expected values
    Mockito.when(mockConnection.prepareStatement("select * from users where username = ? limit 1"))
        .thenReturn(mockStatement);
    Mockito.when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    Mockito.when(mockResultSet.next()).thenReturn(false);

    // Call the fetch method and verify the result
    Optional<User> fetchedUser = User.fetch("nonexistentuser");
    assertFalse(fetchedUser.isPresent());
  }
}
