package org.edx.mobile.module.notification;

import org.edx.mobile.model.api.CourseEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * We extends HashMap to make the implementation extendible
 */
public class NotificationPreference extends ArrayList<EdxLocalParseChannel>{

    public NotificationPreference(){

    }

    public EdxLocalParseChannel getByCourseId(String courseId){
        for(EdxLocalParseChannel c : this ){
            if ( courseId.equals(c.getCourseId()) )
                return c;
        }
        return null;
    }

    public EdxLocalParseChannel getByChannelId(String channelId){
        for(EdxLocalParseChannel c : this ){
            if ( channelId.equals(c.getChannelId()) )
                return c;
        }
        return null;
    }

    public void removeByCourseId(String courseId){
        EdxLocalParseChannel pc = this.getByChannelId(courseId);
        if ( pc != null )
            this.remove(pc);
    }

    public void removeByChannelId(String channelId){
        EdxLocalParseChannel pc = this.getByChannelId(channelId);
        if ( pc != null )
            this.remove(pc);
    }


    /**
     * find a list of courseId which is not in the dictionary
     * @param courseEntryList
     * @return
     */
    public List<CourseEntry> filterForNewCourses(List<CourseEntry> courseEntryList){
        List<CourseEntry>  newCourseList = new ArrayList<>();
        for(CourseEntry entry : courseEntryList){
            if ( this.getByCourseId(entry.getId()) == null ){
                newCourseList.add(entry);
            }
        }
        return newCourseList;
    }

    /**
     * find all the course entry in saved preference which are not in the current active
     * course list
     * @param courseEntryList
     * @return
     */
    public List<EdxLocalParseChannel> filterForInactiveCourses(List<CourseEntry> courseEntryList){
        Set<String> activeCourseList = new HashSet<>();
        for(CourseEntry entry : courseEntryList){
            activeCourseList.add(entry.getId());
        }
        List<EdxLocalParseChannel> inactiveCourseList = new LinkedList<EdxLocalParseChannel>( );
        for( EdxLocalParseChannel pc : this ){
            if ( !activeCourseList.contains(pc.getCourseId()) ){
                inactiveCourseList.add(pc);
            }
        }
        return inactiveCourseList;
    }

}
