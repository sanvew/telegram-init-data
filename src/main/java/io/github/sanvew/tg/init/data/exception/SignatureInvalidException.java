package io.github.sanvew.tg.init.data.exception;

public class SignatureInvalidException extends TelegramInitDataException {
    private SignatureInvalidException(String message) {
        super(message);
    }

    public static SignatureInvalidException ofHash() {
        return new SignatureInvalidException("The calculated initData hash does not match provided!");
    }

    public static SignatureInvalidException ofSignature() {
        return new SignatureInvalidException("Signature not verified!");
    }

}
