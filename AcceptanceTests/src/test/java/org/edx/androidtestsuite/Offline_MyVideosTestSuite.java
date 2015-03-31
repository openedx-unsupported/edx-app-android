package org.edx.androidtestsuite;

import org.edx.basetest.Offline_MyVideosTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups = "Android")
public class Offline_MyVideosTestSuite extends Offline_MyVideosTestSuiteBase {

	@Override
	public String getOfflineBarId() {
		return offlineBarId;
	}

	@Override
	public String getOfflineLabelName() {
		return txtOfflineName;
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
	public boolean isAndroid() {
		return true;
	}

	@Override
	public String getCourseListId() {
		return btnCourseId;
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
		return null;
	}
	
	@Override
	public String getSettingsPopUpId() {
		return btnSettings;
	}


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
	public String getTxtMyVideosName() {
		return txtMyVideosName;
	}
	@Override
	public String getOkPopupId() {
		return btnOkPopupId;
	}

	@Override
	public String getHeaderNameId() {
		return btnHeaderNameId;
	}

	@Override
	public String getBtnDeletePopupId() {
		return null;
	}

}
