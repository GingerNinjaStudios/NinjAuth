package me.gingerninja.authenticator;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Pattern pattern = Pattern.compile("(?:(.+)(?::|%3A|%3a)(?:(?:%20)| )*(.+))|(.+)");

        Uri uri = Uri.parse("otpauth://totp/ACME%20Co:john.doe@email.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30");

        String scheme = uri.getScheme();
        String type = uri.getHost();
        String label = (uri.getLastPathSegment() != null) ? uri.getLastPathSegment().trim() : null;
        Set<String> paramNames = uri.getQueryParameterNames();

        Log.d("MainAcitvity", "scheme: " + scheme + ", type: " + type + ", label: " + label);
        for (String paramName : paramNames) {
            Log.d("MainAcitvity", "- param: [name=" + paramName + ", value=" + uri.getQueryParameter(paramName) + "]");
        }

        if (label != null) {
            Matcher labelMatcher = pattern.matcher(label);
            if (labelMatcher.matches()) {
                final String accountName, issuer;
                if (labelMatcher.group(3) == null) {
                    issuer = labelMatcher.group(1);
                    accountName = labelMatcher.group(2);
                } else {
                    issuer = null;
                    accountName = labelMatcher.group(3);
                }

                Log.d("MainAcitvity", "accountName: " + accountName + ", issuer: " + issuer);
            }
        }
    }
}
