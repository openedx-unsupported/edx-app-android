package org.humana.mobile.tta.scorm;

import android.support.annotation.Nullable;

import org.humana.mobile.model.course.BlockModel;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.model.course.HasDownloadEntry;
import org.humana.mobile.model.db.DownloadEntry;
import org.humana.mobile.module.storage.IStorage;

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
//        return "http://tutorial.math.lamar.edu/pdf/Trig_Cheat_Sheet.pdf";
        if (data == null){
            return null;
        }
        return data.scormData;
        //"https://www.nationallibertyalliance.org/files/docs/Books/Atlas%20Shrugged.pdf"
    }
}
