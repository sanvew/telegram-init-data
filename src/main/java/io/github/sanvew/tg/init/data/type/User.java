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
 * Represents a Telegram User object as received from the init data payload.
 * <p>
 * Supports known fields such as {@code added_to_attachment_menu}, {@code allows_write_to_pm}, {@code is_premium},
 * {@code first_name}, {@code id}, {@code is_bot}, {@code last_name}, {@code language_code}, {@code photo_url} and {@code username},
 * and exposes every populated property via {@link #getProperties()}.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#user">Telegram Mini Apps Init Data: Chat</a>
 */
public class User {
    public enum Property {
        ADDED_TO_ATTACHMENT_MENU("added_to_attachment_menu"),
        ALLOWS_WRITE_TO_PM ("allows_write_to_pm"),
        IS_PREMIUM("is_premium"),
        FIRST_NAME("first_name"),
        ID("id"),
        IS_BOT("is_bot"),
        LAST_NAME("last_name"),
        LANGUAGE_CODE("language_code"),
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
    private final Boolean addedToAttachmentMenu;
    private final Boolean allowsWriteToPm;
    private final Boolean isPremium;
    private final String firstName;
    private final long id;
    private final Boolean isBot;
    private final String lastName;
    private final String languageCode;
    private final URI photoUrl;
    private final String username;

    public User(
            @Nullable Boolean addedToAttachmentMenu,
            @Nullable Boolean allowsWriteToPm,
            @Nullable Boolean isPremium,
            @NotNull String firstName,
            long id,
            @Nullable Boolean isBot,
            @Nullable String lastName,
            @Nullable String languageCode,
            @Nullable URI photoUrl,
            @Nullable String username,
            @Nullable Map<String, String> extra
    ) {
        final Map<String, Object> props = new HashMap<>();

        this.addedToAttachmentMenu = addedToAttachmentMenu;
        if (addedToAttachmentMenu != null) {
            props.put(Property.ADDED_TO_ATTACHMENT_MENU.value, this.addedToAttachmentMenu);
        }

        this.allowsWriteToPm = allowsWriteToPm;
        if (allowsWriteToPm != null) {
            props.put(Property.ALLOWS_WRITE_TO_PM.value, this.allowsWriteToPm);
        }

        this.isPremium = isPremium;
        if (isPremium != null) {
            props.put(Property.IS_PREMIUM.value, this.isPremium);
        }

        if (firstName == null) {
            throw new PropertyMissingException("firstName");
        }
        this.firstName = firstName;
        props.put(Property.FIRST_NAME.value, this.firstName);

        this.id = id;
        props.put(Property.ID.value, this.id);

        this.isBot = isBot;
        if (isBot != null) {
            props.put(Property.IS_BOT.value, this.isBot);
        }

        this.lastName = lastName;
        if (lastName  != null) {
            props.put(Property.LAST_NAME.value, this.lastName);
        }

        this.languageCode = languageCode;
        if (languageCode != null) {
            props.put(Property.LANGUAGE_CODE.value, this.languageCode);
        }

        this.photoUrl = photoUrl;
        if (photoUrl != null) {
            props.put(Property.PHOTO_URL.value, this.photoUrl);
        }

        this.username = username;
        if (username != null) {
            props.put(Property.USERNAME.value, this.username);
        }

        if (extra != null) {
            for (final Map.Entry<String, String> entry: extra.entrySet()) {
                if (Property.isNotKnown(entry.getKey()) && entry.getValue() != null) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.properties = Collections.unmodifiableMap(props);
    }

    public User(
            @Nullable Boolean addedToAttachmentMenu,
            @Nullable Boolean allowsWriteToPm,
            @Nullable Boolean isPremium,
            @NotNull String firstName,
            long id,
            @Nullable Boolean isBot,
            @Nullable String lastName,
            @Nullable String languageCode,
            @Nullable URI photoUrl,
            @Nullable String username
    ) {
        this(
                addedToAttachmentMenu, allowsWriteToPm, isPremium, firstName, id, isBot, lastName, languageCode,
                photoUrl, username, null
        );
    }

    public @NotNull Map<String, Object> getProperties() {
        return properties;
    }
    public @Nullable Boolean isAddedToAttachmentMenu() {
        return addedToAttachmentMenu;
    }
    public @Nullable Boolean allowsWriteToPm() {
        return allowsWriteToPm;
    }
    public @Nullable Boolean isPremium() {
        return isPremium;
    }
    public @NotNull String getFirstName() {
        return firstName;
    }
    public long getId() {
        return id;
    }
    public @Nullable Boolean isBot() {
        return isBot;
    }
    public @Nullable String getLastName() {
        return lastName;
    }
    public @Nullable String getLanguageCode() {
        return languageCode;
    }
    public @Nullable URI getPhotoUrl() {
        return photoUrl;
    }
    public @Nullable String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        return this.properties.equals(((User) o).properties);
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