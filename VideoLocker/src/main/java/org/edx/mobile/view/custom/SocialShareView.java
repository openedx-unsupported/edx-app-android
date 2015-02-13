package org.edx.mobile.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.util.SocialUtils;


public class SocialShareView extends FrameLayout {

    private OnClickListener storedListener;
    private View activeButton;

    public SocialShareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSocialShareType(SocialUtils.SocialType type) {

        if (activeButton != null){
            removeView(activeButton);
            activeButton = null;
        }

        switch (type) {
            case FACEBOOK:
                activeButton = initializeFacebookShareView();
                break;
            default:
                return;
        }

        addView(activeButton);

    }

    private View initializeFacebookShareView() {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        TextView shareButton = (TextView) inflater.inflate(R.layout.view_fb_share, this, false);
        shareButton.setText(R.string.course_details_share);

        return shareButton;

    }

}
