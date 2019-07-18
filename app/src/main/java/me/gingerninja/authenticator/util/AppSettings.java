package me.gingerninja.authenticator.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.gingerninja.authenticator.R;
import me.gingerninja.authenticator.crypto.Crypto;
import me.gingerninja.authenticator.module.ModuleHandler;
import timber.log.Timber;

@Singleton
public class AppSettings {
    public static final String SHARED_PREFS_NAME = "app_settings";

    private final Context context;
    private final SharedPreferences sharedPrefs;

    private final ModuleHandler moduleHandler;

    private final Crypto crypo;

    private long lockScreenStartTime = -1;

    /**
     * Used during the setup process.
     */
    @Nullable
    private String temporaryTheme;

    @Inject
    public AppSettings(Context context, SharedPreferences sharedPrefs, Crypto crypto, ModuleHandler moduleHandler) {
        this.context = context;
        this.sharedPrefs = sharedPrefs;
        this.crypo = crypto;
        this.moduleHandler = moduleHandler;
    }

    public void applyTheme() {
        String themeValue = temporaryTheme != null ? temporaryTheme : sharedPrefs.getString(getString(R.string.settings_main_theme_key), getString(R.string.settings_appearance_theme_dark_value));
        applyTheme(themeValue);
    }

    public void applyTheme(String themeValue) {
        final int mode;

        switch (themeValue) {
            case "light":
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case "battery":
                mode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }

        AppCompatDelegate.setDefaultNightMode(mode);
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

    public boolean hideFromRecents() {
        return sharedPrefs.getBoolean(getString(R.string.settings_security_hide_recent_key), true);
    }

    public void startLockScreenCounter(boolean forceReset) {
        if (forceReset) {
            lockScreenStartTime = -1;
        } else {
            String raw = sharedPrefs.getString(getString(R.string.settings_security_leave_lock_key), "0");
            if (!crypo.hasLock() || "-1".equals(raw)) {
                lockScreenStartTime = -1;
            } else {
                lockScreenStartTime = SystemClock.elapsedRealtime();
            }
        }
    }


    public boolean stopLockScreenCounter() {
        if (lockScreenStartTime >= 0) {
            String raw = sharedPrefs.getString(getString(R.string.settings_security_leave_lock_key), "0");

            try {
                long lockoutTime = Long.parseLong(raw);
                if (SystemClock.elapsedRealtime() - lockScreenStartTime > TimeUnit.MILLISECONDS.convert(lockoutTime, TimeUnit.SECONDS)) {
                    return true;
                }
            } catch (NumberFormatException e) {
                return true;
            }
        }

        return false;
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
