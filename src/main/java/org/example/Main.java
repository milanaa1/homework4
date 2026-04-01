package org.example;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String BASE_URL = "https://api.spacexdata.com";
    private static final String ALL_URL = BASE_URL + "/v5/launches";
    private static final String LATEST_URL = BASE_URL + "/v5/launches/latest";
    private static final String QUERY_URL = BASE_URL + "/v5/launches/query";

    private static final long TTL = 5 * 60 * 1000;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final SpaceXHttpClient httpClient = new SpaceXHttpClient();
    private static final JsonUtil jsonUtil = new JsonUtil();
    private static final FileCache cache = new FileCache("cache", TTL);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showAllLaunches();
                    break;
                case "2":
                    showLatestLaunch();
                    break;
                case "3":
                    searchByDate(scanner);
                    break;
                case "4":
                    showBySuccess(true);
                    break;
                case "5":
                    showBySuccess(false);
                    break;
                case "6":
                    clearCache();
                    break;
                case "7":
                    System.out.println("Выход.");
                    return;
                default:
                    System.out.println("Неизвестный пункт меню.");
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("=== SpaceX Launch Explorer ===");
        System.out.println("1. Показать все запуски");
        System.out.println("2. Показать последний запуск");
        System.out.println("3. Поиск запусков по дате");
        System.out.println("4. Показать только успешные запуски");
        System.out.println("5. Показать только неудачные запуски");
        System.out.println("6. Очистить кеш");
        System.out.println("7. Выход");
        System.out.print("Выберите пункт: ");
    }

    private static void showAllLaunches() {
        String cacheFile = "launches_all.json";
        try {
            String cached = cache.getValidCache(cacheFile);
            String json;
            if (cached != null) {
                json = cached;
            } else {
                json = httpClient.get(ALL_URL);
                cache.save(cacheFile, json);
            }
            List<Launch> launches = jsonUtil.parseLaunchList(json);
            printLaunches(launches);
        } catch (Exception e) {
            fallbackAll(cacheFile, e);
        }
    }

    private static void showLatestLaunch() {
        String cacheFile = "launches_latest.json";
        try {
            String cached = cache.getValidCache(cacheFile);
            String json;
            if (cached != null) {
                json = cached;
            } else {
                json = httpClient.get(LATEST_URL);
                cache.save(cacheFile, json);
            }
            Launch launch = jsonUtil.parseLaunch(json);
            printLaunchDetails(launch);
        } catch (Exception e) {
            fallbackLatest(cacheFile, e);
        }
    }
    private static void searchByDate(Scanner scanner) {
        System.out.print("Введите дату начала (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();
        System.out.print("Введите дату конца (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();
        String cacheFile = "query_" + startDate + "_" + endDate + ".json";
        try {
            String cached = cache.getValidCache(cacheFile);
            String json;
            if (cached != null) {
                json = cached;
            } else {
                String body = jsonUtil.buildDateRangeQuery(startDate, endDate);
                json = httpClient.post(QUERY_URL, body);
                cache.save(cacheFile, json);
            }
            List<Launch> launches = jsonUtil.parseQueryResponse(json);
            printLaunches(launches);

        } catch (Exception e) {
            fallbackQuery(cacheFile, e);
        }
    }

    private static void showBySuccess(boolean success) {
        String cacheFile = "query_success_" + success + ".json";

        try {
            String cached = cache.getValidCache(cacheFile);
            String json;

            if (cached != null) {
                json = cached;
            } else {
                String body = jsonUtil.buildSuccessQuery(success);
                json = httpClient.post(QUERY_URL, body);
                cache.save(cacheFile, json);
            }

            List<Launch> launches = jsonUtil.parseQueryResponse(json);
            printLaunches(launches);

        } catch (Exception e) {
            fallbackQuery(cacheFile, e);
        }
    }

    private static void clearCache() {
        try {
            cache.clear();
            System.out.println("Кеш очищен.");
        } catch (IOException e) {
            System.out.println("Не удалось очистить кеш: " + e.getMessage());
        }
    }
    private static void fallbackAll(String cacheFile, Exception originalException) {
        try {
            String cached = cache.getValidCache(cacheFile);
            if (cached != null) {
                printWarning(cacheFile);
                List<Launch> launches = jsonUtil.parseLaunchList(cached);
                printLaunches(launches);
                return;
            }
        } catch (Exception ignored) {
        }

        System.out.println("Ошибка: " + originalException.getMessage());
    }

    private static void fallbackLatest(String cacheFile, Exception originalException) {
        try {
            String cached = cache.getValidCache(cacheFile);
            if (cached != null) {
                printWarning(cacheFile);
                Launch launch = jsonUtil.parseLaunch(cached);
                printLaunchDetails(launch);
                return;
            }
        } catch (Exception ignored) {
        }

        System.out.println("Ошибка: " + originalException.getMessage());
    }

    private static void fallbackQuery(String cacheFile, Exception originalException) {
        try {
            String cached = cache.getValidCache(cacheFile);
            if (cached != null) {
                printWarning(cacheFile);
                List<Launch> launches = jsonUtil.parseQueryResponse(cached);
                printLaunches(launches);
                return;
            }
        } catch (Exception ignored) {
        }

        System.out.println("Ошибка: " + originalException.getMessage());
    }

    private static void printWarning(String cacheFile) throws IOException {
        Long savedAt = cache.getSavedAt(cacheFile);
        String time = "неизвестно";

        if (savedAt != null) {
            time = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(savedAt),
                    ZoneId.systemDefault()
            ).format(FORMATTER);
        }

        System.out.println("[!] Сервер недоступен. Показаны данные из кеша (сохранены " + time + ")");
    }

    private static void printLaunches(List<Launch> launches) {
        if (launches == null || launches.isEmpty()) {
            System.out.println("Ничего не найдено.");
            return;
        }

        for (Launch launch : launches) {
            System.out.printf("#%d  %s | %s | Успех: %s%n",
                    launch.getFlightNumber(),
                    safe(launch.getName()),
                    shortDate(launch.getDateUtc()),
                    successText(launch.getSuccess()));
        }
    }

    private static void printLaunchDetails(Launch launch) {
        if (launch == null) {
            System.out.println("Нет данных.");
            return;
        }

        System.out.println("Запуск: " + safe(launch.getName()));
        System.out.println("Номер: " + launch.getFlightNumber());
        System.out.println("Дата: " + safe(launch.getDateUtc()));
        System.out.println("Успех: " + successText(launch.getSuccess()));
        System.out.println("Описание: " + safe(launch.getDetails()));
    }

    private static String shortDate(String dateUtc) {
        if (dateUtc == null || dateUtc.length() < 10) {
            return "неизвестно";
        }
        return dateUtc.substring(0, 10);
    }

    private static String successText(Boolean success) {
        if (success == null) {
            return "неизвестно";
        }
        return success ? "да" : "нет";
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "нет данных";
        }
        return value;
    }
}
