package org.edx.mobile.model.course;

import java.io.Serializable;
import java.util.List;

public interface IBlock extends Serializable {
    String getId();

    void setId(String id);

    String getBlockId();

    void setBlockId(String blockId);

    BlockType getType();

    void setType(BlockType category);

    String getDisplayName();

    void setDisplayName(String name);

    boolean isGraded();

    void setGraded(boolean graded);

    String getBlockUrl();

    void setBlockUrl(String url);

    String getWebUrl();

    void setWebUrl(String webUrl);

    BlockCount getBlockCount();

    void setBlockCount(BlockCount count);

    IBlock getParent();

    List<IBlock> getChildren();

    IBlock getRoot();

    String getCourseId();

    void setCourseId(String courseId);

    String getFormat();

    void setFormat(String format);
}
