package org.edx.mobile.http.notifications;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * A full screen error notification message on top of the content area.
 */
public class FullScreenErrorNotification extends ErrorNotification {
    /**
     * The content view.
     */
    @NonNull
    private final View view;

    /**
     * The error content view.
     */
    @Nullable
    private ViewGroup errorLayout;

    /**
     * Construct a new instance of the notification.
     *
     * @param view The content view, on which the notification will be overlaid.
     */
    public FullScreenErrorNotification(@NonNull final View view) {
        this.view = view;
    }

    /**
     * Show the error notification as an overlay message on top of the content area, according to
     * the provided details.
     * <p>
     * The root view will be determined by walking up the view tree to see if there is any view with
     * the ID of {@link R.id#content_error R.id.content_error}. If one is found, then that would be
     * used as the root. If not, then if the content view's parent is already a FrameLayout, then
     * that would be used; otherwise a new one will be created, inserted as the new parent, and used
     * as the root.
     *
     * @param errorResId      The resource ID of the error message.
     * @param icon            The error icon.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    @Override
    public void showError(@StringRes final int errorResId,
                          @Nullable final Icon icon,
                          @StringRes final int actionTextResId,
                          @Nullable final View.OnClickListener actionListener) {
        showError(view.getContext().getString(errorResId), icon, actionTextResId, actionListener);
    }

    /**
     * Show the error notification according to the provided details.
     *
     * @param errorResId      The resource ID of the error message.
     * @param icon            The error icon.
     * @param actionTextResId The resource ID of the action button text.
     * @param duration        The duration of the error message visibility
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    @Override
    public void showError(int errorResId, @Nullable Icon icon, int actionTextResId, int duration, @Nullable View.OnClickListener actionListener) {
        // Ignoring the duration as it won't be use in Dialog
        showError(errorResId, icon, actionTextResId, actionListener);
    }

    /**
     * Show the error notification as an overlay message on top of the content area, according to
     * the provided details.
     * <p>
     * The root view will be determined by walking up the view tree to see if there is any view with
     * the ID of {@link R.id#content_error R.id.content_error}. If one is found, then that would be
     * used as the root. If not, then if the content view's parent is already a FrameLayout, then
     * that would be used; otherwise a new one will be created, inserted as the new parent, and used
     * as the root.
     *
     * @param errorMsg        The error message.
     * @param icon            The error icon.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    public void showError(@NonNull final String errorMsg,
                          @Nullable final Icon icon,
                          @StringRes final int actionTextResId,
                          @Nullable final View.OnClickListener actionListener) {
        final ViewGroup root = findSuitableAncestorLayout();
        if (root == null) return;

        final View layoutView = root.findViewById(R.id.content_error);
        if (layoutView instanceof ViewGroup) {
            errorLayout = (ViewGroup) layoutView;
        } else {
            final LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
            errorLayout = (ViewGroup) layoutInflater.inflate(R.layout.content_error, root, false);
            root.addView(errorLayout);
        }
        final TextView messageView = (TextView) errorLayout.findViewById(R.id.content_error_text);
        final Button actionButton = (Button) errorLayout.findViewById(R.id.content_error_action);
        final IconImageView iconView = (IconImageView) errorLayout.findViewById(R.id.content_error_icon);

        messageView.setText(errorMsg);
        if (icon == null) {
            iconView.setVisibility(GONE);
        } else {
            iconView.setVisibility(VISIBLE);
            iconView.setIcon(icon);
        }
        if (actionTextResId == 0 || actionListener == null) {
            actionButton.setVisibility(GONE);
        } else {
            actionButton.setVisibility(VISIBLE);
            actionButton.setText(actionTextResId);
            actionButton.setOnClickListener(actionListener);
        }

        view.setVisibility(GONE);
        errorLayout.setVisibility(VISIBLE);
    }

    /**
     * Hides the currently displayed error and shows the content that was previously hidden.
     */
    public void hideError() {
        if (errorLayout != null) {
            errorLayout.setVisibility(View.GONE);
        }
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Tells if the error is currently visible or not.
     *
     * @return <code>true</code> if the error is visible, <code>false</code> otherwise.
     */
    public boolean isShowing() {
        return errorLayout != null && errorLayout.getVisibility() == View.VISIBLE;
    }

    /**
     * Find a suitable layout among the ancestors of the content view to serve as the root view on
     * which the notification can be added, or attempt to create one if not found.
     * <p>
     * The root view will be determined by walking up the view tree to see if there is any view with
     * the ID of {@link R.id#content_error R.id.content_error}. If one is found, then that would be
     * used as the root. If not, then if the content view's parent is already a FrameLayout, then
     * that would be used; otherwise a new one will be created, inserted as the new parent, and used
     * as the root.
     *
     * @return The root view on which to add the notification layout, or null if the view is not
     * part of an existing layout.
     */
    @Nullable
    private ViewGroup findSuitableAncestorLayout() {
        for (View ancestor = view; view.getId() != android.R.id.content; ) {
            if (ancestor instanceof ViewGroup && ancestor.getId() == R.id.content_error_root) {
                return (ViewGroup) ancestor;
            }
            final ViewParent parent = ancestor.getParent();
            if (!(parent instanceof View)) break;
            ancestor = (View) parent;
        }

        final ViewParent viewParent = view.getParent();

        if (viewParent instanceof FrameLayout) {
            return (ViewGroup) viewParent;
        }

        if (viewParent instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup) viewParent;
            final int index = parent.indexOfChild(view);
            parent.removeView(view);
            final FrameLayout newParent = new FrameLayout(view.getContext());
            parent.addView(newParent, index, view.getLayoutParams());
            newParent.addView(view, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            return newParent;
        }

        return null;
    }
}
