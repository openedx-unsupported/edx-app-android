package org.edx.mobile.tta.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.inject.Key;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.edx.mobile.tta.widget.loading.ILoading;
import org.edx.mobile.tta.widget.loading.ProgressDialogLoading;
import org.edx.mobile.view.dialog.AlertDialogFragment;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.activity.event.OnActivityResultEvent;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.activity.event.OnNewIntentEvent;
import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnRestartEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.activity.event.OnSaveInstanceStateEvent;
import roboguice.activity.event.OnStopEvent;
import roboguice.context.event.OnConfigurationChangedEvent;
import roboguice.context.event.OnCreateEvent;
import roboguice.context.event.OnDestroyEvent;
import roboguice.context.event.OnStartEvent;
import roboguice.event.EventManager;
import roboguice.inject.RoboInjector;
import roboguice.util.RoboContext;

public class TaBaseActivity extends RxAppCompatActivity implements RoboContext, ILoading {
    ILoading mLoading;

    protected EventManager eventManager;
    protected HashMap<Key<?>, Object> scopedObjects = new HashMap<Key<?>, Object>();
    protected boolean isInForeground = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final RoboInjector injector = RoboGuice.getInjector(this);
        eventManager = injector.getInstance(EventManager.class);
        injector.injectMembersWithoutViews(this);
        super.onCreate(savedInstanceState);
        eventManager.fire(new OnCreateEvent<Activity>(this, savedInstanceState));
        mLoading = new ProgressDialogLoading(this);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        preventInitialFocus();
    }

    protected void setLoading(ILoading loading) {
        mLoading = loading;
    }

    @Override
    public void show() {
        mLoading.show();
    }

    @Override
    public void hide() {
        mLoading.hide();
    }

    @Override
    public void dismiss() {
        mLoading.dismiss();
    }

    public void showErrorDialog(@Nullable String title, @NonNull String message) {
        AlertDialogFragment.showDialog(getSupportFragmentManager(),title, message);
    }

    public void showShortSnack(String msg){
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
    }

    private void preventInitialFocus() {
        final ViewGroup content = this.findViewById(android.R.id.content);
        final View root = content.getChildAt(0);
        if (root == null) return;
        final View focusDummy = new View(this);
        focusDummy.setFocusable(true);
        focusDummy.setFocusableInTouchMode(true);
        if (root instanceof ViewGroup) {
            if (!(root instanceof ScrollView)) {
                ((ViewGroup)root).addView(focusDummy, 0, new LinearLayout.LayoutParams(0, 0));
            }
        } else {
            content.addView(focusDummy, 0, new LinearLayout.LayoutParams(0, 0));
        }
    }

    public void askForPermissions(String[] permissions, int requestCode){
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        eventManager.fire(new OnSaveInstanceStateEvent(this, outState));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        eventManager.fire(new OnRestartEvent(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventManager.fire(new OnStartEvent<Activity>(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInForeground = true;
        eventManager.fire(new OnResumeEvent(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForeground = false;
        eventManager.fire(new OnPauseEvent(this));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        eventManager.fire(new OnNewIntentEvent(this));
    }

    @Override
    protected void onStop() {
        try {
            eventManager.fire(new OnStopEvent(this));
        } finally {
            super.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            eventManager.fire(new OnDestroyEvent<Activity>(this));
        } finally {
            try {
                RoboGuice.destroyInjector(this);
            } finally {
                super.onDestroy();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final Configuration currentConfig = getResources().getConfiguration();
        super.onConfigurationChanged(newConfig);
        eventManager.fire(new OnConfigurationChangedEvent<Activity>(this, currentConfig, newConfig));
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        RoboGuice.getInjector(this).injectViewMembers(this);
        eventManager.fire(new OnContentChangedEvent(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        eventManager.fire(new OnActivityResultEvent(this, requestCode, resultCode, data));
    }

    @Override
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }

    public boolean isInForeground() {
        return isInForeground;
    }
}
