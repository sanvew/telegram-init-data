package io.github.sanvew.tg.init.data.type;

import io.github.sanvew.tg.init.data.exception.PropertyMissingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the full set of Telegram Init Data parameters passed to a Web App.
 * <p>
 * This class models all known fields provided by Telegram, including both required and optional values.
 * It is intended to be used for parsing, validating, and working with authenticated init data payloads.
 *
 * @see <a href="https://docs.telegram-mini-apps.com/platform/init-data#parameters-list">Telegram Init Data – Parameters List</a>
 */
public class InitData {
    public enum Param {
        AUTH_DATE("auth_date"),
        CAN_SEND_AFTER("can_send_after"),
        CHAT("chat"),
        CHAT_TYPE("chat_type"),
        CHAT_INSTANCE("chat_instance"),
        HASH("hash"),
        SIGNATURE("signature"),
        QUERY_ID("query_id"),
        RECEIVER("receiver"),
        START_PARAM("start_param"),
        USER("user"),
        ;

        private static final Set<String> KNOWN = Arrays.stream(Param.values())
                .map(it -> it.value)
                .collect(Collectors.toSet());

        public final String value;

        Param(String value) {
            this.value = value;
        }

        static boolean isNotKnown(String property) {
            return !KNOWN.contains(property);
        }
    }

    private final Map<String, Object> properties;
    private final long authDate;
    private final Long canSendAfter;
    private final Chat chat;
    private final ChatType chatType;
    private final String chatInstance;
    private final String hash;
    private final String signature;
    private final String queryId;
    private final User receiver;
    private final String startParam;
    private final User user;

    public InitData(
            long authDate,
            @Nullable Long canSendAfter,
            @Nullable Chat chat,
            @Nullable ChatType chatType,
            @Nullable String chatInstance,
            @NotNull String hash,
            @Nullable String signature,
            @Nullable String queryId,
            @Nullable User receiver,
            @Nullable String startParam,
            @Nullable User user,
            @Nullable Map<String, String> extra
    ) {
        final Map<String, Object> props = new HashMap<>();

        this.authDate = authDate;
        props.put(Param.AUTH_DATE.value, this.authDate);

        this.canSendAfter = canSendAfter;
        if (canSendAfter != null ) {
            props.put(Param.CAN_SEND_AFTER.value, this.canSendAfter);
        }

        this.chat = chat;
        if (chat != null ) {
            props.put(Param.CHAT.value, this.chat);
        }

        this.chatType = chatType;
        if (chatType != null ) {
            props.put(Param.CHAT_TYPE.value, this.chatType);
        }

        this.chatInstance = chatInstance;
        if (chatInstance != null ) {
            props.put(Param.CHAT_INSTANCE.value, this.chatInstance);
        }

        if (hash == null) {throw new PropertyMissingException("hash"); }
        this.hash = hash;
        props.put(Param.HASH.value, this.hash);

        this.signature = signature;
        if (signature != null ) {
            props.put(Param.SIGNATURE.value, this.signature);
        }

        this.queryId = queryId;
        if (queryId != null ) {
            props.put(Param.QUERY_ID.value, this.queryId);
        }

        this.receiver = receiver;
        if (receiver != null ) {
            props.put(Param.RECEIVER.value, this.receiver);
        }

        this.startParam = startParam;
        if (startParam != null ) {
            props.put(Param.START_PARAM.value, this.startParam);
        }

        this.user = user;
        if (user != null ) {
            props.put(Param.USER.value, this.user);
        }

        if (extra != null) {
            for (final Map.Entry<String, String> entry: extra.entrySet()) {
                if (Param.isNotKnown(entry.getKey()) && entry.getValue() != null) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.properties = Collections.unmodifiableMap(props);
    }

    public InitData(
            long authDate,
            @Nullable Long canSendAfter,
            @Nullable Chat chat,
            @Nullable ChatType chatType,
            @Nullable String chatInstance,
            @NotNull String hash,
            @Nullable String signature,
            @Nullable String queryId,
            @Nullable User receiver,
            @Nullable String startParam,
            @Nullable User user
    ) {
        this(
                authDate, canSendAfter, chat, chatType, chatInstance, hash, signature, queryId, receiver, startParam,
                user, null
        );
    }

    public @NotNull Map<String, Object> getProperties() { return properties; }
    public long getAuthDate() { return authDate; }
    public @Nullable Long getCanSendAfter() { return canSendAfter; }
    public @Nullable Chat getChat() { return chat; }
    public @Nullable ChatType getChatType() { return chatType; }
    public @Nullable String getChatInstance() { return chatInstance; }
    public @NotNull String getHash() { return hash; }
    public @Nullable String getSignature() { return signature; }
    public @Nullable String getQueryId() { return queryId; }
    public @Nullable User getReceiver() { return receiver; }
    public @Nullable String getStartParam() { return startParam; }
    public @Nullable User getUser() { return user; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InitData)) return false;
        return this.properties.equals(((InitData) o).properties);
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
