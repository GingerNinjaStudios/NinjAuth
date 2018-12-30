package me.gingerninja.authenticator.di.module;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.module.ModuleHandler;

@Module
public class AppModule {
    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    @Singleton
    ModuleHandler provideModuleHandler(Context context) {
        return new ModuleHandler(context);
    }
}
