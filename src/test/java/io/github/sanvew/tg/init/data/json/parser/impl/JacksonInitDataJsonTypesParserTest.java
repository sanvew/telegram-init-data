package io.github.sanvew.tg.init.data.json.parser.impl;

import io.github.sanvew.tg.init.data.json.parser.InitDataJsonTypesParser;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonParseException;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonPropertyMissingException;
import io.github.sanvew.tg.init.data.type.Chat;
import io.github.sanvew.tg.init.data.type.ChatType;
import io.github.sanvew.tg.init.data.type.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonInitDataJsonTypesParserTest {
    final InitDataJsonTypesParser underTest = new JacksonInitDataJsonTypesParser();

    @Nested
    class parseUser_TestGroup {
        @Test
        void parseUser_withAllFieldsProvided_returnsUser() {
            final String inputAllFieldsProvided = "{"
                    + "\"id\":123456789,"
                    + "\"is_bot\":false,"
                    + "\"first_name\":\"Alice\","
                    + "\"last_name\":\"Smith\","
                    + "\"username\":\"alice123\","
                    + "\"language_code\":\"en\","
                    + "\"is_premium\":true,"
                    + "\"allows_write_to_pm\":true,"
                    + "\"added_to_attachment_menu\":true,"
                    + "\"photo_url\":\"https://example.com/avatar.jpg\""
                    + "}";

            final User expected = new User(
                    true,
                    true,
                    true,
                    "Alice",
                    123456789L,
                    false,
                    "Smith",
                    "en",
                    URI.create("https://example.com/avatar.jpg"),
                    "alice123"
            );

            final User actual = underTest.parseUser(inputAllFieldsProvided);

            assertEquals(expected, actual);
        }

        @Test
        void parseUser_withOnlyRequiredFields_returnsUser() {
            final String inputOnlyRequiredFields = "{"
                    + "\"id\":1,"
                    + "\"first_name\":\"A\""
                    + "}";

            final User expected = new User(null, null, null, "A", 1, null, null, null, null, null);

            final User actual = underTest.parseUser(inputOnlyRequiredFields);

            assertEquals(expected, actual);
        }

        @Test
        void parseUser_withNullFieldsValues_returnUser() {
            final String inputNullFieldsValues = "{"
                    + "\"id\": 1,"
                    + "\"is_bot\": null,"
                    + "\"first_name\": \"A\","
                    + "\"last_name\": null,"
                    + "\"username\": null,"
                    + "\"language_code\": null,"
                    + "\"is_premium\": null,"
                    + "\"allows_write_to_pm\": null,"
                    + "\"added_to_attachment_menu\": null,"
                    + "\"photo_url\": null"
                    + "}";

            final User expected = new User(null, null, null, "A", 1, null, null, null, null, null);

            final User actual = underTest.parseUser(inputNullFieldsValues);

            assertEquals(expected, actual);
        }

        @Test
        void parseUser_withExtraFields_returnsUser() {
            final String inputExtraFields = "{"
                    + "\"id\":1,"
                    + "\"first_name\":\"A\","
                    + "\"foo\":\"bar\","
                    + "\"bar\":null"
                    + "}";

            final Map<String, String> expectedExtra = new HashMap<>();
            expectedExtra.put("foo", "bar");
            expectedExtra.put("bar", null);
            final User expected = new User(null, null, null, "A", 1, null, null, null, null, null, expectedExtra);

            final User actual = underTest.parseUser(inputExtraFields);

            assertEquals(expected, actual);
        }

        @Test
        void parseUser_withNullArgument_returnsNull() {
            assertNull(underTest.parseUser(null));
        }

        @Test
        void parseUser_withBlankArgument_returnsNull() {
            assertNull(underTest.parseUser("  "));
        }

        @Test
        void parseUser_withMissingRequiredId_throwsJsonParseException() {
            final String inputRequiredMissingId = "{"
                    + "\"first_name\":\"Alice\""
                    + "}";

            assertThrows(JsonPropertyMissingException.class, () -> underTest.parseUser(inputRequiredMissingId));
        }

        @Test
        void parseUser_withMissingRequiredFirstName_throwsJsonParseException() {
            final String inputMissingRequiredFirstName = "{"
                    + "\"id\":123456"
                    + "}";

            assertThrows(JsonPropertyMissingException.class, () -> underTest.parseUser(inputMissingRequiredFirstName));
        }

        @Test
        void parseUser_withMalformedJson_throwsJsonParseException() {
            final String inputMalformedJson = "{bad_json:";

            assertThrows(JsonParseException.class, () -> underTest.parseUser(inputMalformedJson));
        }
    }

    @Nested
    class parseChat_TestGroup {
        @Test
        void parseChat_withAllFieldsProvided_returnsChat() {
            final String inputAllFieldsProvided = "{"
                    + "\"id\": 1001234567890,"
                    + "\"type\": \"supergroup\","
                    + "\"title\": \"Test Group\","
                    + "\"photo_url\": \"https://example.com/photo.jpg\","
                    + "\"username\": \"testgroup\""
                    + "}";

            final Chat expected = new Chat(
                    1001234567890L, ChatType.SUPERGROUP, "Test Group", URI.create("https://example.com/photo.jpg"),
                    "testgroup"
            );

            final Chat actual = underTest.parseChat(inputAllFieldsProvided);

            assertEquals(expected, actual);
        }

        @Test
        void parseChat_withOnlyRequiredFields_returnsChat() {
            final String inputOnlyRequiredFields = "{"
                    + "\"id\": 1001234567890,"
                    + "\"type\": \"supergroup\","
                    + "\"title\": \"Test Group\""
                    + "}";

            final Chat expected = new Chat(1001234567890L, ChatType.SUPERGROUP, "Test Group", null, null);

            final Chat actual = underTest.parseChat(inputOnlyRequiredFields);

            assertEquals(expected, actual);
        }

        @Test
        void parseChat_withNullFieldsValues_returnChat() {
            final String inputNullFieldsValues = "{"
                    + "\"id\": 1001234567890,"
                    + "\"type\": \"group\","
                    + "\"title\": \"Extras\","
                    + "\"username\": null,"
                    + "\"photo_url\": null"
                    + "}";

            final Chat expected = new Chat(1001234567890L, ChatType.GROUP, "Extras", null, null);

            final Chat actual = underTest.parseChat(inputNullFieldsValues);

            assertEquals(expected, actual);
        }

        @Test
        void parseChat_withExtraFields_returnsChat() {
            final String inputExtraFields = "{"
                    + "\"id\": 1001234567890,"
                    + "\"type\": \"group\","
                    + "\"title\": \"Extras\","
                    + "\"foo\": \"bar\","
                    + "\"bar\": null"
                    + "}";

            final Map<String, String> expectedExtra = new HashMap<>();
            expectedExtra.put("foo", "bar");
            expectedExtra.put("bar", null);
            final Chat expected = new Chat(
                    1001234567890L, ChatType.GROUP, "Extras", null, null, Collections.unmodifiableMap(expectedExtra)
            );

            final Chat actual = underTest.parseChat(inputExtraFields);

            assertEquals(expected, actual);
        }

        @Test
        void parseChat_withNullArgument_returnsNull() {
            assertNull(underTest.parseChat(null));
        }

        @Test
        void parseChat_withBlankArgument_returnsNull() {
            assertNull(underTest.parseChat("   "));
        }

        @Test
        void parseChat_withMissingRequiredId_throwsJsonParseException() {
            final String inputMissingRequiredId = "{"
                    + "\"type\": \"supergroup\","
                    + "\"title\": \"Missing ID\""
                    + "}";

            assertThrows(JsonPropertyMissingException.class, () -> underTest.parseChat(inputMissingRequiredId));
        }

        @Test
        void parseChat_withMissingRequiredType_throwsJsonParseException() {
            final String inputMissingRequiredType = "{"
                    + "\"id\": 1,"
                    + "\"title\": \"Missing Type\""
                    + "}";

            assertThrows(JsonPropertyMissingException.class, () -> underTest.parseChat(inputMissingRequiredType));
        }

        @Test
        void parseChat_withMissingRequiredTitle_throwsJsonParseException() {
            final String missingTitle = "{"
                    + "\"id\": 1,"
                    + "\"type\": \"private\""
                    + "}";

            assertThrows(JsonPropertyMissingException.class, () -> underTest.parseChat(missingTitle));
        }

        @Test
        void parseChat_withMalformedJson_throwsJsonParseException() {
            final String inputMalformedJson = "{ \"id\": 1001234567890, \"type\": \"group\", \"title\": ";

            assertThrows(JsonParseException.class, () -> underTest.parseChat(inputMalformedJson));
        }
    }
}