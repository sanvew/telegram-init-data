package io.github.sanvew.tg.init.data.exception;

public class TelegramInitDataException extends RuntimeException {
    public TelegramInitDataException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public TelegramInitDataException(String message) {
        this(message, null);
    }

    public TelegramInitDataException(Throwable throwable) {
        this(null, throwable);
    }
}
