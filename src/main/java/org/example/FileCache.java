package org.example;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileCache {

    private final Path cacheDir;
    private final Path metaFile;
    private final long ttlMillis;
    private final Gson gson = new Gson();

    public FileCache(String cacheDirName, long ttlMillis) {
        this.cacheDir = Path.of(cacheDirName);
        this.metaFile = this.cacheDir.resolve("cache_meta.json");
        this.ttlMillis = ttlMillis;
    }

    public String getValidCache(String fileName) throws IOException {
        if (!exists(fileName)) {
            return null;
        }

        Long savedAt = getSavedAt(fileName);
        if (savedAt == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        if (now - savedAt > ttlMillis) {
            return null;
        }

        return readFile(cacheDir.resolve(fileName));
    }

    public String getAnyCache(String fileName) throws IOException {
        if (!exists(fileName)) {
            return null;
        }
        return readFile(cacheDir.resolve(fileName));
    }

    public boolean exists(String fileName) {
        return Files.exists(cacheDir.resolve(fileName));
    }

    public void save(String fileName, String json) throws IOException {
        Files.createDirectories(cacheDir);

        writeFile(cacheDir.resolve(fileName), json);

        Map<String, Long> meta = readMeta();
        meta.put(fileName, System.currentTimeMillis());
        writeMeta(meta);
    }

    public Long getSavedAt(String fileName) throws IOException {
        Map<String, Long> meta = readMeta();
        return meta.get(fileName);
    }

    public void clear() throws IOException {
        if (!Files.exists(cacheDir)) {
            return;
        }

        try (var files = Files.list(cacheDir)) {
            files.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void writeFile(Path path, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(content);
        }
    }

    private String readFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private Map<String, Long> readMeta() throws IOException {
        String json = readFile(metaFile);
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }

        Meta meta = gson.fromJson(json, Meta.class);
        if (meta == null || meta.entries == null) {
            return new HashMap<>();
        }
        return meta.entries;
    }

    private void writeMeta(Map<String, Long> entries) throws IOException {
        Meta meta = new Meta();
        meta.entries = entries;
        writeFile(metaFile, gson.toJson(meta));
    }

    private static class Meta {
        Map<String, Long> entries = new HashMap<>();
    }
}