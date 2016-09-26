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

    /**
     *
     * @param courseId
     * @return <code>null</code> if no record found
     */
    public EdxLocalParseChannel getByCourseId(String courseId){
        for(EdxLocalParseChannel c : this ){
            if ( courseId.equals(c.getCourseId()) )
                return c;
        }
        return null;
    }

    /**
     *
     * @param channelId
     * @return <code>null</code> if no record found
     */
    public EdxLocalParseChannel getByChannelId(String channelId){
        for(EdxLocalParseChannel c : this ){
            if ( channelId.equals(c.getChannelId()) )
                return c;
        }
        return null;
    }

    /**
     *
     * @param courseId
     *  @return <code>false</code> if operation fails
     */
    public boolean removeByCourseId(String courseId){
        EdxLocalParseChannel pc = this.getByCourseId(courseId);
        if ( pc != null )
           return this.remove(pc);
        return false;
    }

    /**
     *
     * @param channelId
     *  @return <code>false</code> if operation fails
     */
    public boolean removeByChannelId(String channelId){
        EdxLocalParseChannel pc = this.getByChannelId(channelId);
        if ( pc != null )
           return this.remove(pc);
        return false;
    }

    /**
     *
     * @param channel
     * @return  <code>false</code> if operation fails
     */
    public boolean add(EdxLocalParseChannel channel){
        return super.add( channel );
    }
    /**
     * find a list of courseId which is not in the dictionary
     * @param courseEntryList
     * @return empty list if no record found
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
     * @param courseEntryList a list of course entries which are in active status
     * @return empty list if no record found
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

    /**
     * get all the subscribed channels. it is not the same as
     * total number of elements in this collection
     * @return
     */
    public List<String> getAllSubscribedChannels(){
        List<String> channels = new ArrayList<>();
        for( EdxLocalParseChannel pc : this ){
            if ( pc.isSubscribed()  ){
                channels.add(pc.getChannelId());
            }
        }
        return channels;
    }

    /**
     * get all the channels which failed to sync with parse server
     * @return
     */
    public List<EdxLocalParseChannel> getAllFailedUpdate(){

        List<EdxLocalParseChannel> channelList = new LinkedList<EdxLocalParseChannel>( );
        for( EdxLocalParseChannel pc : this ){
            if ( pc.isOperationFailed() ){
                channelList.add(pc);
            }
        }
        return channelList;
    }

    /**
     * mark all the records failed for update. it happens when doing th bulk update
     */
    public void markAllFailedUpdate(){
        for( EdxLocalParseChannel pc : this ){
            pc.setOperationFailed(true);
        }
    }


}
