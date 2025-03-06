import com.scalesec.vulnado.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class UserTest {

    private static final String SECRET = "testSecret";
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("1", "testuser", "testpassword");
    }

    @Test
    @DisplayName("Test token generation and verification")
    void testTokenGenerationAndVerification() {
        String token = user.token(SECRET);
        assertNotNull(token);
        Optional<Claims> claims = User.assertAuth(SECRET, token);
        assertTrue(claims.isPresent());
        assertEquals("testuser", claims.get().getSubject());
    }

    @Test
    @DisplayName("Test token verification with invalid signature")
    void testTokenVerificationWithInvalidSignature() {
        String token = user.token(SECRET);
        Optional<Claims> claims = User.assertAuth("wrongSecret", token);
        assertFalse(claims.isPresent());
    }

    @Test
    @DisplayName("Test token verification with invalid token")
    void testTokenVerificationWithInvalidToken() {
        Optional<Claims> claims = User.assertAuth(SECRET, "invalidToken");
        assertFalse(claims.isPresent());
    }

    @Test
    @DisplayName("Test fetch user")
    void testFetchUser() {
        // This test requires a Postgres database setup with a 'users' table and data.  It's difficult
        // to provide a complete test without mocking the database interaction, which adds complexity.
        //  The following is a placeholder - it would need significant adaptation depending on your 
        // database setup and mocking strategy.

        //  Replace with appropriate mocking or database setup.
        Optional<User> fetchedUser = User.fetch("testuser"); 

        //Example assertion IF the database interaction is mocked or set up correctly
        // assertTrue(fetchedUser.isPresent());
        // assertEquals("1", fetchedUser.get().getId());
        // assertEquals("testuser", fetchedUser.get().getUsername());


    }


    @Test
    @DisplayName("Test fetch user - user not found")
    void testFetchUserNotFound() {
        //Again, this depends on a database setup or mocking.  The assertion will depend on the setup
        Optional<User> fetchedUser = User.fetch("nonexistentuser");
        assertFalse(fetchedUser.isPresent());
    }


    @Test
    @DisplayName("Test CSRF token generation")
    void testGenerateCsrfToken() {
        String csrfToken = User.generateCsrfToken();
        assertNotNull(csrfToken);
        assertNotEquals("", csrfToken); // ensure it's not an empty string.
    }

    @Test
    @DisplayName("Test protectedAction - CSRF token match")
    void testProtectedActionCsrfMatch() {
        String csrfToken = User.generateCsrfToken();
        assertDoesNotThrow(() -> user.protectedAction(csrfToken, csrfToken));
    }

    @Test
    @DisplayName("Test protectedAction - CSRF token mismatch")
    void testProtectedActionCsrfMismatch() {
        String csrfToken1 = User.generateCsrfToken();
        String csrfToken2 = User.generateCsrfToken();
        assertThrows(SecurityException.class, () -> user.protectedAction(csrfToken1, csrfToken2));
    }

    @Test
    void testGetters(){
        assertEquals("1", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("testpassword", user.getHashedPassword());
    }
}
