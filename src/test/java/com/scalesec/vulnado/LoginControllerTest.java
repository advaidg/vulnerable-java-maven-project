import com.scalesec.vulnado.LoginController;
import com.scalesec.vulnado.LoginRequest;
import com.scalesec.vulnado.LoginResponse;
import com.scalesec.vulnado.Unauthorized;
import com.scalesec.vulnado.User;
import com.scalesec.vulnado.UserRepository;
import com.scalesec.vulnado.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    private LoginController loginController;
    private UserService userService;
    private UserRepository userRepository;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        userRepository = Mockito.mock(UserRepository.class);
        loginController = new LoginController();
        loginController.userService = userService;
        session = new MockHttpSession();
    }

    @Test
    void testLoginSuccess() {
        // Mock user and return a valid user
        User user = new User("testuser", "hashedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(user.verifyPassword("testpassword")).thenReturn(true);
        when(user.token("secret")).thenReturn("testtoken");

        // Perform the login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = "testuser";
        loginRequest.password = "testpassword";
        LoginResponse loginResponse = loginController.login(loginRequest, session);

        // Verify the response
        assertNotNull(loginResponse);
        assertEquals("testtoken", loginResponse.token);
        assertNotNull(loginResponse.csrfToken);
        assertNotEquals("", loginResponse.csrfToken);
        // Verify CSRF token is stored in session
        assertEquals(loginResponse.csrfToken, session.getAttribute("csrfToken"));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(user, times(1)).verifyPassword("testpassword");
        verify(user, times(1)).token("secret");
    }

    @Test
    void testLoginInvalidUsername() {
        // Mock user and return null (invalid username)
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        // Perform the login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = "invaliduser";
        loginRequest.password = "testpassword";

        // Verify the exception is thrown
        assertThrows(Unauthorized.class, () -> loginController.login(loginRequest, session));
        verify(userRepository, times(1)).findByUsername("invaliduser");
        verifyNoInteractions(userService);
    }

    @Test
    void testLoginInvalidPassword() {
        // Mock user and return a valid user but with incorrect password
        User user = new User("testuser", "hashedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(user.verifyPassword("wrongpassword")).thenReturn(false);

        // Perform the login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = "testuser";
        loginRequest.password = "wrongpassword";

        // Verify the exception is thrown
        assertThrows(Unauthorized.class, () -> loginController.login(loginRequest, session));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(user, times(1)).verifyPassword("wrongpassword");
        verifyNoInteractions(userService);
    }
}
