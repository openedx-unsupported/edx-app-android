package org.edx.androidtestsuite;

import org.edx.basetest.MyCoursesTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups = "Android")
public class Online_MyCoursesTestSuite extends MyCoursesTestSuiteBase {

	@Override
	public void gotoMyCoursesView() {
		try {
			while (driver.verifyElementText(getHeaderNameId(), getTxtMyCourseName())) {
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
	public String getLstDownloadId() {
		return lstDownloadVideosId;
	}

	@Override
	public String getTxtMyVideosName() {
		return txtMyVideosName;
	}
	
	@Override
	public String getTxtMyCourseName() {
		return txtMyCourseName;
	}

	@Override
	public String getViewOnWebId() {
		return hlnkViewOnWebId;
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

	@Override
	public String getLogoutId() {
		return btnLogOutId;
	}

	@Override
	public String getCCPopUpCancelId() {
		return popupLanguagesCancel;
	}

	@Override
	public String getSettingsPopUpId() {
		return btnSettings;
	}

	@Override
	public String getCCPopUpId() {
		return "Closed Captions";
	}

	@Override
	public String getDownloadMessage() {
		return msgDownloadId;
	}

	@Override
	public String getFindACourseBtnId() {
		return btnFindACourseId;
	}

	@Override
	public String getLnkFindCourseName() {
		return lnkFindACourseName;
	}

	@Override
	public String getTxtLookingForChallenge() {
		return txtLookingForChallengeName;
	}

	@Override
	public String getCourseWareErrorText() {
		return lbCourseWareName;
	}

	@Override
	public String getCourseWareErrorId() {
		return lbCourseWareId;
	}

	@Override
	public String getCloseId() {
		return btnCloseId;
	}

	@Override
	public String getDontSeeOneOfCoursesId() {
		return btnDontSeeCoursesId;
	}

	@Override
	public String getSeekBarId() {
		return null;
	}

	@Override
	public String getVideoHeaderId() {
		return null;
	}

	@Override
	public String getVideoName() {
		return lbVideoName;
	}

	@Override
	public String getVideoSize() {
		return lbVideoSize;
	}

	@Override
	public String getVideoLength() {
		return lbVideoLength;
	}

	@Override
	public String getBtnViewId() {
		return btnViewId;
	}

	@Override
	public String getMyCoursesHeaderId() {
		return btnHeaderNameId;
	}

	@Override
	public String getWebLinkId() {
		return lnkFindACourseName;
	}

	@Override
	public String getTxtFindACourseName() {
		return txtFindACourseName;
	}

	@Override
	public String getMyVideosId() {
		return null;
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
	public String getShowingOnlyVideosName() {
		return txtShowingOnlyVideos;
	}

	@Override
	public String getCourseInfoName() {
		return btnCourseInfoName;
	}

	@Override
	public String getMySettingsId() {
		return txtMySettingsId;
	}

	@Override
	public String getFindCourseHeaderName() {
		return txtFindCourseName;
	}

	@Override
	public String getFindCourseWebView() {
		return findACoursewebView;
	}
}
