package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.adapter.RestorePagerAdapter;
import me.gingerninja.authenticator.ui.backup.RestoreFragment;

@Module
public class RestoreFragmentModule {
    @Provides
    RestorePagerAdapter providePagerAdapter(RestoreFragment fragment) {
        return new RestorePagerAdapter(fragment.getChildFragmentManager());
    }
}
