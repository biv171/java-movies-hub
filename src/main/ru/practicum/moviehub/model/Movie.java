package ru.practicum.moviehub.model;
import java.util.concurrent.atomic.AtomicInteger;


public class Movie {
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final int id;
    private final String title;
    private final int year;

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
        this.id = 1000000 + generateId();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public static int generateId() {
        return idCounter.incrementAndGet(); // Атомно инкрементирует и возвращает новое значение
    }
}