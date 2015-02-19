package org.edx.basetest;

import org.edx.elementlocators.IMyCoursesLocators;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public abstract class MyCoursesTestSuiteBase extends CommonFunctionalities
		implements IMyCoursesLocators {

	@Test
	public void login() {
		login(driver, getEmailLocatorId(), getPasswordLocatorId(),
				getSignInLocatorId(), isAndroid());
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getMySettingsId());
		driver.clickElementById(getSettingsBtnId());
		driver.clickElementById(getOkPopupId());
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getMyCourseId());
	}

	/**
	 * Verify that user can refresh the Courses Listing screen
	 * 
	 * @throws Throwable
	 */
	@Test
	public void verifyRefreshTest() throws Throwable {
		if (isAndroid()) {
			gotoMyCoursesView();
			driver.swipe(getMyCoursesHeaderId());
		}
	}

	/**
	 * Verify that video can be played
	 * 
	 * @throws Throwable
	 */
	@Test
	public void verifyVideoPlayedTest() throws Throwable {
		Thread.sleep(8000);
		gotoMyCoursesView();
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getMyCourseId());
		Thread.sleep(10 * 1000);
		driver.clickElementWithIndexById(getCourseListId(), 1);
		Thread.sleep(10 * 1000);
		driver.clickElementById(getSectionSubsectionListId());
		Thread.sleep(10 * 1000);
		driver.clickElementById(getSectionSubsectionListId());
		driver.clickElementById(getVideoListId());
		// verify video player
		driver.verifyElementPresentById(getVideoPlayerId());
	}

	/**
	 * Verify that user can navigate to download screen and cancel download
	 * 
	 * @throws Throwable
	 */
	@Test
	public void verifyDownloadScreenTest() throws Throwable {

		Thread.sleep(8000);
		gotoMyCoursesView();
		Thread.sleep(10000);

		driver.clickElementWithIndexById(getCourseListId(), 0);
		Thread.sleep(8000);
		driver.clickElementWithIndexById(getSectionSubsectionDownloadId(), 0);
		driver.verifyElementPresentById(getDownloadMessage());
		driver.clickElementById(getDownloadScreenId());
		driver.clickElementWithIndexById(getDownloadScreenCancelBtnId(), 0);
		driver.insertWait(getLstDownloadId());
		driver.clickElementById(getHeaderId());
		Thread.sleep(30000);
		driver.clickElementWithIndexById(getSectionSubsectionDownloadId(), 1);
		driver.verifyElementPresentById(getDownloadMessage());
		driver.clickElementById(getDownloadScreenId());
		driver.clickElementById(getBtnViewId());
		if (isAndroid()) {
			driver.verifyElementPresentByName(getTxtMyVideosName());
		} else {
			driver.verifyElementPresentById(getMyVideosId());
		}
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getMyCourseId());
	}

	/**
	 * Verify that Last accessed link is displayed
	 * 
	 * @throws Throwable
	 */
	@Test
	public void verifyLastAccessedLinkTest() throws Throwable {
		Thread.sleep(8000);
		gotoMyCoursesView();
		Thread.sleep(10 * 1000);
		driver.clickElementWithIndexById(getCourseListId(), 1);
		Thread.sleep(10 * 1000);
		driver.clickElementWithIndexById(getSectionSubsectionListId(), 2);
		Thread.sleep(10 * 1000);
		driver.clickElementWithIndexById(getSectionSubsectionListId(), 0);
		Thread.sleep(10 * 1000);
		driver.clickElementWithIndexById(getVideoListId(), 0);
		gotoMyCoursesView();
		Thread.sleep(10 * 1000);
		driver.clickElementWithIndexById(getCourseListId(), 1);
		// failed for ios
		Thread.sleep(10 * 1000);
		driver.clickElementById(getLastAccessedBtnId());
		videoInformation(driver, getVideoName(), getVideoLength());
		driver.clickElementById(getVideoListId());
		videoInformation(driver, getVideoName(), getVideoLength());
		driver.verifyElementPresentById(getVideoPlayerId());
		videoPlayer(driver, getFullScreenId(), getLMSId(),
				getVideoPlayerSettings(), getRewindId(), getSeekBarId(),
				getPlayPauseId(), getVideoPlayerId(), false, isAndroid());
	}

	/**
	 * Verify that user can switch to Course info tab and Handouts tab.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void verifyCourseInfoTabTest() throws Throwable {
		Thread.sleep(8000);
		gotoMyCoursesView();
		Thread.sleep(20000);
		driver.clickElementWithIndexById(getCourseListId(), 1);
		driver.clickElementByName(getCourseInfoName());
		driver.verifyElementPresentByName(getAnnouncementsName());
		driver.clickElementByName(getHandoutsName());
	}

	/**
	 * Verify that View on web button is present on section listing, subsection
	 * listing and video listing screen
	 * 
	 * @throws Throwable
	 */
	@Test
	public void verifyViewOnWebTest() throws Throwable {
		gotoMyCoursesView();
		Thread.sleep(8000);
		driver.clickElementWithIndexById(getCourseListId(), 1);
		driver.verifyElementPresentById(getViewOnWebId());
		driver.verifyElementPresentByName(getShowingOnlyVideosName());
		Thread.sleep(8000);
		driver.clickElementWithIndexById(getSectionSubsectionListId(), 0);
		driver.verifyElementPresentById(getViewOnWebId());
		driver.verifyElementPresentByName(getShowingOnlyVideosName());
		Thread.sleep(8000);
		driver.clickElementWithIndexById(getSectionSubsectionListId(), 0);
		driver.verifyElementPresentById(getViewOnWebId());
		driver.verifyElementPresentByName(getShowingOnlyVideosName());
	}

	/**
	 * Verification of Video player components( LMS button, Settings button,
	 * Full screen button, Rewind button)
	 * 
	 * @throws Throwable
	 */
	@Test
	public void verifyVideoPlayerComponentsTest() throws Throwable {
		Thread.sleep(8000);
		gotoMyCoursesView();
		verifyVideoPlayedTest();
		videoInformation(driver, getVideoName(), getVideoLength());
		videoPlayer(driver, getFullScreenId(), getLMSId(),
				getSettingsPopUpId(), getRewindId(), getSeekBarId(),
				getPlayPauseId(), getVideoPlayerId(), false, isAndroid());

	}

	/**
	 * Verification of find a course button
	 * 
	 * @throws Throwable
	 */
	@Test(priority = 30)
	public void verifyFindACourseTest() throws Throwable {
		Thread.sleep(8000);
		gotoMyCoursesView();
		if (isAndroid() || appPath.contains(".ipa")) {

			driver.scrollList(getDontSeeOneOfCoursesId());
			driver.verifyElementPresentByName(getTxtLookingForChallenge());
			driver.clickElementById(getDontSeeOneOfCoursesId());
			driver.clickElementById(getCloseId());
			driver.clickElementById(getFindACourseBtnId());
			driver.verifyElementPresentByName(getFindCourseHeaderName());
		}
	}

	/**
	 * Recovery Scenario for all the screens if any of the test case fails
	 * 
	 * @throws Throwable
	 */
	@AfterMethod(alwaysRun = true)
	public void recoveryScenario(ITestResult rs) throws Throwable {
		if (rs.getStatus() == 2) {
			gotoMyCoursesView();
			Reporter.log("Failed Test: " + rs.getTestName());
		}
	}

	@AfterClass
	public void logout() throws InterruptedException {
		logout(driver, getHeaderId(), getLogoutId(), getEmailLocatorId(),
				isAndroid());
	}

}
