package me.gingerninja.authenticator.ui.security;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import me.gingerninja.authenticator.util.SingleEvent;

public class BiometricsSetViewModel extends ViewModel {
    static final String EVENT_ENABLE = "event.enable";
    static final String EVENT_DISABLE = "event.disable";
    static final String EVENT_SKIP = "event.skip";

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    public boolean isBioEnabled = false;

    @Inject
    public BiometricsSetViewModel() {
    }

    public void setBioEnabled(boolean bioEnabled) {
        isBioEnabled = bioEnabled;
    }

    public void onAuthClick(View v) {
        if (isBioEnabled) {
            events.setValue(new SingleEvent(EVENT_DISABLE));
            // TODO delete bio auth key
        } else {
            events.setValue(new SingleEvent(EVENT_ENABLE));
            // TODO create bio auth key
        }
    }

    public void onSkipClick(View v) {
        events.setValue(new SingleEvent(EVENT_SKIP));
    }

    public LiveData<SingleEvent> getEvents() {
        return events;
    }
}
