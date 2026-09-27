package com.codearena.backend.legacydata;

import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;

class LegacyUserReadConverterTest {

    private final LegacyUserReadConverter converter = new LegacyUserReadConverter();

    @Test
    void convert_readsALegacyAccountWithLowercaseRoleAndNoPasswordHash() {
        ObjectId userId = new ObjectId();
        Document legacy = new Document()
                .append("_id", userId)
                .append("firstname", "Ada")
                .append("lastname", "Lovelace")
                .append("email", "ada@example.com")
                .append("role", "admin")
                .append("salt", "deadbeef")
                .append("hash", "cafebabe");

        User user = converter.convert(legacy);

        assertThat(user.getId()).isEqualTo(userId.toHexString());
        assertThat(user.getFirstname()).isEqualTo("Ada");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        // The old app's salt+hash scheme has no equivalent here - a legacy
        // account simply can't log in through this app, by design.
        assertThat(user.getPasswordHash()).isNull();
    }

    @Test
    void convert_defaultsToUserRoleWhenMissingOrUnrecognized() {
        Document legacy = new Document()
                .append("_id", new ObjectId())
                .append("firstname", "No")
                .append("lastname", "Role")
                .append("email", "norole@example.com");

        User user = converter.convert(legacy);

        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void convert_readsACurrentAppAccountWithUppercaseRoleAndPasswordHash() {
        Document current = new Document()
                .append("_id", new ObjectId())
                .append("firstname", "New")
                .append("lastname", "User")
                .append("email", "new@example.com")
                .append("passwordHash", "$2a$10$examplebcrypthash")
                .append("role", "ADMIN");

        User user = converter.convert(current);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$examplebcrypthash");
    }
}
