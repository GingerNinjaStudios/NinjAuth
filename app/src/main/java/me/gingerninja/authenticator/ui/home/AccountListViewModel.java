package me.gingerninja.authenticator.ui.home;

import android.app.Application;
import android.view.View;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.gingerninja.authenticator.util.SingleEvent;

public class AccountListViewModel extends ViewModel {
    public static final String NAV_ADD_ACCOUNT_FROM_CAMERA = "nav.addAccountFromCamera";

    private Application application;
    private MutableLiveData<SingleEvent<String>> navAction = new MutableLiveData<>();

    @Inject
    public AccountListViewModel(Application application) {
        this.application = application;
    }

    LiveData<SingleEvent<String>> getNavigationAction() {
        return navAction;
    }

    public void onAddAccountFromCameraClick(View view) {
        navAction.setValue(new SingleEvent<>(NAV_ADD_ACCOUNT_FROM_CAMERA));
        /*if (PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(application, Manifest.permission.CAMERA)) {
            // open camera
        } else {
            // request permission

        }*/
    }
}
