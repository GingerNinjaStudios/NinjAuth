package me.gingerninja.authenticator.util.resulthandler;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.NavigatorProvider;

public class ResultNavController {
    public static final String EXTRA_KEY_TARGET = ResultNavController.class.getName() + ":target";
    public static final String EXTRA_KEY_REQUEST_CODE = ResultNavController.class.getName() + ":requestCode";

    private final NavController navController;
    private final String target;
    private final int requestCode;

    private ResultNavController(NavController navController, String target, int requestCode) {
        this.navController = navController;
        this.target = target;
        this.requestCode = requestCode;
    }

    static ResultNavController wrap(NavController navController, FragmentInstanceViewModel viewModel, int requestCode) {
        return new ResultNavController(navController, viewModel.who, requestCode);
    }

    @NonNull
    public NavigatorProvider getNavigatorProvider() {
        return navController.getNavigatorProvider();
    }

    public void addOnDestinationChangedListener(@NonNull NavController.OnDestinationChangedListener listener) {
        navController.addOnDestinationChangedListener(listener);
    }

    public void removeOnDestinationChangedListener(@NonNull NavController.OnDestinationChangedListener listener) {
        navController.removeOnDestinationChangedListener(listener);
    }

    public boolean popBackStack() {
        return navController.popBackStack();
    }

    public boolean popBackStack(int destinationId, boolean inclusive) {
        return navController.popBackStack(destinationId, inclusive);
    }

    public boolean navigateUp() {
        return navController.navigateUp();
    }

    @NonNull
    public NavInflater getNavInflater() {
        return navController.getNavInflater();
    }

    @CallSuper
    public void setGraph(int graphResId) {
        navController.setGraph(graphResId);
    }

    @CallSuper
    public void setGraph(int graphResId, @Nullable Bundle startDestinationArgs) {
        navController.setGraph(graphResId, startDestinationArgs);
    }

    @CallSuper
    public void setGraph(@NonNull NavGraph graph) {
        navController.setGraph(graph);
    }

    @CallSuper
    public void setGraph(@NonNull NavGraph graph, @Nullable Bundle startDestinationArgs) {
        navController.setGraph(graph, startDestinationArgs);
    }

    public boolean handleDeepLink(@Nullable Intent intent) {
        return navController.handleDeepLink(intent);
    }

    @NonNull
    public NavGraph getGraph() {
        return navController.getGraph();
    }

    @Nullable
    public NavDestination getCurrentDestination() {
        return navController.getCurrentDestination();
    }

    public void navigate(int resId) {
        Bundle args = new Bundle(2);
        args.putString(EXTRA_KEY_TARGET, target);
        args.putInt(EXTRA_KEY_REQUEST_CODE, requestCode);
        navController.navigate(resId, args);
    }

    public void navigate(int resId, @Nullable Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        args.putString(EXTRA_KEY_TARGET, target);
        args.putInt(EXTRA_KEY_REQUEST_CODE, requestCode);
        navController.navigate(resId, args);
    }

    public void navigate(int resId, @Nullable Bundle args, @Nullable NavOptions navOptions) {
        if (args == null) {
            args = new Bundle();
        }
        args.putString(EXTRA_KEY_TARGET, target);
        args.putInt(EXTRA_KEY_REQUEST_CODE, requestCode);
        navController.navigate(resId, args, navOptions);
    }

    public void navigate(int resId, @Nullable Bundle args, @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {
        if (args == null) {
            args = new Bundle();
        }
        args.putString(EXTRA_KEY_TARGET, target);
        args.putInt(EXTRA_KEY_REQUEST_CODE, requestCode);
        navController.navigate(resId, args, navOptions, navigatorExtras);
    }

    public void navigate(@NonNull NavDirections directions) {
        Bundle args = directions.getArguments();
        args.putString(EXTRA_KEY_TARGET, target);
        args.putInt(EXTRA_KEY_REQUEST_CODE, requestCode);
        
        navController.navigate(directions.getActionId(), args);
    }

    public void navigate(@NonNull NavDirections directions, @Nullable NavOptions navOptions) {
        Bundle args = directions.getArguments();
        args.putString(EXTRA_KEY_TARGET, target);
        args.putInt(EXTRA_KEY_REQUEST_CODE, requestCode);

        navController.navigate(directions.getActionId(), args, navOptions);
    }

    public void navigate(@NonNull NavDirections directions, @NonNull Navigator.Extras navigatorExtras) {
        Bundle args = directions.getArguments();
        args.putString(EXTRA_KEY_TARGET, target);
        args.putInt(EXTRA_KEY_REQUEST_CODE, requestCode);

        navController.navigate(directions.getActionId(), args, null, navigatorExtras);
    }

    @NonNull
    public NavDeepLinkBuilder createDeepLink() {
        return navController.createDeepLink();
    }

    @Nullable
    @CallSuper
    public Bundle saveState() {
        return navController.saveState();
    }

    @CallSuper
    public void restoreState(@Nullable Bundle navState) {
        navController.restoreState(navState);
    }
}
