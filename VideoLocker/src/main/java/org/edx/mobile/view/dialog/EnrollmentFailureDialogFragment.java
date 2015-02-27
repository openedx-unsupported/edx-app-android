package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;

import java.util.Map;

public class EnrollmentFailureDialogFragment extends DialogFragment {

    private final Logger logger = new Logger(getClass().getName());

    private IDialogCallback callback;

    public void EnrollmentFailureDialogFragment(){}

    public static EnrollmentFailureDialogFragment newInstance(
            Map<String, String> dialogMap, IDialogCallback callback) {
        EnrollmentFailureDialogFragment frag = new EnrollmentFailureDialogFragment();

        frag.callback = callback;
        Bundle args = new Bundle();

        args.putString("dialog_msg_1", dialogMap.get("message_1"));
        args.putString("dialog_yes_btn", dialogMap.get("yes_button"));
        args.putString("dialog_no_btn", dialogMap.get("no_button"));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.delete_video_dialog, container,
                false);
        TextView txtTitle = (TextView) v.findViewById(R.id.tv_dialog_title);
        txtTitle.setVisibility(View.GONE);
        TextView txtDialogMessage = (TextView) v
                .findViewById(R.id.tv_dialog_message1);

        txtDialogMessage.setText(getArguments().getString("dialog_msg_1"));
        // Watch for button clicks.
        Button btnPositive = (Button) v.findViewById(R.id.positiveButton);
        btnPositive.setText(getArguments().getString("dialog_yes_btn"));
        btnPositive.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    if (callback != null && isVisible()) {
                        callback.onPositiveClicked();
                        dismiss();
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        });

        Button btnNegative = (Button) v.findViewById(R.id.negativeButton);
        btnNegative.setText(getArguments().getString("dialog_no_btn").toUpperCase());
        btnNegative.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (callback != null && isVisible()) {
                    callback.onNegativeClicked();
                    dismiss();
                }
            }
        });
        return v;
    }
}
