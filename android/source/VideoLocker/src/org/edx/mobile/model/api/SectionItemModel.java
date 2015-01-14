package org.edx.mobile.model.api;

import org.edx.mobile.interfaces.SectionItemInterface;

/**
 * @author stamboli Class: This class is used for defining Section titles in a
 *         VideoList
 * 
 */
public class SectionItemModel implements SectionItemInterface {
    
    public String name;

    @Override
    public boolean isChapter() {
        return false;
    }

    @Override
    public boolean isSection() {
        return true;
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean isCourse() {
        return false;
    }

    @Override
    public boolean isVideo() {
        return false;
    }

    @Override
    public boolean isDownload() {
        return false;
    }
}
