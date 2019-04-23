package me.gingerninja.authenticator.data.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import me.gingerninja.authenticator.ui.backup.page.RestoreAccountPageFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreLabelPageFragment;

public class RestorePagerAdapter extends FragmentPagerAdapter {
    public RestorePagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RestoreAccountPageFragment();
            case 1:
                return new RestoreLabelPageFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
