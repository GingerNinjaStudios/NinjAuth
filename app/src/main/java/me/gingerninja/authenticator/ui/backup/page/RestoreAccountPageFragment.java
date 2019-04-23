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
import me.gingerninja.authenticator.databinding.RestorePageFragmentBinding;
import me.gingerninja.authenticator.ui.backup.RestoreAccountAdapter;
import me.gingerninja.authenticator.ui.backup.RestoreViewModel;
import me.gingerninja.authenticator.ui.base.BaseFragment;

public class RestoreAccountPageFragment extends BaseFragment<RestorePageFragmentBinding> {

    @Inject
    TemporaryRepository repository;

    private RestoreViewModel restoreViewModel;
    private RestoreAccountPageViewModel pageViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreViewModel = ViewModelProviders.of(getParentFragment(), viewModelFactory).get(RestoreViewModel.class);
        pageViewModel = ViewModelProviders.of(this, viewModelFactory).get(RestoreAccountPageViewModel.class);
    }

    @Override
    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View root, RestorePageFragmentBinding binding) {
        RestoreAccountAdapter adapter = new RestoreAccountAdapter(repository, restoreViewModel);
        pageViewModel.getAccounts().observe(this, adapter::setResults);
        binding.list.setAdapter(adapter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.restore_page_fragment;
    }
}
