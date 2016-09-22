package org.edx.mobile.interfaces;

import java.io.Serializable;

public interface SectionItemInterface extends Serializable {

    public boolean isCourse();
    public boolean isChapter();
    public boolean isSection();
    public boolean isVideo();
    public boolean isDownload();
}
