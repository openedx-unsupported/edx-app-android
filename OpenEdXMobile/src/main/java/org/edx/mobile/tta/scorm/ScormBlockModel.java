package org.edx.mobile.tta.scorm;

import android.support.annotation.Nullable;

import org.edx.mobile.model.course.BlockModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.IStorage;

public class ScormBlockModel extends CourseComponent implements HasDownloadEntry {

    private ScormData data;
    private DownloadEntry downloadEntry;

    public ScormBlockModel(BlockModel blockModel, CourseComponent parent) {
        super(blockModel, parent);
        this.data = (ScormData) blockModel.data;
    }

    public ScormData getData() {
        return data;
    }

    public void setData(ScormData data) {
        this.data = data;
    }

    @Nullable
    @Override
    public DownloadEntry getDownloadEntry(IStorage storage) {
        if ( storage != null ) {
            downloadEntry = (DownloadEntry) storage
                    .getDownloadEntryFromScormModel(this);
        }
        return downloadEntry;
    }

    @Nullable
    @Override
    public String getDownloadUrl() {
        if (data == null){
            return null;
        }
        return data.scormData;
    }

}
