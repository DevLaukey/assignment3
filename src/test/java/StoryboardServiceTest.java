import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import static org.junit.Assert.*;

public class StoryboardServiceTest {

    @Test
    public void testViewStoryboard() {
        JSONObject request = new JSONObject();
        request.put("type", "storyboard");
        request.put("view", true);

        JSONObject response = SockServer.storyboard(request);

        assertTrue(response.getBoolean("ok"));
        assertEquals("storyboard", response.getString("type"));
        JSONArray storyboard = response.getJSONArray("storyboard");
        JSONArray users = response.getJSONArray("users");

        assertEquals(0, storyboard.length());
        assertEquals(0, users.length());
    }

    @Test
    public void testAddToStoryboard() {
        JSONObject request = new JSONObject();
        request.put("type", "storyboard");
        request.put("view", false);
        request.put("name", "TestUser");
        request.put("story", "This is a test story.");

        JSONObject response = SockServer.storyboard(request);

        assertTrue(response.getBoolean("ok"));
        assertEquals("storyboard", response.getString("type"));
        JSONArray storyboard = response.getJSONArray("storyboard");
        JSONArray users = response.getJSONArray("users");

        assertEquals(1, storyboard.length());
        assertEquals(1, users.length());
        assertEquals("This is a test story.", storyboard.getString(0));
        assertEquals("TestUser", users.getString(0));
    }
}

