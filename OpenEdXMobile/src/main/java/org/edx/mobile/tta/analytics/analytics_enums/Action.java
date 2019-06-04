package org.edx.mobile.tta.analytics.analytics_enums;

/**
 * Created by Arjun Chauhan on 2/20/2017.
 */

public enum Action {
    Registration,
    AppOpen,
    AppClose, //new added
    SignIn, //new added
    SignOut, //new added
    RegisterComplete,

    OpenDashboard,
    CourseOpen,
    DBOpen,
    DBPost,
    DBResponse,
    DBView,
    ViewUnit,
    StartScormDownload,
    ScromDownloadCompleted,
    ScormDownloadIncomplete,
    ViewScorm,
    ViewQuestion,
    SubmitAnswer,
    OfflineSections,
    PostFeedback,

    //Certificate
    CertificateEligible,
    CertificateGenerate,
    CertificateView,
    CertificateDownload,

    //Wordpress
    ViewCategory,
    ViewPost,
    LikePost,
    UnlikePost,
    SharePostFB,
    SharePostWP,
    SharePostCP,
    SharePostOther,
    SharePostApp,
    CommentPost,
    BookmarkPost,
    UnbookmarkPost,
    PlayVideoPost,
    SubmitForm,
    Upload,
    ChangeFilter,
    ChangeSort,
    DownloadConnect,

    //please don't change this action.It signify that the entry done for Tin can object.
    TinCanObject,

    //TTA Redesign

    Nav,
    CourseView,
    CourseLike,
    CourseUnlike,
    ShareCourse,
    DBLike,
    DBUnlike,
    DBComment,
    DBCommentReply,
    DBCommentlike,
    DBCommentUnlike,
    BookmarkCourse,
    UnbookmarkCourse,
    ReplyComment,
    ViewSection,
    DownloadPostComplete,
    DeletePost,
    DeleteSection,
    GenerateCertificate,
    ViewCert,
    FollowUser,
    Search,
    ViewProfile,
    ViewPoints,
    ViewBadges,

    MostPopular,
    Certificate,
    Badge,
    Share,
    NewPost,
    Like,
    UnfollowUser, Comment

}
