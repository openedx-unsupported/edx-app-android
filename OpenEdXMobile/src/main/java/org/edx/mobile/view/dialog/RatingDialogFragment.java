package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.RatingBar;

import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.databinding.FragmentDialogRatingBinding;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.Router;

import roboguice.fragment.RoboDialogFragment;

public class RatingDialogFragment extends RoboDialogFragment implements AlertDialog.OnShowListener,
        RatingBar.OnRatingBarChangeListener {
    @Inject
    private Router mRouter;
    private AlertDialog mAlertDialog;
    @NonNull
    private FragmentDialogRatingBinding binding;

    public static RatingDialogFragment newInstance() {
        return new RatingDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getActivity().getLayoutInflater(),
                R.layout.fragment_dialog_rating, null, false);
        binding.ratingBar.setOnRatingBarChangeListener(this);
        binding.tvDescription.setText(R.string.rating_dialog_message);
        mAlertDialog = new AlertDialog.Builder(getContext())
                .setPositiveButton(getString(R.string.label_submit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        submit();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(binding.getRoot())
                .create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.setOnShowListener(this);
        return mAlertDialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (binding.ratingBar.getRating() <= 0.0f) {
            mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    public void submit() {
        final Dialog dialog = getDialog();
        // Persist rating and current version name
        PrefManager.UserPrefManager userPrefs = new PrefManager.UserPrefManager(MainApplication.application);
        userPrefs.setAppRating(binding.ratingBar.getRating());
        userPrefs.setLastRatedVersion(BuildConfig.VERSION_NAME);
        // Next action
        if (binding.ratingBar.getRating() <= AppConstants.APP_NEGATIVE_RATING_THRESHOLD) {
            showFeedbackDialog(getActivity());
        } else {
            showRateTheAppDialog();
        }
        // Close dialog
        dialog.dismiss();
    }

    public void showFeedbackDialog(final FragmentActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.feedback_dialog_title);
        builder.setMessage(R.string.feedback_dialog_message);
        builder.setPositiveButton(R.string.label_send_feedback, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Submit feedback
                mRouter.showFeedbackScreen(activity, activity.getString(R.string.review_email_subject));
            }
        });
        builder.setNegativeButton(R.string.label_maybe_later, null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void showRateTheAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.rate_app_dialog_title);
        builder.setMessage(ResourceUtil.getFormattedString(
                getResources(), R.string.rate_app_dialog_message,
                "platform_name", getString(R.string.platform_name)));
        builder.setPositiveButton(R.string.label_rate_the_app, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Open app in store for rating
                AppStoreUtils.openAppInAppStore(((Dialog) dialog).getContext());
            }
        });
        builder.setNegativeButton(R.string.label_maybe_later, null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        final Button positiveButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (rating > 0.0f) {
            positiveButton.setEnabled(true);
        } else {
            positiveButton.setEnabled(false);
        }
    }
}
