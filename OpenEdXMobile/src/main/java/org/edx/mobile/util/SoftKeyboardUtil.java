package org.edx.mobile.util;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyboardUtil {

    /**
     * Hides the soft keyboard.
     *
     * @param activity The reference of the activity displaying the keyboard.
     */
    public static void hide(@NonNull final Activity activity) {
        final InputMethodManager iManager = (InputMethodManager) activity.
                getSystemService(Context.INPUT_METHOD_SERVICE);
        final View view = activity.getCurrentFocus();
        if (view != null && iManager != null) {
            iManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Hides the soft keyboard.
     *
     * @param view The view object's reference from which we'll get the window token.
     */
    public static void hide(@NonNull final View view) {
        final InputMethodManager iManager = (InputMethodManager) view.getContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (iManager != null) {
            iManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Hides the soft keyboard by clearing view's focus.
     *
     * @param view The view whose focus needs to be cleared.
     */
    public static void clearViewFocus(@NonNull final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.clearFocus();
            }
        });
    }
}
