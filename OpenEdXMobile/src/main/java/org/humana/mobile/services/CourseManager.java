package org.humana.mobile.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.humana.mobile.course.CourseAPI;
import org.humana.mobile.course.ScormBlockModel;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.model.Filter;
import org.humana.mobile.model.VideoModel;
import org.humana.mobile.model.course.BlockModel;
import org.humana.mobile.model.course.BlockType;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.model.course.CourseStructureV1Model;
import org.humana.mobile.model.course.DiscussionBlockModel;
import org.humana.mobile.model.course.DiscussionData;
import org.humana.mobile.model.course.HtmlBlockModel;
import org.humana.mobile.model.course.IBlock;
import org.humana.mobile.model.course.VideoBlockModel;
import org.humana.mobile.model.course.VideoData;
import org.humana.mobile.tta.scorm.PDFBlockModel;
import org.humana.mobile.tta.scorm.ScormData;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the caching mechanism of courses data.
 */
@Singleton
public class CourseManager {
    protected final Logger logger = new Logger(getClass().getName());

    /**
     * This count represents the maximum no of courses that will be cached via app level cache.
     */
    private final int NO_OF_COURSES_TO_CACHE = 15;

    /**
     * An app level cache to keep courses data in memory till ending of app session.
     */
    private final LruCache<String, CourseComponent> cachedComponent;

    private final LruCache<String, List<CourseComponent>> cachedBlockComponents;

    @Inject
    CourseAPI courseApi;

    @Inject
    ServiceManager serviceManager;


    public CourseManager() {
        cachedComponent = new LruCache<>(NO_OF_COURSES_TO_CACHE);
        cachedBlockComponents = new LruCache<>(NO_OF_COURSES_TO_CACHE);
    }

    public void clearAllAppLevelCache() {
        cachedComponent.evictAll();
        cachedBlockComponents.evictAll();
    }

    public void addCourseDataInAppLevelCache(@NonNull String courseId,
                                             @NonNull CourseComponent courseComponent) {
        cachedComponent.put(courseId, courseComponent);
    }

    /**
     * Obtain the course data from app level cache.
     *
     * @param courseId Id of the course.
     * @return Cached course data. In case course data is not present in cache it will return null.
     */
    @Nullable
    public CourseComponent getCourseDataFromAppLevelCache(@NonNull final String courseId) {
        return cachedComponent.get(courseId);
    }

    /**
     * Obtain the course data from persistable cache.
     * <p>
     * <b>WARNING:</b> This function takes time and should call asynchronously.
     * {@link CourseManager#getCourseDataFromAppLevelCache} can be used as an alternate, specially
     * if its sure course data will be available in app level cache.
     *
     * @param courseId Id of the course.
     * @return Cached course data. In case course data is not present in cache it will return null.
     */
    @Nullable
    public CourseComponent getCourseDataFromPersistableCache(@NonNull final String courseId) {
        try {
            final CourseComponent component = courseApi.getCourseStructureFromCache(courseId);
            addCourseDataInAppLevelCache(courseId, component);
            return component;
        } catch (Exception e) {
            // Course data doesn't exist in cache
            return null;
        }
    }

    public static IBlock normalizeCourseStructure(CourseStructureV1Model courseStructureV1Model, String courseId){
        BlockModel topBlock = courseStructureV1Model.getBlockById(courseStructureV1Model.root);
        CourseComponent course = new CourseComponent(topBlock, null);
        course.setCourseId(courseId);
        for (BlockModel m : courseStructureV1Model.getDescendants(topBlock)) {
            normalizeCourseStructure(courseStructureV1Model,m,course);
        }
        return course;
    }

    private static void normalizeCourseStructure(CourseStructureV1Model courseStructureV1Model,
                                                 BlockModel block,
                                                 CourseComponent parent) {
        if (block.isContainer()) {
            CourseComponent child = new CourseComponent(block, parent);
            for (BlockModel m : courseStructureV1Model.getDescendants(block)) {
                normalizeCourseStructure(courseStructureV1Model, m, child);
            }
        } else {
            if (BlockType.VIDEO == block.type && block.data instanceof VideoData) {
                new VideoBlockModel(block, parent);
            } else if (BlockType.DISCUSSION == block.type && block.data instanceof DiscussionData) {
                new DiscussionBlockModel(block, parent);
            } else if (BlockType.SCORM == block.type && block.data instanceof ScormData) {
                new ScormBlockModel(block, parent);
            }
            //added by Arjun to integrate pdf xblock in android.
            else if (BlockType.PDF == block.type && block.data instanceof ScormData)
            {
                ///TODO :need to work on Arjun
                new PDFBlockModel(block, parent);
                //new ScormBlockModel(block, parent);
            }
            else if(BlockType.OFFICEMIX == block.type)
            {
                new HtmlBlockModel(block, parent);
                //new OfficeMixBlockModel(block, parent);
            }
            else { //everything else.. we fallback to html component
                new HtmlBlockModel(block, parent);
            }
        }
    }

    @Deprecated
    @Nullable
    private CourseComponent getCachedCourseData(@NonNull final String courseId) {
        final CourseComponent component = getCourseDataFromAppLevelCache(courseId);
        if (component != null) {
            return component;
        }
        return getCourseDataFromPersistableCache(courseId);
    }

    /**
     * Obtain the course data from app level cache and then find the specified course component in it.
     *
     * @param courseId    Id of the course.
     * @param componentId Id of the course component.
     * @return Searched out course component. In case course data is not present in
     * app level cache or specified component doesn't exist in the course it will return null.
     */
    @Nullable
    public CourseComponent getComponentByIdFromAppLevelCache(@NonNull final String courseId,
                                                             @NonNull final String componentId) {
        CourseComponent courseComponent = getCourseDataFromAppLevelCache(courseId);
        if (courseComponent == null)
            return getBlockComponent(componentId, courseId);
        CourseComponent component = courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent courseComponent) {
                return componentId.equals(courseComponent.getId());
            }
        });
        if (component == null)
            return getBlockComponent(componentId, courseId);

        return component;
    }

    /**
     * This function should be avoided to use because it tries to obtain data from persistable cache
     * which takes time and should happen asynchronously.
     * <p>
     * {@link CourseManager#getComponentByIdFromAppLevelCache} can be used as an alternate,
     * specially if its sure course data will be available in app level cache.
     */
    @Deprecated
    @Nullable
    public CourseComponent getComponentById(@NonNull final String courseId,
                                            @NonNull final String componentId) {
        CourseComponent courseComponent = getCachedCourseData(courseId);
        if (courseComponent == null)
            return getBlockComponent(componentId, courseId);
        CourseComponent component = courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent courseComponent) {
                return componentId.equals(courseComponent.getId());
            }
        });
        if (component == null)
            return getBlockComponent(componentId, courseId);

        return component;
    }

  /*  public CourseComponent getComponentById(String courseId, final String componentId){
        CourseComponent courseComponent = getCourseByCourseId(courseId);
        if ( courseComponent == null )
            return null;
        return courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent courseComponent) {
                return componentId.equals(courseComponent.getId());
            }
        });
    }*/

    public CourseComponent getCourseByCourseId(String courseId){


        //if the id is null return the call:Arjun
        if(courseId==null)
            return null;

        if(courseId.isEmpty())
            return null;

        CourseComponent component = cachedComponent.get(courseId);
        if ( component != null )
            return component;
        try {
            component = serviceManager.getCourseStructureFromCache(courseId);

            //Don't put component to cash if it is null it create null crash to Lurche cache manager::Arjun
            if (component != null)
                cachedComponent.put(courseId, component);
        } catch (Exception e) {
            logger.error(e);
        }
        return component;
    }

    public CourseComponent getComponentByCourseId(String courseId){

        if(courseId==null || courseId.isEmpty() || courseId.equals(""))
            return null;

        CourseComponent courseComponent = getCachedCourseData(courseId);
        if ( courseComponent == null )
            return null;
        return courseComponent.findMxFirstComponent();
    }

    public void addBlockComponentInAppLevelCache(CourseComponent block, String courseId){
        List<CourseComponent> components = cachedBlockComponents.get(courseId);
        if (components == null){
            components = new ArrayList<>();
        }
        if (!components.contains(block)){
            components.add(block);
        }
        cachedBlockComponents.put(courseId, components);
    }

    public CourseComponent getBlockComponent(String blockId, String courseId){
        List<CourseComponent> components = cachedBlockComponents.get(courseId);
        return getComponentFromComponents(components, blockId);
    }

    private CourseComponent getComponentFromComponents(List<CourseComponent> components, String blockId){

        if (components == null){
            return null;
        }

        for (CourseComponent component: components){
            if (component.getId().equals(blockId)){
                return component;
            } else if (component.isContainer()){
                CourseComponent comp = getComponentFromComponents(component.getChildContainers(), blockId);
                if (comp != null){
                    return comp;
                }
                comp = getComponentFromComponents(component.getChildLeafs(), blockId);
                if (comp != null){
                    return comp;
                }
            }
        }
        return null;

    }
}
