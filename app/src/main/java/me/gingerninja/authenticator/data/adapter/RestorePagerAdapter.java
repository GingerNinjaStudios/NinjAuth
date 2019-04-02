package me.gingerninja.authenticator.data.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import me.gingerninja.authenticator.ui.backup.page.RestoreAccountPageFragment;

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
        }

        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }
}
