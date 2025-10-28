package io.github.sanvew.tg.init.data.exception;

import io.github.sanvew.tg.init.data.type.InitData;

public class AuthDateInvalidException extends TelegramInitDataException {
    public AuthDateInvalidException(String invalidAuthDate) {
        super(InitData.Param.AUTH_DATE.value + " is invalid: " + invalidAuthDate);
    }
}
