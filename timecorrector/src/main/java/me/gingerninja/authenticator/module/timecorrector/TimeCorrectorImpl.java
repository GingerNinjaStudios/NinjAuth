package me.gingerninja.authenticator.module.timecorrector;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@Keep
public class TimeCorrectorImpl implements TimeCorrector.BaseTimeCorrector {
    private static final String NTP_GOOGLE = "time.google.com";
    private static final int NETWORK_TIMEOUT = 5000; // ms

    private long delta;

    private Single<Long> getCurrentTime() {
        return Observable.fromArray(NTP_GOOGLE)
                .subscribeOn(Schedulers.newThread())
                .map(host -> {
                    SntpClient client = new SntpClient();
                    if (client.requestTime(host, NETWORK_TIMEOUT)) {
                        return client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
                    }
                    return Long.MIN_VALUE;
                })
                .toList()
                .map(longs -> {
                    final int n = longs.size();
                    long total = 0;
                    for (int i = 0; i < n; i++) {
                        total += longs.get(i);
                    }

                    return total / n;
                });
    }

    private Observable<SntpClient> getSntpClient(final String host) {
        return Observable.just(new SntpClient())
                .subscribeOn(Schedulers.newThread())
                .map(client -> {
                    if (client.requestTime(host, NETWORK_TIMEOUT)) {
                        return client;
                    } else {
                        throw new RuntimeException("Cannot retrieve time.");
                    }
                });
    }

    @Override
    public long getDelta(@NonNull TimeUnit timeUnit) {
        return delta;
    }

    /*@Override
    public void init(@NonNull TimeCorrector timeCorrector) {
        timeCorrector.setDelegate(this);
    }*/

    @Override
    public Completable syncNow() {
        return getSntpClient(NTP_GOOGLE)
                .doOnNext(client -> {
                    // TODO save value
                    delta = System.currentTimeMillis() - client.getTime();
                })
                .ignoreElements();
    }
}
