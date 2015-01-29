package org.edx.basetest;

import org.testng.annotations.Test;

public class Example extends BaseTest {

	@Test
	public void baseTest() {
		//driver.swipe("SIGN IN");
		driver.enterTextToElementById("tbUserName", "aaaa");
		driver.tapOnWifi();
	}
}
