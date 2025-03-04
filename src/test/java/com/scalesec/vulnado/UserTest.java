import com.scalesec.vulnado.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;


    @Test
    void testConstructor() {
        User user = new User("1", "testuser", "hashedpassword");
        assertEquals("1", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("hashedpassword", user.getHashedPassword());
    }

    @Test
    void testTokenGeneration() {
        User user = new User("1", "testuser", "hashedpassword");
        String secret = "testsecret";
        String token = user.token(secret);
        assertNotNull(token);
        //  More robust token validation would involve verifying the claims within the token.  This is simplified for brevity.

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            assertEquals("testuser", claims.getSubject());
        } catch (Exception e){
            fail("Token validation failed: " + e.getMessage());
        }
    }

    @Test
    void testAuthSuccess() {
        String secret = "testsecret";
        String token =  Jwts.builder().setSubject("testuser").signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, secret.getBytes()).compact();
        assertDoesNotThrow(() -> User.assertAuth(secret, token));
    }

    @Test
    void testAuthFailure() {
        String secret = "testsecret";
        String invalidToken = "invalidtoken";
        assertThrows(User.Unauthorized.class, () -> User.assertAuth(secret, invalidToken));
    }


    @Test
    void testFetchUserSuccess() throws SQLException {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("user_id")).thenReturn("1");
        when(mockResultSet.getString("username")).thenReturn("testuser");
        when(mockResultSet.getString("password")).thenReturn("hashedpassword");

        Optional<User> fetchedUser = User.fetch("testuser");
        assertTrue(fetchedUser.isPresent());
        assertEquals("1", fetchedUser.get().getId());
        assertEquals("testuser", fetchedUser.get().getUsername());
        assertEquals("hashedpassword", fetchedUser.get().getHashedPassword());

        verify(mockConnection, times(1)).prepareStatement(anyString());
        verify(mockStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
        verify(mockResultSet, times(1)).getString("user_id");
        verify(mockResultSet, times(1)).getString("username");
        verify(mockResultSet, times(1)).getString("password");


    }

    @Test
    void testFetchUserFailure() throws SQLException {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<User> fetchedUser = User.fetch("nonexistentuser");
        assertFalse(fetchedUser.isPresent());
    }

    @Test
    void testFetchUserSQLException() throws SQLException {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        Optional<User> fetchedUser = User.fetch("testuser");
        assertFalse(fetchedUser.isPresent());
    }

    //Dummy Postgres class for mocking purposes.  Replace with actual implementation in your project.
    static class Postgres {
        static Connection connection() throws SQLException {
            throw new SQLException("Not Implemented");
        }
    }
}
