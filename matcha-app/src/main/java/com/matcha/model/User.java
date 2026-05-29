package com.matcha.model;

/**
 * User — Data model representing an authenticated application user.
 * Maps directly to the 'users' table in the MySQL database.
 */
public class User {

    private final int id;
    private final String username;
    private final String passwordHash;

    public User(int id, String username, String passwordHash) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "'}";
    }
}
