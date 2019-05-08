package me.gingerninja.authenticator.ui.backup.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import javax.inject.Inject;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.databinding.RestoreSummaryPageFragmentBinding;
import me.gingerninja.authenticator.ui.backup.RestoreViewModel;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class RestoreSummaryPageFragment extends BaseFragment<RestoreSummaryPageFragmentBinding> {

    @Inject
    TemporaryRepository repository;

    private RestoreViewModel restoreViewModel;
    private RestoreSummaryPageViewModel pageViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreViewModel = ViewModelProviders.of(getParentFragment(), viewModelFactory).get(RestoreViewModel.class);
        pageViewModel = ViewModelProviders.of(this, viewModelFactory).get(RestoreSummaryPageViewModel.class);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, RestoreSummaryPageFragmentBinding binding) {

    }

    /*private void reveal(int color) {
        RestoreSummaryPageFragmentBinding binding = getDataBinding();

        float cX = binding.card.getWidth() / 2f;
        float cY = binding.card.getHeight() / 2f;
        float radius = (float) Math.hypot(cX, cY);//Math.max(binding.card.getWidth(), binding.card.getHeight()) / 2f;

        CircularRevealWidget.RevealInfo revealInfo = new CircularRevealWidget.RevealInfo(cX, cY, radius);
        binding.cardFrame.setRevealInfo(revealInfo);
        binding.cardFrame.setCircularRevealScrimColor(color);

        Animator animator = CircularRevealCompat.createCircularReveal(binding.cardFrame, cX, cY, 0, radius);
        Animator.AnimatorListener animatorListener = CircularRevealCompat.createCircularRevealListener(binding.cardFrame);
        animator.addListener(animatorListener);
        animator.start();
    }*/

    @Override
    protected int getLayoutId() {
        return R.layout.restore_summary_page_fragment;
    }
}
