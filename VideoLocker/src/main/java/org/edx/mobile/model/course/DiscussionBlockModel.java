package org.edx.mobile.model.course;

public class DiscussionBlockModel extends CourseComponent {
    private DiscussionData data;

    public DiscussionBlockModel(BlockModel blockModel, CourseComponent parent) {
        super(blockModel, parent);
        this.data = (DiscussionData) blockModel.data;
    }

    public DiscussionData getData() {
        return data;
    }

    public void setData(DiscussionData data) {
        this.data = data;
    }
}
