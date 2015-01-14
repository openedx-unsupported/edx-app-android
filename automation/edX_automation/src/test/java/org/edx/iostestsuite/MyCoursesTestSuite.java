package org.edx.iostestsuite;

import org.edx.basetest.MyCoursesTestSuiteBase;

public class MyCoursesTestSuite extends MyCoursesTestSuiteBase{

	@Override
	public String getWebLinkId() {
		return webLinkIdiOS;
	}

	@Override
	public String getOpenInBrowserId() {
		return hlnkOpenInBrowserIdiOS;
	}

	@Override
	public String getHandoutsName() {
		return btnHandOutsNameiOS;
	}

	@Override
	public String getAnnouncementsName() {
		return btnAnnouncementsNameiOS;
	}

	@Override
	public void gotoMyCoursesView() {
		
	}

	@Override
	public String getSignInLocatorId() {
		return btnSigninIdiOS;
	}

	@Override
	public String getPasswordLocatorId() {
		return tbPasswordIdiOS;
	}

	@Override
	public String getEmailLocatorId() {
		return tbEmailIdiOS;
	}

	@Override
	public String getMyCoursesName() {
		return txtMyCourseNameiOS;
	}

	@Override
	public String getVideoPlayerId() {
		return vpVideoPlayerIdiOS;
	}

	@Override
	public String getVideoListId() {
		return btnVideoIdiOS;
	}

	@Override
	public String getSectionSubsectionListId() {
		return btnSectionSubsectionIdiOS;
	}

	@Override
	public String getCourseListId() {
		return btnCourseIdiOS;
	}

	@Override
	public String getMyCourseId() {
		return txtMyCourseIdiOS;
	}

	@Override
	public String getHeaderId() {
		return btnHeaderIdiOS;
	}

	@Override
	public String getDownloadScreenCancelBtnId() {
		return btnDownloadScreenCancelIdiOS;
	}

	@Override
	public String getDownloadScreenId() {
		return btnDownloadScreenIdiOS;
	}

	@Override
	public String getSectionSubsectionDownloadId() {
		return btnSectionSubsectionDownloadIdiOS;
	}

	@Override
	public String getLastAccessedBtnId() {
		return btnLastAccessedIdiOS;
	}

	@Override
	public boolean isAndroid() {
		return false;
	}

	@Override
	public String getPlayPauseId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLMSId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRewindId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullScreenId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVideoPlayerSettings() {
		// TODO Auto-generated method stub
		return null;
	}
  
}
