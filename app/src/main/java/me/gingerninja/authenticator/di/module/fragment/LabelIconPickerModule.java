package me.gingerninja.authenticator.di.module.fragment;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.LabelIconLinker;
import me.gingerninja.authenticator.ui.label.form.LabelIconAdapter;

@Module
public class LabelIconPickerModule {
    @Provides
    LabelIconAdapter provideLabelIconAdapter() {
        return new LabelIconAdapter(LabelIconLinker.ICONS);
    }
}
