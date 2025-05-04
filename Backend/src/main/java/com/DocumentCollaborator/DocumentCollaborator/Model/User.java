package com.DocumentCollaborator.DocumentCollaborator.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class User {
    String userId;
    String username;
    String caretColor;
    Integer caretPosition;

    public User(String username, Integer counter) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.caretColor = String.format("#%02X%02X%02X", (int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256));
        this.caretPosition = 0;
        }
    
    public User(String username, String Color) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.caretColor = Color;
        this.caretPosition = 0;
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
