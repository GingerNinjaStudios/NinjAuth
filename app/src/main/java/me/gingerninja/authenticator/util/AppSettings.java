package me.gingerninja.authenticator.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.module.ModuleHandler;

@Singleton
public class AppSettings {
    public static final String SHARED_PREFS_NAME = "app_settings";

    private final Context context;
    private final SharedPreferences sharedPrefs;

    private final ModuleHandler moduleHandler;

    @Inject
    public AppSettings(Context context, SharedPreferences sharedPrefs, ModuleHandler moduleHandler) {
        this.context = context;
        this.sharedPrefs = sharedPrefs;
        this.moduleHandler = moduleHandler;
    }

    @StyleRes
    public int getTheme() {
        String themeValue = sharedPrefs.getString(getString(R.string.settings_appearance_theme_key), getString(R.string.settings_appearance_theme_dark_value));

        switch (themeValue) {
            case "light":
                return R.style.AppTheme_Light;
            case "dark":
            default:
                return R.style.AppTheme_Dark;
        }
    }

    @Nullable
    public Uri getAutoBackupUri() {
        String uriStr = sharedPrefs.getString(getString(R.string.settings_backup_uri_key), null);
        if (TextUtils.isEmpty(uriStr)) {
            return null;
        }

        return Uri.parse(uriStr);
    }

    public void setAutoBackupUri(@Nullable String uri) {
        sharedPrefs.edit()
                .putString(getString(R.string.settings_backup_uri_key), uri)
                .apply();
    }

    public ModuleHandler getModuleHandler() {
        return moduleHandler;
    }

    @NonNull
    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}