package me.gingerninja.authenticator.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SingleEvent<T> {
    @NonNull
    private final String id;

    @Nullable
    private final T content;

    private final Throwable throwable;

    private boolean handled;

    public SingleEvent(@NonNull String id) {
        this(id, (T) null);
    }

    public SingleEvent(@NonNull String id, @Nullable T content) {
        this.id = id;
        this.content = content;
        this.throwable = null;
    }

    public SingleEvent(@NonNull String id, @NonNull Throwable throwable) {
        this.id = id;
        this.content = null;
        this.throwable = throwable;
    }

    /**
     * Returns {@code true} and sets its handled flag if the event was not handled before;
     * {@code false} otherwise.
     *
     * @return {@code true} if the event was not handled before; {@code false} otherwise.
     */
    public boolean handle() {
        if (handled) {
            return false;
        }

        handled = true;
        return true;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <A extends T> A getContent() {
        return (A) content;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isSuccessful() {
        return throwable == null;
    }

    public boolean isHandled() {
        return handled;
    }
}
