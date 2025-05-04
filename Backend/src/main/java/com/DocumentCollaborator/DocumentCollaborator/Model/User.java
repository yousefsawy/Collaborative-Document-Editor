package com.DocumentCollaborator.DocumentCollaborator.Model;

import java.util.Objects;
import java.util.UUID;

public class User {
    String userId;
    String username;

    public User(String username) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
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
