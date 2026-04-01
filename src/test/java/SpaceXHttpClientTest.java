import org.example.HttpRequestException;
import org.example.SpaceXHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpaceXHttpClientTest {

    private final SpaceXHttpClient client = new SpaceXHttpClient();

    @Test
    void buildUrlCombinesBaseAndPath() {
        String url = client.buildUrl("https://api.spacexdata.com/", "/v5/launches");
        assertEquals("https://api.spacexdata.com/v5/launches", url);
    }

    @Test
    void buildUrlAddsSlashWhenNeeded() {
        String url = client.buildUrl("https://api.spacexdata.com", "v5/launches");
        assertEquals("https://api.spacexdata.com/v5/launches", url);
    }

    @Test
    void processResponseReturnsBodyFor200() throws Exception {
        String body = client.processResponse(200, "{\"ok\":true}");
        assertEquals("{\"ok\":true}", body);
    }

    @Test
    void processResponseThrowsFor404() {
        assertThrows(HttpRequestException.class,
                () -> client.processResponse(404, "{\"error\":\"not found\"}"));
    }

    @Test
    void processResponseThrowsFor500() {
        assertThrows(HttpRequestException.class,
                () -> client.processResponse(500, "{\"error\":\"server error\"}"));
    }
}