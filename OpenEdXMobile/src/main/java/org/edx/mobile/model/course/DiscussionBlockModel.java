package org.edx.mobile.model.course;

import androidx.annotation.NonNull;

public class DiscussionBlockModel extends CourseComponent {
    private DiscussionData data;

    public DiscussionBlockModel(@NonNull DiscussionBlockModel other) {
        super(other);
        this.data = other.data;
    }

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
