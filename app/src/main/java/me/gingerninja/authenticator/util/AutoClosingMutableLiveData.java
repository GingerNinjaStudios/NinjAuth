package me.gingerninja.authenticator.util;

import androidx.lifecycle.MutableLiveData;
import io.requery.sql.ResultSetIterator;

public class AutoClosingMutableLiveData<T> extends MutableLiveData<ResultSetIterator<T>> {
    @Override
    public void setValue(ResultSetIterator<T> value) {
        ResultSetIterator<T> oldValue = getValue();
        super.setValue(value);

        // closing the old iterator after every observer was notified
        if (oldValue != null) {
            oldValue.close();
        }
    }
}
