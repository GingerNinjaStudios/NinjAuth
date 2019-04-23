package me.gingerninja.authenticator.util;

import androidx.lifecycle.MutableLiveData;

import timber.log.Timber;

public class AutoClosingMutableLiveData<T extends AutoCloseable> extends MutableLiveData<T> {
    @Override
    public void setValue(T value) {
        T oldValue = getValue();
        super.setValue(value);

        // closing the old iterator after every observer was notified
        if (oldValue != null) {
            try {
                oldValue.close();
            } catch (Exception e) {
                Timber.w(e, "Cannot close old resource: %s", e.getMessage());
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        T oldValue = getValue();

        // closing the old iterator after every observer was notified
        if (oldValue != null) {
            oldValue.close();
        }
    }
}
