package org.edx.mobile.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyboardUtil {

    /**
     * Hides the soft keyboard
     *
     * @param activity The reference of the activity displaying the keyboard
     */
    public static void hide(@NonNull final Activity activity) {
        final InputMethodManager iManager = (InputMethodManager) activity.
                getSystemService(Context.INPUT_METHOD_SERVICE);
        final View view = activity.getCurrentFocus();
        if (view != null && iManager != null) {
            iManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
