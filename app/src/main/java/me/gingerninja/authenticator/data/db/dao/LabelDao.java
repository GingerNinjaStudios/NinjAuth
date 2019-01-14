package me.gingerninja.authenticator.data.db.dao;

import java.util.List;

import androidx.annotation.CheckResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import me.gingerninja.authenticator.data.db.entity.Label;

public interface LabelDao {
    @CheckResult
    Single<Label> get(long id);

    @CheckResult
    Observable<Label> getAll();

    @CheckResult
    Observable<List<Label>> getAllAndListen();

    @CheckResult
    Single<Label> save(Label label);

    @CheckResult
    Completable saveAll(List<Label> labelList);

    @CheckResult
    Completable delete(Label label);
}
