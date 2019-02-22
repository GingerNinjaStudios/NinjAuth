package me.gingerninja.authenticator.util.resulthandler;

import android.app.Activity;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import timber.log.Timber;

class ResultStore {
    private Map<String, Result> pendingResults = new HashMap<>();
    private SparseArrayCompat<Result> pendingResultsByDestination = new SparseArrayCompat<>();

    void clear() {
        pendingResults.clear();
        pendingResultsByDestination.clear();
    }

    void addPendingResultRequest(@NonNull String who, int requestCode) {
        Timber.v("Adding pending result request for %s", who);
        pendingResults.put(who, new Result(requestCode));
    }

    void remove(@NonNull String who) {
        pendingResults.remove(who);
    }

    void remove(@IdRes int destinationId) {
        pendingResultsByDestination.remove(destinationId);
    }

    @Nullable
    Result getResult(@NonNull String who) {
        Result result = pendingResults.get(who);

        if (result != null) {
            /*if (result.isPending()) {
                return null;
            } else {*/
            pendingResults.remove(who);
            //}
        }

        return result;
    }

    @Nullable
    Result getResult(@IdRes int destinationId) {
        return pendingResultsByDestination.get(destinationId);
    }

    void setResult(@NonNull String who, int resultCode, @Nullable Intent data) {
        Timber.v("Set result for %s: resultCode: %d", who, resultCode);
        Result result = pendingResults.get(who);

        if (result != null) {
            result.setResults(resultCode, data);
        }
    }

    void setResult(@IdRes int destinationId, int resultCode, @Nullable Intent data) {
        Timber.v("Set result for %d: resultCode: %d", destinationId, resultCode);
        Result result = pendingResultsByDestination.get(destinationId);

        if (result != null) {
            result.setResults(resultCode, data);
        }
    }

    static class Result {
        private final int requestCode;
        private int resultCode = Activity.RESULT_CANCELED;
        private boolean pending = true;

        @Nullable
        private Intent data;

        Result(int requestCode) {
            this.requestCode = requestCode;
        }

        int getRequestCode() {
            return requestCode;
        }

        int getResultCode() {
            return resultCode;
        }

        @Nullable
        Intent getData() {
            return data;
        }

        Result setResults(int resultCode, @Nullable Intent data) {
            this.resultCode = resultCode;
            this.data = data;
            pending = false;
            return this;
        }

        boolean isPending() {
            return pending;
        }
    }
}