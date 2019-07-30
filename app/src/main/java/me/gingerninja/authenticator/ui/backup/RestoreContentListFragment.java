package me.gingerninja.authenticator.ui.backup;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeClipBounds;
import androidx.transition.ChangeTransform;
import androidx.transition.TransitionSet;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.databinding.RestoreContentListFragmentBinding;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class RestoreContentListFragment extends BaseFragment<RestoreContentListFragmentBinding> implements WorkUpdateHandler {
    public static final long SHARED_DURATION = 5000;
    @Inject
    BaseRestoreCheckableAdapter adapter;

    private Type type;
    private RestoreContentListViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = RestoreContentListFragmentArgs.fromBundle(requireArguments()).getType();
        viewModel = getViewModel(RestoreContentListViewModel.class);
        viewModel.init(type);
        postponeEnterTransition();
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, RestoreContentListFragmentBinding binding) {
        binding.setViewModel(viewModel);
        binding.toolbar.setNavigationOnClickListener(v -> getNavController().navigateUp());
        viewModel.getData().observe(getViewLifecycleOwner(), adapter::setResults);
        binding.list.setAdapter(adapter);

        setupTransition(binding);
        startPostponedEnterTransition();
    }

    private void setupTransition(RestoreContentListFragmentBinding binding) {
        String transitionName = null;
        switch (type) {
            case ACCOUNTS:
                transitionName = "accounts";
                break;
            case LABELS:
                transitionName = "labels";
                break;
        }

        binding.root.setTransitionName(transitionName);

        TransitionSet setIn = new TransitionSet()
                //.addTransition(new Fade(Fade.IN))
                .addTransition(new ChangeBounds())
                .addTransition(new ChangeClipBounds())
                .addTransition(new ChangeTransform())
                .setDuration(SHARED_DURATION)
                .setOrdering(TransitionSet.ORDERING_TOGETHER);

        TransitionSet setOut = new TransitionSet()
                //.addTransition(new Fade(Fade.OUT))
                .addTransition(new ChangeBounds())
                .addTransition(new ChangeClipBounds())
                .addTransition(new ChangeTransform())
                .setDuration(SHARED_DURATION)
                .setOrdering(TransitionSet.ORDERING_TOGETHER);

        //setSharedElementEnterTransition(setIn);
        //setSharedElementReturnTransition(setOut);

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                float targetElevation = binding.root.getResources().getDimension(R.dimen.account_list_card_elevation_dragging) * 4;
                ObjectAnimator animator = ObjectAnimator.ofFloat(binding.root, "elevation", binding.root.getElevation(), targetElevation);
                animator.setDuration(SHARED_DURATION);
                animator.start();
            }
        });

        /*setExitSharedElementCallback(new SharedElementCallback() {

            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                float targetElevation = 0;
                ObjectAnimator animator = ObjectAnimator.ofFloat(binding.root, "elevation", binding.root.getElevation(), targetElevation);
                animator.setDuration(SHARED_DURATION);
                animator.start();
            }
        });*/
    }

    @Override
    protected int getLayoutId() {
        return R.layout.restore_content_list_fragment;
    }

    @Override
    public void handleRequest(@NonNull Completable completable) {
        viewModel.handleRequest(completable);
    }

    public enum Type {
        ACCOUNTS,
        LABELS
    }
}
