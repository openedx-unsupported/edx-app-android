package org.edx.iostestsuite;

import org.edx.basetest.Offline_MyVideosTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups = "iOS")
public class Offline_MyVideosTestSuite extends Offline_MyVideosTestSuiteBase {

	@Override
	public String getOfflineBarId() {
		return offlineBarIdiOS;
	}

	@Override
	public String getOfflineLabelName() {
		return txtOfflineName;
	}

	@Override
	public String getTxtMyVideosId() {
		return txtMyVideosIdiOS;
	}

	@Override
	public String getHeaderId() {
		return btnHeaderIdiOS;
	}

	@Override
	public String getPlayPauseId() {
		return btnPlayPauseiOS;
	}

	@Override
	public String getSeekBarId() {
		return null;
	}

	@Override
	public String getVideoPlayerId() {
		return vpVideoPlayerIdiOS;
	}

	@Override
	public String getRewindId() {
		return btnRewindiOS;
	}

	@Override
	public String getSettingsPopUpId() {
		return btnSettingsiOS;
	}

	@Override
	public String getFullScreenId() {
		return btnFullScreenIdiOS;
	}

	@Override
	public String getLMSId() {
		return btnLMSiOS;
	}

	@Override
	public String getLstVideoId() {
		return lstVideoIdiOS;
	}

	@Override
	public String getLstCourseId() {
		return lstAllVideos_CoursesiOS;
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
	public void gotoMyVideosView() {
		try {
			while (!driver.verifyElementId(getHeaderNameId())) {
				driver.clickElementById(getHeaderId());
			}
		} catch (Throwable t) {
		}

	}

	@Override
	public String getVideoPlayerSettings() {
		return btnSettingsiOS;
	}

	@Override
	public String getCourseListId() {
		return btnCourseIdiOS;
	}

	@Override
	public boolean isAndroid() {
		return false;
	}

	@Override
	public String getHeaderNameId() {
		return btnHeaderNameIdiOS;
	}

	@Override
	public String getTxtMyVideosName() {
		return txtMyVideosNameiOS;
	}

	@Override
	public String getBtnDeletePopupId() {
		return btnDeletePopupIdiOS;
	}

}
