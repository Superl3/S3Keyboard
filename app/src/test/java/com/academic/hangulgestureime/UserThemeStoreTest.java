package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class UserThemeStoreTest {
    @Test
    public void userThemesEncodeAndDecodeRoundTrip() {
        UserThemeStore.UserTheme[] themes = {
                new UserThemeStore.UserTheme("one", "One", "{\"schemaVersion\":1}"),
                new UserThemeStore.UserTheme("two", "Two", "{\"schemaVersion\":1,\"name\":\"Two\"}")
        };

        UserThemeStore.UserTheme[] decoded = UserThemeStore.decode(UserThemeStore.encode(themes));

        assertEquals(2, decoded.length);
        assertEquals("one", decoded[0].id);
        assertEquals("One", decoded[0].name);
        assertEquals("{\"schemaVersion\":1}", decoded[0].json);
        assertEquals("two", decoded[1].id);
        assertEquals("Two", decoded[1].name);
    }

    @Test
    public void invalidUserThemePayloadDecodesAsEmptyList() {
        assertEquals(0, UserThemeStore.decode("not-json").length);
    }
}
