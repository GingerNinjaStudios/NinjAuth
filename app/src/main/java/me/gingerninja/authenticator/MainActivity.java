package me.gingerninja.authenticator;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dagger.android.AndroidInjection;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.util.CodeGenerator;
import me.gingerninja.authenticator.util.Parser;
import me.gingerninja.authenticator.util.TimeCorrector;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_main);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        Account account = Parser.parseUrl("otpauth://totp/ACME%20Co:john@example.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30");
        TimeCorrector timeCorrector = new TimeCorrector();
        CodeGenerator codeGenerator = new CodeGenerator(timeCorrector);

        Log.d("MainAcitvity", "account: " + account);
        Log.d("MainAcitvity", "code: " + codeGenerator.formatCode(codeGenerator.getCode(account), account.getDigits()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }
}
