package org.edx.mobile.view.dialog;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.storage.BulkVideosDownloadCancelledEvent;

import java.util.Map;

import de.greenrobot.event.EventBus;

public class DownloadSizeExceedDialog extends DialogFragment {

    private final Logger logger = new Logger(getClass().getName());
    private IDialogCallback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * TODO: As a quick fix, avoid the recreation of dialog on orientation change to avoid
         * problematic scenarios For e.g. cancel button stops working on orientation change, we
         * have to fix it properly which is explained and planned to implement in story LEARNER-2177
         */
        if (savedInstanceState != null) {
            dismiss();
            EventBus.getDefault().post(new BulkVideosDownloadCancelledEvent());
        }
    }

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
        TextView title_tv = (TextView) v.findViewById(R.id.tv_dialog_title);
        TextView dialog_tv_1 = (TextView) v
                .findViewById(R.id.tv_dialog_message1);

        title_tv.setText(getArguments().getString("title"));
        dialog_tv_1.setText(getArguments().getString("dialog_msg_1"));
        // Watch for button clicks.
        final Button positiveBtn = (Button) v.findViewById(R.id.positiveButton);
        positiveBtn.setText(getString(R.string.label_download));
        positiveBtn.setOnClickListener(new OnClickListener() {
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

        Button negativeBtn = (Button) v.findViewById(R.id.negativeButton);
        negativeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (callback != null) {
                    callback.onNegativeClicked();
                }
            }
        });

        return v;
    }


}
