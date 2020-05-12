package org.edx.mobile.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import org.edx.mobile.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ViewAnimationUtil {

    private static final long    DEFAULT_ANIMATION_DURATION_MS = 500L;
    private static final float   ANIMATION_DISPLACEMENT = 60f;

    // Prohibit instantiation
    private ViewAnimationUtil() {
        throw new UnsupportedOperationException();
    }

    public static void showMessageBar(View messageView) {
        showMessageBar(messageView, false);
    }

    public static void showMessageBar(View messageView, boolean isPersistent) {
        messageView.setVisibility(VISIBLE);
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
        if (messageView.getVisibility() == VISIBLE) {
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
            view.setVisibility(GONE);
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
        view.setVisibility(VISIBLE);
        final int alpha = visibility == VISIBLE ? 1 : 0;
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

    /**
     * Create & return object moving animation based on direction
     * @param textView     the view to animate
     * @param isAnimLTR    true if screen required animation is LTR false else wise
     * @return {@linkplain TranslateAnimation}
     */
    public static TranslateAnimation getSeekTimeAnimation(TextView textView, boolean isAnimLTR) {
        TranslateAnimation translateAnimation = new TranslateAnimation(0.0f,
                isAnimLTR ? ANIMATION_DISPLACEMENT : -ANIMATION_DISPLACEMENT, 0.0f, 0.0f);
        translateAnimation.setDuration(DEFAULT_ANIMATION_DURATION_MS);
        translateAnimation.setInterpolator(new DecelerateInterpolator());
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                textView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return translateAnimation;
    }
}
