package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.adapter.SetupPagerAdapter;
import me.gingerninja.authenticator.ui.setup.SetupFragment;

@Module
public class SetupFragmentModule {
    @Provides
    SetupPagerAdapter providePagerAdapter(SetupFragment fragment) {
        return new SetupPagerAdapter(fragment.getChildFragmentManager());
    }
}
