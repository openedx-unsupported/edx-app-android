package org.edx.androidtestsuite;

import org.edx.basetest.MyCoursesTestSuiteBase;
import org.testng.annotations.Test;

@Test
public class MyCoursesTestSuite extends MyCoursesTestSuiteBase  {
	
	@Override
	public void gotoMyCoursesView() {
		try {
			while (driver.verifyElementText(btnHeaderId, txtMyCourseName)) {
				driver.clickElementById(btnHeaderId);
			}
		} catch (Throwable t) {
		}
	}
	
	@Override
	public String getWebLinkId() {
		return webLinkId;
	}

	@Override
	public String getOpenInBrowserId() {
		return hlnkOpenInBrowserId;
	}

	@Override
	public String getHandoutsName() {
		return btnHandOutsName;
	}

	@Override
	public String getAnnouncementsName() {
		return btnAnnouncementsName;
	}

	@Override
	public String getSignInLocatorId() {
		return btnSigninId;
	}

	@Override
	public String getPasswordLocatorId() {
		return tbPasswordId;
	}

	@Override
	public String getEmailLocatorId() {
		return tbEmailId;
	}

	@Override
	public String getMyCoursesName() {
		return txtMyCourseName;
	}

	@Override
	public String getVideoPlayerId() {
		return vpVideoPlayerId;
	}

	@Override
	public String getVideoListId() {
		return btnVideoId;
	}

	@Override
	public String getSectionSubsectionListId() {
		return btnSectionSubsectionId;
	}

	@Override
	public String getCourseListId() {
		return btnCourseId;
	}

	@Override
	public String getMyCourseId() {
		return txtMyCourseId;
	}

	@Override
	public String getHeaderId() {
		return btnHeaderId;
	}

	@Override
	public String getDownloadScreenCancelBtnId() {
		return btnDownloadScreenCancelId;
	}

	@Override
	public String getDownloadScreenId() {
		return btnDownloadScreenId;
	}

	@Override
	public String getSectionSubsectionDownloadId() {
		return btnSectionSubsectionDownloadId;
	}

	@Override
	public String getLastAccessedBtnId() {
		return btnLastAccessedId;
	}

	@Override
	public boolean isAndroid() {
		return true;
	}

	@Override
	public String getPlayPauseId() {
		return btnPlayPause;
	}

	@Override
	public String getLMSId() {
		return btnLMS;
	}

	@Override
	public String getRewindId() {
		return btnRewind;
	}

	@Override
	public String getFullScreenId() {
		return btnFullScreenId;
	}

	@Override
	public String getVideoPlayerSettings() {
		return btnSettings;
	}
	
	

}
