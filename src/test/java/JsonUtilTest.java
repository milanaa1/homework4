import org.example.JsonUtil;
import org.example.Launch;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilTest {

    private final JsonUtil jsonUtil = new JsonUtil();

    @Test
    void parseLaunchDeserializesSingleObject() throws IOException {
        String json = readResource("json/launch_single.json");
        Launch launch = jsonUtil.parseLaunch(json);

        assertEquals("FalconSat", launch.getName());
        assertEquals(1, launch.getFlightNumber());
        assertEquals("2006-03-24T22:30:00.000Z", launch.getDateUtc());
    }

    @Test
    void parseLaunchListDeserializesArray() throws IOException {
        String json = readResource("json/launches_array.json");
        List<Launch> launches = jsonUtil.parseLaunchList(json);

        assertEquals(2, launches.size());
        assertEquals("FalconSat", launches.get(0).getName());
        assertEquals("DemoSat", launches.get(1).getName());
    }

    @Test
    void parseReturnsEmptyListForEmptyArray() throws IOException {
        String json = readResource("json/launches_array_empty.json");
        List<Launch> launches = jsonUtil.parseLaunchList(json);

        assertNotNull(launches);
        assertTrue(launches.isEmpty());
    }

    @Test
    void parseLaunchHandlesNullValues() throws IOException {
        String json = readResource("json/launch_single_with_nulls.json");
        Launch launch = jsonUtil.parseLaunch(json);

        assertNull(launch.getSuccess());
        assertNull(launch.getDetails());
        assertNull(launch.getFailures().get(0).getAltitude());
    }

    @Test
    void parseLaunchDeserializesNestedObjects() throws IOException {
        String json = readResource("json/launch_single.json");
        Launch launch = jsonUtil.parseLaunch(json);

        assertNotNull(launch.getFailures());
        assertNotNull(launch.getCores());
        assertEquals(1, launch.getFailures().size());
        assertEquals(1, launch.getCores().size());
    }

    @Test
    void parseLaunchMapsSerializedNameCorrectly() throws IOException {
        String json = readResource("json/launch_single.json");
        Launch launch = jsonUtil.parseLaunch(json);

        assertEquals(1, launch.getFlightNumber());
        assertEquals("2006-03-24T22:30:00.000Z", launch.getDateUtc());
    }

    @Test
    void parseLaunchThrowsExceptionForInvalidJson() throws IOException {
        String json = readResource("json/invalid_launch.json");

        assertThrows(IllegalArgumentException.class, () -> jsonUtil.parseLaunch(json));
    }

    @Test
    void toJsonSerializesOneLaunch() throws IOException {
        String json = readResource("json/launch_single.json");
        Launch launch = jsonUtil.parseLaunch(json);

        String result = jsonUtil.toJson(launch);

        assertTrue(result.contains("\"name\":\"FalconSat\""));
    }

    @Test
    void toJsonSerializesLaunchList() throws IOException {
        String json = readResource("json/launches_array.json");
        List<Launch> launches = jsonUtil.parseLaunchList(json);

        String result = jsonUtil.toJson(launches);

        assertTrue(result.startsWith("["));
        assertTrue(result.contains("FalconSat"));
        assertTrue(result.contains("DemoSat"));
    }

    @Test
    void buildSuccessQueryReturnsCorrectJson() {
        String json = jsonUtil.buildSuccessQuery(true);

        assertTrue(json.contains("\"success\":true"));
    }

    @Test
    void buildDateRangeQueryReturnsCorrectJson() {
        String json = jsonUtil.buildDateRangeQuery("2020-01-01", "2020-12-31");

        assertTrue(json.contains("2020-01-01T00:00:00.000Z"));
        assertTrue(json.contains("2020-12-31T23:59:59.999Z"));
    }

    @Test
    void parseQueryResponseReturnsDocs() throws IOException {
        String json = readResource("json/query_response.json");
        List<Launch> launches = jsonUtil.parseQueryResponse(json);

        assertEquals(2, launches.size());
    }

    private String readResource(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Ресурс не найден: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
