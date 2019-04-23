package me.gingerninja.authenticator.module.timecorrector;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import me.gingerninja.authenticator.module.BaseDynamicModule;
import me.gingerninja.authenticator.module.ModuleHandler;
import timber.log.Timber;

@Singleton
public class TimeCorrector extends BaseDynamicModule<TimeCorrector.BaseTimeCorrector> {
    private static final String SHARED_PREFS_NAME = "module_timecorrector";
    private static final String LAST_SYNC_KEY = "last_sync";
    private static final String DELTA_KEY = "delta";

    private static final long THRESHOLD = 4 * 60 * 60 * 1000; // 4 hours in ms

    private final ModuleHandler moduleHandler;
    private final SharedPreferences sharedPrefs;

    private AtomicBoolean syncInProgress = new AtomicBoolean(false);
    private long lastSync;
    private long delta; // TODO save value and use it properly

    @Inject
    public TimeCorrector(ModuleHandler moduleHandler, Context context) {
        this.moduleHandler = moduleHandler;
        this.sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        lastSync = sharedPrefs.getLong(LAST_SYNC_KEY, 0);
        delta = sharedPrefs.getLong(DELTA_KEY, 0);
    }

    /**
     * Returns the time difference between the {@link System#currentTimeMillis()} and the actual time
     * in milliseconds. The value is positive if the system time is ahead of the current time.
     *
     * @return The time difference between the {@link System#currentTimeMillis()} and the actual time.
     */
    public long getDelta() {
        if (delegate != null &&
                System.currentTimeMillis() - lastSync > THRESHOLD &&
                syncInProgress.compareAndSet(false, true)) {
            //lastSync = System.currentTimeMillis();
            //sharedPrefs.edit().putLong(LAST_SYNC_KEY, lastSync).apply();
            backgroundSync();
        }
        return delegate == null ? 0 : delegate.getDelta(TimeUnit.MILLISECONDS);
    }

    public long getCurrentTime(@NonNull TimeUnit timeUnit) {
        return timeUnit.convert(System.currentTimeMillis() - getDelta(), TimeUnit.MILLISECONDS);
    }

    public Completable syncNow() {
        if (delegate != null) {
            return delegate
                    .syncNow()
                    .doOnTerminate(() -> {
                        lastSync = System.currentTimeMillis();
                        sharedPrefs.edit().putLong(LAST_SYNC_KEY, lastSync).apply();
                    });
        } else {
            return Completable.error(new RuntimeException()); // TODO proper exception
        }
    }

    private void backgroundSync() {
        Timber.d("Background time sync started");
        syncNow()
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        syncInProgress.compareAndSet(true, false);
                        Timber.d("TimeCorrector sync completed, delta: %d", delegate != null ? delegate.getDelta(TimeUnit.MILLISECONDS) : 0);
                    }

                    @Override
                    public void onError(Throwable e) {
                        syncInProgress.compareAndSet(true, false);
                        Timber.d(e, "TimeCorrector sync error");
                    }
                });
    }

    public boolean initExternalModule() {
        if (moduleHandler.isEnabled(ModuleHandler.MODULE_TIME_CORRECTOR)) {
            try {
                BaseTimeCorrector baseTimeCorrector = moduleHandler.getModule(ModuleHandler.MODULE_TIME_CORRECTOR);
                if (baseTimeCorrector != null) {
                    //baseTimeCorrector.init(this);
                    setDelegate(baseTimeCorrector);
                    return true;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return false;
        } else {
            setDelegate(null);
            return true;
        }
    }

    public interface BaseTimeCorrector {
        long getDelta(@NonNull TimeUnit timeUnit);

        //void init(@NonNull TimeCorrector timeCorrector);

        Completable syncNow();
    }
}
