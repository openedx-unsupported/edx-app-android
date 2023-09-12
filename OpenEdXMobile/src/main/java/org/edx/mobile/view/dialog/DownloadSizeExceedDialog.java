package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentVideosizeExceedsDialogBinding;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.storage.BulkVideosDownloadCancelledEvent;
import org.greenrobot.eventbus.EventBus;

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

    public static DownloadSizeExceedDialog newInstance(IDialogCallback callback) {
        DownloadSizeExceedDialog dialog = new DownloadSizeExceedDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        dialog.setCancelable(false);
        dialog.callback = callback;
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentVideosizeExceedsDialogBinding binding = FragmentVideosizeExceedsDialogBinding.inflate(inflater, container, false);

        binding.tvDialogTitle.setText(getString(R.string.download_exceed_title));
        binding.tvDialogMessage1.setText(getString(R.string.download_exceed_message));

        binding.positiveButton.setText(getString(R.string.label_download));
        binding.positiveButton.setOnClickListener(v -> {
            try {
                if (callback != null) {
                    callback.onPositiveClicked();
                }
                dismiss();
            } catch (Exception e) {
                logger.error(e);
            }
        });

        binding.negativeButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onNegativeClicked();
            }
            dismiss();
        });

        return binding.getRoot();
    }
}
