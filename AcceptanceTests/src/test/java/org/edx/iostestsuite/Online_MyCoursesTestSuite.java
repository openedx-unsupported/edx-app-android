package org.edx.iostestsuite;

import org.edx.basetest.MyCoursesTestSuiteBase;
import org.testng.Reporter;
import org.testng.annotations.Test;

@Test(groups ="iOS" )
public class Online_MyCoursesTestSuite extends MyCoursesTestSuiteBase {

	@Override
	public String getWebLinkId() {
		return btnViewOnWebIdiOS;
	}

	@Override
	public String getViewOnWebId() {
		return hlnkViewOnWebIdiOS;
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
	public void gotoMyCoursesView() throws Throwable {
		int i=0;
		try {
			while (!driver.verifyElementId(getMyCoursesHeaderId())) {
				driver.clickElementById(getHeaderId());
				i++;
				if(i>10){
					throw new Exception();
				}
			}
		} catch (Throwable t) {
			Reporter.log("Element not found by id:"+getMyCoursesHeaderId());
			throw t;
		}
	}

	@Override
	public String getMyCoursesHeaderId() {
		return headerMyCoursesIdiOS;
	}

	@Override
	public String getMyCoursesName() {
		return txtMyCourseName;
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
		return btnPlayPauseiOS;
	}

	@Override
	public String getLMSId() {
		return btnLMSiOS;
	}

	@Override
	public String getRewindId() {
		return btnRewindiOS;
	}

	@Override
	public String getFullScreenId() {
		return btnFullScreenIdiOS;
	}

	@Override
	public String getVideoPlayerSettings() {
		return btnSettingsiOS;
	}

	@Override
	public String getLogoutId() {
		return btnLogOutIdiOS;
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
	public String getCCPopUpCancelId() {
		return popupCC;
	}

	@Override
	public String getSettingsPopUpId() {
		return btnSettingsiOS;
	}

	@Override
	public String getCCPopUpId() {
		return null;
	}

	@Override
	public String getDownloadMessage() {
		return msgDownloadIdiOS;
	}

	@Override
	public String getFindACourseBtnId() {
		return btnFindAMobileCourseiOS;
	}

	@Override
	public String getLnkFindCourseName() {
		return null;
	}

	@Override
	public String getTxtLookingForChallenge() {
		return txtLookingForCourseiOS;
	}

	@Override
	public String getCourseWareErrorText() {
		return lbCourseWareName;
	}

	@Override
	public String getCourseWareErrorId() {
		return lbCourseWareIdiOS;
	}

	@Override
	public String getCloseId() {
		return btnCloseIdiOS;
	}

	@Override
	public String getDontSeeOneOfCoursesId() {
		return txtDontSeeACourseiOS;
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
		return lbVideoNameiOS;
	}

	@Override
	public String getVideoSize() {
		return lbVideoSizeiOS;
	}

	@Override
	public String getVideoLength() {
		return lbVideoLengthiOS;
	}

	@Override
	public String getBtnViewId() {
		return btnViewIdiOS;
	}

	@Override
	public String getLstDownloadId() {
		return lbVideoNameiOS;
	}

	@Override
	public String getTxtMyVideosName() {
		return txtMyVideosNameiOS;
	}

	@Override
	public String getTxtMyCourseName() {
		return txtMyCourseName;
	}

	@Override
	public String getHeaderNameId() {
		return btnHeaderNameIdiOS;
	}

	@Override
	public String getTxtFindACourseName() {
		return txtFindACourseName;
	}

	@Override
	public String getMyVideosId() {
		return txtMyVideosIdiOS;
	}

	@Override
	public String getOkPopupId() {
		return btnOkPopupIdiOS;
	}

	@Override
	public String getSettingsBtnId() {
		return btnSwitchiOS;
	}

	@Override
	public String getShowingOnlyVideosName() {
		return txtShowingOnlyVideos;
	}

	@Override
	public String getCourseInfoName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMySettingsId() {
		return txtMySettingsIdiOS;
	}

	@Override
	public String getFindCourseHeaderName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFindCourseWebView() {
		// TODO Auto-generated method stub
		return null;
	}


}
