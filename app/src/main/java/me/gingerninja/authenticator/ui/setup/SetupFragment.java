package me.gingerninja.authenticator.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.adapter.SetupPagerAdapter;
import me.gingerninja.authenticator.databinding.SetupFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;
import me.gingerninja.authenticator.util.AppSettings;

public class SetupFragment extends BaseFragment<SetupFragmentBinding> {

    @Inject
    SetupPagerAdapter pagerAdapter;

    @Inject
    AppSettings appSettings;

    private OnBackPressedCallback backButtonCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            ViewPager viewPager = getDataBinding().viewPager;
            int currItem = viewPager.getCurrentItem();

            if (currItem > 0) {
                viewPager.setCurrentItem(currItem - 1, true);
            } else {
                if (appSettings.setTemporaryTheme(null)) {
                    requireActivity().recreate();
                }
                //this.setEnabled(false);
                //requireActivity().onBackPressed();
                getNavController().popBackStack();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        assert activity != null;

        activity.getOnBackPressedDispatcher().addCallback(this, backButtonCallback);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, SetupFragmentBinding binding) {
        binding.btnPrev.setOnClickListener(this::handleBackButton);
        binding.btnNext.setOnClickListener(this::handleNextButton);

        binding.viewPager.setAdapter(pagerAdapter);
        binding.progressIndicator.setViewPager(binding.viewPager);
        binding.progressIndicator.setDotsClickable(false);
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
        requireActivity().onBackPressed();
        /*if (getDataBinding().viewPager.getCurrentItem() <= 0) {
            appSettings.setTemporaryTheme(null);
            getActivity().recreate();
            getNavController().popBackStack();
        } else {
            //handleOnBackPressed();
            requireActivity().onBackPressed();
        }*/
    }

    @Override
    protected int getLayoutId() {
        return R.layout.setup_fragment;
    }
}
