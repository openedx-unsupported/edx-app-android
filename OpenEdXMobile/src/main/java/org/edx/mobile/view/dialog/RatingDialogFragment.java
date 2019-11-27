package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.RatingBar;

import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.databinding.FragmentDialogRatingBinding;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.Router;

import java.util.HashMap;
import java.util.Map;

import roboguice.fragment.RoboDialogFragment;

public class RatingDialogFragment extends RoboDialogFragment implements AlertDialog.OnShowListener,
        RatingBar.OnRatingBarChangeListener {
    @Inject
    private Router mRouter;
    @Inject
    private AnalyticsRegistry analyticsRegistry;
    private AlertDialog mAlertDialog;
    @NonNull
    private FragmentDialogRatingBinding binding;
    @Nullable
    private OnCancelListener onCancelListener;

    public static RatingDialogFragment newInstance(@Nullable OnCancelListener onCancelListener) {
        RatingDialogFragment fragment = new RatingDialogFragment();
        fragment.setOnCancelListener(onCancelListener);
        return fragment;
    }

    public void setOnCancelListener(@NonNull OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    private void callCancelListener() {
        if (onCancelListener != null) {
            onCancelListener.onCancel();
        }
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
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        persistRatingAndAppVersion(AppConstants.APP_ZERO_RATING);
                        callCancelListener();
                        analyticsRegistry.trackAppRatingDialogCancelled(BuildConfig.VERSION_NAME);
                    }
                })
                .setView(binding.getRoot())
                .create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.setOnShowListener(this);
        return mAlertDialog;
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        persistRatingAndAppVersion(AppConstants.APP_ZERO_RATING);
        callCancelListener();
        analyticsRegistry.trackAppRatingDialogCancelled(BuildConfig.VERSION_NAME);
    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (binding.ratingBar.getRating() <= 0.0f) {
            mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
        // Send rating dialog viewed analytics
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.APP_VERSION, BuildConfig.VERSION_NAME);
        analyticsRegistry.trackScreenView(Analytics.Screens.APP_REVIEWS_VIEW_RATING, null, null, values);
        analyticsRegistry.trackAppRatingDialogViewed(BuildConfig.VERSION_NAME);
    }

    public void submit() {
        persistRatingAndAppVersion(binding.ratingBar.getRating());
        // Next action
        if (binding.ratingBar.getRating() <= AppConstants.APP_NEGATIVE_RATING_THRESHOLD) {
            showFeedbackDialog(getActivity());
        } else {
            showRateTheAppDialog();
        }
        analyticsRegistry.trackUserSubmitRating(BuildConfig.VERSION_NAME, (int) binding.ratingBar.getRating());
        // Close dialog
        getDialog().dismiss();
    }

    public void showFeedbackDialog(final FragmentActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.feedback_dialog_title);
        builder.setMessage(R.string.feedback_dialog_message);
        builder.setPositiveButton(R.string.label_send_feedback, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Submit feedback
                mRouter.showFeedbackScreen(activity, activity.getString(R.string.review_email_subject));
                analyticsRegistry.trackUserSendFeedback(BuildConfig.VERSION_NAME,
                        (int) binding.ratingBar.getRating());
            }
        });
        builder.setNegativeButton(R.string.label_maybe_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                persistRatingAndAppVersion(AppConstants.APP_ZERO_RATING);
                callCancelListener();
                analyticsRegistry.trackUserMayReviewLater(BuildConfig.VERSION_NAME,
                        (int) binding.ratingBar.getRating());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                persistRatingAndAppVersion(AppConstants.APP_ZERO_RATING);
                callCancelListener();
            }
        });
    }

    public void showRateTheAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.rate_app_dialog_title);
        builder.setMessage(ResourceUtil.getFormattedString(
                getResources(), R.string.rate_app_dialog_message,
                "platform_name", getString(R.string.platform_name)));
        builder.setPositiveButton(R.string.label_rate_the_app, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Open app in store for rating
                AppStoreUtils.openAppInAppStore(((Dialog) dialogInterface).getContext());
                analyticsRegistry.trackRateTheAppClicked(BuildConfig.VERSION_NAME,
                        (int) binding.ratingBar.getRating());
            }
        });
        builder.setNegativeButton(R.string.label_maybe_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                persistRatingAndAppVersion(AppConstants.APP_ZERO_RATING);
                callCancelListener();
                analyticsRegistry.trackUserMayReviewLater(BuildConfig.VERSION_NAME,
                        (int) binding.ratingBar.getRating());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                persistRatingAndAppVersion(AppConstants.APP_ZERO_RATING);
                callCancelListener();
            }
        });
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

    public void persistRatingAndAppVersion(final float rating) {
        // Persist rating and current app version name
        final PrefManager.AppInfoPrefManager appPrefs = new PrefManager.AppInfoPrefManager(MainApplication.application);
        appPrefs.setAppRating(rating);
        appPrefs.setLastRatedVersion(BuildConfig.VERSION_NAME);
    }

    public interface OnCancelListener {
        /**
         * This callback will be called when the user will close the rating dialog without performing
         * any action (i.e. open play store for rating or giving feedback).
         */
        void onCancel();
    }
}
