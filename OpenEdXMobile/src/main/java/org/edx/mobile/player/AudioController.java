package org.edx.mobile.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;
import android.widget.TextView;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.IconDrawable;
import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.custom.IconImageViewXml;
import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", and a progress
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
 *
 */

/**
 * Created by arslan on 1/9/18.
 */

@SuppressLint("WrongViewCast")
public class AudioController extends PlayerController {

    private IconImageViewXml playPauseIcon;
    private SeekBar audioProgressSeekbar;
    protected Handler mHandler = new MessageHandler(this);


    private static final Logger logger = new Logger(PlayerController.class.getName());

    public AudioController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
    }

    public AudioController(Context context) {
        super(context);
        mContext = context;

    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }
    @Override
    public void setMediaPlayer(IPlayer player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * @param view The view to which to anchor the controller when it is visible.
     */
    @Override
    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        LayoutParams frameParams = new LayoutParams(
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
    @Override
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.audio_player_controller, null);
        initControllerView(mRoot);
        return mRoot;
    }
    @Override
    protected void initControllerView(View v) {

        playPauseIcon = (IconImageViewXml) v.findViewById(R.id.audio_play_pause_icon);
        playPauseIcon.setOnClickListener(mPauseListener);
        if(playPauseIcon != null){
            playPauseIcon.requestFocus();
            playPauseIcon.setOnClickListener(mPauseListener);
        }

        audioProgressSeekbar = (SeekBar) v.findViewById(R.id.audio_media_seekbar);
        audioProgressSeekbar.setOnSeekBarChangeListener(mSeekListener);
        audioProgressSeekbar.setMax(1000);

        mEndTime = (TextView) v.findViewById(R.id.tv_total_audio_duration);
        mCurrentTime = (TextView) v.findViewById(R.id.tv_current_audio_duration);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mTitleTextView = (TextView) v.findViewById(R.id.audio_title);
        mTopBar = v.findViewById(R.id.audio_top_bar);
    }
    @Override
    protected void show(long timeoutMS) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if(playPauseIcon != null)
            {
                playPauseIcon.requestFocus();
            }
            disableUnsupportedButtons();

            LayoutParams tlp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
        }
        updatePausePlay();
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
    @Override
    protected void disableUnsupportedButtons() {
        if (mPlayer == null) {
            return;
        }

        try {
            if(playPauseIcon != null){
                if(!mPlayer.canPause()){
                    playPauseIcon.setEnabled(false);
                }else{
                    playPauseIcon.setEnabled(true);
                }
            }

            if(audioProgressSeekbar != null){
                if(!mPlayer.canSeekBackward() || ! mPlayer.canSeekForward())
                {
                    audioProgressSeekbar.setEnabled(false);
                }
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }
    @Override
    protected synchronized int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (audioProgressSeekbar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                audioProgressSeekbar.setProgress( (int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            audioProgressSeekbar.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
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
                if (playPauseIcon != null) {
                    playPauseIcon.requestFocus();
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

    @Override
    public void updatePausePlay() {
        if (mRoot == null || playPauseIcon == null || mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            playPauseIcon.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_pause)
                    .colorRes(getContext(), R.color.white));
            playPauseIcon.setContentDescription(getContext().getResources()
                    .getString(R.string.video_player_pause));
        } else {
            playPauseIcon.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_play)
                    .colorRes(getContext(),R.color.white));
            playPauseIcon.setContentDescription(getContext().getResources()
                    .getString(R.string.video_player_play));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (playPauseIcon != null) {
            playPauseIcon.setEnabled(enabled);
        }
        if (audioProgressSeekbar != null) {
            audioProgressSeekbar.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AudioController.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AudioController.class.getName());
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<AudioController> mView;

        MessageHandler(AudioController view) {
            mView = new WeakReference<>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                AudioController view = mView.get();
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
    @Override
    @SuppressWarnings("unused")
    public void hideProgress() {
        if(this.audioProgressSeekbar!=null){
            this.audioProgressSeekbar.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    @SuppressWarnings("unused")
    public void showProgress() {
        this.audioProgressSeekbar.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the visibility of top bar of the player controller
     * @param isVisible true=visible & false=gone
     */
    @Override
    public void setTopBarVisibility(boolean isVisible) {
        if (isVisible){
            mTopBar.setVisibility(View.VISIBLE);
        } else {
            mTopBar.setVisibility(View.GONE);
        }
    }
}
