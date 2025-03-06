import com.scalesec.vulnado.InternalServerError;
import com.scalesec.vulnado.LoginController;
import com.scalesec.vulnado.LoginRequest;
import com.scalesec.vulnado.LoginResponse;
import com.scalesec.vulnado.Unauthorized;
import com.scalesec.vulnado.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class LoginControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement statement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData resultSetMetaData;


    @InjectMocks
    private LoginController loginController;

    @Test
    void testSuccessfulLogin() throws SQLException {
        // Mock database interaction
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("username")).thenReturn("testuser");
        when(resultSet.getString("hashedPassword")).thenReturn("testpassword");
        when(Postgres.secureCompare("testpassword", "testpassword")).thenReturn(true);


        //Simulate ResultSetMetaData for the User object.  Ideally replace with a proper mock.
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnName(1)).thenReturn("username");
        when(resultSetMetaData.getColumnName(2)).thenReturn("hashedPassword");


        LoginRequest input = new LoginRequest();
        input.username = "testuser";
        input.password = "testpassword";

        LoginResponse response = loginController.login(input);
        assertNotNull(response);
        assertNotNull(response.token);
        verify(dataSource).getConnection();
        verify(statement).setString(1, "testuser");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();

    }

    @Test
    void testFailedLoginWrongPassword() throws SQLException {
        // Mock database interaction
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("username")).thenReturn("testuser");
        when(resultSet.getString("hashedPassword")).thenReturn("wrongpassword");

        LoginRequest input = new LoginRequest();
        input.username = "testuser";
        input.password = "testpassword";

        assertThrows(Unauthorized.class, () -> loginController.login(input));
    }

    @Test
    void testFailedLoginUserNotFound() throws SQLException {
        // Mock database interaction
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        LoginRequest input = new LoginRequest();
        input.username = "testuser";
        input.password = "testpassword";

        assertThrows(Unauthorized.class, () -> loginController.login(input));
    }

    @Test
    void testDatabaseError() throws SQLException {
        // Mock database interaction to simulate a SQLException
        when(dataSource.getConnection()).thenThrow(new SQLException("Database error"));

        LoginRequest input = new LoginRequest();
        input.username = "testuser";
        input.password = "testpassword";

        assertThrows(InternalServerError.class, () -> loginController.login(input));
    }

    @Test
    void testInvalidLoginRequestBlankUsername(){
        LoginRequest input = new LoginRequest();
        input.username = "";
        input.password = "testpassword";

        assertThrows(javax.validation.ConstraintViolationException.class, () -> loginController.login(input));
    }

    @Test
    void testInvalidLoginRequestShortUsername(){
        LoginRequest input = new LoginRequest();
        input.username = "te";
        input.password = "testpassword";

        assertThrows(javax.validation.ConstraintViolationException.class, () -> loginController.login(input));
    }


    @Test
    void testInvalidLoginRequestBlankPassword(){
        LoginRequest input = new LoginRequest();
        input.username = "testuser";
        input.password = "";

        assertThrows(javax.validation.ConstraintViolationException.class, () -> loginController.login(input));
    }


    @Test
    void testInvalidLoginRequestShortPassword(){
        LoginRequest input = new LoginRequest();
        input.username = "testuser";
        input.password = "test";

        assertThrows(javax.validation.ConstraintViolationException.class, () -> loginController.login(input));
    }
}
