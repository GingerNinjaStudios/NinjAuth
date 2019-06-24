package me.gingerninja.authenticator.ui.security;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import me.gingerninja.authenticator.util.SingleEvent;

public class PasswordSetViewModel extends ViewModel {
    public static final String EVENT_NEXT = "event.next";

    public ObservableField<String> password = new ObservableField<>("");
    public ObservableBoolean inputEnabled = new ObservableBoolean(true);

    public boolean usePin = false;

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    @Inject
    public PasswordSetViewModel() {
    }

    @NonNull
    public PasswordSetViewModel setUsePin(boolean usePin) {
        this.usePin = usePin;
        return this;
    }

    @NonNull
    public LiveData<SingleEvent> getEvents() {
        return events;
    }

    public void onNextClick(View v) {
        inputEnabled.set(false);
        events.setValue(new SingleEvent(EVENT_NEXT));
    }
}
