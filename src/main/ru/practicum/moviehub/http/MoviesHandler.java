package ru.practicum.moviehub.http;
import java.io.IOException;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import com.sun.net.httpserver.Headers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import java.util.concurrent.atomic.AtomicInteger;


public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;
    private final AtomicInteger counter = new AtomicInteger(0);

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        List<String> detailsValues = new ArrayList<>();
        Map<String, List> message = new HashMap<>();
        InputStream inputStream = ex.getRequestBody();
        String jsonBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        String path = ex.getRequestURI().getPath();
        String[] pathPart = path.split("/");
        String query = ex.getRequestURI().getQuery();

        switch (method) {
            case "DELETE":
                Integer id = parseId(path.split("/")[2]);

                if (id == null) {
                    sendError(ex,400, "Некорректный ID");
                    break;
                }
                if (moviesStore.checkIdExist(id)) {
                    moviesStore.delete(id);
                    sendNoContent(ex);
                    break;
                }
                sendError(ex,404, "Фильм не найден");
                break;

            case "GET":
                //GET без параметров
                if (pathPart.length == 2 && pathPart[1].equals("movies") && query == null) {
                    sendJson(ex,200, gson.toJson(moviesStore.getMovies()));
                    break;
                }
                //GET с ID
                if (pathPart.length == 3 && pathPart[1].equals("movies") && query == null) {
                    Integer idp = parseId(path.split("/")[2]);
                    if (idp == null) {
                        sendError(ex, 400, "Некорректный ID");
                        break;
                    }
                    if (moviesStore.getMovie(idp) == null) {
                        sendError(ex, 404, "Фильм не найден");
                        break;
                    }
                    sendJson(ex, 200, gson.toJson(moviesStore.getMovie(idp)));
                    break;
                }

                //GET с параметрами ?
                if (query != null) {
                    String queryParametr = query.split("=")[0];
                    String queryValue = query.split("=")[1];
                    try {
                        int year = Integer.parseInt(queryValue);
                        if (queryParametr.equals("year") && year >= 1888 && year <= 2026) {
                            sendJson(ex, 200, gson.toJson(moviesStore.getMoviesByYear(year)));
                            break;
                        }
                    } catch (NumberFormatException e) {
                        sendError(ex, 400, "Некорректный параметр запроса - " + queryParametr);
                        break;
                    }
                }
                break;

            case "POST":
                //Если ошибка в заголовке
                Headers requestHeaders = ex.getRequestHeaders();
                List<String> contentTypeValues = requestHeaders.get("Content-type");
                if (!contentTypeValues.contains("application/json")
                        && !contentTypeValues.contains("application/json; charset=UTF-8")) {
                    sendError(ex,415,"Неправильное значение заголовка");
                    break;
                }

                //Если ошибка с данными
                Movie movie = gson.fromJson(jsonBody, Movie.class);
                String title = movie.getTitle();
                int year = movie.getYear();

                if (title.isBlank() || year < 1888 || year > 2026 || title.length() > 100) {
                    message.put("error",List.of("Ошибка валидации"));
                    if (title.isBlank()) {
                        detailsValues.add("название не должно быть пустым");
                    }
                    if (year < 1888 || year > 2026) {
                        detailsValues.add("год должен быть между 1888 и 2026");
                    }
                    if (title.length() > 100) {
                        detailsValues.add("Длина заголовка свыше 100 символов");
                    }
                    sendListError(ex, detailsValues);
                    break;
                }

                Movie newMovie = moviesStore.add(
                        new Movie(title, year, 1000 + counter.incrementAndGet())
                );

                sendJson(ex,201, gson.toJson(newMovie));
                break;

            default:
                sendError(ex, 405, "Некорректный метод");
        }
    }

    private Integer parseId(String idPart) {
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}