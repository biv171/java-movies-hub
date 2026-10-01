package ru.practicum.moviehub.http;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;


public class MoviesDeleteTest {
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
        moviesStore.add(new Movie("Mortal Combat", 2026));
        moviesStore.add(new Movie("Dune2", 2013));
        moviesStore.add(new Movie("Vladimir", 2013));
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void deleteMovieByIdAndStatusMustBe204() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1000002"))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(204, resp.statusCode());
    }

    @Test
    void deleteNotExistMovieByIdAndStatusMustBe404WithErrorInBody() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/777"))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404, resp.statusCode());
    }

    @Test
    void deleteMoviesWithIncorrectIdAndStatusMustBe404WithErrorInBody() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/10g00g00g5"))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404, resp.statusCode());
    }
}