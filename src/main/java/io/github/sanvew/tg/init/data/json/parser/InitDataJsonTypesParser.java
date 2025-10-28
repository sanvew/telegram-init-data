package io.github.sanvew.tg.init.data.json.parser;

import org.jetbrains.annotations.Nullable;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonParseException;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonPropertyMissingException;
import io.github.sanvew.tg.init.data.type.Chat;
import io.github.sanvew.tg.init.data.type.User;
/**
 * Strategy interface for parsing structured fields in Telegram Mini App init data.
 * <p>
 * Telegram init data may contain nested JSON strings for fields like {@code user}, {@code chat}, and {@code receiver}.
 * This interface allows for custom implementations (e.g. using Jackson, Gson, etc.) to convert those raw strings into typed Java objects.
 * <p>
 * Default implementation is {@link io.github.sanvew.tg.init.data.json.parser.impl.JacksonInitDataJsonTypesParser}.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data">Telegram Init Data Documentation</a>
 */
public interface InitDataJsonTypesParser {
    /**
     * Parses a raw JSON string from the {@code user} field into a {@link io.github.sanvew.tg.init.data.type.User} object.
     *
     * @param input the raw JSON string
     * @return parsed {@code User} object, or {@code null} if the input is null
     * @throws JsonParseException if the input is malformed
     * @throws JsonPropertyMissingException if required fields are missing
     */
    @Nullable User parseUser(@Nullable String input) throws JsonParseException, JsonPropertyMissingException;

    /**
     * Parses a raw JSON string from the {@code chat} field into a {@link io.github.sanvew.tg.init.data.type.Chat} object.
     *
     * @param input the raw JSON string
     * @return parsed {@code Chat} object, or {@code null}
     * @throws JsonParseException if the input is malformed
     * @throws JsonPropertyMissingException if required fields are missing
     */
    @Nullable Chat parseChat(@Nullable String input) throws JsonParseException, JsonPropertyMissingException;
}
