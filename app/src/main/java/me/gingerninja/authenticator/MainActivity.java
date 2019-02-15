package me.gingerninja.authenticator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import dagger.android.AndroidInjection;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;
import me.gingerninja.authenticator.databinding.ActivityMainBinding;
import me.gingerninja.authenticator.module.timecorrector.TimeCorrector;
import me.gingerninja.authenticator.util.AppSettings;
import me.gingerninja.authenticator.util.backup.BackupUtils;
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

        setTheme(appSettings.getTheme());

        temporaryDbHandler.openDatabase("fakepass");

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        drawerLayout = binding.drawerLayout;

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        if (binding.navigationView != null && binding.drawerLayout != null) {
            //NavigationUI.setupWithNavController(binding.navigationView, navController);
            NavigationView navView = binding.navigationView;

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                Timber.v("Destination changed: %s", destination.getLabel());
                switch (destination.getId()) {
                    case R.id.accountListFragment:
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        navView.setCheckedItem(R.id.nav_accounts);
                        break;
                    case R.id.labelListFragment:
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        navView.setCheckedItem(R.id.nav_labels);
                        break;
                    default:
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        break;
                }
            });
        }

        timeCorrector.initExternalModule();

        crypto.authenticate(this);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Uri uri = backupUtils.getUriFromIntent(data);
            try {
                if (uri != null) {
                    backupUtils.backup(uri);
                } else {
                    throw new IllegalArgumentException("Uri is null");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
