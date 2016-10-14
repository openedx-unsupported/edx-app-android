package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

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
    public Long call() throws Exception {
        int count = 0;
        for (DownloadEntry de : downloadList) {
            if (environment.getStorage().addDownload(de) != -1) {
                count++;
                transcriptManager.downloadTranscriptsForVideo(de.transcript);
            }
        }
        return (long) count;
    }
}
