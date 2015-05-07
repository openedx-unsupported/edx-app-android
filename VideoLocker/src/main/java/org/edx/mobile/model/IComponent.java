package org.edx.mobile.model;

import java.io.Serializable;

/**
 * Created by hanning on 4/29/15.
 */
public interface IComponent extends Serializable{
    String getId();
    void setId(String id);
    String getCategory();
    void setCategory(String category);
    String getName();
    void setName(String name);
}
