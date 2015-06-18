package org.edx.mobile.model.course;

import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.IStorage;

/**
 * Created by hanning on 5/20/15.
 */
public interface HasDownloadEntry {
    DownloadEntry getDownloadEntry(IStorage storage);
    long getSize();
}
