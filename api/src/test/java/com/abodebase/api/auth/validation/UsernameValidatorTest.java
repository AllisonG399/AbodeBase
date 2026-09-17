package com.abodebase.api.auth.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsernameValidatorTest {

    private UsernameValidator usernameValidator;

    @BeforeEach
    void setUp() {
        usernameValidator = new UsernameValidator();
    }

    @Test
    void validUsernameIsAccepted() {

        String result =
            usernameValidator.validateAndNormalize("Allison399");

        assertEquals("allison399", result);
    }

    @Test
    void usernameIsNormalizedToLowercase() {

        String result =
            usernameValidator.validateAndNormalize("ALLISON399");

        assertEquals("allison399", result);
    }

    @Test
    void surroundingWhitespaceIsRemoved() {

        String result =
            usernameValidator.validateAndNormalize("  Allison399  ");

        assertEquals("allison399", result);
    }

    @Test
    void nullUsernameIsRejected() {

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> usernameValidator.validateAndNormalize(null)
            );

        assertEquals(
            "Username is required.",
            exception.getMessage()
        );
    }

    @Test
    void blankUsernameIsRejected() {

        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> usernameValidator.validateAndNormalize("   ")
            );

        assertEquals(
            "Username is required.",
            exception.getMessage()
        );
    }

    @Test
    void usernameShorterThanThreeCharactersIsRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> usernameValidator.validateAndNormalize("ab")
        );
    }

    @Test
    void usernameLongerThanThirtyCharactersIsRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> usernameValidator.validateAndNormalize(
                "abcdefghijklmnopqrstuvwxyz12345"
            )
        );
    }

    @Test
    void usernameWithSpecialCharactersIsRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> usernameValidator.validateAndNormalize("allison_399")
        );
    }

    @Test
    void reservedUsernameIsRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> usernameValidator.validateAndNormalize("admin")
        );
    }

    @Test
    void reservedUsernameIsCaseInsensitive() {

        assertThrows(
            IllegalArgumentException.class,
            () -> usernameValidator.validateAndNormalize("ADMIN")
        );
    }

    @Test
    void blockedUsernameIsRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> usernameValidator.validateAndNormalize("blockeduser")
        );
    }

    @Test
    void blockedUsernameIsCaseInsensitive() {

        assertThrows(
            IllegalArgumentException.class,
            () -> usernameValidator.validateAndNormalize("BLOCKEDUSER")
        );
    }

    @Test
    void blockedUsernameWithWhitespaceIsRejected() {

        assertThrows(
            IllegalArgumentException.class,
            () -> usernameValidator.validateAndNormalize("  blockeduser  ")
        );
    }
}
