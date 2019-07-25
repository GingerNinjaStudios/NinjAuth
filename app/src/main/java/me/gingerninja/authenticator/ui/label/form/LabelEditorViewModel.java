package me.gingerninja.authenticator.ui.label.form;

import android.app.Application;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import me.gingerninja.authenticator.ui.label.BaseLabelViewModel;
import me.gingerninja.authenticator.util.SingleEvent;
import me.gingerninja.authenticator.util.validator.Validator;

public class LabelEditorViewModel extends BaseLabelViewModel {
    public static final String NAV_ACTION_PICK_COLOR = "label.color_picker";
    public static final String NAV_ACTION_PICK_ICON = "label.icon_picker";
    public static final String NAV_ACTION_SAVE = "label.save";

    public static final int MODE_CREATE = 0;
    public static final int MODE_EDIT = 1;
    public Error error = new Error();
    protected MutableLiveData<SingleEvent<Label>> navAction = new MutableLiveData<>();
    private Disposable saveDisposable;
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

    @Mode
    public int getMode() {
        return mode;
    }

    public LabelEditorViewModel setMode(@Mode int mode) {
        this.mode = mode;
        return this;
    }

    @ColorInt
    public int getColor() {
        return data.color.get();
    }

    public void setColor(@ColorInt int color) {
        data.color.set(color);
    }

    @Nullable
    public String getIcon() {
        return data.icon.get();
    }

    public void setIcon(@Nullable String id) {
        data.icon.set(id);
        prepareIcon();
    }

    private boolean checkValues() {
        return ((EditableData) data).prepareAndCheckData(label, error);
    }

    public void onSaveClick(View view) {
        if (checkValues()) {
            saveDisposable = accountRepo
                    .addLabel(label)
                    .subscribe(label -> navAction.postValue(new SingleEvent<>(NAV_ACTION_SAVE, label)));

        }
    }

    public void onPickColorClick(View view) {
        navAction.setValue(new SingleEvent<>(NAV_ACTION_PICK_COLOR));
    }

    public void onPickIconClick(View view) {
        navAction.setValue(new SingleEvent<>(NAV_ACTION_PICK_ICON));
    }

    LiveData<SingleEvent<Label>> getNavigationAction() {
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

    @IntDef({MODE_CREATE, MODE_EDIT})
    @interface Mode {
    }

    public static class Error {
        public ObservableInt name = new ObservableInt();
    }

    public static class EditableData extends BaseLabelViewModel.Data {
        private boolean prepareAndCheckData(@NonNull Label label, Error error) {
            boolean success = Validator.from(name.get(), label::setName, error.name::set)
                    .notNull(R.string.error_field_empty)
                    .process(String::trim)
                    .test(input -> !input.isEmpty(), R.string.error_field_empty)
                    .done();

            if (success) {
                label.setColor(color.get());
                label.setIcon(icon.get());
            }

            return success;
        }
    }
}
