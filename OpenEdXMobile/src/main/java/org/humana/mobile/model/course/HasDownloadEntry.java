package org.humana.mobile.model.course;

import android.support.annotation.Nullable;

import org.humana.mobile.model.db.DownloadEntry;
import org.humana.mobile.module.storage.IStorage;

public interface HasDownloadEntry {
    @Nullable
    DownloadEntry getDownloadEntry(IStorage storage);

    @Nullable
    String getDownloadUrl();
}
