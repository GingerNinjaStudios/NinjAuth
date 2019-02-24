package me.gingerninja.authenticator.util.resulthandler;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

public class FragmentResultViewModel extends ViewModel {
    private ResultStore resultStore;

    FragmentResultViewModel(@NonNull ResultStore resultStore) {
        this.resultStore = resultStore;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        resultStore.clear();
        resultStore = null;
    }

    void addPendingResultRequest(@NonNull String who, int requestCode) {
        resultStore.addPendingResultRequest(who, requestCode);
    }

    @Nullable
    ResultStore.Result getResult(@NonNull String who) {
        return resultStore.getResult(who);
    }

    void setResult(@NonNull String who, int resultCode, @Nullable Object data) {
        resultStore.setResult(who, resultCode, data);
    }
}
