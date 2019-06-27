package me.gingerninja.authenticator.module;

import androidx.annotation.Nullable;

public abstract class BaseDynamicModule<T> implements DynamicModule<T> {
    @Nullable
    protected T delegate;

    @Override
    @Nullable
    public T getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(@Nullable T delegate) {
        this.delegate = delegate;
    }

}
