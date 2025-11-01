package io.github.sanvew.tg.init.data;

import io.github.sanvew.tg.init.data.exception.AuthDateInvalidException;
import io.github.sanvew.tg.init.data.exception.AuthDateMissingException;
import io.github.sanvew.tg.init.data.exception.ExpiredException;
import io.github.sanvew.tg.init.data.exception.SignatureInvalidException;
import io.github.sanvew.tg.init.data.exception.SignatureMissingException;
import io.github.sanvew.tg.init.data.exception.TelegramInitDataException;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonParseException;
import io.github.sanvew.tg.init.data.type.Chat;
import io.github.sanvew.tg.init.data.type.ChatType;
import io.github.sanvew.tg.init.data.type.InitData;
import io.github.sanvew.tg.init.data.type.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class InitDataUtilsTest {
    @Nested
    class validate_TestGroup {
        final long STUB_AUTH_DATE = 1749945600;
        final String STUB_BOT_TOKEN = "123456789:TEST_FAKE_BOT_TOKEN_EXAMPLE123456";
        final String STUB_VALID_INIT_DATA = "auth_date=" + STUB_AUTH_DATE +
                "&chat_type=group" +
                "&query_id=AAHdF6IQAAAAAN0XohDhrOrc" +
                "&start_param=referral123" +
                "&user=%7B%22id%22%3A123456789%2C%22first_name%22%3A%22John%22%2C%22last_name%22%3A%22Doe%22%2C%22username%22%3A%22johndoe%22%2C%22language_code%22%3A%22en%22%2C%22is_bot%22%3Afalse%2C%22is_premium%22%3Atrue%2C%22allows_write_to_pm%22%3Atrue%2C%22photo_url%22%3A%22https%3A%2F%2Fexample.com%2Favatar%2F4843.jpg%22%7D" +
                "&hash=d88ca7df91a7a28bb3b34857ed9e0ec4d99dfa2bf81fd9321e21e3abf84a8ae3";

        @Test
        void validate_withOfficialDocumentationExample_returns() {
            final String tgOffDocBotToken = "5768337691:AAH5YkoiEuPk8-FZa32hStHTqXiLPtAEhx8";
            final String tgOffDocInitData = "query_id=AAHdF6IQAAAAAN0XohDhrOrc" +
                    "&user=%7B%22id%22%3A279058397%2C%22first_name%22%3A%22Vladislav%22%2C%22last_name%22%3A%22Kibenko%22%2C%22username%22%3A%22vdkfrost%22%2C%22language_code%22%3A%22ru%22%2C%22is_premium%22%3Atrue%7D" +
                    "&auth_date=1662771648" +
                    "&hash=c501b71e775f74ce10e377dea85a7ea24ecd640b223ea86dfe453e0eaed2e2b2";

            assertDoesNotThrow(() -> InitDataUtils.validate(tgOffDocInitData, tgOffDocBotToken));
        }

        @Test
        void validate_withNullArguments_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> InitDataUtils.validate(null, STUB_BOT_TOKEN));

            assertThrows(IllegalArgumentException.class,
                    () -> InitDataUtils.validate(STUB_VALID_INIT_DATA, null));
        }

        @Test
        void validate_withBlankArguments_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> InitDataUtils.validate("    ", STUB_BOT_TOKEN));

            assertThrows(IllegalArgumentException.class,
                    () -> InitDataUtils.validate(STUB_VALID_INIT_DATA, " "));
        }

        @Test
        void validate_withMissingHash_throwsSignatureMissingException() {
            final String initDataMissingHash = "signature=dummysignature&auth_date=" + STUB_AUTH_DATE;
            assertThrows(SignatureMissingException.class, () ->
                    InitDataUtils.validate(initDataMissingHash, STUB_BOT_TOKEN));
        }

        @Test
        void validate_withMissingAuthDate_throwsAuthDateMissingException() {
            final String initDataMissingAuthDate = "hash=dummyhash";
            assertThrows(AuthDateMissingException.class,
                    () -> InitDataUtils.validate(initDataMissingAuthDate, STUB_BOT_TOKEN, Duration.ofMinutes(5)));
        }

        @Test
        void validate_withInvalidAuthDateFormat_throwsAuthDateInvalidException() {
            final String initDataInvalidAuthDate = "auth_date=invalid&hash=dummyhash";
            assertThrows(AuthDateInvalidException.class,
                    () -> InitDataUtils.validate(initDataInvalidAuthDate, STUB_BOT_TOKEN, Duration.ofMinutes(5)));
        }

        @Test
        void validate_withValidInitData_returns() {
            assertDoesNotThrow(() -> InitDataUtils.validate(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN));
        }

        @Test
        void validate_withTamperedHash_throwsSignatureInvalidException() {
            final String differentStubBotToken = "123456789:DIFFERENT_TEST_FAKE_BOT_TOKEN_EXAMPLE123456";
            assertThrows(SignatureInvalidException.class,
                    () -> InitDataUtils.validate(STUB_VALID_INIT_DATA, differentStubBotToken));
        }

        @Test
        void validate_withExpiresInBeforeExpiration_returns() {
            final Duration providedExpiresIn = Duration.ofHours(2);
            final Instant mockInstantNow = Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofHours(1));

            try (final MockedStatic<Instant> mockedStatic = mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
                mockedStatic.when(Instant::now).thenReturn(mockInstantNow);

                assertDoesNotThrow(() ->
                        InitDataUtils.validate(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, providedExpiresIn)
                );
            }
        }

        @Test
        void validate_withExpiresInAtExpirationBoundary_returns() {
            final Duration providedExpiresIn = Duration.ofHours(2);
            final Instant mockInstantNow = Instant.ofEpochSecond(STUB_AUTH_DATE).plus(providedExpiresIn);

            try (final MockedStatic<Instant> mockedStatic = mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
                mockedStatic.when(Instant::now).thenReturn(mockInstantNow);

                assertDoesNotThrow(() ->
                        InitDataUtils.validate(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, providedExpiresIn)
                );
            }
        }

        @Test
        void validate_withExpiresInJustExpired_throwsExpiredException() {
            final Duration providedExpiresIn = Duration.ofHours(2);
            final Instant mockInstantNow = Instant.ofEpochSecond(STUB_AUTH_DATE).plus(providedExpiresIn).plusSeconds(1);

            try (final MockedStatic<Instant> mockedStatic = mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
                mockedStatic.when(Instant::now).thenReturn(mockInstantNow);

                assertThrows(ExpiredException.class, () ->
                        InitDataUtils.validate(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, providedExpiresIn)
                );
            }
        }

        @Test
        void validate_withClockBeforeExpiration_returns() {
            final Clock fixedClock = Clock.fixed(
                    Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofMinutes(30)),
                    ZoneOffset.UTC
            );
            assertDoesNotThrow(() ->
                    InitDataUtils.validate(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, Duration.ofHours(1), fixedClock)
            );
        }

        @Test
        void validate_withClockAtExpirationBoundary_returns() {
            final Clock boundaryClock = Clock.fixed(
                    Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofHours(1)),
                    ZoneOffset.UTC
            );
            assertDoesNotThrow(() ->
                    InitDataUtils.validate(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, Duration.ofHours(1), boundaryClock)
            );
        }

        @Test
        void validate_withClockAfterExpiration_throwsExpiredException() {
            final Clock expiredClock = Clock.fixed(
                    Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofHours(1)).plusSeconds(1),
                    ZoneOffset.UTC
            );
            assertThrows(ExpiredException.class, () ->
                    InitDataUtils.validate(STUB_VALID_INIT_DATA, STUB_BOT_TOKEN, Duration.ofHours(1), expiredClock));
        }
    }

    @Nested
    class validate3rd_TestGroup {
        final long STUB_AUTH_DATE = 1733584787;
        final long STUB_BOT_ID = 7342037359L;
        final String STUB_VALID_INIT_DATA = "user=%7B%22id%22%3A279058397%2C%22first_name%22%3A%22Vladislav%20%2B%20-%20%3F%20%5C%2F%22%2C%22last_name%22%3A%22Kibenko%22%2C%22username%22%3A%22vdkfrost%22%2C%22language_code%22%3A%22ru%22%2C%22is_premium%22%3Atrue%2C%22allows_write_to_pm%22%3Atrue%2C%22photo_url%22%3A%22https%3A%5C%2F%5C%2Ft.me%5C%2Fi%5C%2Fuserpic%5C%2F320%5C%2F4FPEE4tmP3ATHa57u6MqTDih13LTOiMoKoLDRG4PnSA.svg%22%7D" +
                "&chat_instance=8134722200314281151" +
                "&chat_type=private" +
                "&auth_date=1733584787" +
                "&hash=2174df5b000556d044f3f020384e879c8efcab55ddea2ced4eb752e93e7080d6" +
                "&signature=zL-ucjNyREiHDE8aihFwpfR9aggP2xiAo3NSpfe-p7IbCisNlDKlo7Kb6G4D0Ao2mBrSgEk4maLSdv6MLIlADQ";
//        final String SIGNATURE_PARAM = "&signature=zL-ucjNyREiHDE8aihFwpfR9aggP2xiAo3NSpfe-p7IbCisNlDKlo7Kb6G4D0Ao2mBrSgEk4maLSdv6MLIlADQ";
//        final String SIGNATURE_PARAM_TAMPERED = SIGNATURE_PARAM.replace("signature=z", "signature=y");

        @Test
        void validate3rd_withOfficialDocumentationExample_returns() {
            final long tgOffDocBotId = 7342037359L;
            final String tgOffDocInitData = "user=%7B%22id%22%3A279058397%2C%22first_name%22%3A%22Vladislav%20%2B%20-%20%3F%20%5C%2F%22%2C%22last_name%22%3A%22Kibenko%22%2C%22username%22%3A%22vdkfrost%22%2C%22language_code%22%3A%22ru%22%2C%22is_premium%22%3Atrue%2C%22allows_write_to_pm%22%3Atrue%2C%22photo_url%22%3A%22https%3A%5C%2F%5C%2Ft.me%5C%2Fi%5C%2Fuserpic%5C%2F320%5C%2F4FPEE4tmP3ATHa57u6MqTDih13LTOiMoKoLDRG4PnSA.svg%22%7D" +
                    "&chat_instance=8134722200314281151" +
                    "&chat_type=private" +
                    "&auth_date=1733584787" +
                    "&hash=2174df5b000556d044f3f020384e879c8efcab55ddea2ced4eb752e93e7080d6" +
                    "&signature=zL-ucjNyREiHDE8aihFwpfR9aggP2xiAo3NSpfe-p7IbCisNlDKlo7Kb6G4D0Ao2mBrSgEk4maLSdv6MLIlADQ";

            assertDoesNotThrow(() -> InitDataUtils.validate3rd(tgOffDocInitData, tgOffDocBotId));
        }

        @Test
        void validate3rd_withNullInitData_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> InitDataUtils.validate3rd(null, STUB_BOT_ID));
        }

        @Test
        void validate3rd_withBlankInitData_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> InitDataUtils.validate3rd("   ", STUB_BOT_ID));
        }

        @Test
        void validate3rd_withMissingSignature_throwsSignatureMissingException() {
            final String initDataMissingSignature = "auth_date=" + STUB_AUTH_DATE;
            assertThrows(SignatureMissingException.class,
                    () -> InitDataUtils.validate3rd(initDataMissingSignature, STUB_BOT_ID)
            );
        }

        @Test
        void validate3rd_withMissingAuthDate_throwsAuthDateMissingException() {
            final String initDataMissingAuthDate = "signature=dummysignature";
            assertThrows(AuthDateMissingException.class,
                    () -> InitDataUtils.validate3rd(initDataMissingAuthDate, STUB_BOT_ID, false, Duration.ofMinutes(5)));
        }

        @Test
        void validate3rd_withInvalidAuthDateFormat_throwsAuthDateInvalidException() {
            final String initDataInvalidAuthDate = "auth_date=invalid&signature=dummysignature";
            assertThrows(AuthDateInvalidException.class,
                    () -> InitDataUtils.validate3rd(initDataInvalidAuthDate, STUB_BOT_ID, false, Duration.ofMinutes(5)));
        }

        @Test
        void validate3rd_withTamperedSignature_throwsSignatureInvalidException() {
            final long differentStubBotId = 1234567890;
            assertThrows(SignatureInvalidException.class,
                    () -> InitDataUtils.validate3rd(STUB_VALID_INIT_DATA, differentStubBotId)
            );
        }

        @Test
        void validate3rd_withExpiresInBeforeExpiration_returns() {
            final Duration providedExpiresIn = Duration.ofHours(2);
            final Clock clockBeforeExpiration = Clock.fixed(
                    Instant.ofEpochSecond(STUB_AUTH_DATE).plus(Duration.ofHours(1)),
                    ZoneOffset.UTC
            );

            assertDoesNotThrow(() ->
                    InitDataUtils.validate3rd(STUB_VALID_INIT_DATA, STUB_BOT_ID, false, providedExpiresIn, clockBeforeExpiration)
            );
        }

        @Test
        void validate3rd_withExpiresInAtExpirationBoundary_returns() {
            final Duration providedExpiresIn = Duration.ofHours(2);
            final Clock clockAtBoundary = Clock.fixed(
                    Instant.ofEpochSecond(STUB_AUTH_DATE).plus(providedExpiresIn),
                    ZoneOffset.UTC
            );

            assertDoesNotThrow(() ->
                    InitDataUtils.validate3rd(STUB_VALID_INIT_DATA, STUB_BOT_ID, false, providedExpiresIn, clockAtBoundary)
            );
        }

        @Test
        void validate3rd_withExpiresInJustExpired_throwsExpiredException() {
            final Duration providedExpiresIn = Duration.ofHours(2);
            final Clock clockAfterExpiration = Clock.fixed(
                    Instant.ofEpochSecond(STUB_AUTH_DATE).plus(providedExpiresIn).plusSeconds(1),
                    ZoneOffset.UTC
            );

            assertThrows(ExpiredException.class, () ->
                    InitDataUtils.validate3rd(STUB_VALID_INIT_DATA, STUB_BOT_ID, false, providedExpiresIn, clockAfterExpiration)
            );
        }
    }

    @Nested
    class parse_TestGroup {
        @Test
        void parse_fullStubInitData_returnsFullInitData() {
            final String fullStubInitData = "auth_date=1749945600"
                    + "&chat=%7B%22id%22%3A1001234567890%2C%22type%22%3A%22supergroup%22%2C%22title%22%3A%22Test%20Group%22%2C%22photo_url%22%3A%22https%3A%2F%2Fexample.com%2Fgroup.jpg%22%2C%22username%22%3A%22testgroup%22%7D"
                    + "&chat_type=supergroup"
                    + "&can_send_after=120"
                    + "&chat_instance=122233445566778899"
                    + "&hash=dummyhash"
                    + "&signature=dummysignature"
                    + "&query_id=AAHdF6IQAAAAAN0XohDhrOrc"
                    + "&receiver=%7B%22id%22%3A987654321%2C%22first_name%22%3A%22Receiver%22%2C%22is_bot%22%3Afalse%7D"
                    + "&start_param=refParam123"
                    + "&user=%7B%22id%22%3A123456789%2C%22is_bot%22%3Afalse%2C%22first_name%22%3A%22Alice%22%2C%22last_name%22%3A%22Smith%22%2C%22username%22%3A%22alice123%22%2C%22language_code%22%3A%22en%22%2C%22is_premium%22%3Atrue%2C%22allows_write_to_pm%22%3Atrue%2C%22added_to_attachment_menu%22%3Atrue%2C%22photo_url%22%3A%22https%3A%2F%2Fexample.com%2Favatar.jpg%22%7D";

            final User expectedUser = new User(
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

            final User expectedReceiver = new User(
                    null,
                    null,
                    null,
                    "Receiver",
                    987654321L,
                    false,
                    null,
                    null,
                    null,
                    null
            );

            final Chat expectedChat = new Chat(
                    1001234567890L,
                    ChatType.SUPERGROUP,
                    "Test Group",
                    URI.create("https://example.com/group.jpg"),
                    "testgroup"
            );
            
            final InitData expected = new InitData(
                    1749945600L,
                    120L,
                    expectedChat,
                    ChatType.SUPERGROUP,
                    "122233445566778899",
                    "dummyhash",
                    "dummysignature",
                    "AAHdF6IQAAAAAN0XohDhrOrc",
                    expectedReceiver,
                    "refParam123",
                    expectedUser
            );

            final InitData actual = InitDataUtils.parse(fullStubInitData);
            assertEquals(expected, actual);
        }

        @Test
        void parse_withOnlyRequiredFields_returnsMinimalInitData() {
            final String initDataRequiredOnlyParams = "auth_date=1749945600&hash=dummyhashsignature";

            final InitData expected = new InitData(
                    1749945600L, null, null, null, null, "dummyhashsignature", null, null, null, null, null
            );

            final InitData actual = InitDataUtils.parse(initDataRequiredOnlyParams);
            assertEquals(expected, actual);
        }

        @Test
        void parse_withExtraFields_returnsInitDataWithExtraFields() {
            final String initDataExtraFields = "auth_date=1749945600&hash=abc&foo=bar";

            final InitData expected = new InitData(
                    1749945600L, null, null, null, null, "abc", null, null, null, null, null,
                    Map.of("foo", "bar")
            );

            final InitData actual = InitDataUtils.parse(initDataExtraFields);
            assertEquals(expected, actual);
        }

        @Test
        void parse_withNullArgument_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> InitDataUtils.parse(null));
        }

        @Test
        void parse_withBlankArgument_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> InitDataUtils.parse("     "));
        }

        @Test
        void parse_withMissingAuthDate_throwsException() {
            final String initDataMissingAuthDate = "hash=abc";

            assertThrows(AuthDateMissingException.class, () -> InitDataUtils.parse(initDataMissingAuthDate));
        }

        @Test
        void parse_withMalformedCanSendAfter_throwsTelegramInitDataException() {
            final String initMalformedCanSendAfter = "auth_date=1749945600&hash=abc&can_send_after=not_a_unix_timestamp";

            assertThrows(TelegramInitDataException.class, () -> InitDataUtils.parse(initMalformedCanSendAfter));
        }

        @Test
        void parse_withMissingHash_throwsSignatureMissingException() {
            final String initDataMissingHash = "auth_date=1749945600";

            assertThrows(SignatureMissingException.class, () -> InitDataUtils.parse(initDataMissingHash));
        }

        @Test
        void parse_withMalformedJson_throwsException() {
            final String initDataMalformedUser = "auth_date=1749945600&hash=dummy&user={broken}";
            final String initDataMalformedChat = "auth_date=1749945600&hash=dummy&chat={invalid}";

            assertThrows(JsonParseException.class, () -> InitDataUtils.parse(initDataMalformedUser));
            assertThrows(JsonParseException.class, () -> InitDataUtils.parse(initDataMalformedChat));
        }
    }
}
