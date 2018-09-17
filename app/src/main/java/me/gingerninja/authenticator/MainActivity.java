package me.gingerninja.authenticator;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import me.gingerninja.authenticator.data.db.entity.Account;
import me.gingerninja.authenticator.util.CodeGenerator;
import me.gingerninja.authenticator.util.Parser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Account account = Parser.parseUrl("otpauth://totp/ACME%20Co:john@example.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30");
        CodeGenerator codeGenerator = new CodeGenerator();

        Log.d("MainAcitvity", "account: " + account);
        Log.d("MainAcitvity", "code: " + codeGenerator.getCode(account));
    }
}
