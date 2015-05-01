package org.edx.mobile.model;

import org.edx.mobile.model.db.DownloadEntry;

public interface IUnit extends IComponent{
    IVertical getVertical();
    void setVertical(IVertical vertical);

    DownloadEntry getDownloadEntry();
    void setDownloadEntry(DownloadEntry entry);
}
