package org.edx.mobile.view.dialog;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.edx.mobile.R;

/**
 * Created by yervant on 1/20/15.
 */
public class SimpleAlertDialog extends DialogFragment {

    private static final String TAG = SimpleAlertDialog.class.getCanonicalName();

    public static final String EXTRA_TITLE = TAG + ".extra_title";
    public static final String EXTRA_MESSAGE = TAG + ".extra_message";

    public static SimpleAlertDialog newInstance(Bundle args) {
        SimpleAlertDialog frag = new SimpleAlertDialog();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_single_button,
                container, false);
        TextView txtTitle = (TextView) v.findViewById(R.id.tv_dialog_title);
        TextView txtDialogMessageOne = (TextView) v
                .findViewById(R.id.tv_dialog_message1);

        txtTitle.setText(getArguments().getString(EXTRA_TITLE));
        txtDialogMessageOne.setText(getArguments().getString(EXTRA_MESSAGE));
        // Watch for button clicks.

        Button btnPositive = (Button) v.findViewById(R.id.positiveButton);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
}
