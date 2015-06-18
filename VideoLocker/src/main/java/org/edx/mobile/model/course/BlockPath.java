package org.edx.mobile.model.course;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hanning on 6/1/15.
 */
public class BlockPath implements Serializable{
    private List<CourseComponent> path = new ArrayList<>();

    public void addPathNodeToPathFront(CourseComponent component){
        path.add(0, component);
    }
    public List<CourseComponent> getPath(){
        return Collections.unmodifiableList(path);
    }

    /**
     *
     * @param index position on the path from root down. root index is 0
     * @return can be null for overflow.
     */
    public CourseComponent get(int index){
        int size = path.size();
        return size > index ? path.get(index) : null;
    }

    /**
     * return a path from top down,  each node is represented by node id.
     * we will use it in the database for different level of aggregation.
     */
    public String getPathString(){
        StringBuilder sb = new StringBuilder();
        for( CourseComponent comp : path){
            sb.append(comp.getId() + "/");
        }
        return sb.toString();
    }

}
