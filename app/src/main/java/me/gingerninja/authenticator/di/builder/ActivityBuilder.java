package me.gingerninja.authenticator.di.builder;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.gingerninja.authenticator.MainActivity;

@Module
public abstract class ActivityBuilder {
    @ContributesAndroidInjector
    abstract MainActivity bindMainActivity();
}
