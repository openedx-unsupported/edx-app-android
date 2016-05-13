package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.interfaces.SectionItemInterface;

import java.util.List;

public class GetRecentDownloadedVideosTask extends Task<List<SectionItemInterface>> {
    public GetRecentDownloadedVideosTask(@NonNull Context context) {
        super(context);
    }

    @Override
    public List<SectionItemInterface> call() throws Exception {
        return environment.getStorage().getRecentDownloadedVideosList();
    }
}
