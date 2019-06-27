package me.gingerninja.authenticator.module;

import androidx.annotation.Nullable;

interface DynamicModule<T> {
    @Nullable
    T getDelegate();

    void setDelegate(@Nullable T delegate);
}
