import org.example.FileCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileCacheTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndReadBackWorksCorrectly() throws IOException {
        FileCache cache = new FileCache(tempDir.toString(), 5 * 60 * 1000);

        cache.save("test.json", "{\"name\":\"FalconSat\"}");
        String result = cache.getValidCache("test.json");

        assertNotNull(result);
        assertTrue(result.contains("FalconSat"));
    }

    @Test
    void getValidCacheReturnsNullForMissingFile() throws IOException {
        FileCache cache = new FileCache(tempDir.toString(), 5 * 60 * 1000);

        String result = cache.getValidCache("missing.json");

        assertNull(result);
    }

    @Test
    void saveOverwritesExistingFile() throws IOException {
        FileCache cache = new FileCache(tempDir.toString(), 5 * 60 * 1000);

        cache.save("test.json", "old");
        cache.save("test.json", "new");

        String result = cache.getValidCache("test.json");
        assertEquals("new", result);
    }

    @Test
    void cacheBecomesInvalidAfterTtl() throws Exception {
        FileCache cache = new FileCache(tempDir.toString(), 1);

        cache.save("test.json", "{\"a\":1}");
        Thread.sleep(10);

        String result = cache.getValidCache("test.json");
        assertNull(result);
    }

    @Test
    void clearRemovesAllFiles() throws IOException {
        FileCache cache = new FileCache(tempDir.toString(), 5 * 60 * 1000);

        cache.save("a.json", "1");
        cache.save("b.json", "2");
        cache.clear();

        assertFalse(cache.exists("a.json"));
        assertFalse(cache.exists("b.json"));
    }
}