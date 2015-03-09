package org.edx.basetest;

import org.edx.elementlocators.IMyVideosLocators_Offline;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public abstract class Offline_MyVideosTestSuiteBase extends
		CommonFunctionalities implements IMyVideosLocators_Offline {

	@Test(priority = -1)
	public void offline() throws InterruptedException {
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getTxtMyVideosId());
		Thread.sleep(3 * 1000);
	}

	/**
	 * Check for offline mode label present on My Videos screen
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void OfflineModeLabelTest() throws InterruptedException {
		// Offline label cannot be verified
		// driver.verifyElementPresentByName(getOfflineLabelName());
		driver.verifyElementPresentById(getOfflineBarId());
		Thread.sleep(3 * 1000);
	}

	/**
	 * Verify that downloaded video can be played from All videos
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void verifyOfflineVideoPlayerAllVideosTest()
			throws InterruptedException {
		driver.clickElementByName(getTxtAllVideosName());
		driver.clickElementWithIndexById(getLstCourseId(), 0);
		Thread.sleep(3 * 1000);
		driver.clickElementWithIndexById(getLstVideoId(), 0);
		videoPlayer(driver, getFullScreenId(), getLMSId(),
				getSettingsPopUpId(), getRewindId(), getSeekBarId(),
				getPlayPauseId(), getVideoPlayerId(), true, isAndroid());
		// Navigating back to the My Videos screen
		driver.clickElementById(getHeaderId());
		Thread.sleep(3 * 1000);
	}

	/**
	 * Verify that downloaded video can be played from Recent videos
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void verifyOfflineVideoPlayerRecentVideosTest()
			throws InterruptedException {
		driver.clickElementByName(getTxtRecentVideosName());
		driver.clickElementWithIndexById(getLstVideoId(), 0);
		videoPlayer(driver, getFullScreenId(), getLMSId(),
				getSettingsPopUpId(), getRewindId(), getSeekBarId(),
				getPlayPauseId(), getVideoPlayerId(), true, isAndroid());
		Thread.sleep(3 * 1000);
	}

	/**
	 * Verify that downloaded video can be deleted from All videos
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void verifyOfflineVideoDeletionOnAllVideosScreenTest()
			throws InterruptedException {
		// Navigating to All Videos tab
		driver.clickElementByName(getTxtAllVideosName());
		driver.clickElementWithIndexById(getLstCourseId(), 0);
		if (isAndroid()) {
			deleteFuctionality(driver, getBtnEditId(), getCbVideoSelectId(),
					getBtnDeleteId(), getOkPopupId(), 0);
		} else {
			deleteFuctionality(driver, getBtnEditId(), getCbVideoSelectId(),
					getBtnDeleteId(), getBtnDeletePopupId(), 0);
		}
		// Navigating Back to My Videos screen
		driver.clickElementById(getHeaderId());
	}

	/**
	 * Verify that downloaded video can be deleted from Recent videos
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void verifyOfflineVideoDeletionOnRecentVideosScreenTest()
			throws InterruptedException {
		driver.clickElementByName(getTxtRecentVideosName());
		if (isAndroid()) {
			deleteFuctionality(driver, getBtnEditId(), getCbVideoSelectId(),
					getBtnDeleteId(), getOkPopupId(), 0);
		} else {
			deleteFuctionality(driver, getBtnEditId(), getCbVideoSelectId(),
					getBtnDeleteId(), getBtnDeletePopupId(), 0);
		}
		Thread.sleep(3 * 1000);
	}

	/**
	 * Recovery Scenario for My Videos screen if any of the test case fails
	 * 
	 * @throws Throwable
	 */
	@AfterMethod(alwaysRun = true)
	public void recoveryScenario(ITestResult rs) throws Throwable {
		if (rs.getStatus() == 2) {
			Reporter.log("Failed Test: " + rs.getTestName());
			gotoMyVideosView();
		}
	}

}
