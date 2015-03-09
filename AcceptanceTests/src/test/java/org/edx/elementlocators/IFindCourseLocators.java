/**
 * 
 */
package org.edx.elementlocators;

/**
 * @author divakarpatil
 * 
 */
public interface IFindCourseLocators {

	String getLNPFindCoursesId();

	String getFindCourseName();

	String getHeaderId();

	String getViewingCoursesById();

	String getFilterCoursesById();

	String getCourseEnrollButtonByXpath();

	String getCourseEnrollmentByXpath();
	
	//Android id's
	
	String Android_lnpFindCoursesId="org.edx.mobile:id/drawer_option_find_courses";
	String Android_FindCoursesName="Find Courses";
	String Android_HeaderId="android:id/up";
	String Android_ViewingCoursesId="Viewing 123 courses matching Heading";
	String Android_FilterCoursesId="FILTER COURSES Link";
	String Android_CourseEnrollButtonXpath="//android.view.View[1]/android.widget.FrameLayout[2]/android.view.View[1]/android.widget.RelativeLayout[1]/android.view.View[1]/android.view.View[2]/android.view.View[5]/android.view.View[2]";
	String Android_CourseEnrollmentXpath="//android.view.View[1]/android.widget.FrameLayout[2]//android.widget.RelativeLayout[1]//android.view.View[2]/android.view.View[5]/android.view.View[1]";

}
