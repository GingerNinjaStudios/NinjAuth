package me.gingerninja.authenticator.ui.backup;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.util.SingleEvent;

public class BackupViewModel extends ViewModel {
    public static final String EVENT_CREATE_BACKUP = "event.createBackup";

    public Data data = new Data();
    public Error error = new Error();

    private MutableLiveData<SingleEvent> events = new MutableLiveData<>();

    @NonNull
    private AccountRepository repository;

    @Inject
    public BackupViewModel(@NonNull AccountRepository repository) {
        this.repository = repository;
    }

    public LiveData<SingleEvent> getEvents() {
        return events;
    }

    public void onBackupClick(View v) {
        events.setValue(new SingleEvent(EVENT_CREATE_BACKUP));
    }

    public class Data {
        public final ObservableField<String> pass = new ObservableField<>();
        public final ObservableField<String> notes = new ObservableField<>();
    }

    public class Error {
        public final ObservableInt pass = new ObservableInt();
        public final ObservableInt notes = new ObservableInt();
    }
}
