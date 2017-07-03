package org.edx.mobile.http.notifications;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.util.NetworkUtil;

import static android.support.design.widget.Snackbar.LENGTH_INDEFINITE;

/**
 * A persistent Snackbar notification error message.
 */
public class SnackbarErrorNotification extends ErrorNotification {
    /**
     * A view from the content layout.
     */
    @NonNull
    private final View view;

    /**
     * The snack bar being shown.
     */
    @Nullable
    private Snackbar snackbar = null;

    /**
     * Construct a new instance of the notification.
     *
     * @param view A view from the content layout, used to seek an appropriate anchor for the
     *             Snackbar.
     */
    public SnackbarErrorNotification(@NonNull final View view) {
        this.view = view;
    }

    /**
     * Show the error notification as a persistent Snackbar, according to the provided details.
     *
     * @param errorResId      The resource ID of the error message.
     * @param icon            The error icon. This is ignored here, since Snackbar doesn't really support
     *                        icons.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    @Override
    public void showError(@StringRes final int errorResId,
                          @Nullable final Icon icon,
                          @StringRes final int actionTextResId,
                          @Nullable final View.OnClickListener actionListener) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, errorResId, LENGTH_INDEFINITE);
            if (actionTextResId != 0) {
                // SnackBar automatically dimisses when the action item is pressed.
                // This workaround has been implemented to by pass that behaviour.
                snackbar.setAction(actionTextResId, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    snackbar = null;
                }
            });
            // By applying the listener to the button like we have done below, the Snackbar
            // doesn't automatically dismiss and we have to manually dismiss it.
            final Button actionButton = (Button) snackbar.getView().findViewById(android.support.design.R.id.snackbar_action);
            actionButton.setOnClickListener(actionListener);
            snackbar.show();
        }
    }

    /**
     * Show the error notification as a persistent Snackbar, for offline mode.
     *
     * @param listener The {@link RefreshListener} to use when action item is pressed on Snackbar.
     */
    public void showOfflineError(final RefreshListener listener) {
        showError(R.string.offline_text, FontAwesomeIcons.fa_wifi,
                R.string.lbl_reload, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (NetworkUtil.isConnected(MainApplication.application)) {
                            listener.onRefresh();
                            hideError();
                        }
                    }
                });
    }

    /**
     * Hides the currently displayed error.
     */
    public void hideError() {
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
    }
}
