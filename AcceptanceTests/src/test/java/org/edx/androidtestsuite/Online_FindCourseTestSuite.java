package org.edx.androidtestsuite;

import org.edx.basetest.FindCourseTestSuiteBase;
import org.testng.annotations.Test;

@Test
public class Online_FindCourseTestSuite extends FindCourseTestSuiteBase{

	@Override
	public String getLNPFindCoursesId() {
		return Android_lnpFindCoursesId;
	}

	@Override
	public String getFindCourseName() {
		return Android_FindCoursesName;
	}

	@Override
	public String getHeaderId() {
		return Android_HeaderId;
	}
	
	@Override
	public String getViewingCoursesById() {
		return Android_ViewingCoursesId;
	}

	@Override
	public String getFilterCoursesById() {
		return Android_FilterCoursesId;
	}

	@Override
	public String getCourseEnrollButtonByXpath() {
		return Android_CourseEnrollButtonXpath;
	}

	@Override
	public String getCourseEnrollmentByXpath() {
		return Android_CourseEnrollmentXpath;
	}

}
