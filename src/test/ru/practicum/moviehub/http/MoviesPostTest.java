package ru.practicum.moviehub.http;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public class MoviesPostTest {
    private static final String BASE = "http://localhost:8080";
    private static final int port = 8080;
    private static MoviesServer server;
    private static HttpClient client;
    private static final MoviesStore moviesStore = new MoviesStore();
    private static final Gson gson = new Gson();

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
        moviesStore.add(new Movie("Dune2", 2027));
        moviesStore.add(new Movie("Vladimir", 2013));
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void postNewMovieStatusCodeMustBe201() throws Exception {
        String jsonBody = "{\"title\":\"Terminator\",\"year\":1990}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Movie movie = gson.fromJson(resp.body(), Movie.class);

        assertEquals(201, resp.statusCode());
        assertEquals(1000013, movie.getId());
    }

    @Test
    void postEmptyTitleStatusCodeMustBe422() throws Exception {
        String jsonBody = "{\"title\":\"\",\"year\":1990}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(422, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(error.getDetails().contains("название не должно быть пустым"));
    }

    @Test
    void postTitleWithIncorrectYearStatusCodeMustBe422() throws Exception {
        String jsonBody = "{\"title\":\"Terminator\",\"year\":990}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(422, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(error.getDetails().contains("год должен быть между 1888 и 2026"));
    }

    @Test
    void postTitleWithLongTitleAndStatusCodeMustBe422() throws Exception {
        String longTitle = "abc".repeat(101);
        String jsonBody = "{\"title\":\"" + longTitle + "\",\"year\":1990}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(422, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(error.getDetails().contains("Длина заголовка свыше 100 символов"));
    }

    @Test
    void postWrongContentTypeMustBeStatusCode415() throws Exception {
        String jsonBody = "{\"title\":\"Terminator\",\"year\":1990}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(415, resp.statusCode());
    }
}