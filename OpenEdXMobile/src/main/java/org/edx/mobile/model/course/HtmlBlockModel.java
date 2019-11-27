package org.edx.mobile.model.course;

import androidx.annotation.NonNull;

public class HtmlBlockModel extends CourseComponent{

    private final BlockData data;

    public HtmlBlockModel(@NonNull HtmlBlockModel other) {
        super(other);
        this.data = other.data;
    }

    public HtmlBlockModel(BlockModel blockModel, IBlock parent){
        super(blockModel, (CourseComponent)parent);
        this.data =  blockModel.data;
    }

    public BlockData getData() {
        return data;
    }
}
