package org.edx.mobile.task;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.player.TranscriptManager;

import java.util.List;

public abstract class EnqueueDownloadTask extends Task<Long> {
    @Inject
    @NonNull
    TranscriptManager transcriptManager;
    @NonNull
    List<DownloadEntry> downloadList;

    public EnqueueDownloadTask(@NonNull Context context, @NonNull List<DownloadEntry> downloadList) {
        super(context);
        this.downloadList = downloadList;
    }

    @Override
    public Long call() {
        int count = 0;
        for (DownloadEntry downloadEntry : downloadList) {
            if (environment.getStorage().addDownload(downloadEntry) != -1) {
                count++;
                for (String value : downloadEntry.transcript.values()) {
                    transcriptManager.downloadTranscriptsForVideo(value, null);
                }
            }
        }
        return (long) count;
    }
}
