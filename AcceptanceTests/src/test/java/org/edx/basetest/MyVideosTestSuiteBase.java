package org.edx.basetest;

import java.io.IOException;

import org.edx.elementlocators.IMyVideosLocators;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public abstract class MyVideosTestSuiteBase extends CommonFunctionalities
		implements IMyVideosLocators {

	@Test(priority = -5)
	public void login() {
		login(driver, getEmailLocatorId(), getPasswordLocatorId(),
				getSignInLocatorId(), isAndroid());

	}

	/**
	 * Navigating the elements present on left navigation panel
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test(priority = -3)
	public void verifyLeftNavigationPanelTest() throws IOException,
			InterruptedException {
		Thread.sleep(10000);
		driver.clickElementById(getHeaderId());
		driver.verifyElementPresentById(getSubmitFeedBackId());
		driver.verifyElementPresentById(getVersion());
		driver.verifyElementPresentById(getUserNameId());
		driver.verifyElementPresentById(getEmailId());
		driver.verifyElementPresentById(getTxtMySettingsId());
		driver.clickElementById(getTxtMyVideosId());
	}

	

	/**
	 * Verifying that user can switch between the tabs
	 */
	@Test
	public void verifyTabSwitchTest() {
		driver.clickElementByName(getTxtRecentVideosName());
		driver.clickElementByName(getTxtAllVideosName());
	}

	/**
	 * Verifying that user can play video from All videos tab
	 * 
	 * @throws InterruptedException
	 * 
	 */
	@Test(priority = -1)
	public void verifyVideoPlayerAllVideosTest() throws InterruptedException {

		Thread.sleep(10000);
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getTxtMyVideosId());
		Thread.sleep(10000);
		driver.clickElementWithIndexById(getLstCourseId(), 0);
		Thread.sleep(10000);
		driver.clickElementWithIndexById(getLstVideoId(), 0);
		videoPlayer(driver, getFullScreenId(), getLMSId(),
				getSettingsPopUpId(), getRewindId(), getSeekBarId(),
				getPlayPauseId(), getVideoPlayerId(), true, isAndroid());
		// Navigating back to the My Videos screen
		driver.clickElementById(getHeaderId());
	}

	/**
	 * Verifying that user can play video from Recent Videos tab
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void verifyVideoPlayerRecentVideosTest() throws InterruptedException {
		// Navigating to Recent Videos tab
		driver.clickElementByName(getTxtRecentVideosName());
		driver.clickElementWithIndexById(getLstVideoId(), 0);
		videoPlayer(driver, getFullScreenId(), getLMSId(),
				getSettingsPopUpId(), getRewindId(), getSeekBarId(),
				getPlayPauseId(), getVideoPlayerId(), true, isAndroid());
	}

	/**
	 * Verifying that user can delete video from All Videos Screen
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 20)
	public void verifyVideoDeletionOnAllVideosScreenTest()
			throws InterruptedException {
		// Navigating to All Videos tab
		driver.clickElementByName(getTxtAllVideosName());
		Thread.sleep(10000);
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
	 * Verifying that user can delete video from Recent Videos Screen
	 * 
	 * @throws InterruptedException
	 */
	@Test(priority = 25)
	public void verifyVideoDeletionOnRecentVideosScreenTest()
			throws InterruptedException {
		driver.clickElementByName(getTxtRecentVideosName());// Deleting any

		if (isAndroid()) {
			deleteFuctionality(driver, getBtnEditId(), getCbVideoSelectId(),
					getBtnDeleteId(), getOkPopupId(), 0);
		} else {
			deleteFuctionality(driver, getBtnEditId(), getCbVideoSelectId(),
					getBtnDeleteId(), getBtnDeletePopupId(), 0);
		}
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
