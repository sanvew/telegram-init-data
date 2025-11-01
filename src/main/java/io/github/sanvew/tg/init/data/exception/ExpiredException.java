package io.github.sanvew.tg.init.data.exception;

import io.github.sanvew.tg.init.data.type.InitData;

public class ExpiredException extends TelegramInitDataException {
    public ExpiredException(long expiresAt, long now) {
        super("initData is expired, " + InitData.Param.AUTH_DATE.value + " expires at " + expiresAt + " but now is " + now);
    }
}
