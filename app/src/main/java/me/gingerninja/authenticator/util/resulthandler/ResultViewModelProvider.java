package me.gingerninja.authenticator.util.resulthandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreAccessor;

public class ResultViewModelProvider extends ViewModelProvider {
    private static final String DEFAULT_KEY = "androidx.lifecycle.ViewModelProvider.DefaultKey";

    private final ViewModelStore viewModelStore;

    @NonNull
    private final DisablerFactory factory;

    public static ResultViewModelProvider of(FragmentActivity activity) {
        return new ResultViewModelProvider(activity.getViewModelStore(), DisablerFactory.getInstance());
    }

    public static ResultViewModelProvider of(Fragment fragment) {
        return new ResultViewModelProvider(fragment.getViewModelStore(), DisablerFactory.getInstance());
    }

    private ResultViewModelProvider(@NonNull ViewModelStore store, @NonNull DisablerFactory factory) {
        super(store, factory);
        this.factory = factory;
        this.viewModelStore = store;
    }

    @Nullable
    public <T extends ViewModel> T getIfExists(@NonNull Class<T> modelClass) {
        String canonicalName = modelClass.getCanonicalName();
        if (canonicalName == null) {
            throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
        }

        return getIfExists(DEFAULT_KEY + ":" + canonicalName, modelClass);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends ViewModel> T getIfExists(@NonNull String key, @NonNull Class<T> modelClass) {
        ViewModel viewModel = ViewModelStoreAccessor.get(viewModelStore, key);

        if (modelClass.isInstance(viewModel)) {
            return (T) viewModel;
        }

        return null;
        /*factory.setCreatorModeDisabled(true);
        T viewModel = super.get(key, modelClass);
        factory.setCreatorModeDisabled(false);
        return viewModel;*/
    }

    static class DisablerFactory implements Factory {
        private static DisablerFactory instance;
        private ResultStore resultStore;

        private boolean creatorModeDisabled;

        private DisablerFactory() {
            resultStore = new ResultStore();
        }

        public static DisablerFactory getInstance() {
            if (instance == null) {
                instance = new DisablerFactory();
            }
            return instance;
        }

        void setCreatorModeDisabled(boolean disabled) {
            this.creatorModeDisabled = disabled;
        }

        @SuppressWarnings({"ConstantConditions", "unchecked", "NullableProblems"})
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (creatorModeDisabled) {
                return null;
            }

            if (modelClass.isAssignableFrom(FragmentInstanceViewModel.class)) {
                return (T) new FragmentInstanceViewModel(resultStore);
            } else if (modelClass.isAssignableFrom(FragmentResultViewModel.class)) {
                return (T) new FragmentResultViewModel(resultStore);
            }
            return null;
        }
    }
}
