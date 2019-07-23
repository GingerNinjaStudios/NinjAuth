package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.adapter.LabelListAdapter;
import me.gingerninja.authenticator.data.adapter.LabelListIteratorAdapter;

@Module
public class LabelListModule {
    @Provides
    LabelListAdapter provideLabelListAdapter() {
        return new LabelListAdapter();
    }

    @Provides
    LabelListIteratorAdapter provideLabelListIteratorAdapter() {
        return new LabelListIteratorAdapter();
    }
}
