package org.edx.basetest;

import org.testng.annotations.Test;
import org.edx.elementlocators.IFindCourseLocators;

public abstract class FindCourseTestSuiteBase extends CommonFunctionalities
		implements IFindCourseLocators {

	// TODO Change the context of appium to webview before performing any
	// operations on webview

	@Test(priority = 0)
	private void verifyFindCoursesTest() {
		driver.clickElementById(getHeaderId());
		driver.clickElementById(getLNPFindCoursesId());
		driver.verifyElementPresentByName(getFindCourseName());
	}

	@Test(priority = 2)
	private void verifyCourseEnrollmentTest() {
		driver.clickElementByXpath(getCourseEnrollmentByXpath());
		driver.clickElementByXpath(getCourseEnrollButtonByXpath());

	}

}
