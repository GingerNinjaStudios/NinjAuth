package me.gingerninja.authenticator.ui.label;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.db.entity.Label;
import me.gingerninja.authenticator.data.repo.AccountRepository;
import timber.log.Timber;

public abstract class BaseLabelViewModel extends ViewModel {
    public final ObservableBoolean hasLoaded = new ObservableBoolean(false);
    protected final Application application;
    protected final AccountRepository accountRepo;
    public Data data;
    protected Label label;
    private Disposable disposable;

    public ObservableField<Drawable> iconDrawable = new ObservableField<>();

    public BaseLabelViewModel(Application application, @NonNull AccountRepository accountRepo) {
        this.application = application;
        this.accountRepo = accountRepo;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void init(@Nullable Bundle bundle) {
        if (label != null) {
            return;
        }

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        if (data == null) {
            data = createData();
        }

        disposable = getLabel(getIdFromBundle(bundle))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(labelEntity -> {
                    label = labelEntity;
                    initFieldsFromLabel();
                    hasLoaded.set(true);
                }, throwable -> {
                    // TODO
                    Timber.e(throwable, "Error loading label: %s", throwable.getMessage());
                });


    }

    protected abstract long getIdFromBundle(@Nullable Bundle bundle);

    private Single<Label> getLabel(long id) {
        if (id == 0) {
            return Single.just(new Label().setColor(application.getResources().getColor(R.color.pink)));
        } else {
            return accountRepo.getLabel(id);
        }
    }

    protected Data createData() {
        return new Data();
    }

    private void initFieldsFromLabel() {
        if (label == null) {
            return;
        }

        data.init(label);
        prepareIcon();
    }

    protected void prepareIcon() {
        String iconId = data.icon.get();
        iconDrawable.set(Label.getIconDrawable(application, iconId));
    }

    public static class Data {
        public ObservableField<String> name = new ObservableField<>();
        public ObservableInt color = new ObservableInt();
        public ObservableField<String> icon = new ObservableField<>();

        private void init(@NonNull Label label) {
            name.set(label.getName());
            color.set(label.getColor());
            icon.set(label.getIcon());
        }
    }
}
