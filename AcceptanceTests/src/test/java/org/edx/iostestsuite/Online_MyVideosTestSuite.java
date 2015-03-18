/**
 * 
 */
package org.edx.iostestsuite;

import org.edx.basetest.MyVideosTestSuiteBase;
import org.testng.annotations.Test;

/**
 * @author divakarpatil
 * 
 */
@Test(groups = "iOS")
public class Online_MyVideosTestSuite extends MyVideosTestSuiteBase {

	@Override
	public String getOkPopupId() {
		return btnOkPopupIdiOS;
	}

	@Override
	public String getSettingsBtnId() {
		return btnSwitchiOS;
	}

	@Override
	public String getSubmitFeedBackId() {
		return btnSubmitFeedBackIdiOS;
	}

	@Override
	public String getHeaderId() {
		return btnHeaderIdiOS;
	}

	@Override
	public String getTxtAllVideosName() {
		return txtAllVideosNameiOS;
	}

	@Override
	public String getTxtRecentVideosName() {
		return txtRecentVideosNameiOS;
	}

	@Override
	public String getTxtMyVideosId() {
		return txtMyVideosIdiOS;
	}

	@Override
	public String getLstVideoId() {
		return lstVideoIdiOS;
	}

	@Override
	public String getLstCourseId() {
		return btnCourseIdiOS;
	}

	@Override
	public String getLstDownloadId() {
		return null;
	}

	@Override
	public String getBtnDownloadScreenId() {
		return null;
	}

	@Override
	public String getBtnDeleteId() {
		return btnDeleteIdiOS;
	}

	@Override
	public String getCbVideoSelectId() {
		return cbVideoSelectIdiOS;
	}

	@Override
	public String getBtnEditId() {
		return btnEditIdiOS;
	}

	@Override
	public String getEmailLocatorId() {
		return tbEmailIdiOS;
	}

	@Override
	public boolean isAndroid() {
		return false;
	}

	@Override
	public String getPasswordLocatorId() {
		return tbPasswordIdiOS;
	}

	@Override
	public String getSignInLocatorId() {
		return btnSigninIdiOS;
	}

	@Override
	public String getLogoutId() {
		return btnLogOutIdiOS;
	}

	@Override
	public String getSectionSubsectionDownloadId() {
		return null;
	}

	@Override
	public String getCourseListId() {
		return null;
	}

	@Override
	public String getEmailId() {
		return txtEmailIdiOS;
	}

	@Override
	public String getUserNameId() {
		return txtUserNameIdiOS;
	}

	@Override
	public String getVersion() {
		return txtVersioniOS;
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
	public String getVideoPlayerId() {
		return vpVideoPlayerIdiOS;
	}

	@Override
	public void gotoMyVideosView() {
		try {
			while (!driver.verifyElementId(getHeaderNameId())) {
				driver.clickElementById(getHeaderId());
			}
		} catch (Throwable t) {
		}
	}

	@Override
	public String getSeekBarId() {
		return null;
	}

	@Override
	public String getVideoHeaderId() {
		return lbVideoHeaderIdiOS;
	}

	@Override
	public String getSettingsPopUpId() {
		return btnSettingsiOS;
	}

	@Override
	public String getTxtMyVideosName() {
		return txtMyVideosNameiOS;
	}

	@Override
	public String getHeaderNameId() {
		return btnHeaderNameIdiOS;
	}

	@Override
	public String getBtnDeletePopupId() {
		return btnDeletePopupIdiOS;
	}

	@Override
	public String getTxtMySettingsId() {
		return txtMySettingsIdiOS;
	}

}
