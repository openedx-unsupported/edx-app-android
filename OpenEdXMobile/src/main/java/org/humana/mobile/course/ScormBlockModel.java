package org.humana.mobile.course;

import org.humana.mobile.model.course.BlockModel;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.scorm.ScormData;

public class ScormBlockModel extends CourseComponent {
    private ScormData data;

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
}
