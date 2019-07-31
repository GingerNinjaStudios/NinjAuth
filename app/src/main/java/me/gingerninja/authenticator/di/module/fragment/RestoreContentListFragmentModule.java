package me.gingerninja.authenticator.di.module.fragment;

import androidx.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.ui.backup.BaseRestoreCheckableAdapter;
import me.gingerninja.authenticator.ui.backup.RestoreAccountAdapter;
import me.gingerninja.authenticator.ui.backup.RestoreContentListFragment;
import me.gingerninja.authenticator.ui.backup.RestoreContentListFragmentArgs;
import me.gingerninja.authenticator.ui.backup.RestoreLabelAdapter;

@Module
public class RestoreContentListFragmentModule {
    @Provides
    BaseRestoreCheckableAdapter adapter(@NonNull TemporaryRepository temporaryRepository, @NonNull RestoreContentListFragment fragment) {
        final RestoreContentListFragment.Type type = RestoreContentListFragmentArgs.fromBundle(fragment.requireArguments()).getType();

        switch (type) {
            case ACCOUNTS:
                return new RestoreAccountAdapter(temporaryRepository, fragment);
            case LABELS:
                return new RestoreLabelAdapter(temporaryRepository, fragment);
            default:
                throw new IllegalArgumentException("Unknown restore content type: " + type);
        }
    }
}
