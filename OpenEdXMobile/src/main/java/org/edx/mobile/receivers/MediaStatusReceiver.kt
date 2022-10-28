package org.edx.mobile.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.event.MediaStatusChangeEvent
import org.edx.mobile.model.VideoModel
import org.edx.mobile.model.db.DownloadEntry
import org.edx.mobile.model.db.DownloadEntry.DownloadedState
import org.edx.mobile.module.db.DataCallback
import org.edx.mobile.module.db.IDatabase
import org.edx.mobile.util.FileUtil
import org.edx.mobile.util.Sha1Util
import org.edx.mobile.util.VideoUtil
import org.greenrobot.eventbus.EventBus
import java.io.File
import javax.inject.Inject

/**
 * BroadcastReceiver to receive the removable storage (such as SD-card) status events.
 */
@AndroidEntryPoint
class MediaStatusReceiver : BroadcastReceiver() {

    @Inject
    lateinit var db: IDatabase

    @Inject
    lateinit var environment: IEdxEnvironment

    override fun onReceive(context: Context, intent: Intent) {
        val hashedUsername = if (environment.loginPrefs.isUserLoggedIn)
            Sha1Util.SHA1(environment.loginPrefs.username) else null
        val sdCardPath = intent.dataString?.replace("file://", "")

        val sdCardAvailable = when (intent.action) {
            Intent.ACTION_MEDIA_REMOVED,
            Intent.ACTION_MEDIA_UNMOUNTED -> {
                handleSDCardUnmounted(hashedUsername, sdCardPath)
                false
            }

            Intent.ACTION_MEDIA_MOUNTED -> {
                handleSDCardMounted(context, hashedUsername)
                true
            }

            else -> {
                false
            }
        }
        EventBus.getDefault().postSticky(MediaStatusChangeEvent(sdCardAvailable))
    }

    private fun handleSDCardUnmounted(hashedUsername: String?, sdCardPath: String?) {
        if (sdCardPath.isNullOrEmpty()) return

        db.getAllVideos(hashedUsername, object : DataCallback<List<VideoModel>>() {

            override fun onResult(result: List<VideoModel>) {
                result.filter { sdCardPath in it.filePath }
                    .forEach { videoModel ->
                        VideoUtil.updateVideoDownloadState(
                            db,
                            videoModel,
                            DownloadedState.ONLINE.ordinal
                        )
                    }
            }

            override fun onFail(ex: Exception) {
                Log.e(this.javaClass.simpleName, "Unable to get to get list of Videos")
            }
        })
    }

    private fun handleSDCardMounted(context: Context, hashedUsername: String?) {
        db.getAllVideos(hashedUsername, object : DataCallback<List<VideoModel>>() {

            override fun onResult(result: List<VideoModel>) {
                val externalAppDir = FileUtil.getExternalAppDir(context)?.absolutePath
                val removableStorageAppDir =
                    FileUtil.getRemovableStorageAppDir(context)?.absolutePath
                val downloadToSdCard = environment.userPrefs.isDownloadToSDCardEnabled

                if (externalAppDir.isNullOrEmpty() || removableStorageAppDir.isNullOrEmpty())
                    return

                result.forEach { videoModel ->
                    updateVideoDownloadFilePathState(
                        videoModel,
                        downloadToSdCard,
                        externalAppDir,
                        removableStorageAppDir
                    )
                }
            }

            override fun onFail(ex: Exception) {
                Log.e(this.javaClass.simpleName, "Unable to get list of Videos")
            }
        })
    }

    /**
     * Utility method to update the downloaded video file info(if multiple file are exist in phone
     * memory / SD-Card for the single video).
     *
     * @param videoModel              Video info need to update.
     * @param downloadToSdCardEnabled User preference from Profile screen.
     * @param externalAppDir          Phone memory path where downloaded video files exist
     * @param removableStorageAppDir  SD-Card storage path where downloaded video files exist.
     */
    private fun updateVideoDownloadFilePathState(
        videoModel: VideoModel, downloadToSdCardEnabled: Boolean,
        externalAppDir: String, removableStorageAppDir: String
    ) {
        val file = try {
            File(videoModel.filePath)
        } catch (ex: Exception) {
            return
        }
        if (file.exists().not()) return

        val duplicateFile = getDuplicateFile(
            videoModel.filePath,
            externalAppDir,
            removableStorageAppDir
        )

        if (duplicateFile != null && duplicateFile.exists()) {
            /**
             * If the duplicate file exist in the SD-Card, and DOWNLOAD_TO_SDCARD is enabled from
             * the Profile screen then keep the duplicate file and delete the original file from
             * the Phone memory.
             */
            if (removableStorageAppDir in duplicateFile.absolutePath && downloadToSdCardEnabled) {
                updateVideoModelWithPreferredFilePath(
                    videoModel,
                    duplicateFile,
                    file
                )
            }
            /**
             * If the file exist in the Phone memory, and DOWNLOAD_TO_SDCARD is disabled from the
             * Profile screen then keep the phone memory file and delete the duplicate file (file
             * exist in the SD-Card).
             */
            else if (externalAppDir in file.absolutePath && !downloadToSdCardEnabled) {
                updateVideoModelWithPreferredFilePath(
                    videoModel,
                    file,
                    duplicateFile
                )
            } else {
                FileUtil.deleteRecursive(duplicateFile)
            }
        }
        VideoUtil.updateVideoDownloadState(
            db,
            videoModel,
            DownloadedState.DOWNLOADED.ordinal
        )

    }

    /**
     * Utility method to update the video info based on the preferred file and delete duplicate file.
     *
     * @param videoModel    Video model need to update.
     * @param preferredFile User preferred file.
     * @param duplicateFile Duplicate file need to delete.
     * @return return updated video model.
     */
    private fun updateVideoModelWithPreferredFilePath(
        videoModel: VideoModel,
        preferredFile: File,
        duplicateFile: File
    ): VideoModel {
        val videoDownloadEntry = DownloadEntry().apply {
            setDownloadInfo(videoModel)
            filepath = preferredFile.absolutePath
            videoId = videoModel.videoId
        }
        FileUtil.deleteRecursive(duplicateFile)
        return videoDownloadEntry
    }

    /**
     * Utility method to find the duplicate file path from Phone memory / SD-Card if there are two
     * files for the same video in Phone memory and SD-Card.
     *
     * @param videoPath              video path from the database.
     * @param externalAppDir         Phone memory storage path.
     * @param removableStorageAppDir SD-Card storage path.
     * @return return duplicate file.
     */
    private fun getDuplicateFile(
        videoPath: String,
        externalAppDir: String,
        removableStorageAppDir: String
    ): File? {
        return if (externalAppDir in videoPath) {
            File(videoPath.replace(externalAppDir, removableStorageAppDir))
        } else if (removableStorageAppDir in videoPath) {
            File(videoPath.replace(removableStorageAppDir, externalAppDir))
        } else {
            null
        }
    }
}
