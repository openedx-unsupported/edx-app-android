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

public class DownloadSizeExceedDialog extends DialogFragment {

    private final Logger logger = new Logger(getClass().getName());
    private IDialogCallback callback;

    public static DownloadSizeExceedDialog newInstance(
            Map<String, String> dialogMap, IDialogCallback callback) {
        DownloadSizeExceedDialog frag = new DownloadSizeExceedDialog();

        frag.callback = callback;
        Bundle args = new Bundle();

        args.putString("title", dialogMap.get("title"));
        args.putString("dialog_msg_1", dialogMap.get("message_1"));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_videosize_exceeds_dialog, container,
                false);
        TextView txtTitle = (TextView) v.findViewById(R.id.tv_dialog_title);
        TextView txtDialogMessage = (TextView) v
                .findViewById(R.id.tv_dialog_message1);

        txtTitle.setText(getArguments().getString("title"));
        txtDialogMessage.setText(getArguments().getString("dialog_msg_1"));
        // Watch for button clicks.
        final Button btnPositive = (Button) v.findViewById(R.id.positiveButton);
        btnPositive.setText(getString(R.string.label_download));
        btnPositive.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    dismiss();
                    if (callback != null) {
                        callback.onPositiveClicked();
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        });

        Button btnNegative = (Button) v.findViewById(R.id.negativeButton);
        btnNegative.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (callback != null) {
                    callback.onNegativeClicked();
                }
            }
        });

        return v;
    }


}
