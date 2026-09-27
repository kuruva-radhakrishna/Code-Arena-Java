package com.codearena.backend.legacydata;

import static com.codearena.backend.legacydata.LegacyDocumentAccess.idString;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.instant;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.string;

import java.time.Instant;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;

/**
 * Reads a "users" document written by either app. Field names already line
 * up (firstname/lastname/email); the only thing that would otherwise crash
 * is {@code role}, stored as lowercase text ("admin"/"user") by the old app.
 * Pre-rewrite accounts have no {@code passwordHash} at all (they used
 * passport-local-mongoose's separate salt+hash fields, a different, one-way
 * incompatible scheme) - those users simply can't log in through this app,
 * which is expected and not something this converter tries to fix.
 */
@ReadingConverter
public class LegacyUserReadConverter implements Converter<Document, User> {

    @Override
    public User convert(Document doc) {
        return User.builder()
                .id(idString(doc, "_id"))
                .firstname(string(doc, "firstname"))
                .lastname(string(doc, "lastname"))
                .email(string(doc, "email"))
                .passwordHash(string(doc, "passwordHash"))
                .role(LegacyEnums.parseLenient(Role.class, string(doc, "role"), Role.USER))
                .createdAt(orNow(instant(doc, "createdAt")))
                .build();
    }

    private static Instant orNow(Instant value) {
        return value != null ? value : Instant.now();
    }
}
