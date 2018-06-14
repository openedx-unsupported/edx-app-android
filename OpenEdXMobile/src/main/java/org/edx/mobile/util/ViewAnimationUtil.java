package org.edx.mobile.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.edx.mobile.R;

public class ViewAnimationUtil {
    // Prohibit instantiation
    private ViewAnimationUtil() {
        throw new UnsupportedOperationException();
    }

    public static void showMessageBar(View messageView) {
        showMessageBar(messageView, false);
    }

    public static void showMessageBar(View messageView, boolean isPersistent) {
        messageView.setVisibility(View.VISIBLE);
        Context context = messageView.getContext();
        Animation animate;
        if (isPersistent) {
            animate = AnimationUtils.loadAnimation(context, R.anim.show_msg_bar);
        } else {
            animate = AnimationUtils.loadAnimation(context, R.anim.show_msg_bar_temp);
            animate.setAnimationListener(new HideListener(messageView));
        }
        messageView.startAnimation(animate);
    }


    public static void hideMessageBar(View messageView) {
        if (messageView.getVisibility() == View.VISIBLE) {
            Animation animate = AnimationUtils.loadAnimation(
                    messageView.getContext(), R.anim.hide_msg_bar);
            animate.setAnimationListener(new HideListener(messageView));
            messageView.startAnimation(animate);
        }
    }


    //To clear animation set previously
    public static void stopAnimation(View view) {
        view.clearAnimation();
    }

    private static class HideListener implements Animation.AnimationListener {
        private final View view;

        HideListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            view.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * Animates the fade in/out of view within a specific duration.
     *
     * @param view       The view to animate.
     * @param visibility Visibility to set after the view has finished animating.
     */
    public static void fadeViewTo(@NonNull final View view, final int visibility) {
        final int durationMs = 1000;
        final boolean animationNeeded = view.getVisibility() != visibility;
        if (!animationNeeded) {
            return;
        }
        // View needs to be visible for the animation to be visible
        view.setVisibility(View.VISIBLE);
        final int alpha = visibility == View.VISIBLE ? 1 : 0;
        view.animate()
                .alpha(alpha)
                .setDuration(durationMs)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(visibility);
                    }
                });

    }
}
