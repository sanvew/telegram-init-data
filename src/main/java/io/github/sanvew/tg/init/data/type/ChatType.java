package io.github.sanvew.tg.init.data.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Closed hierarchy describing chat types supported by Telegram init data.
 * <p>
 * Known types are exposed as singletons (mirroring the previous enum constants). Unknown values
 * produce dedicated {@link Unknown} instances whose equality/hashCode depend on the raw value,
 * so different non-standard inputs remain distinguishable.
 */
public abstract sealed class ChatType permits ChatType.Named, ChatType.Unknown {
    public static final ChatType SENDER = new Named("sender");
    public static final ChatType PRIVATE = new Named("private");
    public static final ChatType GROUP = new Named("group");
    public static final ChatType SUPERGROUP = new Named("supergroup");
    public static final ChatType CHANNEL = new Named("channel");
    private static final Map<String, ChatType> KNOWN_BY_VALUE = Map.of(
            SENDER.value(), SENDER,
            PRIVATE.value(), PRIVATE,
            GROUP.value(), GROUP,
            SUPERGROUP.value(),SUPERGROUP,
            CHANNEL.value(), CHANNEL
    );

    private final String value;

    private ChatType(@NotNull String value) {
        this.value = value;
    }

    public final @NotNull String value() {
        return value;
    }

    /**
     * Resolves the provided raw Telegram value to a {@link ChatType}.
     *
     * @param value raw chat type string; {@code null} yields {@code null}
     * @return matching singleton for known values, or an {@link Unknown} carrying the raw value
     */
    public static @Nullable ChatType fromValue(@Nullable String value) {
        if (value == null) { return null; }
        return KNOWN_BY_VALUE.getOrDefault(value, (ChatType) new Unknown(value));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(value, ((ChatType) o).value);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + value + "}";
    }

    static final class Named extends ChatType {
        private Named(String value) {
            super(value);
        }
    }

    public static final class Unknown extends ChatType {
        public Unknown(@Nullable String value) {
            super(value);
        }
    }
}
