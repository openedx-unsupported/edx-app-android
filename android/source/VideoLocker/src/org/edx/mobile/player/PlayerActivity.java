package org.edx.mobile.player;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.db.DownloadEntry;

import java.io.File;

public abstract class PlayerActivity extends BaseFragmentActivity implements IPlayerEventCallback {

    protected final Handler playHandler = new Handler();
    protected Runnable playPending;
    public PlayerFragment playerFragment;
    private View.OnClickListener prev;
    private View.OnClickListener next;

    /**
     * Call this method from the sub-class, just after setContentView().
     * @param savedInstanceState
     */
    public void onCreatePlayer(Bundle savedInstanceState) {
        restore(savedInstanceState);

        View container = findViewById(R.id.container_player);
        if (container == null) {
            // looks like this activity doesn't have player component
            return;
        } 

        if (playerFragment == null) {
            // this is to lock to portrait while player is invisible
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            playerFragment = new PlayerFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container_player, playerFragment, "player");
            ft.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            View container = findViewById(R.id.container_player);
            if (container != null) {
                outState.putInt("playerVisibility", container.getVisibility());
                if (playerFragment != null) {
                    getSupportFragmentManager().putFragment(outState, "player", playerFragment);
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restore(savedInstanceState);
    }

    /**
     * Sub-class may override this to restore more elements.
     * @param savedInstanceState
     */
    protected void restore(Bundle savedInstanceState) {
        try{
            if (savedInstanceState != null) {
                int visibility = savedInstanceState.getInt("playerVisibility", View.GONE);
                View container = findViewById(R.id.container_player);
                if (container != null) {
                    container.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
                }

                if (playerFragment == null) {
                    FragmentManager fm = getSupportFragmentManager();
                    playerFragment = (PlayerFragment) fm.getFragment(
                            savedInstanceState, "player");
                    if (playerFragment != null && container != null) {
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.replace(R.id.container_player, playerFragment, "player");
                        ft.commit();
                    } else {
                        logger.warn("PlayerFragement needs to be restored, no container found!");
                        restore(playerFragment);
                    }
                }
            } 
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    protected void restore(PlayerFragment pf) {
        // sub-class should handle this
        logger.debug("Restore player fragment in the sub-class");
    }

    public synchronized void playVideoModel(final DownloadEntry video) {
        try{
            if (playerFragment == null) {
                return;
            }

            // set callback for player events
            if (playerFragment != null) {
                playerFragment.setCallback(this);
            }

            playerFragment.setPrevNxtListners(next, prev);

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

            TranscriptModel transcript = null;
            Api api = new Api(this);
            try {
                transcript = api.getTranscriptsOfVideo(video.eid, video.videoId);
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
                        logger.debug("Playing from local file");
                    } 
                }
            } else {
                DownloadEntry de = (DownloadEntry) db.getIVideoModelByVideoUrl(
                        video.url, null);
                if(de!=null){
                    if(de.filepath!=null){
                        File f = new File(de.filepath);
                        if (f.exists()) {
                            // play from local
                            filepath = de.filepath;
                            logger.debug("Playing from local file for " +
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
            
            logger.debug("Playing from URL: " + filepath);
            playerFragment.play(filepath, video.lastPlayedOffset, 
                    video.getTitle(), transcript, video);
        }catch(Exception e){
            logger.error(e);
        }
    }

    public void setRecentNextPrevListeners(View.OnClickListener next, View.OnClickListener prev){
        this.next = next;
        this.prev = prev;
    }
}
