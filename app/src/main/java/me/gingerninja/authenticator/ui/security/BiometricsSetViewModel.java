package me.gingerninja.authenticator.ui.security;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import me.gingerninja.authenticator.util.SingleEvent;

public class BiometricsSetViewModel extends ViewModel {
    static final String EVENT_AUTH = "event.auth";
    static final String EVENT_SKIP = "event.skip";

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    @Inject
    public BiometricsSetViewModel() {
    }

    public void onAuthClick(View v) {
        events.setValue(new SingleEvent(EVENT_AUTH));
    }

    public void onSkipClick(View v) {
        events.setValue(new SingleEvent(EVENT_SKIP));
    }

    public LiveData<SingleEvent> getEvents() {
        return events;
    }
}
