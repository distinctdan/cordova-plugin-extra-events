package org.apache.cordova.extraEvents;

import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import java.util.function.Consumer;

public class CdvExtraEventsWindowCallback implements Window.Callback {
    protected Window.Callback existingCallback;
    protected Consumer<Boolean> onWindowFocusedChangedCallback;

    public CdvExtraEventsWindowCallback(Window.Callback existingCallback,
                                        Consumer<Boolean> windowFocusChangedCallback) {
        this.existingCallback = existingCallback;
        this.onWindowFocusedChangedCallback = windowFocusChangedCallback;
    }

    public boolean dispatchKeyEvent (KeyEvent event) {
        return this.existingCallback.dispatchKeyEvent(event);
    }

    public boolean dispatchKeyShortcutEvent (KeyEvent event) {
        return this.existingCallback.dispatchKeyShortcutEvent(event);
    }

    public boolean dispatchTouchEvent (MotionEvent event) {
        return this.existingCallback.dispatchTouchEvent(event);
    }

    public boolean dispatchTrackballEvent (MotionEvent event) {
        return this.existingCallback.dispatchTrackballEvent(event);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return this.existingCallback.dispatchGenericMotionEvent(event);
    }

    public boolean dispatchPopulateAccessibilityEvent (AccessibilityEvent event) {
        return this.existingCallback.dispatchPopulateAccessibilityEvent(event);
    }

    public View onCreatePanelView (int featureId) {
        return this.existingCallback.onCreatePanelView(featureId);
    }

    public boolean onCreatePanelMenu (int featureId, Menu menu) {
        return this.existingCallback.onCreatePanelMenu(featureId, menu);
    }

    public boolean onPreparePanel (int featureId, View view, Menu menu) {
        return this.existingCallback.onPreparePanel(featureId, view, menu);
    }

    public boolean onMenuOpened (int featureId, Menu menu) {
        return this.existingCallback.onMenuOpened(featureId, menu);
    }

    public boolean onMenuItemSelected (int featureId, MenuItem item) {
        return this.existingCallback.onMenuItemSelected(featureId, item);
    }

    public void onWindowAttributesChanged (WindowManager.LayoutParams attrs) {
        this.existingCallback.onWindowAttributesChanged(attrs);
    }

    public void onContentChanged () {
        this.existingCallback.onContentChanged();
    }

    public void onWindowFocusChanged (boolean hasFocus) {
        this.onWindowFocusedChangedCallback.accept(hasFocus);
        this.existingCallback.onWindowFocusChanged(hasFocus);
    }

    public void onAttachedToWindow () {
        this.existingCallback.onAttachedToWindow();
    }

    public void onDetachedFromWindow () {
        this.existingCallback.onDetachedFromWindow();
    }

    public void onPanelClosed (int featureId, Menu menu) {
        this.existingCallback.onPanelClosed(featureId, menu);
    }

    public boolean onSearchRequested () {
        return this.existingCallback.onSearchRequested();
    }

    public boolean onSearchRequested (SearchEvent searchEvent) {
        return this.existingCallback.onSearchRequested(searchEvent);
    }

    public ActionMode onWindowStartingActionMode (ActionMode.Callback callback) {
        return this.existingCallback.onWindowStartingActionMode(callback);
    }

    public ActionMode onWindowStartingActionMode (ActionMode.Callback callback, int type) {
        return this.existingCallback.onWindowStartingActionMode(callback, type);
    }

    public void onActionModeStarted (ActionMode mode) {
        this.existingCallback.onActionModeStarted(mode);
    }

    public void onActionModeFinished (ActionMode mode) {
        this.existingCallback.onActionModeFinished(mode);
    }
}