
package org.edx.basetest;

import org.edx.elementlocators.IMyCoursesLocators;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public abstract  class MyCoursesTestSuiteBase extends BaseTest implements IMyCoursesLocators{
	boolean recovery=false;

	@BeforeMethod
	public void recoveryMethod() throws Throwable {
		if (recovery){
			recoveryScenario();
			test_OpenApp();
		}
	}
	
	@BeforeTest
	public void test_OpenApp() {
		try {
			driver.enterTextToElementById(getEmailLocatorId(), "xxx");
			if(isAndroid()){
			driver.hideKeyboard();
			}
			driver.enterTextToElementById(getPasswordLocatorId(), "xxxx");
			if(isAndroid()){
				driver.hideKeyboard();
			}
			driver.clickElementById(getSignInLocatorId());
			recovery=false;
		} catch (Throwable e) {
			recovery=true;
			throw e;
		}
	}
	
	@Test
	public void verifyRefreshTest() {
			try {
				driver.swipe(getMyCoursesName());
				recovery=false;
			} catch (Throwable e) {
				recovery=true;
				throw e;
			}
	}
	
	/**
	 * Verify that video can be played
	 */
	@Test
	public void verifyVideoPlayedTest() {
		try {
			driver.clickElementById(getHeaderId());
			driver.clickElementById(getMyCourseId());
			driver.clickElementWithIndexById(getCourseListId(), 1);
			driver.clickElementById(getSectionSubsectionListId());
			driver.clickElementById(getSectionSubsectionListId());
			driver.clickElementById(getVideoListId());
			// verify video player
			driver.verifyElementPresentById(getVideoPlayerId());
			gotoMyCoursesView();
			recovery=false;
		} catch (Throwable e) {
			recovery=true;
			throw e;
		}
		
	}

	/**
	 * Verify that user can navigate to download screen and cancel download
	 */
	@Test
	public void verifyDownloadScreenTest() {
		try {
			driver.clickElementWithIndexById(getCourseListId(), 2);
			driver.clickElementWithIndexById(getSectionSubsectionDownloadId(), 0);
			driver.clickElementById(getDownloadScreenId());
			driver.clickElementWithIndexById(getDownloadScreenCancelBtnId(), 0);
			driver.clickElementById(getHeaderId());
			// Navigating to the My Courses screen
			gotoMyCoursesView();
			recovery=false;
		} catch (Throwable e) {
			recovery=true;
			throw e;
		}
		
	}

	/**
	 * Verify that Last accessed link is displayed 
	 */
	@Test
	public void verifyLastAccessedLinkTest() {
		try {
			driver.clickElementWithIndexById(getCourseListId(), 2);
			driver.clickElementWithIndexById(getSectionSubsectionListId(), 2);
			driver.clickElementById(getSectionSubsectionListId());
			driver.clickElementById(getVideoListId());
			gotoMyCoursesView();
			driver.clickElementWithIndexById(getCourseListId(), 2);
			driver.clickElementById(getLastAccessedBtnId());
			driver.verifyElementPresentById(getVideoPlayerId());
			gotoMyCoursesView();
			recovery=false;
		} catch (Throwable e) {
			recovery=true;
			throw e;
		}
	}

	
	@Test
	public void verifyAnnouncementsAndHandoutsTab() {
		try {
			driver.clickElementWithIndexById(getCourseListId(), 2);
			driver.clickElementByName(getAnnouncementsName());
			driver.clickElementByName(getHandoutsName());
			gotoMyCoursesView();
			recovery=false;
		} catch (Throwable e) {
			recovery=true;
			throw e;
		}

	}
	
	
	@Test
	public void verifyViewOnWebTest() {
		try {
			driver.clickElementWithIndexById(getCourseListId(), 1);
			driver.clickElementById(getOpenInBrowserId());
			driver.clickElementById(getWebLinkId());
			driver.launchApp();
			test_OpenApp();
			gotoMyCoursesView();
			recovery=false;
		} catch (Throwable e) {
			recovery=true;
			throw e;
		}
	}
	
	 @Test
	public void verifyVideoPlayerComponentsTest() {
		 	try {
				driver.clickElementWithIndexById(getCourseListId(), 2);
				driver.clickElementWithIndexById(getSectionSubsectionListId(), 2);
				driver.clickElementById(getSectionSubsectionListId());
				driver.clickElementById(getVideoListId());
				driver.insertWait(getVideoPlayerSettings());
				driver.verifyElementPresentById(getVideoPlayerSettings());
				driver.verifyElementPresentById(getFullScreenId());
				driver.verifyElementPresentById(getRewindId());
				driver.verifyElementPresentById(getLMSId());
				driver.verifyElementPresentById(getPlayPauseId());
				gotoMyCoursesView();
				recovery=false;
			} catch (Exception e) {
				recovery=true;
				throw e;
			}
	}

	
}
