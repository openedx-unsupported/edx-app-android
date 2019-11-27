package org.edx.mobile.googlecast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.MenuItem;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.AppConstants;

public class GoogleCastDelegate extends RemoteMediaClient.Callback implements
        SessionManagerListener<CastSession> {



    public interface CastSessionListener {
        void onApplicationConnected();

        void onApplicationDisconnected();

        void onVideoComplete();

        void onVideoPlaying();
    }

    private static GoogleCastDelegate delegate;
    private final Context context;
    private CastContext castContext;
    private CastSession castSession;

    private IntroductoryOverlay introductoryOverlay;
    private CastSessionListener sessionListener;
    private AnalyticsRegistry analyticsRegistry;

    private GoogleCastDelegate(@NonNull Context context, AnalyticsRegistry analyticsRegistry) {
        this.context = context;
        this.analyticsRegistry = analyticsRegistry;
        init();
    }

    public static synchronized GoogleCastDelegate getInstance(AnalyticsRegistry analyticsRegistry) {
        if (delegate == null) {
            // Injecting the {@link AnalyticsRegistry} field at class level throwing exception during
            // the execution of test cases.
            // Sample test case is {@link org.edx.mobile.view.CourseUnitNavigationActivityTest}.
            // To resolve this {@link AnalyticsRegistry} field is passing as a variable.
            delegate = new GoogleCastDelegate(MainApplication.instance(), analyticsRegistry);
        }
        return delegate;
    }

    private void init() {
        // TODO: Replace the try-catch block with more appropriate logic so Travis-ci build get passed.
        // Can't get CastContext instance while executing test cases of "CourseUnitNavigationActivityTest"
        // and throw exception.
        try {
            castContext = CastContext.getSharedInstance(this.context);
            castSession = castContext.getSessionManager().getCurrentCastSession();
            castContext.getSessionManager().addSessionManagerListener(this,
                    CastSession.class);
            registerRemoteCallback();
        } catch (Exception ignore) {
        }
    }

    private void registerRemoteCallback() {
        if (castContext.getCastState() == CastState.CONNECTED) {
            castSession.getRemoteMediaClient().registerCallback(this);
        }
    }

    @Override
    public void onSessionEnded(CastSession session, int error) {
        onApplicationDisconnected(session);
    }

    @Override
    public void onSessionResumed(CastSession session, boolean wasSuspended) {
        onApplicationConnected(session);
    }

    @Override
    public void onSessionResumeFailed(CastSession session, int error) {
        onApplicationDisconnected(session);
    }

    @Override
    public void onSessionStarted(CastSession session, String sessionId) {
        onApplicationConnected(session);
        analyticsRegistry.trackCastDeviceConnectionChanged(Analytics.Events.CAST_CONNECTED,
                Analytics.Values.CAST_CONNECTED, Analytics.Values.GOOGLE_CAST);
    }

    @Override
    public void onSessionStartFailed(CastSession session, int error) {
        onApplicationDisconnected(session);
    }

    @Override
    public void onSessionStarting(CastSession session) {
    }

    @Override
    public void onSessionEnding(CastSession session) {
        onApplicationDisconnected(session);
        analyticsRegistry.trackCastDeviceConnectionChanged(Analytics.Events.CAST_DISCONNECTED,
                Analytics.Values.CAST_DISCONNECTED, Analytics.Values.GOOGLE_CAST);
    }

    @Override
    public void onSessionResuming(CastSession session, String sessionId) {
    }

    @Override
    public void onSessionSuspended(CastSession session, int reason) {
        onApplicationDisconnected(session);
    }

    private void onApplicationConnected(@NonNull CastSession session) {
        this.castSession = session;
        if (sessionListener != null) {
            sessionListener.onApplicationConnected();
        }
        registerRemoteCallback();
    }

    private void onApplicationDisconnected(@NonNull CastSession session) {
        this.castSession = session;
        if (sessionListener != null) {
            sessionListener.onApplicationDisconnected();
        }
    }

    public void loadRemoteMedia(@NonNull Activity activity, @NonNull DownloadEntry videoEntry, long position, boolean autoPlay) {
        if (castSession == null || !castSession.isConnected()) {
            return;
        }
        final String videoUrl = videoEntry.getBestEncodingUrl(activity);
        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        // If remote media player is not idle and playing the same video, don't do anything
        if (remoteMediaClient == null ||
                (remoteMediaClient.getIdleReason() != MediaStatus.IDLE_REASON_FINISHED &&
                        remoteMediaClient.getMediaInfo() != null &&
                        remoteMediaClient.getMediaInfo().getContentId().equals(videoUrl))) {
            return;
        }
        // open expanded controls when start the video casting.
        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                final Intent intent = new Intent(activity, ExpandedControlsActivity.class);
                activity.startActivity(intent);
                remoteMediaClient.unregisterCallback(this);
                // Track video is casted on casting device.
                double currentTime = position / AppConstants.MILLISECONDS_PER_SECOND;
                analyticsRegistry.trackVideoPlaying(videoEntry.videoId, currentTime, videoEntry.eid,
                        videoEntry.lmsUrl, Analytics.Values.GOOGLE_CAST);
            }
        });
        // load video media on remote client / media player.
        remoteMediaClient.load(buildMediaInfo(videoEntry, videoUrl),
                new MediaLoadOptions.Builder()
                        .setAutoplay(autoPlay)
                        .setPlayPosition(position).build());
    }

    private MediaInfo buildMediaInfo(@NonNull DownloadEntry videoEntry, @NonNull String videoUrl) {
        final MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_TITLE, videoEntry.title);
        movieMetadata.addImage(new WebImage(Uri.parse(videoEntry.videoThumbnail)));

        return new MediaInfo.Builder(videoUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/*")
                .setMetadata(movieMetadata)
                .setStreamDuration(videoEntry.getDuration() * 1000)
                .build();
    }

    public void addCastStateListener(@NonNull CastStateListener stateListener) {
        if (castContext != null) {
            castContext.addCastStateListener(stateListener);
        }
        if (castSession == null) {
            init();
        }
    }

    public void removeCastStateListener(@NonNull CastStateListener stateListener) {
        if (castContext != null) {
            castContext.removeCastStateListener(stateListener);
        }
    }

    public void showIntroductoryOverlay(@NonNull Activity activity, @Nullable final MenuItem mediaRouteMenuItem) {
        if (castContext.getCastState() != CastState.NO_DEVICES_AVAILABLE) {
            if (introductoryOverlay != null) {
                introductoryOverlay.remove();
            }
            if (mediaRouteMenuItem != null && mediaRouteMenuItem.isVisible()) {
                new Handler().post(() -> {
                    introductoryOverlay = new IntroductoryOverlay.Builder(
                            activity, mediaRouteMenuItem)
                            .setTitleText(context.getString(R.string.introducing_cast_text))
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    () -> introductoryOverlay = null)
                            .build();
                    introductoryOverlay.show();
                });
            }
        }
    }

    @Override
    public void onStatusUpdated() {
        super.onStatusUpdated();
        if (sessionListener != null && castSession != null) {
            final RemoteMediaClient remoteMediaPlayer = castSession.getRemoteMediaClient();
            if (remoteMediaPlayer != null) {
                if (remoteMediaPlayer.getPlayerState() == MediaStatus.PLAYER_STATE_IDLE &&
                        remoteMediaPlayer.getIdleReason() == MediaStatus.IDLE_REASON_FINISHED) {
                    sessionListener.onVideoComplete();
                } else if (remoteMediaPlayer.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING) {
                    sessionListener.onVideoPlaying();
                }
            }
        }
    }

    public boolean isConnected() {
        return castSession != null && castSession.isConnected();
    }

    public void setSessionListener(CastSessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }
}
