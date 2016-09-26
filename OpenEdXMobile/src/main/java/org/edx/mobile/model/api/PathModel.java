package org.edx.mobile.model.api;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PathModel implements Serializable, IPathNode {
    
    private String category;
    private String name;
    private String id;
    
    /*
     * Returns true if this is a CHAPTER.
     */
    public boolean isChapter() {
        return category.equalsIgnoreCase("chapter");
    }
    
    /**
     * Returns true if this is a SECTION.
     * @return
     */
    public boolean isSequential() {
        return category.equalsIgnoreCase("sequential");
    }

    /*
     * Returns true if this is a VERTICAL.
     * This is not to be used in the mobile app.
     */
    public boolean isVertical() {
        return category.equalsIgnoreCase("vertical");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
