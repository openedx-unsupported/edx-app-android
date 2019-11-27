package org.edx.mobile.model.course;

import androidx.annotation.Nullable;

import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.IStorage;

public interface HasDownloadEntry {
    @Nullable
    DownloadEntry getDownloadEntry(IStorage storage);

    @Nullable
    String getDownloadUrl();
}
