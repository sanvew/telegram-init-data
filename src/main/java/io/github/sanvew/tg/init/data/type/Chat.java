package io.github.sanvew.tg.init.data.type;

import io.github.sanvew.tg.init.data.exception.PropertyMissingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Telegram Chat object as received from the init data payload.
 * <p>
 * Supports known fields such as {@code id}, {@code type}, {@code title}, {@code photo_url}, and {@code username},
 * and exposes every populated property via {@link #getProperties()}.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#chat">Telegram Mini Apps Init Data: Chat</a>
 */
public class Chat {
    public enum Property {
        ID("id"),
        TYPE("type"),
        TITLE("title"),
        PHOTO_URL("photo_url"),
        USERNAME("username"),
        ;

        private static final Set<String> KNOWN = Arrays.stream(Property.values())
                .map(it -> it.value)
                .collect(Collectors.toSet());

        public final String value;

        Property(String value) {
            this.value = value;
        }

        public static boolean isNotKnown(String property) {
            return !KNOWN.contains(property);
        }
    }

    private final Map<String, Object> properties;
    private final long id;
    private final ChatType type;
    private final String title;
    private final URI photoUrl;
    private final String username;

    public Chat(
            long id,
            @NotNull ChatType type,
            @NotNull String title,
            @Nullable URI photoUrl,
            @Nullable String username,
            @Nullable Map<String, String> extra
    ) {
        final Map<String, Object> props = new HashMap<>();

        this.id = id;
        props.put(Property.ID.value, this.id);

        if (type == null) { throw new PropertyMissingException("type"); }
        this.type = type;
        props.put(Property.TYPE.value, this.type);

        if (title == null) { throw new PropertyMissingException("title"); }
        this.title = title;
        props.put(Property.TITLE.value, this.title);

        this.photoUrl = photoUrl;
        if (photoUrl != null) { props.put(Property.PHOTO_URL.value, this.photoUrl); }

        this.username = username;
        if (username != null) { props.put(Property.USERNAME.value, this.username); }

        if (extra != null) {
            for (final Map.Entry<String, String> entry: extra.entrySet()) {
                if (Property.isNotKnown(entry.getKey()) && entry.getValue() != null) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.properties = Collections.unmodifiableMap(props);
    }

    public Chat(
            long id,
            @NotNull ChatType type,
            @NotNull String title,
            @Nullable URI photoUrl,
            @Nullable String username
    ) {
        this(id, type, title, photoUrl, username, null);
    }

    public @NotNull Map<String, Object> getProperties() { return properties; }
    public long getId() {return id; }
    public @NotNull ChatType getType() {return type; }
    public @NotNull String getTitle() { return title; }
    public @Nullable URI getPhotoUrl() { return photoUrl; }
    public @Nullable String getUsername() { return username; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chat)) return false;
        return this.properties.equals(((Chat) o).properties);
    }

    @Override
    public int hashCode() {
        return this.properties.hashCode();
    }

    @Override
    public String toString() {
        return this.properties.toString();
    }
}
