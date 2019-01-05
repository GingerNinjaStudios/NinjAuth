package me.gingerninja.authenticator.ui.label;

import javax.inject.Inject;

import androidx.lifecycle.ViewModel;
import me.gingerninja.authenticator.data.repo.AccountRepository;

public class LabelsViewModel extends ViewModel {
    @Inject
    public LabelsViewModel(AccountRepository accountRepository) {

    }
}
