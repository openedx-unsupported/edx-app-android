package org.edx.mobile.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

/**
 * Created by Zohaib Asaad on 1/9/2018.
 */

public class YouTubeMediaPlayerSupportFragment extends YouTubePlayerSupportFragment
        implements YouTubePlayer.OnInitializedListener {
    private static final String KEY_VIDEO_ID = "VIDEO_ID";
    private static final String KEY_API_KEY = "API_KEY";

    private String apiKey;
    private String videoId;

    private YouTubePlayer mPlayer;

    public static YouTubeMediaPlayerSupportFragment newInstance(String apiKey, String videoId) {

        Bundle args = new Bundle();
        args.putString(KEY_VIDEO_ID, videoId);
        args.putString(KEY_API_KEY, apiKey);

        YouTubeMediaPlayerSupportFragment fragment = new YouTubeMediaPlayerSupportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void initialize() {
        initialize(apiKey, this);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getArguments() != null) {
            apiKey = getArguments().getString(KEY_API_KEY);
            videoId = getArguments().getString(KEY_VIDEO_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(layoutInflater, viewGroup, bundle);
        initialize();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        mPlayer = youTubePlayer;
        if (!wasRestored) {
            // load your video
            mPlayer.loadVideo(videoId);
        } else {
            mPlayer.play();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult error) {
        String errorMessage = error.toString();
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
        Log.e(YouTubePlayerSupportFragment.class.getSimpleName(), errorMessage);
    }

}
