package ru.practicum.moviehub.model;

public class Movie {
    private final int id;
    private final String title;
    private final int year;

    public Movie(String title, int year, int id) {
        this.title = title;
        this.year = year;
        this.id = id;
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
}