package org.edx.iostestsuite;

import org.edx.basetest.Offline_MyCoursesTestSuiteBase;
import org.testng.Reporter;
import org.testng.annotations.Test;

@Test(groups ="iOS" )
public class Offline_MyCoursesTestSuite extends Offline_MyCoursesTestSuiteBase {

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
		return hlnkOpenInBrowserIdiOS;
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
	public String getMyCoursesName() {
		return txtMyCourseName;
	}

	@Override
	public String getVideoPlayerId() {
		return vpVideoPlayerIdiOS;
	}

	@Override
	public String getCCPopUpCancelId() {
		return popupLanguagesCanceliOS;
	}

	@Override
	public String getSettingsPopUpId() {
		return btnSettingsiOS;
	}

	@Override
	public String getCCPopUpId() {
		return popupCCiOS;
	}

	@Override
	public String getOkPopupId() {
		return btnOkPopupIdiOS;
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
	public String getSeekBarId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getViewOnWebId() {
		return btnViewOnWebIdiOS;
	}

	@Override
	public String getDisabledSectionErrorMessage() {
		return txtDisabledSectionMessage;
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
	public String getOfflineBarId() {
		return offlineBarIdiOS;
	}

	@Override
	public String getBtnHeaderNameId() {
		return btnHeaderNameIdiOS;
	}

	@Override
	public String getBtnDeletePopupId() {
		return btnDeletePopupIdiOS;
	}

	@Override
	public String getMyCoursesHeaderId() {
		return headerMyCoursesIdiOS;
	}

	@Override
	public String getVideosNotAvailableMsg() {
		return txtDisabledVideoMessage;
	}

	
}
