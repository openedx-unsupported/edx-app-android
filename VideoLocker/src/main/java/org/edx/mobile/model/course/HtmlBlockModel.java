package org.edx.mobile.model.course;

public class HtmlBlockModel extends CourseComponent{

    private final BlockData data;

    public HtmlBlockModel(BlockModel blockModel, IBlock parent){
        super(blockModel, (CourseComponent)parent);
        this.data =  blockModel.data;
    }

    public BlockData getData() {
        return data;
    }
}
