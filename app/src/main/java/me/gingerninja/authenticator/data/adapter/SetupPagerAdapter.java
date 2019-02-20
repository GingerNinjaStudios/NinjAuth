package me.gingerninja.authenticator.data.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import me.gingerninja.authenticator.ui.setup.page.BackupSetupPageFragment;
import me.gingerninja.authenticator.ui.setup.page.ProtectionSelectorPageFragment;
import me.gingerninja.authenticator.ui.setup.page.ThemeSelectorPageFragment;

public class SetupPagerAdapter extends FragmentPagerAdapter {
    public SetupPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ThemeSelectorPageFragment();
            case 1:
                return new ProtectionSelectorPageFragment();
            case 2:
                return new BackupSetupPageFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
