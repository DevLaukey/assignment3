import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class CharCountTest {
    public class CharCountServiceTest {

        @Test
        public void testGeneralCharCount() {
            JSONObject request = new JSONObject();
            request.put("type", "charcount");
            request.put("findchar", false);
            request.put("count", "Hello, World!");

            JSONObject response = SockServer.charCount(request);

            assertTrue(response.getBoolean("ok"));
            assertEquals("charcount", response.getString("type"));
            assertEquals(13, response.getInt("result"));
        }

        @Test
        public void testSpecificCharCount() {
            JSONObject request = new JSONObject();
            request.put("type", "charcount");
            request.put("findchar", true);
            request.put("find", "o");
            request.put("count", "Hello, World!");

            JSONObject response = SockServer.charCount(request);

            assertTrue(response.getBoolean("ok"));
            assertEquals("charcount", response.getString("type"));
            assertEquals(2, response.getInt("result"));
        }
}
}
