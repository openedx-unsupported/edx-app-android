package org.edx.mobile.model.api;

import org.edx.mobile.interfaces.SectionItemInterface;


@SuppressWarnings("serial")
public class VideoResponseModel implements SectionItemInterface {

    private PathModel[] path;
    private SummaryModel summary;
    private String courseId;
    public String section_url;
    public String unit_url;

    /**
     * Returns chapter object of this model. The chapter object is found
     * by traversing down a PathModel[]. If the chapter cannot be found,
     * returns null.
     *
     * @return      Chapter object of PathModel
     */
    public PathModel getChapter() {
        // not being depend on array index
        // check if the object is really a chapter object

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
    public PathModel getSection() {
        // not being depend on array index
        // check if the object is really a section object

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
            return summary.getName();
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
        return getChapter().name;
    }

    public String getSequentialName() {
        return getSection().name;
    }
    
}
