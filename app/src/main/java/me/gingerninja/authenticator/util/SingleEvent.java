package me.gingerninja.authenticator.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SingleEvent<T> {
    @NonNull
    private T content;

    private boolean handled;

    public SingleEvent(@NonNull T content) {
        this.content = content;
    }

    @Nullable
    public <A extends T> T getContentAndMarkHandled() {
        return getContentAndMarkHandled(null);
    }

    @Nullable
    public <A extends T> T getContentAndMarkHandled(A defaultValue) {
        if (!handled) {
            handled = true;
            return content;
        } else {
            return defaultValue;
        }
    }

    @NonNull
    public T peekContent() {
        return content;
    }

    public boolean isHandled() {
        return handled;
    }
}
