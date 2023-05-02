package org.edx.mobile.view

import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackQuality
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.event.NetworkConnectivityChangeEvent
import org.edx.mobile.model.course.VideoBlockModel
import org.edx.mobile.model.db.DownloadEntry
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.db.impl.DatabaseFactory
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.DeviceSettingUtil
import org.edx.mobile.util.NetworkUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import subtitleFile.Caption
import subtitleFile.TimedTextObject

@AndroidEntryPoint
class CourseUnitYoutubePlayerFragment : BaseCourseUnitVideoFragment(), YouTubePlayerListener,
    FullscreenListener {

    private var youTubePlayerView: YouTubePlayerView? = null
    private var youTubePlayer: YouTubePlayer? = null
    private var currentPlayerState: PlayerState = PlayerState.UNSTARTED
    private var currentTimeInSec = 0.0
    private var attempts = 0
    private var isFullscreen = false

    override fun onResume() {
        super.onResume()
        if (unit != null) {
            setVideoModel()
            initializeYoutubePlayer()
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
        }
    }

    private fun initializeYoutubePlayer() {
        try {
            if (activity != null && NetworkUtil.verifyDownloadPossible(activity as BaseFragmentActivity)) {
                downloadTranscript()
                releaseYoutubePlayer()
                youTubePlayerView = YouTubePlayerView(requireContext())
                youTubePlayerView?.enableAutomaticInitialization = false
                youTubePlayerView?.addFullscreenListener(this)
                attempts = 0
                val iFramePlayerOptions: IFramePlayerOptions = IFramePlayerOptions.Builder()
                    .controls(1)
                    .fullscreen(1) // enable full screen button
                    .build()
                youTubePlayerView?.initialize(this, false, iFramePlayerOptions)
                addPlayerToScreen(youTubePlayerView)
            }
        } catch (localException: NullPointerException) {
            logger.error(localException)
        }
    }

    private fun addPlayerToScreen(youTubePlayerView: View?) {
        view?.let {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            val playerContainer = it.findViewById<ViewGroup>(R.id.player_container)
            playerContainer.removeAllViews()
            playerContainer.addView(youTubePlayerView, params)
        }
    }

    override fun onPause() {
        super.onPause()
        releaseYoutubePlayer()
        EventBus.getDefault().unregister(this)
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        activity?.let { activity ->
            this.youTubePlayer = youTubePlayer
            youTubePlayer.addListener(this)
            val orientation = activity.resources?.configuration?.orientation
            var currentPos = 0
            if (videoModel != null) {
                currentPos = videoModel.getLastPlayedOffset().toInt()
            }
            val uri = Uri.parse(unit.data.encodedVideos.youtubeVideoInfo?.url)
            /*
             *  Youtube player loads the video using the video id from the url
             *  the url has the following format "https://www.youtube.com/watch?v=3_yD_cEKoCk" where v is the video id
             */
            val videoId = uri.getQueryParameter("v")
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                youTubePlayer.toggleFullscreen()
            }
            videoId?.let {
                youTubePlayer.loadVideo(
                    it,
                    (currentPos / AppConstants.MILLISECONDS_PER_SECOND).toFloat()
                )
            }
        }
    }

    override fun canProcessSubtitles(): Boolean {
        return youTubePlayer != null && currentPlayerState == PlayerState.PLAYING
    }

    override fun getPlayerCurrentPosition(): Long {
        return if (youTubePlayer == null) 0 else currentTimeMillis
    }

    override fun setFullScreen(fullscreen: Boolean) {
        if (DeviceSettingUtil.isDeviceRotationON(activity)) {
            val orientation = resources.configuration.orientation
            if ((isFullscreen && orientation == Configuration.ORIENTATION_LANDSCAPE) ||
                (!isFullscreen && orientation != Configuration.ORIENTATION_LANDSCAPE)
            ) {
                return
            }
            youTubePlayer?.toggleFullscreen()
        }
    }

    override fun playPauseVideoPlayback(pauseVideo: Boolean) {
        if (pauseVideo) {
            youTubePlayer?.pause()
        } else {
            youTubePlayer?.play()
        }
    }

    private fun redirectToYoutubeDialog() {
        releaseYoutubePlayer()
        if (activity?.isDestroyed != true) {
            (activity as BaseFragmentActivity)
                .showAlertDialog(
                    getString(R.string.assessment_unable_to_play_video),
                    getString(R.string.assessment_unable_to_play_video_message),
                    getString(R.string.assessment_open_on_youtube),
                    { _: DialogInterface, _: Int ->
                        BrowserUtil
                            .open(
                                activity,
                                unit.data.encodedVideos.youtubeVideoInfo?.url, true
                            )
                    },
                    getString(R.string.label_ok), null
                )
        }
    }

    private fun releaseYoutubePlayer() {
        saveCurrentPlaybackPosition(currentTimeMillis)
        (view?.findViewById<View>(R.id.player_container) as ViewGroup).removeAllViews()
        youTubePlayerView?.release()
        youTubePlayerView = null
        youTubePlayer = null
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
    }

    public override fun seekToCaption(caption: Caption) {
        saveCurrentPlaybackPosition(currentTimeMillis)
        youTubePlayer?.seekTo(caption.start.mseconds / 1000f)
    }

    private fun setVideoModel() {
        videoModel = DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE)
            .getVideoEntryByVideoId(unit.id, null) as DownloadEntry?
        if (videoModel == null) {
            val downloadEntry = DownloadEntry()
            downloadEntry.videoId = unit.id
            addVideoDatatoDb(downloadEntry)
            videoModel = downloadEntry
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEvent(event: NetworkConnectivityChangeEvent) {
        if (activity != null && NetworkUtil.isConnected(activity)) {
            initializeYoutubePlayer()
        }
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        currentTimeInSec = second.toDouble()
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerError) {
        /*
         * The most common errorReason is because there is a previous player running so this sets free it
         * and re-initialize the youtube player
         */
        if (attempts <= 3) {
            releaseYoutubePlayer()
            initializeYoutubePlayer()
            attempts++
        } else {
            redirectToYoutubeDialog()
        }
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerState) {
        currentPlayerState = state
        when (state) {
            PlayerState.ENDED -> {
                youTubePlayer.seekTo(0f)
                youTubePlayer.pause()
                onPlaybackComplete()
            }
            PlayerState.PLAYING -> {
                updateTranscriptCallbackStatus(true)
                if (videoModel != null) {
                    environment.analyticsRegistry.trackVideoPlaying(
                        videoModel.videoId,
                        currentTimeInSec, videoModel.eid, videoModel.lmsUrl,
                        Analytics.Values.YOUTUBE
                    )
                }
            }
            PlayerState.PAUSED -> {
                saveCurrentPlaybackPosition(playerCurrentPosition)
                updateTranscriptCallbackStatus(false)
                if (videoModel != null) {
                    environment.analyticsRegistry.trackVideoPause(
                        videoModel.videoId,
                        currentTimeInSec, videoModel.eid, videoModel.lmsUrl,
                        Analytics.Values.YOUTUBE
                    )
                }
            }
            PlayerState.UNKNOWN -> {
                redirectToYoutubeDialog()
            }
            else -> {}
        }
    }

    private val currentTimeMillis: Long get() = (currentTimeInSec * AppConstants.MILLISECONDS_PER_SECOND).toLong()

    override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: Function0<Unit>) {
        isFullscreen = true
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        addPlayerToScreen(fullscreenView)
        if (videoModel != null) {
            environment.analyticsRegistry.trackVideoOrientation(
                videoModel.videoId,
                currentTimeInSec, true, videoModel.eid, videoModel.lmsUrl,
                Analytics.Values.YOUTUBE
            )
        }
    }

    override fun onExitFullscreen() {
        isFullscreen = false
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        addPlayerToScreen(youTubePlayerView)
        if (videoModel != null) {
            environment.analyticsRegistry.trackVideoOrientation(
                videoModel.videoId,
                currentTimeInSec, false, videoModel.eid, videoModel.lmsUrl,
                Analytics.Values.YOUTUBE
            )
        }
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {}
    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}
    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {}
    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
    override fun showClosedCaptionData(subtitles: TimedTextObject) {}
    override fun updateClosedCaptionData(caption: Caption?) {}
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlaybackRate) {}
    override fun onPlaybackQualityChange(
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlaybackQuality
    ) {
    }

    companion object {
        @JvmStatic
        fun newInstance(unit: VideoBlockModel?): CourseUnitYoutubePlayerFragment {
            val fragment = CourseUnitYoutubePlayerFragment()
            val args = Bundle()
            args.putSerializable(Router.EXTRA_COURSE_UNIT, unit)
            fragment.arguments = args
            return fragment
        }
    }
}
