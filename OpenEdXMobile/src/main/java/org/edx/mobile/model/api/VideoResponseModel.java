package org.edx.mobile.model.api;

import androidx.annotation.Nullable;

import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.IStorage;

import java.util.EnumSet;


@SuppressWarnings("serial")
public class VideoResponseModel implements SectionItemInterface, HasDownloadEntry {

    private PathModel[] path;
    private SummaryModel summary;
    private String courseId;
    private String section_url;
    private String unit_url;
    public VideoBlockModel videoBlockModel;

    private DownloadEntry downloadEntry;

    /**
     * Returns chapter object of this model. The chapter object is found
     * by traversing down a PathModel[]. If the chapter cannot be found,
     * returns null.
     *
     * @return      Chapter object of PathModel
     */
    public IPathNode getChapter() {
        // not being depend on array index
        // check if the object is really a chapter object
        if ( videoBlockModel != null)
            return videoBlockModel.getAncestor(EnumSet.of(BlockType.CHAPTER));

        for (int i = 0; i < path.length; i++) {
            if (path[i].isChapter())
                return path[i];
        }
        return null;
    }

    /**
     * Returns section object of this model. The section object is found
     * by traversing down a PathModel[]. If the section cannot be found,
     * returns null
     *
     * @return      Section object of this PathModel
     */
    public IPathNode getSection() {
        // not being depend on array index
        // check if the object is really a section object
        if ( videoBlockModel != null)
            return videoBlockModel.getAncestor(EnumSet.of(BlockType.SECTION, BlockType.SEQUENTIAL));

        for (int i = 0; i < path.length; i++) {
            if (path[i].isSequential())
                return path[i];
        }
        return null;
    }

    public SummaryModel getSummary() {
        return summary;
    }

    public void setSummary(SummaryModel summary) {
        this.summary = summary;
    }

    @Override
    public boolean isSection() {
        // video response model is never a section
        return false;
    }

    public boolean isDownload_allowed() {
        // TODO API should specify if the download for this video is allowed or not ?
        return true;
    }

    @Override
    public boolean isChapter() {
        // video response model is never a chapter
        return false;
    }
    
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    @Override
        public String toString() {
            return summary.getDisplayName();
        }

    @Override
    public boolean isCourse() {
        return false;
    }

    @Override
    public boolean isVideo() {
        return true;
    }

    @Override
    public boolean isDownload() {
        return false;
    }

    public String getChapterName() {
        return getChapter().getDisplayName();
    }

    public String getSequentialName() {
        return getSection().getDisplayName();
    }

    public String getUnitUrl() {
        return unit_url;
    }

    public void setUnitUrl(String unit_url) {
        this.unit_url = unit_url;
    }

    public String getSectionUrl() {
        return section_url;
    }

    public void setSectionUrl(String section_url) {
        this.section_url = section_url;
    }

    @Override
    public DownloadEntry getDownloadEntry(IStorage storage) {
        if ( storage != null ) {
            downloadEntry = (DownloadEntry) storage
                .getDownloadEntryfromVideoResponseModel(this);
        }
        return downloadEntry;
    }

    @Nullable
    @Override
    public String getDownloadUrl() {
        return null;
    }

    public long getSize(){
        return summary == null ? 0 : summary.getSize();
    }
}
