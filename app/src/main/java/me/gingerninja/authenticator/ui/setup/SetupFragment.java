package me.gingerninja.authenticator.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.SetupPagerAdapter;
import me.gingerninja.authenticator.databinding.SetupFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.AppSettings;

public class SetupFragment extends BaseFragment<SetupFragmentBinding> implements OnBackPressedCallback {

    @Inject
    SetupPagerAdapter pagerAdapter;

    @Inject
    AppSettings appSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        assert activity != null;
        activity.addOnBackPressedCallback(this, this);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, SetupFragmentBinding binding) {
        // overcoming MaterialButton's inability to set the icon to the end
        binding.btnNext.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(getActivity(), R.drawable.ic_chevron_next_24dp), null);

        binding.btnPrev.setOnClickListener(this::handleBackButton);
        binding.btnNext.setOnClickListener(this::handleNextButton);

        binding.viewPager.setAdapter(pagerAdapter);
        binding.progressIndicator.setViewPager(binding.viewPager);
        binding.progressIndicator.setDotsClickable(false);
    }

    @Override
    public boolean handleOnBackPressed() {
        boolean handled = false;

        ViewPager viewPager = getDataBinding().viewPager;

        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            handled = true;
        } else {
            appSettings.setTemporaryTheme(null);
            getActivity().recreate();
        }

        return handled;
    }

    private void handleNextButton(View v) {
        ViewPager viewPager = getDataBinding().viewPager;

        if (viewPager.getCurrentItem() == pagerAdapter.getCount() - 1) {
            // TODO

            getNavController().navigate(R.id.finishSetupAction);
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        }
    }

    private void handleBackButton(View v) {
        if (getDataBinding().viewPager.getCurrentItem() <= 0) {
            appSettings.setTemporaryTheme(null);
            getActivity().recreate();
            getNavController().popBackStack();
        } else {
            handleOnBackPressed();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.setup_fragment;
    }
}
