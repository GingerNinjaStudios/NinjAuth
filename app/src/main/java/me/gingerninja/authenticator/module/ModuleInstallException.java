package me.gingerninja.authenticator.module;

import com.google.android.play.core.splitinstall.SplitInstallSessionState;

import androidx.annotation.NonNull;

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
