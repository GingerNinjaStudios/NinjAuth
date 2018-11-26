package me.gingerninja.authenticator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import dagger.android.AndroidInjection;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;
import me.gingerninja.authenticator.databinding.ActivityMainBinding;
import me.gingerninja.authenticator.util.AppSettings;
import me.gingerninja.authenticator.util.backup.BackupUtils;

public class MainActivity extends AppCompatActivity {
    @Inject
    AppSettings appSettings;

    @Inject
    DatabaseHandler temporaryDbHandler;

    @Inject
    BackupUtils backupUtils;

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

        NavigationUI.setupWithNavController(binding.navigationView, navController);

        /*Account account = Parser.parseUrl("otpauth://totp/ACME%20Co:john@example.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30");
        TimeCorrector timeCorrector = new TimeCorrector();
        CodeGenerator codeGenerator = new CodeGenerator(timeCorrector);

        Log.d("MainAcitvity", "account: " + account);
        Log.d("MainAcitvity", "code: " + codeGenerator.formatCode(codeGenerator.getCode(account), account.getDigits()));*/

        //backupUtils.createFile(this, 1, "ninjauth-backup.zip");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            if (appSettings.getTheme() == R.style.AppTheme_Light) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                int flags = decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
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
