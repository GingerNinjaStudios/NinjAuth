package me.gingerninja.authenticator.data.db.dao;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.data.db.entity.AccountHasLabel;
import me.gingerninja.authenticator.data.db.entity.Label;

public interface LabelDao {
    @CheckResult
    Single<Label> get(long id);

    @CheckResult
    Single<Label> get(String uid);

    @CheckResult
    Observable<Label> getAll(long... exceptions);

    @CheckResult
    Observable<AccountHasLabel> getLabelsByAccount(@NonNull Account account);

    @CheckResult
    Completable saveLabelsForAccount(@NonNull Account account, @NonNull List<Label> labels);

    @CheckResult
    Observable<List<Label>> getAllAndListen();

    @CheckResult
    Single<Label> save(Label label);

    @CheckResult
    Completable saveAll(List<Label> labelList);

    @CheckResult
    Completable delete(Label label);
}
