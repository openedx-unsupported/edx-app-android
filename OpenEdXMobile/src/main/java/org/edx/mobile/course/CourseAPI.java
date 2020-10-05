package org.edx.mobile.course;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.exception.CourseContentNotValidException;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.ErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.IPathNode;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SummaryModel;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.BlockModel;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseDates;
import org.edx.mobile.model.course.CourseBannerInfoModel;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.DiscussionBlockModel;
import org.edx.mobile.model.course.DiscussionData;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.model.course.ResetCourseDates;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.course.VideoData;
import org.edx.mobile.model.course.VideoInfo;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

import static org.edx.mobile.http.constants.TimeInterval.HOUR;
import static org.edx.mobile.http.util.CallUtil.executeStrict;

@Singleton
public class CourseAPI {

    @Inject
    protected Config config;

    @NonNull
    private final CourseService courseService;
    @NonNull
    private final UserPrefs userPrefs;


    @Inject
    public CourseAPI(@NonNull CourseService courseService, @NonNull UserPrefs userPrefs) {
        this.courseService = courseService;
        this.userPrefs = userPrefs;
    }

    @NonNull
    public Call<Page<CourseDetail>> getCourseList(final int page) {
        return courseService.getCourseList(getUsername(), true, config.getOrganizationCode(), page);
    }

    @NonNull
    public Call<CourseDetail> getCourseDetail(@NonNull final String courseId) {
        // Empty courseId will return a 200 for a list of course details, instead of a single course
        if (TextUtils.isEmpty(courseId)) throw new IllegalArgumentException();
        return courseService.getCourseDetail(courseId, getUsername());
    }

    /**
     * @return Course upgrade status for the given user.
     */
    @NonNull
    public Call<CourseUpgradeResponse> getCourseUpgradeStatus(@NonNull final String courseId) {
        return courseService.getCourseUpgradeStatus(courseId);
    }

    /**
     * @return Enrolled courses of given user.
     */
    @NonNull
    public Call<List<EnrolledCoursesResponse>> getEnrolledCourses() {
        return courseService.getEnrolledCourses(getUsername(), config.getOrganizationCode());
    }

    /**
     * @return Course dates against the given course Id.
     */
    @NonNull
    public Call<CourseDates> getCourseDates(@NonNull String courseId) {
        return courseService.getCourseDates(courseId);
    }

    /**
     * @return Course dates banner info against the given course Id.
     */
    @NonNull
    public Call<CourseBannerInfoModel> getCourseBannerInfo(@NonNull String courseId) {
        return courseService.getCourseBannerInfo(courseId);
    }

    /**
     * @return Reset course dates against the given course Id.
     */
    @NonNull
    public Call<ResetCourseDates> resetCourseDates(@NonNull HashMap<String, String> courseBody) {
        return courseService.resetCourseDates(courseBody);
    }

    /**
     * @return Server response on video completion.
     */
    @NonNull
    public Call<JSONObject> markBlocksCompletion(@NonNull String courseId, @NonNull String[] blockIds) {
        return courseService.markBlocksCompletion(new CourseService.BlocksCompletionBody(getUsername(), courseId, blockIds));
    }

    /**
     * @return Enrolled courses of given user, only from the cache.
     */
    @NonNull
    public Call<List<EnrolledCoursesResponse>> getEnrolledCoursesFromCache() {
        return courseService.getEnrolledCoursesFromCache(
                getUsername(), config.getOrganizationCode());
    }

    /**
     * @param courseId The course ID.
     * @return The course identified by the provided ID if available from the cache, null if no
     *         course is found.
     */
    @Nullable
    public EnrolledCoursesResponse getCourseById(@NonNull final String courseId) throws Exception {
        for (EnrolledCoursesResponse r : executeStrict(getEnrolledCoursesFromCache())) {
            if (r.getCourse().getId().equals(courseId)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Checks if course is enrolled by the user or not.
     *
     * @param courseId Id of the course.
     * @return true if Course is enrolled by the user, false other wise
     */
    public Boolean isCourseEnrolled(@NonNull final String courseId) {
        try {
            return getCourseById(courseId) != null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static abstract class GetCourseByIdCallback extends
            ErrorHandlingCallback<List<EnrolledCoursesResponse>> {
        @NonNull
        private final String courseId;

        public GetCourseByIdCallback(@NonNull final Context context,
                                     @NonNull final String courseId) {
            super(context);
            this.courseId = courseId;
        }

        public GetCourseByIdCallback(@NonNull final Context context,
                                     @NonNull final String courseId,
                                     @Nullable final TaskProgressCallback progressCallback) {
            super(context, progressCallback);
            this.courseId = courseId;
        }

        @Override
        protected final void onResponse(
                @NonNull final List<EnrolledCoursesResponse> courseResponses) {
            for (EnrolledCoursesResponse coursesResponse : courseResponses) {
                if (coursesResponse.getCourse().getId().equals(courseId)) {
                    onResponse(coursesResponse);
                    return;
                }
            }
            onFailure(new Exception("Course not found in user's enrolled courses."));
        }

        protected abstract void onResponse(@NonNull final EnrolledCoursesResponse coursesResponse);
    }

    @NonNull
    public Call<SyncLastAccessedSubsectionResponse> syncLastAccessedSubsection(
            @NonNull final String courseId,
            @NonNull final String lastVisitedModuleId) {
        return courseService.syncLastAccessedSubsection(getUsername(), courseId,
                new CourseService.SyncLastAccessedSubsectionBody(lastVisitedModuleId));
    }

    @NonNull
    public Call<SyncLastAccessedSubsectionResponse> getLastAccessedSubsection(
            @NonNull final String courseId) {
        return courseService.getLastAccessedSubsection(getUsername(), courseId);
    }

    @NonNull
    public Call<CourseStructureV1Model> getCourseStructureWithoutStale(@NonNull String blocksApiVersion, @NonNull String courseId) {
        return courseService.getCourseStructure(null, blocksApiVersion, getUsername(), courseId);
    }

    @NonNull
    public Call<CourseStructureV1Model> getCourseStructure(@NonNull String blocksApiVersion, @NonNull String courseId) {
        return courseService.getCourseStructure("max-stale=" + HOUR, blocksApiVersion, getUsername(), courseId);
    }

    @NonNull
    public CourseComponent getCourseStructureFromCache(@NonNull String blocksApiVersion, @NonNull String courseId)
            throws Exception {
        CourseStructureV1Model model = executeStrict(
                courseService.getCourseStructure("only-if-cached, max-stale", blocksApiVersion, getUsername(), courseId));
        return (CourseComponent) normalizeCourseStructure(model, courseId);
    }

    public static abstract class GetCourseStructureCallback
            extends ErrorHandlingCallback<CourseStructureV1Model> {
        @NonNull
        private final String courseId;

        public GetCourseStructureCallback(@NonNull final Context context,
                                          @NonNull final String courseId,
                                          @Nullable final TaskProgressCallback progressCallback) {
            super(context, progressCallback);
            this.courseId = courseId;
        }

        public GetCourseStructureCallback(@NonNull final Context context,
                                          @NonNull final String courseId,
                                          @Nullable final TaskProgressCallback progressCallback,
                                          @Nullable final ErrorNotification errorNotification,
                                          @Nullable final SnackbarErrorNotification snackbarErrorNotification,
                                          @Nullable final RefreshListener refreshListener) {
            super(context, progressCallback, errorNotification, snackbarErrorNotification, refreshListener);
            this.courseId = courseId;
        }

        @Override
        protected final void onResponse(@NonNull final CourseStructureV1Model model) {
            try {
                onResponse((CourseComponent) normalizeCourseStructure(model, courseId));
            } catch (CourseContentNotValidException e) {
                onFailure(e);
            }
        }

        protected abstract void onResponse(@NonNull final CourseComponent courseComponent);
    }

    /**
     * we handle both name and id for backward compatibility. legacy code use name, it is not a good idea as name is not
     * grantee to be unique.
     */
    @Nullable
    public LectureModel getLecture(@NonNull final CourseComponent courseComponent,
                                   @NonNull final String chapterName,
                                   @NonNull final String chapterId,
                                   @NonNull final String lectureName,
                                   @NonNull final String lectureId)
            throws Exception {
        //TODO - we may use a generic filter to fetch the data?
        for(IBlock chapter : courseComponent.getChildren()){
            if ( chapter.getId().equals(chapterId) ){
                for(IBlock lecture : chapter.getChildren() ){
                    //TODO - check to see if need to compare id or not
                    if ( lecture.getId().equals(lectureId) ){
                        LectureModel lm = new LectureModel();
                        lm.name = lecture.getDisplayName();
                        lm.videos = (ArrayList) mappingAllVideoResponseModelFrom((CourseComponent)lecture, null );
                        return lm;
                    }
                }
            }
        }
        //if we can not find object by id, try to get by name.
        for(IBlock chapter : courseComponent.getChildren()){
            if ( chapter.getDisplayName().equals(chapterName) ){
                for(IBlock lecture : chapter.getChildren() ){
                    //TODO - check to see if need to compare id or not
                    if ( lecture.getDisplayName().equals(lectureName) ){
                        LectureModel lm = new LectureModel();
                        lm.name = lecture.getDisplayName();
                        lm.videos = (ArrayList) mappingAllVideoResponseModelFrom((CourseComponent)lecture, null );
                        return lm;
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    public VideoResponseModel getVideoById(@NonNull final CourseComponent courseComponent,
                                           @NonNull final String videoId)
            throws Exception {
        for(HasDownloadEntry item : courseComponent.getVideos()) {
            VideoBlockModel model = (VideoBlockModel)item;
            if (model.getId().equals(videoId))
                return mappingVideoResponseModelFrom((VideoBlockModel) item);
        }
        return null;
    }

    /**
     *
     * @param courseComponent
     * @param subsectionId
     * @return
     */
    @Nullable
    public VideoResponseModel getSubsectionById(@NonNull final CourseComponent courseComponent,
                                                @NonNull final String subsectionId) {
        ////TODO - we may use a generic filter to fetch the data?
        Map<String, SectionEntry> map = mappingCourseHierarchyFrom(courseComponent);
        for (Map.Entry<String, SectionEntry> chapterentry : map.entrySet()) {
            // iterate lectures
            for (Map.Entry<String, ArrayList<VideoResponseModel>> entry :
                    chapterentry.getValue().sections.entrySet()) {
                // iterate videos
                for (VideoResponseModel v : entry.getValue()) {
                    // identify the subsection (module) if id matches
                    IPathNode node = v.getSection();
                    if (node != null  && subsectionId.equals(node.getId())) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private String getUsername() {
        final ProfileModel profile = userPrefs.getProfile();
        return null == profile ? null : profile.username;
    }

    /**
     * Mapping from raw data structure from getCourseStructure() API
     * @param courseStructureV1Model
     * @return
     */
    @NonNull
    public static IBlock normalizeCourseStructure(
            @NonNull final CourseStructureV1Model courseStructureV1Model,
            @NonNull final String courseId) throws CourseContentNotValidException {
        BlockModel topBlock = courseStructureV1Model.getBlockById(courseStructureV1Model.root);
        if (topBlock == null) {
            throw new CourseContentNotValidException("Server didn't send a proper response for this course: " + courseStructureV1Model.root);
        }
        CourseComponent course = new CourseComponent(topBlock, null);
        course.setCourseId(courseId);
        for (BlockModel m : courseStructureV1Model.getDescendants(topBlock)) {
            normalizeCourseStructure(courseStructureV1Model, m, course);
        }
        return course;
    }

    private static void normalizeCourseStructure(
            @NonNull final CourseStructureV1Model courseStructureV1Model,
            @NonNull final BlockModel block,
            @NonNull final CourseComponent parent) {

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
            } else { //everything else.. we fallback to html component
                new HtmlBlockModel(block, parent);
            }
        }
    }

    /**
     * we map the new course outline data to old data model.
     * TODO : Ideally we should update all the code to match the new data model.
     * @param courseComponent
     * @return
     */
    @NonNull
    private static List<SectionItemInterface> mappingAllVideoResponseModelFrom(
            @NonNull final CourseComponent courseComponent,
            @NonNull final Filter<VideoResponseModel> filter) {
        List<SectionItemInterface> items = new ArrayList<>();
        for(HasDownloadEntry item : courseComponent.getVideos()){
            VideoResponseModel model = mappingVideoResponseModelFrom((VideoBlockModel)item);
            if ( filter == null )
                items.add( model );
            else {
                if (filter.apply(model)){
                    items.add( model );
                }
            }
        }
        return items;
    }

    /**
     * from new VideoBlockModel to legacy VideoRsponseModel
     * @param videoBlockModel
     * @return
     */
    @NonNull
    private static VideoResponseModel mappingVideoResponseModelFrom(
            @NonNull final VideoBlockModel videoBlockModel) {
        VideoResponseModel model = new VideoResponseModel();
        model.setCourseId(videoBlockModel.getCourseId());
        SummaryModel summaryModel = mappingSummaryModelFrom(videoBlockModel);
        model.setSummary(summaryModel);
        model.videoBlockModel = videoBlockModel;
        model.setSectionUrl(videoBlockModel.getParent().getBlockUrl());
        model.setUnitUrl(videoBlockModel.getBlockUrl());

        return model;
    }

    @NonNull
    private static SummaryModel mappingSummaryModelFrom(
            @NonNull final VideoBlockModel videoBlockModel) {
        SummaryModel model = new SummaryModel();
        model.setType(videoBlockModel.getType());
        model.setDisplayName(videoBlockModel.getDisplayName());
        model.setDuration((int)videoBlockModel.getData().duration);
        model.setOnlyOnWeb(videoBlockModel.getData().onlyOnWeb);
        model.setId(videoBlockModel.getId());
        final VideoInfo videoInfo = videoBlockModel.getData().encodedVideos.getPreferredVideoInfo();
        if (null != videoInfo) {
            model.setVideoUrl(videoInfo.url);
            model.setSize(videoInfo.fileSize);
        }
        model.setTranscripts(videoBlockModel.getData().transcripts);
        //FIXME = is this field missing?
        // private EncodingsModel encodings;
        return model;
    }

    /**
     * from new CourseComponent to legacy data structure.
     * @param courseComponent
     * @return
     */
    @NonNull
    private static Map<String, SectionEntry> mappingCourseHierarchyFrom(
            @NonNull final CourseComponent courseComponent) {
        Map<String, SectionEntry> map = new HashMap<>();
        for(IBlock block : courseComponent.getChildren()){
            CourseComponent chapter = (CourseComponent)block;
            SectionEntry entry = new SectionEntry();
            entry.chapter = chapter.getDisplayName();
            entry.isChapter = true;
            entry.section_url = chapter.getBlockUrl();
            map.put(entry.chapter, entry);

            for( IBlock subBlock : chapter.getChildren() ){
                CourseComponent section = (CourseComponent)subBlock;

                entry.sections.put(section.getDisplayName(),
                        (ArrayList) mappingAllVideoResponseModelFrom(section, null));
            }
        }
        return map;
    }
}
