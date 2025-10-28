package io.github.sanvew.tg.init.data.json.parser.exception;

import io.github.sanvew.tg.init.data.exception.TelegramInitDataException;

public class JsonParseException extends TelegramInitDataException {
    public JsonParseException(Class<?> clazz, Exception e) {
        super("Unable to parse: " + clazz, e);
    }
}
