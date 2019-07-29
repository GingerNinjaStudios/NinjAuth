package me.gingerninja.authenticator.ui.setup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;

public class SetupCompleteViewModel extends ViewModel {
    @NonNull
    private final DatabaseHandler dbHandler;

    @NonNull
    private final Crypto crypto;

    @Inject
    SetupCompleteViewModel(@NonNull Crypto crypto, @NonNull DatabaseHandler dbHandler) {
        this.crypto = crypto;
        this.dbHandler = dbHandler;

        openUnlockedDatabase();
    }

    void openUnlockedDatabase() {
        if (!crypto.hasLock()) {
            dbHandler.openDatabaseDefaultPassword();
        }
    }
}
