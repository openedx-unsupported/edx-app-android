package org.edx.mobile.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.widget.LikeView;

import org.edx.mobile.R;
import org.edx.mobile.util.SocialUtils;

/**
 * Multiple social affirmation buttons can be added here
 * Created by Sumeshjohn on 2014-11-24.
 */
public class SocialAffirmView extends FrameLayout {

    private View activeView;

    public SocialAffirmView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private View initializeFacebookAffirmViews(String url) {
        //Set up facebook like
        LikeView facebookLike = new LikeView(getContext());
        facebookLike.setLikeViewStyle(LikeView.Style.STANDARD);
        facebookLike.setAuxiliaryViewPosition(LikeView.AuxiliaryViewPosition.INLINE);
        facebookLike.setObjectId(url);
        facebookLike.setForegroundColor(getResources().getColor(R.color.grey_5));
        return facebookLike;

    }

    public void setSocialAffirmType(SocialUtils.SocialType type, String url) {

        if (activeView != null){
            removeView(activeView);
            activeView = null;
        }

        switch (type) {
            case FACEBOOK:
                activeView = initializeFacebookAffirmViews(url);
                break;
            default:
                return;
        }
        addView(activeView);

    }

}
