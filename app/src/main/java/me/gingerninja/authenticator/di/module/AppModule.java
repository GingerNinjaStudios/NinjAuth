package me.gingerninja.authenticator.di.module;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.util.CodeGenerator;
import me.gingerninja.authenticator.util.TimeCorrector;

@Module
public class AppModule {
    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }
}
