package org.edx.mobile.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.edx.mobile.R;

/**
 * Created by marcashman on 2014-12-18.
 */
public class InstallFacebookDialog extends DialogFragment {

    private static final String FACEBOOK_INSTALL_URL = "https://play.google.com/store/apps/details?id=com.facebook.katana";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(getString(R.string.dialog_fbinstall_title))
                .setMessage(getString(R.string.dialog_fbinstall_msg))
                .setPositiveButton(getString(R.string.dialog_fbinstall_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(FACEBOOK_INSTALL_URL));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
