package me.gingerninja.authenticator.util.resulthandler;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import java.util.UUID;

public class FragmentInstanceViewModel extends ViewModel {
    final String who = UUID.randomUUID().toString();

    private ResultStore resultStore;

    FragmentInstanceViewModel(@NonNull ResultStore resultStore) {
        this.resultStore = resultStore;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        resultStore.remove(who);
        resultStore = null;
    }

    @NonNull
    @Override
    public String toString() {
        return "FragmentInstanceViewModel [who=" + who + "]";
    }
}
