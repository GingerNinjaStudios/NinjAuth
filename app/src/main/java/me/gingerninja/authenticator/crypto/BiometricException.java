package me.gingerninja.authenticator.crypto;

import androidx.annotation.Nullable;

public class BiometricException extends RuntimeException {
    public static final int ERROR_KEY_INVALIDATED = -1;
    public static final int ERROR_SHOULD_RETRY = -2;
    public static final int ERROR_KEY_SECURITY_UPDATE = -3;

    private final int errorCode;
    @Nullable
    private final CharSequence errString;

    BiometricException(int errorCode, @Nullable CharSequence errString) {
        this.errorCode = errorCode;
        this.errString = errString;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Nullable
    public CharSequence getErrorString() {
        return errString;
    }


    @Override
    public String getMessage() {
        return "code: " + errorCode + ", str: " + errString;
    }
}
