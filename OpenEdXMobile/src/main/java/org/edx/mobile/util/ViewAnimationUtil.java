package org.edx.mobile.util;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import org.edx.mobile.R;

public class ViewAnimationUtil {

    private static final long DEFAULT_ANIMATION_DURATION_MS = 500L;
    private static final float ANIMATION_DISPLACEMENT = 60f;

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
     * Create & return object moving animation based on direction
     *
     * @param textView  the view to animate
     * @param isAnimLTR true if screen required animation is LTR false else wise
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
