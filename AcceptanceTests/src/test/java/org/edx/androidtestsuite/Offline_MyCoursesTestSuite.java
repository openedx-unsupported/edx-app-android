package org.edx.androidtestsuite;

import org.edx.basetest.Offline_MyCoursesTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups = "Android")
public class Offline_MyCoursesTestSuite extends Offline_MyCoursesTestSuiteBase {

	@Override
	public boolean isAndroid() {
		return true;
	}

	@Override
	public String getOfflineLabelName() {
		return txtOfflineName;
	}

	@Override
	public String getOfflineErrorMessageName() {
		return txtOfflineMessageName;
	}

	@Override
	public String getOpenInBrowserId() {
		return hlnkOpenInBrowserId;
	}

	@Override
	public void gotoMyCoursesView() {
		try {
			while (driver.verifyElementText(getBtnHeaderNameId(), getMyCoursesName())) {
				driver.clickElementById(getHeaderId());
			}
		} catch (Throwable t) {
		}
	}
	
	@Override
	public String getBtnHeaderNameId() {
		return btnHeaderNameId;
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
	public String getCCPopUpCancelId() {
		return null;
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
	public String getOkPopupId() {
		return btnOkPopupId;
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
	public String getSeekBarId() {
		return null;
	}

	@Override
	public String getViewOnWebId() {
		return hlnkOpenInBrowserId;
	}

	@Override
	public String getDisabledSectionErrorMessage() {
		return txtDisabledSectionMessage;
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
	public String getLastAccessedBtnId() {
		return btnLastAccessedId;
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
	public String getOfflineBarId() {
		return offlineBarId;
	}

	@Override
	public String getBtnDeletePopupId() {
		return btnDeletePopupIdiOS;
	}

	@Override
	public String getMyCoursesHeaderId() {
		return null;
	}

	@Override
	public String getVideosNotAvailableMsg() {
		return txtDisabledVideoMessage;
	}

}
