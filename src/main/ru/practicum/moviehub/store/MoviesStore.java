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

    public int getMoviesLength() {
        return movies.size();
    }

    public boolean checkIdExist(int id) {
        if (movies.containsKey(id))
            return true;
        return false;
    }

    public List<Movie> getMoviesByYear(int tyear) {
        return movies.values().stream()
            .filter(movie -> movie.getYear() == tyear)
            .toList();
    }

    public void delete(Integer id) {
        movies.remove(id);
    }

    public void clear() {
        if (!movies.isEmpty())
            movies.clear();
    }
}