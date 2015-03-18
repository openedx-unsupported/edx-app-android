package org.edx.basetest;

import org.edx.elementlocators.IFindCourseLocators_Offline;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public abstract class Offline_FindCourseTestSuiteBase extends BaseTest
		implements IFindCourseLocators_Offline {

	@Test(priority = 0)
	public void verifyFindCoursesTest() {
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getLNPFindCoursesId());
		driver.verifyElementPresentByName(getFindCourseName());
	}

	@Test(priority = 1)
	private void verifyElementsPresentOnFindCoursesScreenTest() {
		driver.verifyElementPresentById(getOfflineModeLabelId());
		driver.verifyElementPresentById(getOfflineBarId());
		driver.verifyElementPresentById(getOfflineModeTextId());
	}
	
	/**
	 * Recovery Scenario for Find Courses screen if any of the test case fails
	 * 
	 * @throws Throwable
	 */
	@AfterMethod(alwaysRun = true)
	public void recoveryScenario(ITestResult rs) throws Throwable {
		if (rs.getStatus() == 2) {
			Reporter.log("Failed Test: " + rs.getTestName());
		}
	}

}
