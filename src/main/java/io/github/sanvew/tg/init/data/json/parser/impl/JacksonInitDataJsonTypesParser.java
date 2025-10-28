package io.github.sanvew.tg.init.data.json.parser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.sanvew.tg.init.data.type.ChatType;
import org.jetbrains.annotations.Nullable;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonParseException;
import io.github.sanvew.tg.init.data.json.parser.exception.JsonPropertyMissingException;
import io.github.sanvew.tg.init.data.json.parser.InitDataJsonTypesParser;
import io.github.sanvew.tg.init.data.type.Chat;
import io.github.sanvew.tg.init.data.type.User;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class JacksonInitDataJsonTypesParser implements InitDataJsonTypesParser {
    public static final InitDataJsonTypesParser INSTANCE = new JacksonInitDataJsonTypesParser();

    private final ObjectMapper objectMapper;

    public JacksonInitDataJsonTypesParser() {
        final Map<Class<?>, JsonDeserializer<?>> deserializer = Map.of(
                Chat.class, new ChatDeserializer(),
                User.class, new UserDeserializer()
        );
        this.objectMapper = new ObjectMapper()
                .registerModule(new SimpleModule(
                        "SimpleModule-" + getClass().getName(), Version.unknownVersion(), deserializer
                ));
    }

    @Override
    public @Nullable User parseUser(@Nullable String input) {
        if (input == null || input.isBlank()) { return null; }
        try {
            return this.objectMapper.readValue(input, User.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new JsonParseException(User.class, e);
        }
    }

    @Override
    public @Nullable Chat parseChat(@Nullable String input) {
        if (input == null || input.isBlank()) { return null; }
        try {
            return this.objectMapper.readValue(input, Chat.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new JsonParseException(Chat.class, e);
        }
    }
}

class ChatDeserializer extends StdDeserializer<Chat> {
    public ChatDeserializer() { super((Class<?>) null); }

    @Override
    public Chat deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final long id = node.optional(Chat.Property.ID.value).map(JsonNode::asLong)
                .orElseThrow(() -> new JsonPropertyMissingException(Chat.class, Chat.Property.ID.value));
        final ChatType type = node.optional(Chat.Property.TYPE.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(it -> ChatType.fromValue(it.asText()))
                .orElseThrow(() -> new JsonPropertyMissingException(Chat.class, Chat.Property.TYPE.value));
        final String title = node.optional(Chat.Property.TITLE.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .orElseThrow(() -> new JsonPropertyMissingException(Chat.class, Chat.Property.TITLE.value));
        final URI photoUrl = node.optional(Chat.Property.PHOTO_URL.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .map(URI::create)
                .orElse(null);
        final String username = node.optional(Chat.Property.USERNAME.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .orElse(null);

        final Map<String, String> extra = node.propertyStream()
                .filter(it -> !it.getValue().isNull() && Chat.Property.isNotKnown(it.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().asText()));

        return new Chat(id, type, title, photoUrl, username, extra);
    }
}

class UserDeserializer extends StdDeserializer<User> {
    public UserDeserializer() { super((Class<?>) null); }

    @Override
    public User deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final long id = node.optional(User.Property.ID.value).map(JsonNode::asLong)
                .orElseThrow(() -> new JsonPropertyMissingException(User.class, User.Property.ID.value));
        final String firstName = node.optional(User.Property.FIRST_NAME.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .orElseThrow(() -> new JsonPropertyMissingException(User.class, User.Property.FIRST_NAME.value));
        final Boolean isBot = node.optional(User.Property.IS_BOT.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asBoolean)
                .orElse(null);
        final String lastName = node.optional(User.Property.LAST_NAME.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .orElse(null);
        final String username = node.optional(User.Property.USERNAME.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .orElse(null);
        final String languageCode = node.optional(User.Property.LANGUAGE_CODE.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .orElse(null);
        final Boolean isPremium = node.optional(User.Property.IS_PREMIUM.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asBoolean)
                .orElse(null);
        final Boolean allowsWriteToPm = node.optional(User.Property.ALLOWS_WRITE_TO_PM.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asBoolean)
                .orElse(null);
        final Boolean addedToAttachmentMenu = node.optional(User.Property.ADDED_TO_ATTACHMENT_MENU.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asBoolean)
                .orElse(null);
        final URI photoUrl = node.optional(User.Property.PHOTO_URL.value)
                .filter(Predicate.not(JsonNode::isNull))
                .map(JsonNode::asText)
                .map(URI::create)
                .orElse(null);

        final Map<String, String> extra = node.propertyStream()
                .filter(it -> !it.getValue().isNull() && User.Property.isNotKnown(it.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().asText()));

        return new User(
                addedToAttachmentMenu, allowsWriteToPm, isPremium, firstName, id, isBot, lastName, languageCode,
                photoUrl, username, extra
        );
    }
}
