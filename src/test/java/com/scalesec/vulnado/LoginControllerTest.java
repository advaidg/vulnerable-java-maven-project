import com.scalesec.vulnado.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement statement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private LoginController loginController;


    @Test
    void testLoginSuccess() throws SQLException {
        // Mock data
        String username = "testuser";
        String password = "password123";
        String hashedPassword = DigestUtils.md5Hex(password);
        String secret = "mysecret";
        String expectedToken = "token_" + username;

        // Mock database interaction
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE username = ?")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("username")).thenReturn(username);
        when(resultSet.getString("hashedPassword")).thenReturn(hashedPassword);
        when(resultSet.getString("token")).thenReturn("sometoken"); // This value is irrelevant for this test.


        // Test execution
        LoginRequest input = new LoginRequest();
        input.username = username;
        input.password = password;

        LoginResponse response = loginController.login(input);

        // Assertions
        assertEquals(expectedToken, response.token);
        verify(dataSource).getConnection();
        verify(statement).setString(1, username);
        verify(statement).executeQuery();
        verify(resultSet).next();
        verify(connection).close();
        verify(statement).close();
        verify(resultSet).close();


    }

    @Test
    void testLoginFailureWrongPassword() throws SQLException {
        // Mock data
        String username = "testuser";
        String password = "wrongpassword";
        String hashedPassword = DigestUtils.md5Hex("password123"); //Correct password
        String secret = "mysecret";

        // Mock database interaction
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE username = ?")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("username")).thenReturn(username);
        when(resultSet.getString("hashedPassword")).thenReturn(hashedPassword);
        when(resultSet.getString("token")).thenReturn("sometoken");

        // Test execution and assertion
        LoginRequest input = new LoginRequest();
        input.username = username;
        input.password = password;

        assertThrows(Unauthorized.class, () -> loginController.login(input));
    }

    @Test
    void testLoginFailureUserNotFound() throws SQLException {
        // Mock data
        String username = "nonexistentuser";
        String password = "password123";

        // Mock database interaction
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE username = ?")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Test execution and assertion

        LoginRequest input = new LoginRequest();
        input.username = username;
        input.password = password;

        assertThrows(Unauthorized.class, () -> loginController.login(input));
    }

    @Test
    void testLoginNullInput() {
        assertThrows(BadRequest.class, () -> loginController.login(null));
    }

    @Test
    void testLoginEmptyInput() {
        LoginRequest input = new LoginRequest();
        assertThrows(BadRequest.class, () -> loginController.login(input));
    }


    @Test
    void testLoginEmptyUsername() {
        LoginRequest input = new LoginRequest();
        input.password = "password123";
        assertThrows(BadRequest.class, () -> loginController.login(input));
    }

    @Test
    void testLoginEmptyPassword() {
        LoginRequest input = new LoginRequest();
        input.username = "testuser";
        assertThrows(BadRequest.class, () -> loginController.login(input));
    }

    @Test
    void testDatabaseError() throws SQLException {
        // Mock database exception
        when(dataSource.getConnection()).thenThrow(new SQLException("Simulated database error"));

        LoginRequest input = new LoginRequest();
        input.username = "testuser";
        input.password = "password123";

        assertThrows(InternalServerError.class, () -> loginController.login(input));
    }

    @Test
    void testConstantTimeCompare(){
        assertTrue(loginController.constantTimeCompare("hello", "hello"));
        assertFalse(loginController.constantTimeCompare("hello", "world"));
        assertFalse(loginController.constantTimeCompare("hello", "hell"));
        assertFalse(loginController.constantTimeCompare("hell", "hello"));

    }

}
