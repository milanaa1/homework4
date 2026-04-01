package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private final Gson gson = new Gson();

    public Launch parseLaunch(String json) {
        try {
            return gson.fromJson(json, Launch.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Некорректный JSON объекта Launch", e);
        }
    }

    public List<Launch> parseLaunchList(String json) {
        try {
            Type listType = new TypeToken<List<Launch>>() {}.getType();
            return gson.fromJson(json, listType);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Некорректный JSON массива Launch", e);
        }
    }

    public List<Launch> parseQueryResponse(String json) {
        try {
            QueryResponse response = gson.fromJson(json, QueryResponse.class);
            return response.docs;
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Некорректный JSON ответа query", e);
        }
    }

    public String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public String buildSuccessQuery(boolean success) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> options = new HashMap<>();

        query.put("success", success);
        options.put("sort", Map.of("date_utc", "asc"));

        request.put("query", query);
        request.put("options", options);

        return gson.toJson(request);
    }

    public String buildDateRangeQuery(String startDate, String endDate) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> date = new HashMap<>();
        Map<String, Object> options = new HashMap<>();

        date.put("$gte", startDate + "T00:00:00.000Z");
        date.put("$lte", endDate + "T23:59:59.999Z");

        query.put("date_utc", date);
        options.put("sort", Map.of("date_utc", "asc"));

        request.put("query", query);
        request.put("options", options);

        return gson.toJson(request);
    }

    private static class QueryResponse {
        List<Launch> docs;
    }
}