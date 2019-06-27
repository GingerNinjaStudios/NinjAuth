package me.gingerninja.authenticator;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.StrictMode;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManagerFix;

import com.google.android.play.core.splitcompat.SplitCompat;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import me.gingerninja.authenticator.di.component.DaggerAppComponent;
import me.gingerninja.authenticator.util.AppSettings;
import timber.log.Timber;

public class MyApplication extends Application implements HasActivityInjector, HasServiceInjector, HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    @Inject
    DispatchingAndroidInjector<Service> serviceDispatchingAndroidInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentAndroidInjector;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        SplitCompat.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    //.detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());

            Timber.plant(new Timber.DebugTree());
        }

        PreferenceManagerFix.setDefaultValues(this, AppSettings.SHARED_PREFS_NAME, Context.MODE_PRIVATE, R.xml.settings, true);

        DaggerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this);

    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }

    /**
     * Returns an {@link AndroidInjector} of {@link Service}s.
     */
    @Override
    public AndroidInjector<Service> serviceInjector() {
        return serviceDispatchingAndroidInjector;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentAndroidInjector;
    }
}
