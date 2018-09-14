package me.gingerninja.authenticator;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // SqlCipherDatabaseSource a = new SqlCipherDatabaseSource(this, null, "name", "pass", 1);
    }
}
