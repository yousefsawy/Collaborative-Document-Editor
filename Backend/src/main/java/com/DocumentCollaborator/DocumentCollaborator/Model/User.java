package com.DocumentCollaborator.DocumentCollaborator.Model;

import java.util.Objects;
import java.util.UUID;

public class User {
    private String userId;
    private String username;
    private String cursorColor;
    private Integer cursorPosition;

    public User(String username, Integer count) {
        this.userId = UUID.randomUUID().toString();
        this.cursorColor = "rgb(" + (int) (Math.random() * 256) + "," + (int) (Math.random() * 256) + "," + (int) (Math.random() * 256) + ")";
        this.cursorPosition = 0;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getCursorColor() {
        return cursorColor;
    }

    public Integer getCursorPosition() {
        return cursorPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return username;
    }
}