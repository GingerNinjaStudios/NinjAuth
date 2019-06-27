package me.gingerninja.authenticator.module;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import me.gingerninja.authenticator.module.timecorrector.TimeCorrector;

public class ModuleHandler implements SplitInstallStateUpdatedListener {
    public static final String MODULE_TIME_CORRECTOR = "timecorrector";
    private static final String SHARED_PREF_NAME = "dynamic_modules";
    @NonNull
    private final SharedPreferences sharedPrefs;
    private Map<String, Provider<?>> moduleProviders = new HashMap<>();
    private SplitInstallManager splitInstallManager;
    private BehaviorSubject<SplitInstallSessionState> sessionStateSubject = BehaviorSubject.create();

    {
        moduleProviders.put(MODULE_TIME_CORRECTOR, new SingletonProvider<TimeCorrector.BaseTimeCorrector>() {
            @Override
            protected TimeCorrector.BaseTimeCorrector create() throws RuntimeException {
                try {
                    return (TimeCorrector.BaseTimeCorrector) Class.forName("me.gingerninja.authenticator.module.timecorrector.TimeCorrectorImpl").newInstance();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public ModuleHandler(@NonNull Context context) {
        this(context, SHARED_PREF_NAME);
    }

    public ModuleHandler(@NonNull Context context, @NonNull String name) {
        splitInstallManager = SplitInstallManagerFactory.create(context.getApplicationContext());
        splitInstallManager.registerListener(this);
        this.sharedPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public boolean isInstalled(@NonNull @DynamicModule String moduleName) {
        return splitInstallManager.getInstalledModules().contains(moduleName);
    }

    public boolean isEnabled(@NonNull @DynamicModule String moduleName) {
        return isInstalled(moduleName) && this.sharedPrefs.getBoolean(moduleName, false);
    }

    public Set<String> getEnabledModules() {
        Set<String> modules = new HashSet<>();

        for (Map.Entry<String, ?> entry : sharedPrefs.getAll().entrySet()) {
            Boolean enabled = (Boolean) entry.getValue();
            if (enabled) {
                modules.add(entry.getKey());
            }
        }

        return modules;
    }

    /**
     * Enables one or more modules. This is a fail-fast method to enable multiple modules at once:
     * if any of the modules are not installed, it throws an exception and none of the modules gets
     * enabled.
     *
     * @param moduleName  the name of a module
     * @param moduleNames additional module names
     * @throws IllegalStateException if the module is not installed yet
     */
    private void enable(@NonNull @DynamicModule String moduleName, @NonNull @DynamicModule String... moduleNames) {
        SharedPreferences.Editor editor = sharedPrefs.edit();

        if (!isInstalled(moduleName)) {
            throw new IllegalStateException("Module " + moduleName + " is not installed");
        } else {
            editor.putBoolean(moduleName, true);
        }

        for (String name : moduleNames) {
            if (!isInstalled(name)) {
                throw new IllegalStateException("Module " + name + " is not installed");
            } else {
                editor.putBoolean(name, true);
            }
        }

        editor.apply();
    }

    /**
     * Disables one or more modules. Note that this does not prevent the use of already existing
     * module instances.
     *
     * @param moduleName  the name of a module
     * @param moduleNames additional module names
     */
    private void disable(@NonNull @DynamicModule String moduleName, @NonNull @DynamicModule String... moduleNames) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(moduleName, false);

        for (String name : moduleNames) {
            editor.putBoolean(name, false);
        }

        editor.apply();
    }

    /**
     * Installs the given module(s). Note that {@link Subject#onComplete()} might not be
     * called.
     *
     * @param moduleName  the name of a module
     * @param moduleNames additional module names
     * @return an observable subject that reports the download/install state
     */
    public BehaviorSubject<SplitInstallSessionState> install(@NonNull @DynamicModule String moduleName, @NonNull @DynamicModule String... moduleNames) {
        final BehaviorSubject<SplitInstallSessionState> subject = BehaviorSubject.create();

        SplitInstallRequest.Builder requestBuilder = SplitInstallRequest.newBuilder();
        requestBuilder.addModule(moduleName);

        for (String name : moduleNames) {
            requestBuilder.addModule(name);
        }

        splitInstallManager
                .startInstall(requestBuilder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Integer sessionId = task.getResult();
                        if (sessionId == 0) {
                            subject.onComplete();
                        } else {
                            sessionStateSubject
                                    .filter(state -> state.sessionId() == sessionId)
                                    .doOnNext(state -> {
                                        if (state.errorCode() != SplitInstallErrorCode.NO_ERROR) {
                                            throw new ModuleInstallException(state);
                                        }
                                    })
                                    .subscribe(subject);
                        }
                    } else {
                        subject.onError(task.getException());
                    }
                });

        return subject;
    }

    public void uninstall(@NonNull @DynamicModule String moduleName, @NonNull @DynamicModule String... moduleNames) {
        disable(moduleName, moduleNames);

        ArrayList<String> moduleList = new ArrayList<>(1 + moduleNames.length);
        moduleList.add(moduleName);
        moduleList.addAll(Arrays.asList(moduleNames));

        splitInstallManager
                .deferredUninstall(moduleList)
                .addOnCompleteListener(task -> {
                });
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getModule(@NonNull @DynamicModule String moduleName) {
        Provider<?> provider = moduleProviders.get(moduleName);
        if (provider == null) {
            return null;
        }

        return (T) provider.get();
    }

    /**
     * Implementation of {@link SplitInstallStateUpdatedListener}.
     *
     * @param sessionState state of a split download/install session
     */
    @Override
    public void onStateUpdate(SplitInstallSessionState sessionState) {
        sessionStateSubject.onNext(sessionState);
    }

    @StringDef({MODULE_TIME_CORRECTOR})
    @interface DynamicModule {
    }
}
