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

	String getViewingCoursesByXpath();

	String getFeaturedCoursesByXpath();

	String getFilterCoursesByXpath();

	String getCourseEnrollButtonByXpath();

	String getCourseEnrollmentByXpath();
	
	//Android id's
	
	String Android_lnpFindCoursesId="org.edx.mobile:id/drawer_option_find_courses";
	String Android_FindCoursesName="Find Courses";
	String Android_HeaderId="android:id/up";
	String Android_ViewingCoursesXpath="//android.view.View[1]/android.widget.FrameLayout[2]/android.view.View[1]/android.widget.RelativeLayout[1]/android.view.View[1]/android.view.View[2]/android.view.View[2]";
	String Android_FeaturedCoursesXpath="//android.view.View[1]/android.widget.FrameLayout[2]/android.view.View[1]/android.widget.RelativeLayout[1]/android.view.View[1]/android.view.View[2]/android.view.View[5]/android.view.View[1]";
	String Android_FilterCoursesXpath="//android.view.View[1]/android.widget.FrameLayout[2]/android.view.View[1]/android.widget.RelativeLayout[1]/android.view.View[1]/android.view.View[2]/android.view.View[3]/android.view.View[1]";
	String Android_CourseEnrollButtonXpath="//android.view.View[1]/android.widget.FrameLayout[2]/android.view.View[1]/android.widget.RelativeLayout[1]/android.view.View[1]/android.view.View[2]/android.view.View[5]/android.view.View[2]";
	String Android_CourseEnrollmentXpath="//android.view.View[1]/android.widget.FrameLayout[2]/android.view.View[1]/android.widget.RelativeLayout[1]/android.view.View[1]/android.view.View[2]/android.view.View[3]";

}
