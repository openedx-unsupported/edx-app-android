/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edx.mobile.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageButton;
import com.joanzapata.iconify.IconDrawable;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 * 
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setNextPreviousListeners()
 *   has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 *   setNextPreviousListeners() was called with null listeners
 * <li> The "rewind" and "fastforward" buttons are shown unless requested
 *   otherwise by using the MediaController(Context, boolean) constructor
 *   with the boolean set to false
 * </ul>
 */
@SuppressLint("WrongViewCast")
public class PlayerController extends FrameLayout {

    public static final long    DEFAULT_TIMEOUT_MS = 3000L;

    private long                mTimeoutMS = DEFAULT_TIMEOUT_MS;
    private IPlayer             mPlayer;
    private Context             mContext;
    private ViewGroup           mAnchor;
    private View                mRoot;
    private ProgressBar         mProgress;
    private TextView            mEndTime, mCurrentTime;
    private boolean             mShowing;
    private boolean             mPauseAccessibilityRequestQueued;
    private boolean             mDragging;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    private boolean             mUseFastForward;
    private boolean             mFromXml;
    private boolean             mListenersSet;
    private View.OnClickListener mNextListener, mPrevListener;
    StringBuilder               mFormatBuilder;
    Formatter                   mFormatter;
    private IconImageButton     mPauseButton;
    private ImageButton         mRewButton;
    private ImageButton         mNextButton;
    private ImageButton         mPrevButton;
    private IconImageButton     mFullscreenButton;
    private IconImageButton     mSettingsButton;
    private Handler             mHandler = new MessageHandler(this);
    private String              mTitle;
    private TextView            mTitleTextView;
    private boolean             mIsAutoHide = true;
    private String              mLmsUrl;
    private View                mTopBar;

    private static final Logger logger = new Logger(PlayerController.class.getName());

    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;
    }

    public PlayerController(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;

    }

    public PlayerController(Context context) {
        this(context, true);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setMediaPlayer(IPlayer player) {
        mPlayer = player;
        updatePausePlay();
        updateFullScreen();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
                );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.player_controller, null);

        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View v) {
        mPauseButton = (IconImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = (IconImageButton) v.findViewById(R.id.fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mRewButton = (ImageButton) v.findViewById(R.id.rewind_button);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            if (!mFromXml) {
                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        // By default these are hidden. They will be enabled when setNextPreviousListeners() is called
        mNextButton = (ImageButton) v.findViewById(R.id.next);
        if (mNextButton != null && !mFromXml && !mListenersSet) {
            mNextButton.setVisibility(View.GONE);
        }
        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        if (mPrevButton != null && !mFromXml && !mListenersSet) {
            mPrevButton.setVisibility(View.GONE);
        }

        mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mSettingsButton = (IconImageButton) v.findViewById(R.id.settings);
        if (mSettingsButton != null) {
            mSettingsButton.requestFocus();
            mSettingsButton.setOnClickListener(mSettingsListener);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        installPrevNextListeners();

        mTitleTextView = (TextView) v.findViewById(R.id.video_title);
        mTopBar = v.findViewById(R.id.video_top_bar);
    }

    /**
     * @param timeoutMS The timeout in milliseconds that controls should be shown on screen,
     *                  or 0 for no timeout
     *                  The value will be applied to next call of show()
     */
    public void setShowTimeoutMS(long timeoutMS) {
        if (timeoutMS <= 0L) {
            timeoutMS = 0L;
        }

        mTimeoutMS = timeoutMS;
    }

    /**
     * Resets value to DEFAULT_TIMEOUT_MS
     */
    public void resetShowTimeoutMS() {
        mTimeoutMS = DEFAULT_TIMEOUT_MS;
    }

    /**
     * Show the controller on screen. It will timeout according to setShowTimeoutMS().
     * Use this as opposed to showSpecial() when you want the timeout to be consistent with
     *  tapping the screen, activating a control, etc.
     */
    public void show() {
        show(mTimeoutMS);
    }

    /**
     * Show the controller for a specified timeout.
     * Use this as opposed to show() when you want to show the controls for a time inconsistent with
     *  tapping the screen, activating a control, etc
     *
     * @param timeoutMS The timeout to show controls for.
     *                  Value of <= 0 means no timeout
     */
    public void showSpecial(long timeoutMS) {
        show(timeoutMS);
    }

    private void show(long timeoutMS) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();

            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
        }
        updatePausePlay();
        updateFullScreen();
        updateTitle();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeoutMS > 0L) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeoutMS);
        }
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        if (mPlayer == null) {
            return;
        }

        try {
            if (mPauseButton != null) {
                if (!mPlayer.canPause()) {
                    mPauseButton.setEnabled(false);
                } else {
                    mPauseButton.setEnabled(true);
                }
            } 
            if (mRewButton != null) {
                if (!mPlayer.canSeekBackward()) {
                    mRewButton.setEnabled(false);
                } else {
                    mRewButton.setEnabled(true);
                }
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor!= null && mShowing) {

            mAnchor.removeView(this);
            mHandler.removeMessages(SHOW_PROGRESS);

            mShowing = false;
        }
    }

    /**
     * Puts accessibility focus on pause/play button.
     * If pause/play not showing, will put focus there next time it shows.
     */
    public void requestAccessibilityFocusPausePlay() {
        if (mShowing && mAnchor != null) {
            setAccessibilityFocusPausePlay(true);
        }
        else {
            mPauseAccessibilityRequestQueued = true;
        }
    }

    private void setAccessibilityFocusPausePlay(boolean force) {
        if ((mPauseButton != null) && (mPauseAccessibilityRequestQueued || force)) {
            mPauseButton.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            mPauseAccessibilityRequestQueued = false;
        }
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private synchronized int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show();
        return false;
    }
    
    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show();
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show();
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show();
        return super.dispatchKeyEvent(event);
    }

    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show();
        }
    };

    private View.OnClickListener mFullscreenListener = new View.OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
            show();
        }
    };

    private View.OnClickListener mSettingsListener = new View.OnClickListener() {
        public void onClick(View v) {
            try{
                setSettingsBtnDrawable(true);
                int[] location = new int[2];
                // Get the x, y location and store it in the location[] array
                // location[0] = x, location[1] = y.
                v.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Point p = new Point();
                p.x = location[0];
                p.y = location[1];

                callSettings(p);
            }catch(Exception e){
                logger.error(e);
            }
        }
    };

    private void updateTitle() {
        mTitleTextView.setText(mTitle);
    }

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPauseButton.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_pause)
                    .colorRes(getContext(), R.color.white));
            mPauseButton.setContentDescription(getContext().getResources()
                    .getString(R.string.video_player_pause));
        } else {
            mPauseButton.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_play)
                    .colorRes(getContext(),R.color.white));
            mPauseButton.setContentDescription(getContext().getResources()
                    .getString(R.string.video_player_play));
        }
    }

    public void updateFullScreen() {
        if (mRoot == null || mFullscreenButton == null || mPlayer == null) {
            return;
        }

        mFullscreenButton.setBackgroundColor(Color.TRANSPARENT);
        if (mPlayer.isFullScreen()) {
            mFullscreenButton.setIcon(FontAwesomeIcons.fa_compress);
            mFullscreenButton.setContentDescription(getContext().getResources()
                    .getString(R.string.video_player_exit_fullscreen));
        } else {
            mFullscreenButton.setIcon(FontAwesomeIcons.fa_expand);
            mFullscreenButton.setContentDescription(getContext().getResources()
                    .getString(R.string.video_player_enter_fullscreen));
        }
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    private void callSettings(Point p){
        try{
            if (mPlayer == null) {
                return;
            }
            mPlayer.callSettings(p);

        }catch(Exception e){
            logger.error(e);
        }
        
        // callback this event
        /*if (mEventListener != null) {
            mEventListener.callSettings();
        }*/
    }

    private void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }

        mPlayer.toggleFullScreen();

        // callback this event
//      if (mEventListener != null) {
//          mEventListener.onFullScreen(mPlayer.isFullScreen());
//      }
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        long startPos = 0;
        long endPos = 0;
        public void onStartTrackingTouch(SeekBar bar) {
            show();
            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
            if(mPlayer!=null){
                startPos = mPlayer.getCurrentPosition();
            }
        }

        public synchronized void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null) {
                return;
            }

            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo( (int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime( (int) newposition));

            //  callback this event
//          if (mEventListener != null) {
//              mEventListener.onSeek((int) newposition);
//          }
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show();

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);

            try{
                if(mPlayer!=null) {
                    endPos = mPlayer.getCurrentPosition();
                    logger.debug("Seek bar Start Pos: " + startPos + " End Pos: " + endPos);
                    mPlayer.callPlayerSeeked(startPos, endPos, false);
                }
            }catch(Exception e){
                logger.error(e);
            }
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(PlayerController.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(PlayerController.class.getName());
    }

    /**
     * Listener for the rewind 30 seconds button in the media player
     */
    private View.OnClickListener mRewListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }
            int pos = mPlayer.getCurrentPosition();
            try{
                mPlayer.callPlayerSeeked(pos, pos-30000, true);
            }catch(Exception e){
                logger.error(e);
            }
            
            // apply 30 seconds rewind
            pos -= (30 * 1000); // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show();

            // callback this event
//          if (mEventListener != null) {
//              mEventListener.onSeek(pos);
//          }
        }
    };


    private void installPrevNextListeners() {
        if(mNextListener!=null){
            if (mNextButton != null) {
                mNextButton.setOnClickListener(mNextListener);
                mNextButton.setEnabled(mNextListener != null);
            }
        }

        if(mPrevListener!=null){
            if (mPrevButton != null) {
                mPrevButton.setOnClickListener(mPrevListener);
                mPrevButton.setEnabled(mPrevListener != null);
            }
        }
    }

    public void setNextPreviousListeners(View.OnClickListener next, View.OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;

        mListenersSet = true;

        if (mRoot != null) {
            installPrevNextListeners();

            if (mNextButton != null && !mFromXml) {
                if(mNextListener!=null){
                    mNextButton.setVisibility(View.VISIBLE);
                }else{
                    mNextButton.setVisibility(View.GONE);
                }
            }
            if (mPrevButton != null && !mFromXml) {
                if(mPrevListener!=null){
                    mPrevButton.setVisibility(View.VISIBLE);
                }else{
                    mPrevButton.setVisibility(View.GONE);
                }
            }
        }
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<PlayerController> mView; 

        MessageHandler(PlayerController view) {
            mView = new WeakReference<>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                PlayerController view = mView.get();
                if (view == null || view.mPlayer == null) {
                    return;
                }

                int pos;
                switch (msg.what) {
                case FADE_OUT:
                    if (view.mIsAutoHide) {
                        view.hide();
                    }
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    // FIXME: got illegalstateexception for player here
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                }
            } catch(Exception ex) {
                logger.error(ex);
            }
        }
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setLmsUrl(String lmsUrl) {
        this.mLmsUrl = lmsUrl;
    }

    public void setAutoHide(boolean mIsAutoHide) {
        this.mIsAutoHide = mIsAutoHide;
    }

    @SuppressWarnings("unused")
    public void hideProgress() {
        if(this.mProgress!=null){
            this.mProgress.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressWarnings("unused")
    public void showProgress() {
        this.mProgress.setVisibility(View.VISIBLE);
    }
    
    /**
     * Triggers click of next button.
     */
    public void playNext() {
        if (mNextListener != null 
                && mNextButton != null
                && mNextButton.getVisibility() == View.VISIBLE) {
            mNextListener.onClick(mNextButton);
        }
    }
    
    /**
     * Triggers click of previous button.
     */
    public void playPrevious() {
        if (mPrevListener != null
                && mPrevButton != null
                && mPrevButton.getVisibility() == View.VISIBLE) {
            mPrevListener.onClick(mPrevButton);
        }
    }
    
    public void setSettingsBtnDrawable(boolean isSettingEnabled){
        if (mSettingsButton != null) {
            if (isSettingEnabled) {
                mSettingsButton.setIconColor(getResources().getColor(R.color.edx_brand_primary_accent));
            } else {
                mSettingsButton.setIconColor(getResources().getColor(R.color.white));
            }
        }
    }

    /**
     * Sets the visibility of top bar of the player controller
     * @param isVisible true=visible & false=gone
     */
    public void setTopBarVisibility(boolean isVisible) {
        if (isVisible){
            mTopBar.setVisibility(View.VISIBLE);
        } else {
            mTopBar.setVisibility(View.GONE);
        }
    }
}
