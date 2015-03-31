package org.edx.basetest;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.edx.elementlocators.IFindCourseLocators;

public abstract class FindCourseTestSuiteBase extends CommonFunctionalities
		implements IFindCourseLocators {

	@Test(priority = 0)
	public void verifyFindCoursesTest() {
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getLNPFindCoursesId());
		driver.verifyElementPresentByName(getFindCourseName());
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
