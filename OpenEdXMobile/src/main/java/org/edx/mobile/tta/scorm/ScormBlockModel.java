package org.edx.mobile.tta.scorm;

import org.edx.mobile.model.course.BlockModel;
import org.edx.mobile.model.course.CourseComponent;

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
