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
        TextView title_tv = (TextView) v.findViewById(R.id.tv_dialog_title);
        TextView dialog_tv_1 = (TextView) v
                .findViewById(R.id.tv_dialog_message1);

        title_tv.setText(getArguments().getString(EXTRA_TITLE));
        dialog_tv_1.setText(getArguments().getString(EXTRA_MESSAGE));
        // Watch for button clicks.

        Button button = (Button) v.findViewById(R.id.positiveButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
}
