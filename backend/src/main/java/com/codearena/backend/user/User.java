package com.codearena.backend.user;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String firstname;

    private String lastname;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
