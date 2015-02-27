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

import java.util.Map;

public class SuccessDialogFragment extends DialogFragment {

    public static SuccessDialogFragment newInstance(
            Map<String, String> dialogMap) {
        SuccessDialogFragment frag = new SuccessDialogFragment();

        Bundle args = new Bundle();

        args.putString("title", dialogMap.get("title"));
        args.putString("dialog_msg_1", dialogMap.get("message_1"));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_reset_successful_dialog,
                container, false);
        TextView txtTitle = (TextView) v.findViewById(R.id.tv_dialog_title);
        TextView txtDialogMessageOne = (TextView) v
                .findViewById(R.id.tv_dialog_message1);

        txtTitle.setText(getArguments().getString("title"));
        txtDialogMessageOne.setText(getArguments().getString("dialog_msg_1"));
        // Watch for button clicks.
        Button btnPositive = (Button) v.findViewById(R.id.positiveButton);
        btnPositive.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Dismiss the dialog on button click
                dismiss();
            }
        });
        return v;
    }
}