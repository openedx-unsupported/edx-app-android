/**
 * 
 */
package org.edx.elementlocators;

/**
 * @author divakarpatil
 * 
 */
public interface IMyVideosLocators {
	
	public String getTxtMySettingsId();
	
	public String getTxtMyVideosName();

	public String getHeaderNameId();

	public String getOkPopupId();

	public String getSettingsBtnId();

	public String getSectionSubsectionDownloadId();

	public String getCourseListId();

	public String getSubmitFeedBackId();

	public String getHeaderId();

	public String getTxtAllVideosName();

	public String getTxtRecentVideosName();

	public String getTxtMyVideosId();

	public String getLstVideoId();

	public String getLstCourseId();

	public String getLstDownloadId();

	public String getBtnDownloadScreenId();

	public String getBtnDeleteId();

	public String getCbVideoSelectId();

	public String getBtnEditId();

	public String getEmailLocatorId();

	public boolean isAndroid();

	public String getPasswordLocatorId();

	public String getSignInLocatorId();

	public String getEmailId();

	public String getUserNameId();

	public String getVersion();

	public String getLogoutId();

	public String getPlayPauseId();

	public String getLMSId();

	public String getRewindId();

	public String getFullScreenId();

	public String getVideoPlayerSettings();

	public String getVideoPlayerId();

	public void gotoMyVideosView();

	public String getSeekBarId();

	public String getVideoHeaderId();

	public String getSettingsPopUpId();

	public String getBtnDeletePopupId();

	/*
	 * Android id's
	 */

	// Login id
	String btnLogOutId = "org.edx.mobile:id/logout_button";
	String tbPasswordId = "org.edx.mobile:id/password_et";
	String btnSigninId = "org.edx.mobile:id/login_button_layout";
	String tbEmailId = "org.edx.mobile:id/email_et";

	// Header id
	String btnHeaderId = "android:id/up";
	String btnHeaderNameId = "android:id/action_bar_title";

	// LeftNavigationPanel
	String txtMyVideosName = "My Videos";
	String txtFindCoursesId="org.edx.mobile:id/drawer_option_find_courses";
	String txtMyVideosId = "org.edx.mobile:id/drawer_option_my_videos";
	String txtAllVideosName = "All Videos";
	String txtMySettingsId="org.edx.mobile:id/drawer_option_my_settings";
	String btnSubmitFeedBackId = "org.edx.mobile:id/drawer_option_submit_feedback";
	String btnOkPopupId = "org.edx.mobile:id/positiveButton";
	String btnCancelPopupId = "org.edx.mobile:id/negativeButton";
	String txtVersion = "org.edx.mobile:id/tv_version_no";
	String txtUserNameId = "org.edx.mobile:id/name_tv";
	String txtEmailId = "org.edx.mobile:id/email_tv";
	
	//Video player
	String btnLMS = "org.edx.mobile:id/lms_btn";
	String btnPlayPause = "org.edx.mobile:id/pause";
	String btnRewind = "org.edx.mobile:id/rew";
	String btnSettings = "org.edx.mobile:id/settings";
	String btnFullScreenId = "org.edx.mobile:id/fullscreen";
	String vpVideoPlayerId = "org.edx.mobile:id/preview";

	String btnViewId = "org.edx.mobile:id/button_view";

	String btnCourseId = "org.edx.mobile:id/course_row_layout";

	String txtRecentVideosName = "Recent Videos";
	String lstVideoId = "org.edx.mobile:id/video_row_layout";
	String btnEditId = "org.edx.mobile:id/edit_btn";
	String btnDeleteId = "org.edx.mobile:id/delete_btn";
	String btnCancelId = "org.edx.mobile:id/cancel_btn";
	String cbAllSelectId = "org.edx.mobile:id/select_checkbox";
	String cbVideoSelectId = "org.edx.mobile:id/video_select_checkbox";
	String btnDownloadScreenId = "org.edx.mobile:id/down_arrow";
	String lstDownloadVideosId = "org.edx.mobile:id/downloads_row_layout";
	String btnSettingsId = "org.edx.mobile:id/wifi_setting";
	String lstAllVideos_Courses = "org.edx.mobile:id/my_video_course_list";
	String lstRecentVideos = "org.edx.mobile:id/list_video";
	String intVideoCount = "org.edx.mobile:id/no_of_videos";

	String btnSectionSubsectionDownloadId = "org.edx.mobile:id/bulk_download";

	// iOS Id's
	String btnHeaderIdiOS = "btnNavigation";
	String btnHeaderNameIdiOS = "myVideosHeader";
	String btnCourseIdiOS = "lbCourseTitle";

	String tbEmailIdiOS = "tbUserName";
	String tbPasswordIdiOS = "tbPassword";
	String btnSigninIdiOS = "btnSignIn";

	// Left Navigation Bar
	String txtUserNameIdiOS = "lbUser";
	String txtEmailIdiOS = "lbEmail";
	String txtMyVideosNameiOS = "My Videos";
	String txtMySettingsIdiOS="";
	String txtAllVideosNameiOS = "ALL VIDEOS";
	String btnSubmitFeedBackIdiOS = "txtSubmitFeedBackLNP";
	String btnSettingsIdiOS = "lbSettingsLNP";
	String txtDownloadsiOS = "Download only on Wi-Fi";

	String btnLogOutIdiOS = "btnLogout";
	String txtVersioniOS = "lbVersion";
	String btnSwitchiOS = "btnSwitch";
	String btnOkPopupIdiOS = "ALLOW";
	String btnCancelPopupIdiOS = "Cancel";
	String txtMyVideosIdiOS = "txtMyVideosLNP";

	String txtRecentVideosNameiOS = "RECENT VIDEOS";
	String lstVideoIdiOS = "lbVideoName";
	String btnEditIdiOS = "btnEdit";
	String btnDeleteIdiOS = "btnDelete";
	String btnCancelIdiOS = "btnCancel";
	String cbAllSelectIdiOS = "btnSelectAllCheckBox";
	String cbVideoSelectIdiOS = "btnCheckBoxDelete";
	String btnDeletePopupIdiOS = "Delete";

	String vpVideoPlayerIdiOS = "Video";
	String btnLMSiOS = "btnLMS";
	String btnPlayPauseiOS = "btnPlayPause";
	String btnRewindiOS = "btnRewind";
	String btnSettingsiOS = "btnSettings";
	String btnFullScreenIdiOS = "btnFullScreen";
	String lbVideoHeaderIdiOS = "lbVideoTitle";

}
