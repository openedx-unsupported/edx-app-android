package org.edx.mobile.interfaces;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * {@link android.app.Activity#onActivityResult(int, int, Intent)} is a protected function in the
 * {@link android.app.Activity} class. This interface is designed to make it publicly accessible
 * to {@link DialogFragment} implementations.
 */
public interface OnActivityResultListener {
    /**
     * <p>Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link android.app.Activity#RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.</p>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.</p>
     * <p>This method is never invoked if your activity sets
     * {@link android.R.styleable#AndroidManifestActivity_noHistory noHistory} to
     * <code>true</code>.</p>
     * <p>This method may also be invoked manually by a {@link DialogFragment}.</p>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    /**
     * We can't have concrete functions inside interfaces till Java 8, therefore this
     * class has been defined to add static utilities to this interface.
     */
    class Util {
        /**
         * Utility function to deliver a result to the base Fragment/Activity.
         *
         * @param dialogFragment The DialogFragment that is delivering the result.
         * @param requestCode    The integer request code originally supplied while creating
         *                       the DialogFragment, allowing you to identify who this
         *                       result came from.
         * @param resultCode     The integer result code returned by the DialogFragment.
         * @param data           An Intent, which can return result data to the caller
         *                       (various data can be attached to Intent "extras").
         */
        public static void deliverResult(@NonNull final DialogFragment dialogFragment,
                                         final int requestCode, final int resultCode,
                                         @Nullable final Intent data) {
            final OnActivityResultListener listener;
            final Fragment parentFragment = dialogFragment.getParentFragment();
            if (parentFragment instanceof OnActivityResultListener) {
                listener = (OnActivityResultListener) parentFragment;
            } else {
                final Activity activity = dialogFragment.getActivity();
                if (activity instanceof OnActivityResultListener) {
                    listener = (OnActivityResultListener) activity;
                } else {
                    return;
                }
            }
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }
}
