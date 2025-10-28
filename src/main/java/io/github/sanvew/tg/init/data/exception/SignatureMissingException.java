package io.github.sanvew.tg.init.data.exception;

import io.github.sanvew.tg.init.data.type.InitData;

public class SignatureMissingException extends PropertyMissingException {
    private SignatureMissingException(String arg) {
        super(arg);
    }

    public static SignatureMissingException ofHash() {
        return new SignatureMissingException(InitData.Param.HASH.value);
    }

    public static SignatureMissingException ofSignature() {
        return new SignatureMissingException(InitData.Param.SIGNATURE.value);
    }
}
