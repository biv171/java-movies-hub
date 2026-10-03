package ru.practicum.moviehub.http;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesFilterTest {
    private static final String BASE = "http://localhost:8080";
    private static final int port = 8080;
    private static MoviesServer server;
    private static HttpClient client;
    private static final MoviesStore moviesStore = new MoviesStore();

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(moviesStore, port);
        server.start();
        client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    }

    @BeforeEach
    void beforeEach() {
        moviesStore.clear();
        moviesStore.add(new Movie("Mortal Combat", 2026, 1000001));
        moviesStore.add(new Movie("Dune2", 2013, 1000002));
        moviesStore.add(new Movie("Vladimir", 2013, 1000003));
    }

    @AfterAll
    static void afterAll() {
    server.stop();
}

    @Test
    void filterMovieByYearAndStatusMustBe200() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE + "/movies?year=2013"))
            .header("Content-Type", "application/json")
            .GET()
            .build();

        HttpResponse<String> resp =
            client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
    }

    @Test
    void filterNotExistsMovieByYearAndStatusMustBe200AndReturnEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2025"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void filterMovieWithIncorrectYearAndStatusMustBe400AndReturnError() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2ddd025"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }
}