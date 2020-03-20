package org.edx.mobile.view;

import androidx.annotation.NonNull;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.AuthPanelBinding;

/**
 * The "auth panel" is the panel that appears at the bottom of the screen when not logged in.
 * It has options to "Log In" or "Sign Up". It only appears if NEW_LOGISTRATION is enabled.
 */
public class AuthPanelUtils {

    // Should use AuthPanelBinding variant of this function, once callers switch to data binding
    @Deprecated
    public static void configureAuthPanel(@NonNull final View rootView, @NonNull final IEdxEnvironment environment) {
        setAuthPanelVisible(shouldAuthPanelBeVisible(environment), rootView,
                rootView.findViewById(R.id.log_in), rootView.findViewById(R.id.sign_up), environment);
    }

    public static void configureAuthPanel(@NonNull final AuthPanelBinding binding, @NonNull final IEdxEnvironment environment) {
        setAuthPanelVisible(shouldAuthPanelBeVisible(environment), binding, environment);
    }

    public static boolean shouldAuthPanelBeVisible(@NonNull final IEdxEnvironment environment) {
        return null == environment.getLoginPrefs().getUsername()
                && environment.getConfig().isNewLogistrationEnabled();
    }

    public static void setAuthPanelVisible(boolean visible, @NonNull final AuthPanelBinding binding, @NonNull final IEdxEnvironment environment) {
        setAuthPanelVisible(visible, binding.getRoot(), binding.logIn, binding.signUp, environment);
    }

    private static void setAuthPanelVisible(boolean visible, @NonNull final View rootView, @NonNull final View logInButton, @NonNull final View signUpButton, @NonNull final IEdxEnvironment environment) {
        if (visible) {
            rootView.setVisibility(View.VISIBLE);
            logInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(environment.getRouter().getLogInIntent());
                }
            });
            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    environment.getAnalyticsRegistry().trackUserSignUpForAccount();
                    v.getContext().startActivity(environment.getRouter().getRegisterIntent());
                }
            });
        } else {
            rootView.setVisibility(View.GONE);
        }
    }
}
