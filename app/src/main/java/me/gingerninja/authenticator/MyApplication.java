package me.gingerninja.authenticator;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import me.gingerninja.authenticator.di.component.DaggerAppComponent;

public class MyApplication extends Application implements HasActivityInjector, HasServiceInjector, HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    @Inject
    DispatchingAndroidInjector<Service> serviceDispatchingAndroidInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        // SqlCipherDatabaseSource a = new SqlCipherDatabaseSource(this, null, "name", "pass", 1);
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
