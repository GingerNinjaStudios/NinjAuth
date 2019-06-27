package me.gingerninja.authenticator.module;

import androidx.annotation.NonNull;

import com.google.android.play.core.splitinstall.SplitInstallSessionState;

public class ModuleInstallException extends RuntimeException {
    @NonNull
    private final SplitInstallSessionState sessionState;

    ModuleInstallException(@NonNull SplitInstallSessionState sessionState) {
        this.sessionState = sessionState;
    }

    @NonNull
    public SplitInstallSessionState getSessionState() {
        return sessionState;
    }
}
