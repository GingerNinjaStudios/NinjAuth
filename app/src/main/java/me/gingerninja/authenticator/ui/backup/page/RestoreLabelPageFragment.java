package me.gingerninja.authenticator.ui.backup.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.data.repo.TemporaryRepository;
import me.gingerninja.authenticator.databinding.RestorePageFragmentBinding;
import me.gingerninja.authenticator.ui.backup.RestoreLabelAdapter;
import me.gingerninja.authenticator.ui.backup.RestoreViewModel;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class RestoreLabelPageFragment extends BaseFragment<RestorePageFragmentBinding> {

    @Inject
    TemporaryRepository repository;

    private RestoreViewModel restoreViewModel;
    private RestoreLabelPageViewModel pageViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreViewModel = ViewModelProviders.of(getParentFragment(), viewModelFactory).get(RestoreViewModel.class);
        pageViewModel = ViewModelProviders.of(this, viewModelFactory).get(RestoreLabelPageViewModel.class);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, RestorePageFragmentBinding binding) {
        RestoreLabelAdapter adapter = new RestoreLabelAdapter(repository, restoreViewModel);
        pageViewModel.getLabels().observe(this, adapter::setResults);
        binding.list.setAdapter(adapter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.restore_page_fragment;
    }
}
