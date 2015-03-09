package org.edx.basetest;

import org.edx.elementlocators.ISignUpLocators;
import org.testng.annotations.Test;

public abstract class SignUpTestSuiteBase extends BaseTest implements ISignUpLocators{

	/**
	 * Check for various options present on Landing screen 
	 */
	@Test(priority = 1)
	public void checkForLandingPageTest() {
		driver.verifyElementPresentById(getEdxLogoById());
		driver.clickElementByName(getSignUpButtonByName());
	}
	
	/**
	 * Check that Show optional fields and Sign up for edX text are present on Sign up page 
	 */
	@Test(priority = 2)
	public void checkForOptionalFieldTextTest() {
		driver.verifyElementPresentByName(getCreateMyAccountByName());
		driver.verifyElementPresentByName(getShowOptionalFieldByName());
		driver.verifyElementPresentByName(getSignUpTextByName());
	}
	
	/**
	 * Check that offline mode message appears if user tries to create account in offline mode
	 * @throws InterruptedException
	 */
	@Test(priority = 3)
	public void checkForOfflineModeMessageTest() throws InterruptedException {
		driver.setNetworkConnection(false, false, false);
		driver.verifyElementPresentByName(getCreateMyAccountByName());
		driver.verifyElementPresentByName(getShowOptionalFieldByName());
		driver.verifyElementPresentByName(getSignUpTextByName());
		driver.clickElementById(getCreateMyAccountById());
		driver.verifyElementPresentByName(getOfflineMessageHeaderByName());
		driver.verifyElementPresentByName(getOfflineMessageByName());
		driver.setNetworkConnection(true, true, true);
		driver.clickElementById(getCloseButtonById());
	}

	
}
