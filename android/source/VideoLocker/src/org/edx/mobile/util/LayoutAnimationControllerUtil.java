package org.edx.mobile.util;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import android.os.Handler;
import android.view.View;
import android.view.animation.TranslateAnimation;

public class LayoutAnimationControllerUtil {
    private View messageView;
    private Handler mHideHandler = new Handler();
    private final Logger logger = new Logger(getClass().getName());

    public LayoutAnimationControllerUtil(View notificationView) {
        messageView = notificationView;
    }

    public void showMessageBar() {
        if(messageView!=null){
            messageView.setVisibility(View.VISIBLE);
            mHideHandler.postDelayed(mHideRunnable, messageView
                    .getResources().getInteger(R.integer.message_delay));
            slideToBottom();    
        }
    }


    public void hideMessageBar() {
        slideToTop();
    }


    public void hideView(View view) {
        try{
            TranslateAnimation animate = new 
                    TranslateAnimation(0,0,-view.getHeight(),0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            view.startAnimation(animate);
        }catch(Exception e){
            logger.error(e);
        }
    }

    // To animate view slide out from top to bottom
    public void slideToBottom(){
        try{
            TranslateAnimation animate = new 
                    TranslateAnimation(0,0,-messageView.getHeight(),0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            messageView.startAnimation(animate);
        }catch(Exception e){
            logger.error(e);
        }
    }

    // To animate view slide out from bottom to top
    public void slideToTop(){
        TranslateAnimation animate = new 
                TranslateAnimation(0,0,0,-messageView.getHeight());
        animate.setDuration(1000);
        animate.setFillAfter(true);
        messageView.startAnimation(animate);
        messageView.setVisibility(View.GONE);
    }

    //To clear animation set previously
    public void stopAnimation(){
        messageView.clearAnimation();
    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            if(messageView!=null){
                if(messageView.getVisibility()==View.VISIBLE){
                    hideMessageBar();
                }else{
                    messageView.setVisibility(View.GONE);
                }
            }
        }
    };
}
