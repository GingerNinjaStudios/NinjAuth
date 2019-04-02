package me.gingerninja.authenticator.ui.backup;

import androidx.annotation.NonNull;
import io.reactivex.Completable;

public interface WorkUpdateHandler {
    /**
     * Handles a database request, such as updating the restore mode or should-restore.
     *
     * @param completable the completable to handle
     */
    void handleRequest(@NonNull Completable completable);
}
