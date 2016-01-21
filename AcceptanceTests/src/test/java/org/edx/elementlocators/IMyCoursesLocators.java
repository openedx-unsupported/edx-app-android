/**
 * 
 */
package org.edx.elementlocators;

/**
 * @author divakarpatil
 * 
 */
public interface IMyCoursesLocators {
	
	public String getFindCourseWebView();
	
	public String getFindCourseHeaderName();

	public String getMySettingsId();
	
	public String getShowingOnlyVideosName();

	public String getOkPopupId();
	
	public String getAnnouncementsName();

	public String getSettingsBtnId();

	public String getWebLinkId();

	public String getMyCoursesHeaderId();

	public String getTxtMyCourseName();

	public String getHeaderNameId();

	public String getLstDownloadId();

	public String getTxtMyVideosName();

	public String getBtnViewId();

	public String getLogoutId();

	public String getViewOnWebId();

	public String getHandoutsName();

	public String getCourseInfoName();

	public String getDownloadMessage();

	public void gotoMyCoursesView() throws Throwable;

	public String getSignInLocatorId();

	public String getPasswordLocatorId();

	public String getEmailLocatorId();

	public String getMyCoursesName();

	public String getVideoPlayerId();

	public String getCCPopUpCancelId();

	public String getSettingsPopUpId();

	public String getCCPopUpId();

	public String getVideoListId();

	public String getSectionSubsectionListId();

	public String getCourseListId();

	public String getMyCourseId();

	public String getHeaderId();

	public String getDownloadScreenCancelBtnId();

	public String getDownloadScreenId();

	public String getSectionSubsectionDownloadId();

	public String getLastAccessedBtnId();

	public boolean isAndroid();

	public String getPlayPauseId();

	public String getLMSId();

	public String getRewindId();

	public String getFullScreenId();

	public String getVideoPlayerSettings();

	public String getFindACourseBtnId();

	public String getLnkFindCourseName();

	public String getTxtLookingForChallenge();

	public String getCourseWareErrorText();

	public String getCourseWareErrorId();

	public String getCloseId();

	public String getDontSeeOneOfCoursesId();

	public String getSeekBarId();

	public String getVideoHeaderId();

	public String getVideoName();

	public String getVideoSize();

	public String getVideoLength();

	public String getTxtFindACourseName();

	public String getMyVideosId();

	/*
	 * Android Id's
	 */

	// Login Id's
	String tbEmailId = "org.edx.mobile:id/email_et";
	String tbPasswordId = "org.edx.mobile:id/password_et";
	String btnSigninId = "org.edx.mobile:id/login_button_layout";
	String btnLogOutId = "org.edx.mobile:id/logout_button";

	// Header Id's
	String btnHeaderId = "android:id/up";
	String btnHeaderNameId = "android:id/action_bar_title";

	// Find A Course id's
	String btnFindACourseId = "org.edx.mobile:id/course_btn";
	String btnDontSeeCoursesId = "org.edx.mobile:id/course_not_listed_tv";
	String btnCloseId = "org.edx.mobile:id/positiveButton";
	String lnkFindACourseName = "https://www.edx.org/course-search?type=mobile";
	String txtLookingForChallengeName = "Looking for a new challenge?";

	// Navigation through the course to the video id's
	String btnCourseId = "org.edx.mobile:id/course_row_layout";
	String btnSectionSubsectionDownloadId = "org.edx.mobile:id/bulk_download_layout";
	String btnSectionSubsectionId = "org.edx.mobile:id/chapter_row_layout";
	String btnCourseWareName = "Courseware";
	String btnCourseInfoName = "Course Info";
	String btnAnnouncementsName="Announcements";
	String btnHandOutsName = "View course handouts";
	String hlnkViewOnWebId = "org.edx.mobile:id/open_in_browser_btn";
	String btnVideoId = "org.edx.mobile:id/video_row_layout";
	String btnVideoDownloadId = "org.edx.mobile:id/video_start_download";

	// Download Screen, Download Message Id's
	String btnDownloadScreenId = "org.edx.mobile:id/down_arrow";
	String btnDownloadScreenCancelId = "org.edx.mobile:id/close_btn";
	String dlgLargeDownloadsId = "org.edx.mobile:id/dialog_layout";
	String lbVideoName = "org.edx.mobile:id/video_title";
	String lbVideoSize = "org.edx.mobile:id/video_size";
	String lbVideoLength = "org.edx.mobile:id/video_playing_time";
	String btnViewId = "org.edx.mobile:id/button_view";
	String lstDownloadVideosId = "org.edx.mobile:id/downloads_row_layout";
	String msgDownloadId = "org.edx.mobile:id/flying_message";
	String downloadProgressWheel = "org.edx.mobile:id/progress_wheel";

	// Video player Id's
	String vpVideoPlayerId = "org.edx.mobile:id/preview";
	String lbVideoNameVideoPlayerId = "org.edx.mobile:id/video_title";
	String btnLMS = "org.edx.mobile:id/lms_btn";
	String btnPlayPause = "org.edx.mobile:id/pause";
	String btnRewind = "org.edx.mobile:id/rew";
	String btnSettings = "org.edx.mobile:id/settings";
	String btnFullScreenId = "org.edx.mobile:id/fullscreen";
	String popupCC = "org.edx.mobile:id/tv_closedcaption";
	String popupLanguages = "org.edx.mobile:id/row_cc_lang";
	String txtSubtitlesId = "org.edx.mobile:id/txtSubtitles_tv";
	String popupLanguagesCancel = "org.edx.mobile:id/tv_cc_cancel";

	// No CourseWare available id
	String lbCourseWareId = "org.edx.mobile:id/no_chapter_tv";
	String lbCourseWareName = "No courseware is currently available.";

	// Last Accessed button Id
	String btnLastAccessedId = "org.edx.mobile:id/last_viewed_tv";

	// Left Navigation Panel id's
	String txtMyCourseId = "org.edx.mobile:id/drawer_option_my_courses";
	String txtMyVideosName = "My Videos";
	String txtMySettingsId="org.edx.mobile:id/drawer_option_my_settings";
	String txtCellularDownloadName="ALLOW CELLULAR DOWNLOAD";
	String txtCellularDownload1Name="Allow your device to download videos over your cellular connection when" 
+"Wi-Fi is not available. Data charges may apply.";
	String btnOkPopupId = "org.edx.mobile:id/positiveButton";
	String btnSettingsId = "org.edx.mobile:id/wifi_setting";
	
	//Find Courses
	String txtFindCourseName="Find Courses";
	String findACoursewebView="org.edx.mobile:id/webview";
	

	/*
	 * IOS Locators id's
	 */

	// Login Locator id
	String tbEmailIdiOS = "tbUserName";
	String tbPasswordIdiOS = "tbPassword";
	String btnSigninIdiOS = "btnSignIn";
	String btnLogOutIdiOS = "btnLogout";

	// Header id
	String btnHeaderIdiOS = "btnNavigation";
	String btnHeaderNameIdiOS = "txtHeader";// Header id for all the screen
											// except the My Courses screen
	String headerMyCoursesIdiOS = "myCoursesHeader";

	// Find A Course id's
	String btnFindAMobileCourseiOS = "btnFindACourse";
	String txtDontSeeACourseiOS = "btnDontSeeCourse";
	String txtLookingForCourseiOS = "Looking for a new challenge?";
	String btnCloseIdiOS = "btnClose";

	// Navigation to the video through the course id's
	String btnCourseIdiOS = "lbCourseTitle";
	String btnSectionSubsectionIdiOS = "lbSectionSubsection";
	String btnCourseWareNameiOS = "COURSEWARE";
	String btnAnnouncementsNameiOS = "ANNOUNCEMENTS";
	String btnHandOutsNameiOS = "HANDOUTS";
	String btnViewOnWebIdiOS = "btnViewOnWeb";
	String hlnkViewOnWebIdiOS = "VIEW ON WEB";
	String btnVideoIdiOS = "lbVideoName";
	String lbNoOfVideos = "lbVideoNumbers";

	// Download Screen id's
	String btnSectionSubsectionDownloadIdiOS = "btnVideosDownload";
	String btnDownloadScreenIdiOS = "btnDownloadScreen";
	String lbVideoNameiOS = "lbVideoName";
	String lbVideoSizeiOS = "lbVideoSize";
	String lbVideoLengthiOS = "lbVideoLength";
	String btnDownloadScreenCancelIdiOS = "btnVideoDownloadCancel";
	String btnViewIdiOS = "btnDownloadView";
	String btnVideoDownloadIdiOS = "btnVideosDownload";
	String dlgLargeDownloadsIdiOS = "";
	String msgDownloadIdiOS = "floatingMessages";

	// Video player Id's
	String vpVideoPlayerIdiOS = "Video";
	String btnLMSiOS = "btnLMS";
	String btnPlayPauseiOS = "btnPlayPause";
	String btnRewindiOS = "btnRewind";
	String btnSettingsiOS = "btnSettings";
	String btnFullScreenIdiOS = "btnFullScreen";
	String popupCCiOS = "";
	String popupLanguagesiOS = "";
	String popupLanguagesCanceliOS = "";

	// Last Accessed button id
	String btnLastAccessedIdiOS = "btnLastAccessed";

	// Left Navigation Panel Id
	String txtMyVideosNameiOS = "My Videos";
	String txtMyVideosIdiOS = "myVideosHeader";
	String txtMyCourseIdiOS = "txtMyCoursesLNP";
	String btnSwitchiOS = "btnSwitch";
	String btnOkPopupIdiOS = "ALLOW";
	String txtMySettingsIdiOS="";

	/* Common Locators */
	String txtMyCourseName = "My Courses";
	String lbCourseWareIdiOS = "txtNoCourseWareAvailable";
	String txtFindACourseName = "FIND A MOBILE-FRIENDLY COURSE";
	String txtShowingOnlyVideos = "Showing only Videos";

}
