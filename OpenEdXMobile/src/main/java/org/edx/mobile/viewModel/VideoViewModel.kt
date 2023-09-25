package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.edx.mobile.R
import org.edx.mobile.model.course.HasDownloadEntry
import org.edx.mobile.model.db.DownloadEntry
import org.edx.mobile.module.analytics.AnalyticsRegistry
import org.edx.mobile.module.storage.BulkVideosDownloadCancelledEvent
import org.edx.mobile.module.storage.Storage
import org.edx.mobile.player.TranscriptManager
import org.edx.mobile.util.DownloadUtil
import org.edx.mobile.util.MemoryUtil
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * The [VideoViewModel] is shared between Parent & Child Fragment so they can only communicate with
 * each other. In this way, the fragments do not need to know about each other, and the activity
 * does not need to do anything to facilitate the communication.
 */
@HiltViewModel
class VideoViewModel @Inject constructor(
    private val storage: Storage,
    private val transcriptManager: TranscriptManager,
    private val analyticsRegistry: AnalyticsRegistry,
) : ViewModel() {

    private val _infoMessage = MutableLiveData<Event<Int>>()
    val infoMessage: LiveData<Event<Int>> = _infoMessage

    private val _downloadSizeExceeded = MutableLiveData<Event<MutableList<DownloadEntry>>>()
    val downloadSizeExceeded: LiveData<Event<MutableList<DownloadEntry>>> = _downloadSizeExceeded

    private val _refreshUI = MutableLiveData<Event<Boolean>>()
    val refreshUI: LiveData<Event<Boolean>> = _refreshUI

    private val _clearChoices = MutableLiveData<Event<Boolean>>()
    val clearChoices: LiveData<Event<Boolean>> = _clearChoices

    private val _selectedVideosPosition = MutableLiveData<Event<Pair<Int, Int>>>()
    val selectedVideosPosition: LiveData<Event<Pair<Int, Int>>> = _selectedVideosPosition

    private var shouldClearChoices = true

    fun downloadMultipleVideos(downloads: MutableList<HasDownloadEntry>?) {
        if (downloads.isNullOrEmpty()) return

        var totalDownloadSize = 0L
        val videosToDownload = mutableListOf<DownloadEntry>()

        for (video in downloads) {
            val downloadEntry: DownloadEntry = video.getDownloadEntry(storage) ?: continue
            downloadEntry.url =
                video.downloadUrl.takeUnless { it.isNullOrEmpty() } ?: downloadEntry.url

            if (downloadEntry.isDownloading || downloadEntry.isDownloaded || downloadEntry.isVideoForWebOnly) {
                continue
            } else {
                totalDownloadSize += downloadEntry.getSize().toInt()
                videosToDownload.add(downloadEntry)
            }
        }

        if (totalDownloadSize > MemoryUtil.getAvailableExternalMemory()) {
            _infoMessage.postEvent(R.string.file_size_exceeded)
            EventBus.getDefault().post(BulkVideosDownloadCancelledEvent())
            return
        }

        if (DownloadUtil.isDownloadSizeWithinLimit(totalDownloadSize, MemoryUtil.GB)
            && videosToDownload.isNotEmpty()
        ) {
            startDownload(videosToDownload)
        } else {
            _downloadSizeExceeded.postEvent(videosToDownload)
        }
    }

    fun downloadSingleVideo(download: DownloadEntry?) {
        download?.let {
            startDownload(listOf(download), false)
        }
    }

    fun startDownload(videosToDownload: List<DownloadEntry>, bulkDownload: Boolean = true) {
        if (videosToDownload.isEmpty()) return
        trackDownloadEvent(videosToDownload, bulkDownload)

        viewModelScope.launch {
            var count = 0
            for (downloadEntry in videosToDownload) {
                if (storage.addDownload(downloadEntry) != -1L) {
                    count++
                    downloadEntry.transcript?.run {
                        for (value in this.values) {
                            transcriptManager.downloadTranscriptsForVideo(value, null)
                        }
                    }
                }
            }
            _refreshUI.postEvent(true)
        }
    }

    fun deleteVideosAtPosition(position: Pair<Int, Int>) {
        _selectedVideosPosition.postEvent(position)
        shouldClearChoices = false
    }

    fun clearChoices() {
        // Avoid clearing choices when the flag is false, as video deletion will handle choice
        // clearance automatically.
        if (shouldClearChoices) {
            _clearChoices.postEvent(true)
        }
        shouldClearChoices = true
    }

    private fun trackDownloadEvent(videosToDownload: List<DownloadEntry>, bulkDownload: Boolean) {
        if (bulkDownload) {
            videosToDownload[0].apply {
                analyticsRegistry.trackSubSectionBulkVideoDownload(
                    sectionName, chapterName, enrollmentId, size
                )
            }
        } else {
            videosToDownload[0].apply {
                analyticsRegistry.trackSingleVideoDownload(videoId, enrollmentId, videoUrl)
            }
        }
    }
}
