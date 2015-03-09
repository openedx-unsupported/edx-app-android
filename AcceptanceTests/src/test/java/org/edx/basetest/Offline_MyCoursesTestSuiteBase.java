package org.edx.basetest;

import org.edx.elementlocators.IMyCoursesLocators_Offline;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

//Test cases specific for Android(device and emulator) and iOS(only device)
public abstract class Offline_MyCoursesTestSuiteBase extends
		CommonFunctionalities implements IMyCoursesLocators_Offline {

	@Test(priority = -10)
	public void offline() throws InterruptedException {
		driver.setNetworkConnection(false, false, false);
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getMyCourseId());

	}

	/**
	 * TODO - Verify offline message
	 * //driver.verifyElementPresentByName(getOfflineErrorMessageName()); Check
	 * for offline mode message and offline label
	 * 
	 * @throws Throwable
	 */
	@Test(priority = -7)
	public void verifyOfflineModeMessageTest() throws Throwable {
		gotoMyCoursesView();
		// driver.verifyElementPresentByName(getOfflineLabelName());
		driver.verifyElementPresentById(getOfflineBarId());
	}

	/**
	 * Verify that sections from which videos where downloaded are enabled.
	 * Verify that last accessed link and View on web options are not available
	 * in offline mode
	 * 
	 * @throws Throwable
	 */
	@Test(priority = -5)
	private void verifyEnabledAndDisabledSectionTest() throws Throwable {
		gotoMyCoursesView();
		driver.clickElementWithIndexById(getCourseListId(), 0);
		// driver.verifyElementPresentByName(getOfflineLabelName());
		driver.verifyElementPresentById(getOfflineBarId());
		// Verification of last accessed and view on web
		if (isAndroid()) {
			driver.verifyElementNotPresntById(getLastAccessedBtnId());
			driver.verifyElementNotPresntById(getViewOnWebId());
		}
		// Disabled section test
		driver.clickElementWithIndexById(getSectionSubsectionListId(), 2);
		driver.verifyElementPresentByName(getDisabledSectionErrorMessage());
		Thread.sleep(3 * 1000);
		// driver.verifyElementPresentByName(getOfflineLabelName());
		driver.verifyElementPresentById(getOfflineBarId());
		// Enabled section test
		driver.clickElementWithIndexById(getSectionSubsectionListId(), 0);
		// driver.verifyElementPresentByName(getOfflineLabelName());
		driver.verifyElementPresentById(getOfflineBarId());
		driver.verifyElementPresentById(getVideoListId());
		driver.verifyElementPresentById(getBtnEditId());
	}

	/**
	 * Verify that user can play downloaded video in offline mode
	 * 
	 * @throws Throwable
	 */
	@Test(priority = -3)
	private void verifyVideoPlayerTest() throws Throwable {
		gotoMyCoursesView();
		driver.clickElementWithIndexById(getCourseListId(), 0);
		driver.clickElementWithIndexById(getSectionSubsectionListId(), 0);
		driver.clickElementWithIndexById(getVideoListId(), 2);
		// driver.verifyElementPresentByName(getOfflineLabelName());
		driver.verifyElementPresentById(getOfflineBarId());
		videoPlayer(driver, getFullScreenId(), getLMSId(),
				getSettingsPopUpId(), getRewindId(), getSeekBarId(),
				getPlayPauseId(), getVideoPlayerId(), true, isAndroid());
	}

	/**
	 * Verify that user can delete videos from My courses screen in offline mode
	 * 
	 * @throws Throwable
	 */
	@Test
	public void verifyDeleteFunctionalityTest() throws Throwable {
		gotoMyCoursesView();
		driver.clickElementWithIndexById(getCourseListId(), 0);
		driver.clickElementWithIndexById(getSectionSubsectionListId(), 0);
		if (isAndroid()) {
			deleteFuctionality(driver, getBtnEditId(), getCbVideoSelectId(),
					getBtnDeleteId(), getOkPopupId(), 2);
		} else {
			deleteFuctionality(driver, getBtnEditId(), getCbVideoSelectId(),
					getBtnDeleteId(), getBtnDeletePopupId(), 2);
		}
	}

	/**
	 * Recovery Scenario for all the screens if any of the test case fails
	 * 
	 * @throws Throwable
	 */
	@AfterMethod(alwaysRun = true)
	public void recoveryScenario(ITestResult rs) throws Throwable {
		gotoMyCoursesView();
		if (rs.getStatus() == 2) {
			Reporter.log("Failed Test: " + rs.getTestName());
		}
	}

}
