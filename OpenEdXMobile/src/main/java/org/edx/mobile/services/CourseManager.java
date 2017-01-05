package org.edx.mobile.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.course.CourseComponent;

/**
 *  A central place for course data model transformation
 */

@Singleton
public class CourseManager {
    protected final Logger logger = new Logger(getClass().getName());

    private final LruCache<String, CourseComponent> cachedComponent;


    @Inject
    CourseAPI courseApi;

    public CourseManager() {
        cachedComponent = new LruCache<>(1);
    }

    @Nullable
    public CourseComponent getCourseByCourseId(@NonNull final String courseId) {
        CourseComponent component = cachedComponent.get(courseId);
        if ( component != null )
            return component;
        try {
            component = courseApi.getCourseStructureFromCache(courseId);
            cachedComponent.put(courseId,component);
        } catch (Exception e) {
           logger.error(e);
        }
        return component;
    }

    @Nullable
    public CourseComponent getComponentById(@NonNull final String courseId,
                                            @NonNull final String componentId) {
        CourseComponent courseComponent = getCourseByCourseId(courseId);
        if ( courseComponent == null )
            return null;
        return courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent courseComponent) {
                return componentId.equals(courseComponent.getId());
            }
        });
    }

}
