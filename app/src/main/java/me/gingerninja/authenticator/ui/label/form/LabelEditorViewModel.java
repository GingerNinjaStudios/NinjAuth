package me.gingerninja.authenticator.ui.label.form;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import javax.inject.Inject;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.ui.label.BaseLabelViewModel;
import me.gingerninja.authenticator.util.SingleEvent;

public class LabelEditorViewModel extends BaseLabelViewModel {
    public static final String NAV_ACTION_PICK_COLOR = "label.color_picker";
    public static final String NAV_ACTION_SAVE = "label.save";

    public static final int MODE_CREATE = 0;
    public static final int MODE_EDIT = 1;

    @IntDef({MODE_CREATE, MODE_EDIT})
    @interface Mode {
    }

    public Error error = new Error();
    private Disposable saveDisposable;

    protected MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();

    @Mode
    private int mode = MODE_CREATE;

    @Inject
    public LabelEditorViewModel(Application application, @NonNull AccountRepository accountRepo) {
        super(application, accountRepo);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (saveDisposable != null && !saveDisposable.isDisposed()) {
            saveDisposable.dispose();
        }
    }

    public int getMode() {
        return mode;
    }

    public LabelEditorViewModel setMode(@Mode int mode) {
        this.mode = mode;
        return this;
    }

    @ColorInt
    public int getColor() {
        return label.getColor();
    }

    public void setColor(@ColorInt int color) {
        label.setColor(color);
        data.color.set(color);
    }

    private boolean checkValues() {
        return ((EditableData) data).prepareAndCheckData(label, error);
    }

    public void onSaveClick(View view) {
        if (checkValues()) {
            saveDisposable = accountRepo
                    .addLabel(label)
                    .subscribe(label -> navAction.postValue(new SingleEvent<>(NAV_ACTION_SAVE, label.getName())));

        }
    }

    public void onPickColorClick(View view) {
        navAction.setValue(new SingleEvent<>(NAV_ACTION_PICK_COLOR));
    }

    public LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }

    @Override
    protected long getIdFromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            return 0;
        } else {
            return LabelEditorFragmentArgs.fromBundle(bundle).getId();
        }
    }

    @Override
    protected Data createData() {
        return new EditableData();
    }

    public static class Error {
        public ObservableInt name = new ObservableInt();
    }

    public static class EditableData extends BaseLabelViewModel.Data {
        private boolean prepareAndCheckData(@NonNull Label label, Error error) {
            final String rawTitle = trim(name.get());
            boolean hasError = !checkAndSetStringData(rawTitle, label::setName, error.name::set);
            return !hasError;
        }

        @Nullable
        private String trim(@Nullable String data) {
            if (data == null) {
                return null;
            } else {
                return data.trim();
            }
        }

        private boolean checkAndSetStringData(String value, Function<String, Label> function, Consumer<Integer> errorFunction) {
            try {
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(value.trim())) {
                    function.apply(value.trim());
                    errorFunction.accept(0);
                    return true;
                } else {
                    errorFunction.accept(R.string.error_field_empty);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
