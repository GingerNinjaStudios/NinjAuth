package me.gingerninja.authenticator.util;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TimeCorrector {

    @Inject
    public TimeCorrector() {

    }

    /**
     * Returns the time different between the {@link System#currentTimeMillis()} and the actual time
     * in milliseconds. The value is positive if the system time is ahead of the current time.
     *
     * @return The time different between the {@link System#currentTimeMillis()} and the actual time.
     */
    public long getDelta() {
        return 0;
    }

    public long getCurrentTime(TimeUnit timeUnit) {
        return timeUnit.convert(System.currentTimeMillis() - getDelta(), TimeUnit.MILLISECONDS);
    }
}
