package org.example.texteditor.models;

import java.util.Objects;

public class User {
    private String userId;
    private String username;
    private String cursorColor;
    private Integer cursorPosition;

    // Required for Jackson
    public User() {}

    public User(String username, String userId, String cursorColor, Integer cursorPosition) {
        this.userId = userId;
        this.username = username;
        this.cursorColor = cursorColor;
        this.cursorPosition = cursorPosition;

        System.out.println("User created: " + this.username + ", " + this.userId + ", " + this.cursorColor + ", " + this.cursorPosition);
    }

    // Add setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setCursorColor(String cursorColor) { this.cursorColor = cursorColor; }
    public void setCursorPosition(Integer cursorPosition) { this.cursorPosition = cursorPosition; }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getCursorColor() { return cursorColor; }
    public Integer getCursorPosition() { return cursorPosition; }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return username;
    }
}

