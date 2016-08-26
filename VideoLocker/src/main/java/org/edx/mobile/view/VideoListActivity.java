package org.edx.mobile.view;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseVideosDownloadStateActivity;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.player.IPlayerEventCallback;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.player.VideoListFragment;
import org.edx.mobile.player.VideoListFragment.VideoListCallback;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.CheckboxDrawableUtil;
import org.edx.mobile.util.NetworkUtil;

import java.io.File;

public class VideoListActivity extends BaseVideosDownloadStateActivity
        implements VideoListCallback, IPlayerEventCallback {

    private MenuItem selectAllMenuItem;
    private View offlineBar;
    private PlayerFragment playerFragment;
    private VideoListFragment listFragment;
    private final Handler playHandler = new Handler();
    private Runnable playPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        restore(savedInstanceState);

        if (playerFragment == null) {
            // this is to lock to portrait while player is invisible
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            playerFragment = new PlayerFragment();
            try {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.container_player, playerFragment, "player");
                ft.commit();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        offlineBar = findViewById(R.id.offline_bar);

        listFragment = (VideoListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.list_fragment);
        listFragment.setCallback(this);

        // Full-screen video in landscape.
        if (isLandscape()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setActionBarVisible(false);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Wait until PlayerFragment.onActivityCreated() is called, so that PlayerFragment.player != null
        // Wait until VideoListFragment.onActivityCreated() is called, so that VideoListFragment.adapter has been filled
        playerFragment.setCallback(this);
        playerFragment.setNextPreviousListeners(listFragment.getNextListener(), listFragment.getPreviousListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            View container = findViewById(R.id.container_player);
            if (container == null || container.getVisibility() != View.VISIBLE) {
                // this is to lock to portrait while player is invisible
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActionBar bar = getSupportActionBar();
        if (bar != null && !isLandscape()) {
            bar.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        View container = findViewById(R.id.container_player);
        outState.putInt("playerVisibility", container.getVisibility());
        getSupportFragmentManager().putFragment(outState, "player", playerFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            super.onRestoreInstanceState(savedInstanceState);
            restore(savedInstanceState);
        }
    }

    private void restore(Bundle savedInstanceState) {
        try {
            if (savedInstanceState != null) {
                int visibility = savedInstanceState.getInt("playerVisibility", View.GONE);
                View container = findViewById(R.id.container_player);
                container.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);

                if (playerFragment == null) {
                    FragmentManager fm = getSupportFragmentManager();
                    playerFragment = (PlayerFragment) fm.getFragment(
                            savedInstanceState, "player");
                    if (playerFragment != null) {
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.replace(R.id.container_player, playerFragment, "player");
                        ft.commit();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public synchronized void playVideoModel(final DownloadEntry video) {
        try {
            if (playerFragment.isPlaying()) {
                if (video.getVideoId().equals(playerFragment.getPlayingVideo().getVideoId())) {
                    logger.debug("this video is already being played, skipping play event");
                    return;
                }
            }
        } catch (Exception ex) {
        }


        try {
            View container = findViewById(R.id.container_player);
            container.setVisibility(View.VISIBLE);

            // reload this model
            environment.getStorage().reloadDownloadEntry(video);

            logger.debug("Resumed= " + playerFragment.isResumed());
            if (!playerFragment.isResumed()) {
                // playback can work only if fragment is resume
                if (playPending != null) {
                    playHandler.removeCallbacks(playPending);
                }
                playPending = new Runnable() {
                    public void run() {
                        playVideoModel(video);
                    }
                };
                playHandler.postDelayed(playPending, 200);
                return;
            } else {
                if (playPending != null) {
                    playHandler.removeCallbacks(playPending);
                }
            }

            playerFragment.setNextPreviousListeners(listFragment.getNextListener(),
                    listFragment.getPreviousListener());

            TranscriptModel transcript = null;
            try {
                if (video.videoId != null) {
                    transcript = environment.getServiceManager().getTranscriptsOfVideo(video.eid, video.videoId);
                }

            } catch (Exception e) {
                logger.error(e);
            }

            String filepath = null;
            if (video.filepath != null && video.filepath.length() > 0) {
                if (video.isDownloaded()) {
                    File f = new File(video.filepath);
                    if (f.exists()) {
                        // play from local
                        filepath = video.filepath;
                        logger.debug("playing from local file");
                    }
                }
            } else {
                DownloadEntry de = (DownloadEntry) environment.getDatabase().getIVideoModelByVideoUrl(
                        video.url, null);
                if (de != null) {
                    if (de.filepath != null) {
                        File f = new File(de.filepath);
                        if (f.exists()) {
                            // play from local
                            filepath = de.filepath;
                            logger.debug("playing from local file for " +
                                    "another Download Entry");
                        }
                    }
                }
            }

            if (filepath == null || filepath.length() <= 0) {
                // not available on local, so play online
                logger.warn("Local file path not available");

                filepath = video.getBestEncodingUrl(this);
            }

            playerFragment.play(filepath, video.lastPlayedOffset,
                    video.getTitle(), transcript, video);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * Gets called from VideoListFragment2
     */
    public void updateProgress() {
        supportInvalidateOptionsMenu();
    }

    public void showCheckBox() {
        AppConstants.myVideosDeleteMode = true;
        supportInvalidateOptionsMenu();
    }

    public void hideCheckBox() {
        AppConstants.myVideosDeleteMode = false;
        supportInvalidateOptionsMenu();
    }

    public void setSelectAllChecked(boolean isChecked) {
        if (null == selectAllMenuItem) {
            return;
        }
        selectAllMenuItem.setChecked(isChecked);
        selectAllMenuItem.setIcon(CheckboxDrawableUtil.createActionBarDrawable(this, selectAllMenuItem.isChecked()));
    }

    @Override
    protected void onOffline() {
        super.onOffline();
        if (offlineBar != null) {
            offlineBar.setVisibility(View.VISIBLE);
        }
        if (playerFragment != null) {
            playerFragment.onOffline();
        }

        if (listFragment != null) {
            listFragment.onOffline();
        }

        if (playerFragment != null && listFragment != null) {
            playerFragment.setNextPreviousListeners(listFragment.getNextListener(),
                    listFragment.getPreviousListener());
        }
    }

    @Override
    protected void onConnectedToMobile() {
        if (playerFragment != null) {
            playerFragment.onConnectedToMobile();
        }
    }

    @Override
    protected void onConnectedToWifi() {
        if (playerFragment != null) {
            playerFragment.onConnectedToWifi();
        }
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        if (offlineBar != null) {
            offlineBar.setVisibility(View.GONE);
        }
        AppConstants.videoListDeleteMode = false;
        if (playerFragment != null) {
            playerFragment.onOnline();
        }
        listFragment.onOnline();
        if (playerFragment != null && listFragment != null) {
            playerFragment.setNextPreviousListeners(listFragment.getNextListener(),
                    listFragment.getPreviousListener());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final boolean superReturn = super.onCreateOptionsMenu(menu);
        if (AppConstants.myVideosDeleteMode) {
            getMenuInflater().inflate(R.menu.video_list, menu);
            selectAllMenuItem = menu.findItem(R.id.delete_checkbox);
            setSelectAllChecked(selectAllMenuItem.isChecked());
            return true;
        } else {
            selectAllMenuItem = null;
            return superReturn;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                hideCheckBox();
                if (!NetworkUtil.isConnected(this)) {
                    Intent intent = new Intent();
                    intent.setAction(AppConstants.VIDEOLIST_BACK_PRESSED);
                    sendBroadcast(intent);
                }
                finish();
                return true;
            case R.id.delete_checkbox: {
                setSelectAllChecked(!item.isChecked());
                if (item.isChecked()) {
                    listFragment.setAllVideosChecked();
                } else {
                    listFragment.unsetAllVideosChecked();
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        hideCheckBox();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        listFragment.handleDeleteView();
        listFragment.setAdaptertoVideoList();
        listFragment.notifyAdapter();

    }

    @Override
    public void onError() {
    }

    @Override
    public void onPlaybackStarted() {
        listFragment.markPlaying();
        listFragment.notifyAdapter();
    }

    @Override
    public void onPlaybackComplete() {
        listFragment.onPlaybackComplete();
        listFragment.notifyAdapter();
    }

    @Override
    public synchronized void saveCurrentPlaybackPosition(int currentPosition) {
        listFragment.saveCurrentPlaybackPosition(currentPosition);
    }

    public void onBackPressed() {
        if (!NetworkUtil.isConnected(this)) {
            Intent intent = new Intent();
            intent.setAction(AppConstants.VIDEOLIST_BACK_PRESSED);
            sendBroadcast(intent);
        }
        finish();
    }

    @Override
    public boolean showInfoMessage(String message) {
        //If the wifi settings message is already shown on video player,
        //then do not show the info message
        if (playerFragment.isShownWifiSettingsMessage()
                && message.equalsIgnoreCase(getString(R.string.wifi_off_message))) {
            return false;
        }
        return super.showInfoMessage(message);
    }

}
