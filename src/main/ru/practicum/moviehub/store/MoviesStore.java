package ru.practicum.moviehub.store;
import ru.practicum.moviehub.model.Movie;
import java.util.*;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new HashMap();

    public Movie add(Movie movie) {
        movies.put(movie.getId(), movie);
        return movie;
    }

    public List getMovies() {
        return new ArrayList<>(movies.values());
    }

    public Movie getMovie(int id) {
        return movies.get(id);
    }

    public List<Movie> getMoviesByYear(int tyear) {
        return movies.values().stream()
            .filter(movie -> movie.getYear() == tyear)
            .toList();
    }

    public boolean delete(Integer id) {
        return movies.remove(id) != null;
    }

    public void clear() {
        if (!movies.isEmpty())
            movies.clear();
    }
}