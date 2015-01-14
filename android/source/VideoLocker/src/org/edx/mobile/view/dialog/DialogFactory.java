package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

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
}
