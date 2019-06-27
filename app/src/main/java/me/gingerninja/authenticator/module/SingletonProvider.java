package me.gingerninja.authenticator.module;

import javax.inject.Provider;

public abstract class SingletonProvider<T> implements Provider<T> {
    protected T instance;

    protected abstract T create() throws RuntimeException;

    @Override
    public final synchronized T get() {
        if (instance == null) {
            instance = create();
        }

        return instance;
    }
}
