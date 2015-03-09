package org.edx.androidtestsuite;

import org.edx.basetest.Offline_FindCourseTestSuiteBase;

public class Offline_FindCourseTestSuite extends Offline_FindCourseTestSuiteBase{

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
	public String getOfflineModeTextId() {
		return Android_OfflineModeTextId;
	}

	@Override
	public String getOfflineBarId() {
		return Android_OfflineBarId;
	}

	@Override
	public String getOfflineModeLabelId() {
		return Android_OfflineModeLabelId;
	}

}
