package me.gingerninja.authenticator.di.component;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;
import me.gingerninja.authenticator.MyApplication;
import me.gingerninja.authenticator.di.builder.ActivityBuilder;
import me.gingerninja.authenticator.di.builder.FragmentBuilder;
import me.gingerninja.authenticator.di.module.AppModule;
import me.gingerninja.authenticator.di.module.ViewModelModule;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ActivityBuilder.class,
        FragmentBuilder.class,
        ViewModelModule.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();

    }

    void inject(MyApplication app);

}
