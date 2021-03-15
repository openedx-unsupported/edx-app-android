package org.edx.mobile.task

import android.content.Context
import com.google.inject.Inject
import org.edx.mobile.model.db.DownloadEntry
import org.edx.mobile.player.TranscriptManager

abstract class EnqueueDownloadTask(context: Context,
                                   private var downloadList: List<DownloadEntry>) : Task<Long?>(context) {
    @Inject
    lateinit var transcriptManager: TranscriptManager

    override fun call(): Long {
        var count = 0
        for (downloadEntry in downloadList) {
            if (environment.storage.addDownload(downloadEntry) != -1L) {
                count++
                downloadEntry.transcript?.run {
                    for (value in this.values) {
                        transcriptManager.downloadTranscriptsForVideo(value, null)
                    }
                }
            }
        }
        return count.toLong()
    }

    override fun onException(ex: Exception) {
        super.onException(ex)
        logger.error(ex)
    }
}
