package org.example.texteditor.DTO;

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
        System.out.println("User color: " + this.caretColor);
        this.caretPosition = 0; 
    }
}
