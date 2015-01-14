package org.edx.basetest;

import java.io.IOException;

import org.edx.elementlocators.IMyVideosLocators;
import org.testng.annotations.Test;

public abstract class MyVideosTestSuiteBase extends BaseTest implements IMyVideosLocators{
	
	/**
	 * Navigating the elements present on left navigation panel
	 * 
	 * @throws IOException
	 */
	@Test
	public void verifyLeftNavigationPanelTest() throws IOException {
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getSubmitFeedBackId());
		//Navigating back to the app 
		driver.back();
		driver.clickElementById(getSettingsBtnId());
		driver.clickElementById(getOkPopupId());
	}

	
	/**
	 * Verifying that user can switch between the tabs
	 */
	@Test
	public void verifyTabSwitchTest() {
		driver.clickElementById(getTxtMyVideosId());
		driver.clickElementByName(getTxtRecentVideosName());
		driver.clickElementByName(getTxtAllVideosName());
	}

	/**
	 * Verifying that user can play video from All videos tab
	 * 
	 */
	@Test
	public void verifyVideoPlayerAllVideosTest() {
		driver.clickElementById(getBtnDownloadScreenId());
		driver.insertWait(getLstDownloadId());
		driver.clickElementById(getHeaderId());
		driver.clickElementWithIndexById(getLstCourseId(), 	0);
		driver.clickElementWithIndexById(getLstVideoId(), 0);
		// Navigating back to the My Videos screen
		driver.clickElementById(getHeaderId());
	}

		/**
	 * Verifying that user can play video from Recent Videos tab
	 */
	@Test
	public void verifyVideoPlayerRecentVideosTest() {
		// Navigating to Recent Videos tab
		driver.clickElementByName(getTxtRecentVideosName());
		driver.clickElementWithIndexById(getLstVideoId(), 0);
	}
	
	/**
	 * Verifying that user can delete video from All Videos Screen 
	 */
	@Test
	public void verifyVideoDeletionOnAllVideosScreenTest() {
		// Navigating to All Videos tab
		driver.clickElementByName(getTxtAllVideosName());
		driver.clickElementWithIndexById(getLstCourseId(), 0);
		driver.clickElementById(getBtnEditId());
		driver.clickElementWithIndexById(getCbVideoSelectId(), 0);
		driver.clickElementById(getBtnDeleteId());
		driver.clickElementById(getOkPopupId());
		// Navigating Back to My Videos screen
		driver.clickElementById(getHeaderId());
	}

	
	/**
	 *Verifying that user can delete video from Recent Videos Screen 
	 */
	@Test(dependsOnMethods={"verifyVideoDeletionOnAllVideosScreenTest"})
	public void verifyVideoDeletionOnRecentVideosScreenTest() {
		driver.clickElementByName(getTxtRecentVideosName());
		driver.clickElementById(getBtnEditId());
		driver.clickElementWithIndexById(getCbVideoSelectId(), 0);
		driver.clickElementById(getBtnDeleteId());
		driver.clickElementById(getOkPopupId());
	}

	


}
