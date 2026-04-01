package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SpaceXHttpClient {

    public String get(String url) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(url, "GET");
            int statusCode = connection.getResponseCode();
            String body = readResponse(connection, statusCode);
            return processResponse(statusCode, body);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String post(String url, String jsonBody) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(url, "POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = connection.getResponseCode();
            String body = readResponse(connection, statusCode);
            return processResponse(statusCode, body);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection openConnection(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    protected String readResponse(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream inputStream;
        if (statusCode == 200) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }
        return readStream(inputStream);
    }

    protected String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    public String processResponse(int statusCode, String body) throws HttpRequestException {
        if (statusCode == 200) {
            return body;
        }
        throw new HttpRequestException(statusCode, body);
    }

    public String buildUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }
}