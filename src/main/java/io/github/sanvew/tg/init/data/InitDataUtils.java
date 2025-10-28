package io.github.sanvew.tg.init.data;

import io.github.sanvew.tg.init.data.exception.AuthDateInvalidException;
import io.github.sanvew.tg.init.data.exception.AuthDateMissingException;
import io.github.sanvew.tg.init.data.exception.ExpiredException;
import io.github.sanvew.tg.init.data.exception.SignatureInvalidException;
import io.github.sanvew.tg.init.data.exception.SignatureMissingException;
import io.github.sanvew.tg.init.data.json.parser.InitDataJsonTypesParser;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonParseException;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonPropertyMissingException;
import io.github.sanvew.tg.init.data.json.parser.impl.JacksonInitDataJsonTypesParser;
import io.github.sanvew.tg.init.data.type.Chat;
import io.github.sanvew.tg.init.data.type.ChatType;
import io.github.sanvew.tg.init.data.type.InitData;
import io.github.sanvew.tg.init.data.type.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for parsing and validating Telegram Mini App {@code initData} payloads.
 * <p>
 * Provides static methods to parse, validate, and verify the authenticity of Telegram init data.
 * <p>
 * This class supports default parsing logic via Jackson and exposes overloads for custom parsers.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Init Data Documentation</a>
 */
public class InitDataUtils {
    private static final Base64.Decoder base64UrlDecoder = Base64.getUrlDecoder();

    private static final byte[] TG_HASH_KEY = "WebAppData".getBytes(StandardCharsets.UTF_8);
    private static final PublicKey TG_PROD_ED25519_PUBLIC_KEY = CryptoUtils.importEd25519PublicKey(
            "e7bf03a2fa4602af4580703d88dda5bb59f32ed8b02a56c187fe7d34caed242d"
    );
    private static final PublicKey TG_TEST_ED25519_PUBLIC_KEY = CryptoUtils.importEd25519PublicKey(
            "40055058a4ee38156a06562e52eece92a771bcd8346a8c4615cb7376eddf72ec"
    );

    private InitDataUtils() {}

    /**
     * Validates the provided {@code initData} using Telegram's bot token–based HMAC hash as described in the
     * <a href="https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-bot-token">Telegram Mini Apps Init Data</a> documentation.
     * <p>
     * Uses the supplied bot token to verify the {@code hash} parameter and, when requested, checks that {@code auth_date}
     * remains within the allowed time window.
     *
     * @param initData  the initialization data string received from the Telegram Mini App
     * @param botToken  the bot token associated with the Telegram bot
     * @param expiresIn optional duration indicating how long the init data is valid (based on {@code auth_date});
     *                  if {@code null}, no expiration validation is performed
     * @param clock     optional clock to use for time comparison; if {@code null}, the system default clock is used
     * @throws IllegalArgumentException  if {@code initData} or {@code botToken} is {@code null}
     * @throws SignatureMissingException if the {@code hash} parameter is missing in {@code initData}
     * @throws AuthDateMissingException  if {@code auth_date} is missing when expiration validation is required
     * @throws AuthDateInvalidException  if {@code auth_date} cannot be parsed into a valid timestamp
     * @throws ExpiredException          if the {@code auth_date} is outside the allowed {@code expiresIn} window
     * @throws SignatureInvalidException if the computed hash does not match the one provided in {@code initData}
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-bot-token">Telegram Mini Apps Init Data: Validating using Telegram Bot Token</a>
     */
    public static void validate(
            @NotNull String initData,
            @NotNull String botToken,
            @Nullable Duration expiresIn,
            @Nullable Clock clock
    ) {
        if (initData == null || initData.isBlank()) {
            throw buildExceptionArgumentNotProvided("initData");
        }
        if (botToken == null || botToken.isBlank()) {
            throw buildExceptionArgumentNotProvided("botToken");
        }

        final Map<String, String> parsedInitData = parseQueryString(initData);

        final String hashFromInitData = parsedInitData.remove(InitData.Param.HASH.value);
        if (hashFromInitData == null) {
            throw SignatureMissingException.ofHash();
        }

        if (expiresIn != null) {
            validateAuthDate(parsedInitData.get(InitData.Param.AUTH_DATE.value), expiresIn, clock);
        }

        final byte[] formattedInitDataByteArr = formatInitData(parsedInitData).getBytes(StandardCharsets.UTF_8);
        final byte[] botTokenByteArr = botToken.getBytes(StandardCharsets.UTF_8);
        if (!CryptoUtils.verifyHmac256Hash(formattedInitDataByteArr, botTokenByteArr, TG_HASH_KEY, hashFromInitData)) {
            throw SignatureInvalidException.ofHash();
        }
    }

    /**
     * Validates the provided {@code initData} using the default system clock and optionally checks the
     * {@code auth_date} against an expiration duration.
     * <p>
     * Delegates to {@link #validate(String, String, Duration, Clock)} with {@code clock} set to {@code null}.
     *
     * @param initData  the initialization data string received from the Telegram Mini App
     * @param botToken  the bot token associated with the Telegram bot
     * @param expiresIn optional duration indicating how long the init data is valid (based on {@code auth_date});
     *                  if {@code null}, no expiration validation is performed
     * @throws IllegalArgumentException  if {@code initData} or {@code botToken} is {@code null}
     * @throws SignatureMissingException if the {@code hash} parameter is missing in {@code initData}
     * @throws AuthDateMissingException  if {@code auth_date} is missing when expiration validation is required
     * @throws AuthDateInvalidException  if {@code auth_date} cannot be parsed into a valid timestamp
     * @throws ExpiredException          if the {@code auth_date} is outside the allowed {@code expiresIn} window
     * @throws SignatureInvalidException if the computed hash does not match the one provided in {@code initData}
     * @see #validate(String, String, Duration, Clock)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-bot-token">Telegram Mini Apps Init Data: Validating using Telegram Bot Token</a>
     */
    public static void validate(@NotNull String initData, @NotNull String botToken, @Nullable Duration expiresIn) {
        validate(initData, botToken, expiresIn, null);
    }

    /**
     * Validates the provided {@code initData} using the default system clock without checking for expiration, as defined in the
     * <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Mini Apps Init Data</a> documentation.
     * <p>
     * Delegates to {@link #validate(String, String, Duration, Clock)} with {@code clock} and {@code expiresIn} set to {@code null}.
     *
     * @param initData the initialization data string received from the Telegram Mini App
     * @param botToken the bot token associated with the Telegram bot
     * @throws IllegalArgumentException  if {@code initData} or {@code botToken} is {@code null}
     * @throws SignatureMissingException if the {@code hash} parameter is missing in {@code initData}
     * @throws SignatureInvalidException if the computed hash does not match the one provided in {@code initData}
     * @see #validate(String, String, Duration, Clock)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-bot-token">Telegram Mini Apps Init Data: Validating using Telegram Bot Token</a>
     */
    public static void validate(@NotNull String initData, @NotNull String botToken) {
        validate(initData, botToken, null, null);
    }

    /**
     * Validates the provided {@code initData} signed with Telegram's Ed25519 web-app public key.
     * <p>
     * Supports both production and test keys and can enforce {@code auth_date}-based expiration.
     *
     * @param initData        Telegram init data query string that contains a {@code signature} parameter
     * @param botId           numeric bot identifier used during verification
     * @param testEnvironment {@code true} to use the Telegram test Ed25519 public key, {@code false} for production
     * @param expiresIn       optional duration indicating how long the init data is valid (based on {@code auth_date});
     *                        if {@code null}, no expiration validation is performed
     * @param clock           optional clock to use for time comparison when {@code expiresIn} is provided;
     *                        if {@code null}, {@link Instant#now()} is used
     * @throws IllegalArgumentException   if {@code initData} is {@code null} or blank
     * @throws SignatureMissingException  if the {@code signature} parameter is missing in {@code initData}
     * @throws AuthDateMissingException   if {@code auth_date} is missing when expiration validation is required
     * @throws AuthDateInvalidException   if {@code auth_date} cannot be parsed into a valid timestamp
     * @throws ExpiredException           if the {@code auth_date} is outside the allowed {@code expiresIn} window
     * @throws SignatureInvalidException  if the Ed25519 signature does not match the one provided in {@code initData}
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-public-key">Telegram Mini Apps Init Data: Validating using Telegram Public Key</a>
     */
    public static void validate3rd(
            @NotNull String initData,
            long botId,
            boolean testEnvironment,
            @Nullable Duration expiresIn,
            @Nullable Clock clock
    ) {
        if (initData == null || initData.isBlank()) {
            throw buildExceptionArgumentNotProvided("initData");
        }

        final Map<String, String> parsedInitData = parseQueryString(initData);

        parsedInitData.remove(InitData.Param.HASH.value);
        final String base64EncodedSignatureFromInitData = parsedInitData.remove(InitData.Param.SIGNATURE.value);
        if (base64EncodedSignatureFromInitData == null) {
            throw SignatureMissingException.ofSignature();
        }

        if (expiresIn != null) {
            validateAuthDate(parsedInitData.get(InitData.Param.AUTH_DATE.value), expiresIn, clock);
        }

        final byte[] fmtInitData = formatInitData3rd(parsedInitData, botId).getBytes(StandardCharsets.UTF_8);
        final PublicKey publicKey = testEnvironment ? TG_TEST_ED25519_PUBLIC_KEY : TG_PROD_ED25519_PUBLIC_KEY;
        final byte[] signatureFromInitData = base64UrlDecoder.decode(base64EncodedSignatureFromInitData);
        if (!CryptoUtils.verifyEd25519Signature(fmtInitData, signatureFromInitData, publicKey)) {
            throw SignatureInvalidException.ofSignature();
        }
    }

    /**
     * Validates the provided {@code initData} using Telegram's Ed25519 key and the system clock.
     * <p>
     * Delegates to {@link #validate3rd(String, long, boolean, Duration, Clock)} with {@code clock} set to {@code null}.
     *
     * @param initData        init data query string that contains a {@code signature} parameter
     * @param botId           numeric bot identifier used during verification
     * @param testEnvironment {@code true} to use the Telegram test key, {@code false} for production
     * @param expiresIn       optional duration indicating how long the init data is valid (based on {@code auth_date});
     *                        if {@code null}, no expiration validation is performed
     * @throws IllegalArgumentException  if {@code initData} is {@code null} or blank
     * @throws SignatureMissingException if the {@code signature} parameter is missing in {@code initData}
     * @throws AuthDateMissingException  if {@code auth_date} is missing when expiration validation is required
     * @throws AuthDateInvalidException  if {@code auth_date} cannot be parsed into a valid timestamp
     * @throws ExpiredException          if the {@code auth_date} is outside the allowed {@code expiresIn} window
     * @throws SignatureInvalidException if the Ed25519 signature does not match the one provided in {@code initData}
     * @see #validate3rd(String, long, boolean, Duration, Clock)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-public-key">Telegram Mini Apps Init Data: Validating using Telegram Public Key</a>
     */
    public static void validate3rd(
            @NotNull String initData,
            long botId,
            boolean testEnvironment,
            @Nullable Duration expiresIn
    ) {
        validate3rd(initData, botId, testEnvironment, expiresIn, null);
    }

    /**
     * Validates {@code initData} using Telegram's Ed25519 key without enforcing expiration.
     * <p>
     * Delegates to {@link #validate3rd(String, long, boolean, Duration, Clock)} with {@code expiresIn} and {@code clock}
     * set to {@code null}.
     *
     * @param initData        init data query string that contains a {@code signature} parameter
     * @param botId           numeric bot identifier used during verification
     * @param testEnvironment {@code true} to use the Telegram test key, {@code false} for production
     * @throws IllegalArgumentException  if {@code initData} is {@code null} or blank
     * @throws SignatureMissingException if the {@code signature} parameter is missing in {@code initData}
     * @throws SignatureInvalidException if the Ed25519 signature does not match the one provided in {@code initData}
     * @see #validate3rd(String, long, boolean, Duration, Clock)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-public-key">Telegram Mini Apps Init Data: Validating using Telegram Public Key</a>
     */
    public static void validate3rd(@NotNull String initData, long botId, boolean testEnvironment) {
        validate3rd(initData, botId, testEnvironment, null, null);
    }

    /**
     * Validates {@code initData} against Telegram's production Ed25519 key without expiration checks.
     * <p>
     * Delegates to {@link #validate3rd(String, long, boolean, Duration, Clock)} with {@code testEnvironment},
     * {@code expiresIn}, and {@code clock} set to {@code false}, {@code null}, and {@code null}.
     *
     * @param initData init data query string that contains a {@code signature} parameter
     * @param botId    numeric bot identifier used during verification
     * @throws IllegalArgumentException  if {@code initData} is {@code null} or blank
     * @throws SignatureMissingException if the {@code signature} parameter is missing in {@code initData}
     * @throws SignatureInvalidException if the Ed25519 signature does not match the one provided in {@code initData}
     * @see #validate3rd(String, long, boolean, Duration, Clock)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#using-telegram-public-key">Telegram Mini Apps Init Data: Validating using Telegram Public Key</a>
     */
    public static void validate3rd(@NotNull String initData, long botId) {
        validate3rd(initData, botId, false, null, null);
    }

    /**
     * Parses the provided {@code initData} string into an {@link InitData} object using the given {@code parser}.
     * The string must be in the format defined by Telegram Mini Apps and include at minimum an {@code auth_date} and {@code hash}.
     * <p>
     * If any optional values are not present in the string, they will be {@code null} in the resulting {@link InitData}.
     *
     * @param initData the raw init data string received from Telegram (must be URL query format)
     * @param parser   optional parser to deserialize structured fields like {@code user} and {@code chat};
     *                 if {@code null}, a default Jackson-based parser is used
     * @return parsed {@link InitData} object with typed fields
     * @throws IllegalArgumentException     if {@code initData} is {@code null} or {@code isBlank() == true}
     * @throws NumberFormatException        if {@code can_send_after} can't be parsed to {@link Long}
     * @throws JsonParseException           if there are occurred during json field parsing (e.g. {@code user}, {@code chat} etc.)
     * @throws JsonPropertyMissingException if any required property in json object is missing
     * @throws SignatureMissingException    if the {@code hash} parameter is missing in {@code initData}
     * @throws AuthDateInvalidException     if {@code auth_date} cannot be parsed into a valid timestamp
     * @see #validate3rd(String, long, boolean, Duration, Clock)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Init Data documentation</a>
     */
    public static @NotNull InitData parse(@NotNull String initData, @Nullable final InitDataJsonTypesParser parser) {
        if (initData == null || initData.isBlank()) {
            throw buildExceptionArgumentNotProvided("initData");
        }

        final InitDataJsonTypesParser parserUsed = parser == null ? JacksonInitDataJsonTypesParser.INSTANCE : parser;

        final Map<String, String> parsedInitData = parseQueryString(initData);

        final Long authDate = parsedInitData.containsKey(InitData.Param.AUTH_DATE.value)
                ? parseAuthDate(parsedInitData.remove(InitData.Param.AUTH_DATE.value))
                : null;
        if (authDate == null) {
            throw new AuthDateMissingException();
        }

        Long canSendAfter;
        try {
            canSendAfter = parsedInitData.containsKey(InitData.Param.CAN_SEND_AFTER.value)
                    ? Long.parseLong(parsedInitData.remove(InitData.Param.CAN_SEND_AFTER.value))
                    : null;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Unable to parse "
                    + InitData.Param.CAN_SEND_AFTER.value
                    + ": "
                    + parsedInitData.get(InitData.Param.CAN_SEND_AFTER.value)
            );
        }
        final Chat chat = parserUsed.parseChat(parsedInitData.remove(InitData.Param.CHAT.value));
        final ChatType chatType = ChatType.fromValue(parsedInitData.remove(InitData.Param.CHAT_TYPE.value));
        final String chatInstance = parsedInitData.remove(InitData.Param.CHAT_INSTANCE.value);
        final String hash = parsedInitData.remove(InitData.Param.HASH.value);
        if (hash == null) {
            throw SignatureMissingException.ofHash();
        }
        final String signature = parsedInitData.remove(InitData.Param.SIGNATURE.value);
        final String queryId = parsedInitData.remove(InitData.Param.QUERY_ID.value);
        final User receiver = parserUsed.parseUser(parsedInitData.remove(InitData.Param.RECEIVER.value));
        final String startParam = parsedInitData.remove(InitData.Param.START_PARAM.value);
        final User user = parserUsed.parseUser(parsedInitData.remove(InitData.Param.USER.value));

        return new InitData(
                authDate, canSendAfter, chat, chatType, chatInstance, hash, signature, queryId, receiver, startParam,
                user, parsedInitData
        );
    }

    /**
     * Parses the provided {@code initData} string into an {@link InitData} object using the default parser implementation.
     * <p>
     * Internally delegates to {@link #parse(String, InitDataJsonTypesParser)} with a default Jackson-based parser.
     *
     * @param initData the raw init data string received from Telegram (must be URL query format)
     * @return parsed {@link InitData} object with typed fields
     * @throws IllegalArgumentException     if {@code initData} is {@code null} or {@code isBlank() == true}
     * @throws NumberFormatException        if {@code can_send_after} can't be parsed to {@link Long}
     * @throws JsonParseException           if there are occurred during json field parsing (e.g. {@code user}, {@code chat} etc.)
     * @throws JsonPropertyMissingException if any required property in json object is missing
     * @throws SignatureMissingException    if the {@code hash} parameter is missing in {@code initData}
     * @throws AuthDateInvalidException     if {@code auth_date} cannot be parsed into a valid timestamp
     * @see #parse(String, InitDataJsonTypesParser)
     * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Init Data documentation</a>
     */
    public static @NotNull InitData parse(@NotNull String initData) {
        return InitDataUtils.parse(initData, null);
    }

    // =================================================================================================================
    // initData parsing
    // =================================================================================================================
    private static Map<String, String> parseQueryString(String queryString) {
        final Map<String, String> parameters = new TreeMap<>();
        final String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
            final String value = idx > 0 && pair.length() > idx + 1
                    ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                    : null;
            parameters.put(key, value);
        }
        return parameters;
    }

    private static String formatInitData(Map<String, String> initData) {
        final Map<String, String> sortedInitData = new TreeMap<>(initData);
        final StringBuilder builder = new StringBuilder();
        final Iterator<Map.Entry<String, String>> entriesIterator = sortedInitData.entrySet().iterator();
        while (entriesIterator.hasNext()) {
            final Map.Entry<String, String> entry = entriesIterator.next();
            builder.append(entry.getKey()).append("=").append(entry.getValue());
            if (entriesIterator.hasNext()) {
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    private static String formatInitData3rd(Map<String, String> initData, long botId) {
        return botId + ":WebAppData\n" + formatInitData(initData);
    }

    // =================================================================================================================
    // auth_date validation
    // =================================================================================================================
    private static long parseAuthDate(String authDate) {
        try {
            return Long.parseLong(authDate);
        } catch (NumberFormatException e) {
            throw new AuthDateInvalidException(authDate);
        }
    }

    private static void validateAuthDate(String authDate, Duration expiresIn, Clock clock) {
        if (authDate == null) {
            throw new AuthDateMissingException();
        }

        final Instant instantAuthDate = Instant.ofEpochSecond(parseAuthDate(authDate));
        final Instant instantNow = clock != null ? Instant.now(clock) : Instant.now();

        final Instant expiration = instantAuthDate.plus(expiresIn);
        if (instantNow.isAfter(expiration)) {
            throw new ExpiredException(instantAuthDate.getEpochSecond(), instantNow.getEpochSecond());
        }
    }

    // =================================================================================================================
    // misc methods
    // =================================================================================================================
    private static IllegalArgumentException buildExceptionArgumentNotProvided(String arg) {
        return new IllegalArgumentException("Argument \"" + arg + "\" is null or empty!");
    }
}
