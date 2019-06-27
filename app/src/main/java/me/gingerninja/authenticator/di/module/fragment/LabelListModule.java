package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.adapter.LabelListAdapter;

@Module
public class LabelListModule {
    @Provides
    LabelListAdapter provideLabelListAdapter() {
        return new LabelListAdapter();
    }
}
