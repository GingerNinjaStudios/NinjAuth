package me.gingerninja.authenticator.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.module.ModuleHandler;
import timber.log.Timber;

@Singleton
public class AppSettings {
    public static final String SHARED_PREFS_NAME = "app_settings";

    private final Context context;
    private final SharedPreferences sharedPrefs;

    private final ModuleHandler moduleHandler;

    /**
     * Used during the setup process.
     */
    @Nullable
    private String temporaryTheme;

    @Inject
    public AppSettings(Context context, SharedPreferences sharedPrefs, ModuleHandler moduleHandler) {
        this.context = context;
        this.sharedPrefs = sharedPrefs;
        this.moduleHandler = moduleHandler;
    }

    @StyleRes
    public int getTheme() {
        String themeValue = temporaryTheme != null ? temporaryTheme : sharedPrefs.getString(getString(R.string.settings_appearance_theme_key), getString(R.string.settings_appearance_theme_dark_value));

        switch (themeValue) {
            case "light":
                return R.style.AppTheme_Light;
            case "dark":
            default:
                return R.style.AppTheme_Dark;
        }
    }

    /**
     * Sets the temporary theme to be used by the app. This is used during the initial setup
     * process.
     *
     * @param theme the theme to be used
     * @return Returns {@code true} if the change was made; false otherwise
     */
    public boolean setTemporaryTheme(@Nullable String theme) {
        boolean changed = !TextUtils.equals(theme, temporaryTheme);
        Timber.v("Setting temporary theme from %s to %s, changed: %s", temporaryTheme, theme, changed);

        if (theme == null) {
            temporaryTheme = null;
        } else {
            switch (theme) {
                case "light":
                case "dark":
                    temporaryTheme = theme;
                    break;
                default:
                    temporaryTheme = getString(R.string.settings_appearance_theme_dark_value);
            }
        }

        return changed;
    }

    public boolean isFirstRunComplete() {
        return sharedPrefs.getBoolean(getString(R.string.settings_first_run_complete), false);
    }

    public void setFirstRunComplete() {
        sharedPrefs.edit().putBoolean(getString(R.string.settings_first_run_complete), true).apply();
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
