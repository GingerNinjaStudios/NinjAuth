package me.gingerninja.authenticator.data.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import me.gingerninja.authenticator.ui.backup.page.RestoreAccountPageFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreLabelPageFragment;
import me.gingerninja.authenticator.ui.backup.page.RestoreSummaryPageFragment;

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
            case 2:
            default:
                return new RestoreSummaryPageFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
