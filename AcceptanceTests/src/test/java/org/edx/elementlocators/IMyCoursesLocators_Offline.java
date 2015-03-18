/**
 * 
 */
package org.edx.elementlocators;

/**
 * @author divakarpatil
 * 
 */
public interface IMyCoursesLocators_Offline {
	
	public String getVideosNotAvailableMsg();

	public String getBtnHeaderNameId();

	public String getMyCoursesHeaderId();

	public String getOfflineLabelName();

	public String getOfflineErrorMessageName();

	public String getOpenInBrowserId();

	public void gotoMyCoursesView() throws Throwable;

	public String getMyCoursesName();

	public String getVideoPlayerId();

	public String getCCPopUpCancelId();

	public String getSettingsPopUpId();

	public String getCCPopUpId();

	public String getOkPopupId();

	public String getBtnDeleteId();

	public String getCbVideoSelectId();

	public String getBtnEditId();

	public String getSeekBarId();

	public String getViewOnWebId();

	public String getDisabledSectionErrorMessage();

	public String getVideoListId();

	public String getSectionSubsectionListId();

	public String getCourseListId();

	public String getMyCourseId();

	public String getHeaderId();

	public String getLastAccessedBtnId();

	public boolean isAndroid();

	public String getPlayPauseId();

	public String getLMSId();

	public String getRewindId();

	public String getFullScreenId();

	public String getVideoPlayerSettings();

	public String getOfflineBarId();

	public String getBtnDeletePopupId();

	// Android id
	String btnHeaderId = "android:id/up";
	String btnHeaderNameId = "android:id/action_bar_title";
	String btnCourseId = "org.edx.mobile:id/course_row_layout";
	String btnCourseWareName = "Courseware";
	String btnSectionSubsectionId = "org.edx.mobile:id/chapter_row_layout";
	String hlnkOpenInBrowserId = "org.edx.mobile:id/open_in_browser_btn";//
	String btnVideoId = "org.edx.mobile:id/video_row_layout";
	String txtMyCourseName = "My Courses";
	String vpVideoPlayerId = "org.edx.mobile:id/preview";
	
	String txtMyCourseId = "org.edx.mobile:id/drawer_option_my_courses";
	String btnLastAccessedId = "org.edx.mobile:id/last_viewed_tv";
	String btnLMS = "org.edx.mobile:id/lms_btn";
	String btnPlayPause = "org.edx.mobile:id/pause";
	String btnRewind = "org.edx.mobile:id/rew";
	String btnSettings = "org.edx.mobile:id/settings";
	String btnFullScreenId = "org.edx.mobile:id/fullscreen";
	String btnLogOutId = "org.edx.mobile:id/logout_button";
	String popupCC = "org.edx.mobile:id/tv_closedcaption";
	String popupLanguages = "org.edx.mobile:id/cc_list";
	String popupLanguagesCancel = "org.edx.mobile:id/tv_cc_cancel";
	String txtSectionName="org.edx.mobile:id/txt_chapter_title";

	// offline
	String btnOkPopupId = "org.edx.mobile:id/positiveButton";
	String btnDeleteId = "org.edx.mobile:id/delete_btn";
	String cbVideoSelectId = "org.edx.mobile:id/video_select_checkbox";
	String btnEditId = "org.edx.mobile:id/edit_btn";
	String offlineBarId = "org.edx.mobile:id/offline_bar";

	// IOS Locators id's
	String btnHeaderIdiOS = "btnNavigation";
	String headerMyCoursesIdiOS = "myCoursesHeader";
	String btnHeaderNameIdiOS = "txtHeader";

	String btnCourseIdiOS = "lbCourseTitle";
	String lbVideoName = "lbVideoName";
	String lbVideoSize = "lbVideoSize";
	String lbVideoLength = "lbVideoLength";
	String btnLastAccessedIdiOS = "btnLastAccessed";
	String btnOkPopupIdiOS = "OK";
	String btnViewOnWebIdiOS = "btnViewOnWeb";
	String btnSectionSubsectionDownloadIdiOS = "btnVideosDownload";
	String btnSectionSubsectionIdiOS = "lbSectionSubsection";
	String hlnkOpenInBrowserIdiOS = "VIEW ON WEB";

	String btnVideoIdiOS = "lbVideoName";
	String lbNoOfVideos = "lbVideoNumbers";

	String txtMyCourseIdiOS = "txtMyCoursesLNP";
	String offlineBarIdiOS = "offline";

	String vpVideoPlayerIdiOS = "Video";
	String btnLMSiOS = "btnLMS";
	String btnPlayPauseiOS = "btnPlayPause";
	String btnRewindiOS = "btnRewind";
	String btnSettingsiOS = "btnSettings";
	String btnFullScreenIdiOS = "btnFullScreen";
	String popupCCiOS = "";
	String popupLanguagesiOS = "";
	String popupLanguagesCanceliOS = "";

	String cbVideoSelectIdiOS = "btnCheckBoxDelete";
	String btnEditIdiOS = "btnEdit";
	String btnDeleteIdiOS = "btnDelete";
	String btnDeletePopupIdiOS = "Delete";

	// Common offline mode
	String txtOfflineName = "OFFLINE MODE";
	String txtOfflineMessageName = "In offline mode, you can only view videos that are saved on your device.";
	String txtDisabledSectionMessage = "This section is not available in offline mode";
	String txtDisabledVideoMessage="This video is not available offline.";
	String txtDisabledVideoMessage1="Please select a video that you've downloaded onto your device.";
}
