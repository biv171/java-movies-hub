package ru.practicum.moviehub.http;
import com.google.gson.Gson;
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

public class MoviesGetIDTest {
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
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void searchMovieByIdAndStatusMustBe200() throws Exception {
        moviesStore.add(new Movie("Mortal Combat", 2026, 1000001));
        moviesStore.add(new Movie("Dune2", 2027, 1000002));
        moviesStore.add(new Movie("Vladimir", 2013, 1000003));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1000003"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        Movie movie = gson.fromJson(resp.body(), Movie.class);
        assertEquals(200, resp.statusCode());
        assertEquals("Vladimir", movie.getTitle());
        assertEquals(2013, movie.getYear());
    }

    @Test
    void getNotExistsMovieByIdAndStatusMustBe404() throws Exception {
        moviesStore.add(new Movie("Mortal Combat", 2026, 1000001));
        moviesStore.add(new Movie("Dune2", 2027, 1000002));
        moviesStore.add(new Movie("Vladimir", 2013, 1000003));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/888"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404, resp.statusCode());
    }

    @Test
    void getMovieByTextIdAndStatusMustBe400() throws Exception {
        moviesStore.add(new Movie("Mortal Combat", 2026, 1000001));
        moviesStore.add(new Movie("Dune2", 2027, 1000002));
        moviesStore.add(new Movie("Vladimir", 2013, 1000003));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/a100"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, resp.statusCode());
    }
}