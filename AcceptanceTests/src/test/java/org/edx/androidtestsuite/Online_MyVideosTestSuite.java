package org.edx.androidtestsuite;

import org.edx.basetest.MyVideosTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups = "Android")
public class Online_MyVideosTestSuite extends MyVideosTestSuiteBase {

	@Override
	public void gotoMyVideosView() {
		try {
			while (driver.verifyElementText(getHeaderNameId(), getTxtMyVideosName())) {
				driver.clickElementById(getHeaderId());
			}
		} catch (Throwable t) {
		}
	}
	
	@Override 
	public String getHeaderNameId() {
		return btnHeaderNameId;
	}

	@Override
	public String getTxtMyVideosName() {
		return txtMyVideosName;
	}
	
	@Override
	public String getOkPopupId() {
		return btnOkPopupId;
	}

	@Override
	public String getSettingsBtnId() {
		return btnSettingsId;
	}

	@Override
	public String getSubmitFeedBackId() {
		return btnSubmitFeedBackId;
	}

	@Override
	public String getHeaderId() {
		return btnHeaderId;
	}

	@Override
	public String getTxtAllVideosName() {
		return txtAllVideosName;
	}

	@Override
	public String getTxtRecentVideosName() {
		return txtRecentVideosName;
	}

	@Override
	public String getTxtMyVideosId() {
		return txtMyVideosId;
	}

	@Override
	public String getLstVideoId() {
		return lstVideoId;
	}

	@Override
	public String getLstCourseId() {
		return lstAllVideos_Courses;
	}

	@Override
	public String getLstDownloadId() {
		return lstDownloadVideosId;
	}

	@Override
	public String getBtnDownloadScreenId() {
		return btnDownloadScreenId;
	}

	@Override
	public String getBtnDeleteId() {
		return btnDeleteId;
	}

	@Override
	public String getCbVideoSelectId() {
		return cbVideoSelectId;
	}

	@Override
	public String getBtnEditId() {
		return btnEditId;
	}

	@Override
	public String getSignInLocatorId() {
		return btnSigninId;
	}

	@Override
	public String getEmailLocatorId() {
		return tbEmailId;
	}

	@Override
	public boolean isAndroid() {
		return true;
	}

	@Override
	public String getPasswordLocatorId() {
		return tbPasswordId;
	}

	@Override
	public String getLogoutId() {
		return btnLogOutId;
	}

	@Override
	public String getSectionSubsectionDownloadId() {
		return btnSectionSubsectionDownloadId;
	}

	@Override
	public String getCourseListId() {
		return btnCourseId;
	}

	@Override
	public String getEmailId() {
		return txtEmailId;
	}

	@Override
	public String getUserNameId() {
		return txtUserNameId;
	}

	@Override
	public String getVersion() {
		return txtVersion;
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
	
	@Override
	public String getVideoPlayerId() {
		return vpVideoPlayerId;
	}

	@Override
	public String getSeekBarId() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getVideoHeaderId() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getSettingsPopUpId() {
		return btnSettings;
	}

	@Override
	public String getBtnDeletePopupId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTxtMySettingsId() {
		return txtMySettingsId;
	}



}
