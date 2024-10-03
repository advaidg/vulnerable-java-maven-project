import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.List;
import com.scalesec.vulnado.Comment;
import com.scalesec.vulnado.CommentRequest;
import com.scalesec.vulnado.CommentsController;
import com.scalesec.vulnado.BadRequest;
import com.scalesec.vulnado.ServerError;

@WebMvcTest(CommentsController.class)
public class CommentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Comment commentMock;

    @Test
    public void testComments_ValidToken() throws Exception {
        String token = "secret";
        List<Comment> comments = Arrays.asList(new Comment("user1", "comment1"), new Comment("user2", "comment2"));
        when(commentMock.fetch_all()).thenReturn(comments);
        mockMvc.perform(MockMvcRequestBuilders.get("/comments")
                .header("x-auth-token", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[{\"username\":\"user1\",\"body\":\"comment1\"},{\"username\":\"user2\",\"body\":\"comment2\"}]"));
        verify(commentMock, times(1)).fetch_all();
    }

    @Test
    public void testComments_InvalidToken() throws Exception {
        String token = "invalid_token";
        mockMvc.perform(MockMvcRequestBuilders.get("/comments")
                .header("x-auth-token", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateComment_ValidToken() throws Exception {
        String token = "secret";
        CommentRequest request = new CommentRequest();
        request.username = "user3";
        request.body = "comment3";
        Comment newComment = new Comment("user3", "comment3");
        when(commentMock.create(request.username, request.body)).thenReturn(newComment);
        mockMvc.perform(MockMvcRequestBuilders.post("/comments")
                .header("x-auth-token", token)
                .content(TestUtil.convertObjectToJsonBytes(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\"username\":\"user3\",\"body\":\"comment3\"}"));
        verify(commentMock, times(1)).create(request.username, request.body);
    }

    @Test
    public void testCreateComment_InvalidToken() throws Exception {
        String token = "invalid_token";
        CommentRequest request = new CommentRequest();
        request.username = "user3";
        request.body = "comment3";
        mockMvc.perform(MockMvcRequestBuilders.post("/comments")
                .header("x-auth-token", token)
                .content(TestUtil.convertObjectToJsonBytes(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteComment_ValidToken() throws Exception {
        String token = "secret";
        String id = "1";
        when(commentMock.delete(id)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete("/comments/" + id)
                .header("x-auth-token", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(commentMock, times(1)).delete(id);
    }

    @Test
    public void testDeleteComment_InvalidToken() throws Exception {
        String token = "invalid_token";
        String id = "1";
        mockMvc.perform(MockMvcRequestBuilders.delete("/comments/" + id)
                .header("x-auth-token", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteComment_ServerError() throws Exception {
        String token = "secret";
        String id = "1";
        when(commentMock.delete(id)).thenThrow(new ServerError("Server error"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/comments/" + id)
                .header("x-auth-token", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}

class TestUtil {
    public static byte[] convertObjectToJsonBytes(Object object) {
        try {
            return com.fasterxml.jackson.databind.ObjectMapper.class.getDeclaredMethod("writeValueAsBytes", Object.class).invoke(new com.fasterxml.jackson.databind.ObjectMapper(), object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
