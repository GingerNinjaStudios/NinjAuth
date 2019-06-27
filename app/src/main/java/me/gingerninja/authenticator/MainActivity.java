package me.gingerninja.authenticator;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;
import me.gingerninja.authenticator.databinding.ActivityMainBinding;
import me.gingerninja.authenticator.module.timecorrector.TimeCorrector;
import me.gingerninja.authenticator.util.AppSettings;
import me.gingerninja.authenticator.util.backup.BackupUtils;
import me.gingerninja.authenticator.util.resulthandler.FragmentResultListener;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    @Inject
    AppSettings appSettings;

    @Inject
    TimeCorrector timeCorrector;

    @Inject
    DatabaseHandler temporaryDbHandler;

    @Inject
    BackupUtils backupUtils;

    @Inject
    Crypto crypto;

    private NavController navController;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        setTheme(appSettings.getTheme());

        temporaryDbHandler.openDatabase("fakepass");

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        drawerLayout = binding.drawerLayout;

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        FragmentResultListener.registerNavController(this, navController);

        if (binding.navigationView != null && drawerLayout != null) {
            //NavigationUI.setupWithNavController(binding.navigationView, navController);
            NavigationView navView = binding.navigationView;

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                Timber.v("Destination changed: %s", destination.getLabel());

                int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

                switch (destination.getId()) {
                    case R.id.accountListFragment:
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); // FIXME LOCK_MODE_UNLOCKED
                        navView.setCheckedItem(R.id.nav_accounts);
                        break;
                    case R.id.labelListFragment:
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); // FIXME LOCK_MODE_UNLOCKED
                        navView.setCheckedItem(R.id.nav_labels);
                        break;
                    case R.id.addAccountFromCameraFragment:
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        setRequestedOrientation(requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                        break;
                    default:
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        break;
                }

                if (requestedOrientation != getRequestedOrientation()) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            });

            navView.setNavigationItemSelectedListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.nav_accounts:
                                if (!item.isChecked()) {
                                    navController.navigate(R.id.openAccountList);
                                }
                                break;
                            case R.id.nav_labels:
                                if (!item.isChecked()) {
                                    navController.navigate(R.id.openLabelList);
                                }
                                break;
                            case R.id.nav_settings:
                                navController.navigate(R.id.openSettingsFragment);
                                break;
                        }

                        drawerLayout.closeDrawers();

                        return true;
                    }
            );
        }

        timeCorrector.initExternalModule();

        //crypto.authenticate(this);
        /*crypto.test(this)
                .subscribe(() -> {
                    Timber.v("Crypto test done");
                });*/

        /*Account account = Parser.parseUrl("otpauth://totp/ACME%20Co:john@example.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30");
        TimeCorrector timeCorrector = new TimeCorrector();
        CodeGenerator codeGenerator = new CodeGenerator(timeCorrector);

        Log.d("MainAcitvity", "account: " + account);
        Log.d("MainAcitvity", "code: " + codeGenerator.formatCode(codeGenerator.getCode(account), account.getDigits()));*/

        //backupUtils.createFile(this, 1, "ninjauth-backup.zip");
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            if (appSettings.getTheme() == R.style.AppTheme_Light) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            } else {
                int flags = decorView.getSystemUiVisibility() & ~(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                decorView.setSystemUiVisibility(flags);
            }
        }

        /*getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, drawerLayout);
    }
}
