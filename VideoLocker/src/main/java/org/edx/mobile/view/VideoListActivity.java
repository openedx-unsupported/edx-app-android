package org.edx.mobile.view;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.player.IPlayerEventCallback;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.player.VideoListFragment;
import org.edx.mobile.player.VideoListFragment.VideoListCallback;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

import java.io.File;

@SuppressWarnings("serial")
public class VideoListActivity extends BaseFragmentActivity implements
VideoListCallback, IPlayerEventCallback {

    private boolean myVideosFlag;
    private CheckBox checkBox;
    private CourseVideoCheckBoxListener checklistener;
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
            try{
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.container_player, playerFragment, "player");
                ft.commit();
            }catch(Exception ex){
                logger.error(ex);
            }
        }

        try{
            myVideosFlag = this.getIntent().getBooleanExtra("FromMyVideos", false);
        }catch(Exception ex){
            logger.error(ex);
        }

        if(!(NetworkUtil.isConnected(this))){
            View offlineBar = (View) findViewById(R.id.offline_bar);
            if (offlineBar != null) 
                offlineBar.setVisibility(View.VISIBLE);

            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
        }else{
            AppConstants.offline_flag = false;
        }

        listFragment = (VideoListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.list_fragment);
        listFragment.setCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            if (playerFragment != null) {
                playerFragment.setCallback(this);
            }

            View container = findViewById(R.id.container_player);
            if (container == null || container.getVisibility() != View.VISIBLE) {
                // this is to lock to portrait while player is invisible
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } 
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActionBar bar = getActionBar();
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
        try{
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
        }catch(Exception ex){
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
        } catch(Exception ex) {}

        try{
            View container = findViewById(R.id.container_player);
            container.setVisibility(View.VISIBLE);

            // reload this model
            storage.reloadDownloadEntry(video);

            logger.debug("Resumed= " + playerFragment.isResumed());
            if ( !playerFragment.isResumed()) {
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

            playerFragment.setPrevNxtListners(listFragment.getNextListener(), 
                    listFragment.getPreviousListener());

            TranscriptModel transcript = null;
            Api api = new Api(this);
            try {
                if(video.videoId!=null){
                    transcript = api.getTranscriptsOfVideo(video.eid, video.videoId);
                }

            } catch (Exception e) {
                logger.error(e);
            }

            String filepath = null;
            // check if file available on local
            if (video.filepath != null && video.filepath.length()>0) {
                if (video.isDownloaded()) {
                    File f = new File(video.filepath);
                    if (f.exists()) {
                        // play from local
                        filepath = video.filepath;
                        logger.debug("playing from local file");
                    } 
                }
            } else {
                DownloadEntry de = (DownloadEntry)db.getIVideoModelByVideoUrl(
                        video.url, null); 
                if(de!=null){
                    if(de.filepath!=null){
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
            
            if(filepath==null || filepath.length()<=0){
                // not available on local, so play online
                logger.warn("Local file path not available");

                filepath = video.getBestEncodingUrl(this);
            }

            playerFragment.play(filepath, video.lastPlayedOffset,
                    video.getTitle(), transcript, video);
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    /**
     * Gets called from VideoListFragment2
     */
    public void updateProgress() {
        invalidateOptionsMenu();
    }

    public void showCheckBox() {
        if (myVideosFlag) {
            AppConstants.myVideosDeleteMode = true;
        } else {
            AppConstants.videoListDeleteMode = true;
        }
        invalidateOptionsMenu();
    }

    public void hideCheckBox() {
        if (myVideosFlag) {
            AppConstants.myVideosDeleteMode = false;
            if (checkBox != null)
                checkBox.setChecked(false);
        } else {
            AppConstants.videoListDeleteMode = false;
            if (checkBox != null)
                checkBox.setChecked(false);
        }
        invalidateOptionsMenu();
    }

    public void setCheckBoxSelected(){
        try{
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(true);
            checkBox.setOnCheckedChangeListener(checklistener);
            //checkBox.setSelected(true);
            checkBox.setButtonDrawable(R.drawable.ic_checkbox_active);
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    public void unsetCheckBoxSelected(){
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(false);
        checkBox.setOnCheckedChangeListener(checklistener);
        //checkBox.setSelected(false);
        checkBox.setButtonDrawable(R.drawable.ic_checkbox_default);
        //checkBox.setBackgroundResource(R.drawable.ic_checkbox_default);
    }

    @Override
    protected void onOffline() {
        View offlineBar = findViewById(R.id.offline_bar);
        if (offlineBar != null) {
            offlineBar.setVisibility(View.VISIBLE);
        }
        if (playerFragment != null) {
            playerFragment.onOffline();
        }

        if(listFragment!=null){
            listFragment.onOffline();
        }

        if(playerFragment!=null && listFragment!=null){
            playerFragment.setPrevNxtListners(listFragment.getNextListener(), 
                    listFragment.getPreviousListener());
        }

        invalidateOptionsMenu();
    }

    @Override
    protected void onOnline() {
        View offlineBar = findViewById(R.id.offline_bar);
        if (offlineBar != null) {
            offlineBar.setVisibility(View.GONE);
        }
        if (!myVideosFlag) {
            AppConstants.videoListDeleteMode = false;
        }
        if (playerFragment != null) {
            playerFragment.onOnline();
        }
        listFragment.onOnline();
        if(playerFragment!=null && listFragment!=null){
            playerFragment.setPrevNxtListners(listFragment.getNextListener(), 
                    listFragment.getPreviousListener());
        }
        invalidateOptionsMenu();
    }

    private class CourseVideoCheckBoxListener implements OnCheckedChangeListener {
        private boolean lastIsChecked = false;

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked == lastIsChecked) {
                return;
            }
            lastIsChecked = isChecked;

            if(isChecked){
                listFragment.setAllVideosChecked();
                checkBox.setButtonDrawable(R.drawable.ic_checkbox_active);
            } else {
                listFragment.unsetAllVideosChecked();
                checkBox.setButtonDrawable(R.drawable.ic_checkbox_default);
                //checkBox.setBackgroundResource(R.drawable.ic_checkbox_default);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        View checkBoxView = checkBox_menuItem.getActionView();
        checkBox = (CheckBox) checkBoxView.findViewById(R.id.select_checkbox);

        if(checklistener==null) {
            checklistener = new CourseVideoCheckBoxListener();
        }

        if(myVideosFlag){
            if(AppConstants.myVideosDeleteMode) {
                checkBox_menuItem.setVisible(true);
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setOnCheckedChangeListener(checklistener);
            }else{
                checkBox_menuItem.setVisible(false);
                checkBox.setVisibility(View.GONE);
                checkBox.setOnCheckedChangeListener(null);
            }
        }else{
            if(AppConstants.videoListDeleteMode){
                checkBox_menuItem.setVisible(true);
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setOnCheckedChangeListener(checklistener);
            }else{
                checkBox_menuItem.setVisible(false);
                checkBox.setVisibility(View.GONE);
                checkBox.setOnCheckedChangeListener(null);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
        case android.R.id.home:
            hideCheckBox();
            if(AppConstants.offline_flag){
                Intent intent = new Intent();
                intent.setAction(AppConstants.VIDEOLIST_BACK_PRESSED);
                sendBroadcast(intent); 
            }
            finish();
            return true;

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
        if(myVideosFlag){
            listFragment.handleDeleteView();
        } else {
            if(AppConstants.offline_flag){
                listFragment.handleDeleteView();
            }
        }

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
        if(AppConstants.offline_flag){
            Intent intent = new Intent();
            intent.setAction(AppConstants.VIDEOLIST_BACK_PRESSED);
            sendBroadcast(intent);
        }
        finish();
    };
}
