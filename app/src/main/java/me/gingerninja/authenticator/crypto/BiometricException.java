package me.gingerninja.authenticator.crypto;

import androidx.annotation.NonNull;

public class BiometricException extends RuntimeException {
    private final int errorCode;
    @NonNull
    private final CharSequence errString;

    BiometricException(int errorCode, @NonNull CharSequence errString) {
        this.errorCode = errorCode;
        this.errString = errString;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @NonNull
    public CharSequence getErrorString() {
        return errString;
    }


    @Override
    public String getMessage() {
        return "code: " + errorCode + ", str: " + errString;
    }
}
