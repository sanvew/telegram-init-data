package io.github.sanvew.tg.init.data.exception;

public class PropertyMissingException extends TelegramInitDataException {
    public PropertyMissingException(String arg, boolean defaultMessage) {
        super(defaultMessage ? "Property \"" + arg + "\" is missing" : arg);
    }

    public PropertyMissingException(String property) {
        this(property, true);
    }
}
