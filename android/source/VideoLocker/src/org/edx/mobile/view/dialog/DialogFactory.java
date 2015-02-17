package org.edx.mobile.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import org.edx.mobile.R;
import org.edx.mobile.util.BrowserUtil;

/**
 * This class will help creating Dialog objects for different screens and events. 
 *
 */
public class DialogFactory {

    public static Dialog createLoginInProgressDialog(Context context, 
            String title, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        return progressDialog;
    }
    
    public static ProgressDialogFragment createProgressDialog(Context context) {
        ProgressDialogFragment progressDialog = new ProgressDialogFragment();
        progressDialog.setCancelable(true);
        return progressDialog;
    }

    /**
     * Returns a confirmation dialog informing user that they may get charged for browsing given URL
     * and asking user whether they want to proceed browsing or not.
     * @param activity
     * @param uri
     * @return
     */
    public static Dialog getChargesApplyConfirmationDialog(final FragmentActivity activity, final String uri) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(activity.getString(R.string.open_external_url_title));
        dialog.setMessage(activity.getString(R.string.open_external_url_desc));
        dialog.setPositiveButton(R.string.label_continue, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int which) {
                BrowserUtil.open(activity, uri);
                d.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                // do nothing
                d.dismiss();
            }
        });
        dialog.setCancelable(false);
        return dialog.create();
    }
}
