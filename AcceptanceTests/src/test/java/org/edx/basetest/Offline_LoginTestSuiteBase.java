package org.edx.basetest;

import org.edx.elementlocators.ILoginLocators_Offline;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public abstract class Offline_LoginTestSuiteBase extends CommonFunctionalities
		implements ILoginLocators_Offline {

	@Test(priority = -5)
	public void offline() throws InterruptedException {
		logout(driver, getHeaderId_Name(), getLogoutId(), getEmailLocatorId(),
				isAndroid());
	}

	/**
	 * Check for offline error message if user tries to login with edx username
	 * and password, facebook button and google button
	 * @throws InterruptedException 
	 */
	@Test
	public void OfflineModeMessageTest() throws InterruptedException {
		driver.enterTextToElementById(getEmailLocatorId(), emailId);
		if (isAndroid()) {
			driver.hideKeyboard();
		}
		driver.enterTextToElementById(getPasswordLocatorId(), password);
		if (isAndroid()) {
			driver.hideKeyboard();
		}
		driver.clickElementById(getSignInLocatorId());
		driver.verifyElementPresentByName(getOfflineModeErrorMsg());
		if (!isAndroid()) {
			driver.tapOnWifi();
		}
		Thread.sleep(3*1000);
		driver.clickElementById(getFaceBookBtnId());
		driver.verifyElementPresentByName(getOfflineModeErrorMsg());
		driver.clickElementById(getGmailBtnId());
		driver.verifyElementPresentByName(getOfflineModeErrorMsg());
	}

	/**
	 * Verify that
	 */
	@Test
	private void forgotPasswordErrorMessageTest() {
		driver.clickElementById(getForgotPasswordId());
		driver.verifyElementPresentByName(getForgotPasswordErrorMsgName());
		driver.clickElementById(getOkPopUpId());
	}

	/**
	 * Verify that user can open EULA page in offline mode
	 */
	@Test
	private void verifyEULAScreenTest() {
		driver.clickElementById(getEULALinkId());
		if (isAndroid()) {
			driver.clickElementById(getOkPopUpId());
		} else {
			driver.clickElementById(getCloseEULAId());
		}
	}

	/**
	 * Verify facebook button, Gmail button, New user? Sign up button
	 * @throws Throwable 
	 */
	@Test
	private void verifyElementsPresentOnScreenTest() throws Throwable {
		driver.verifyElementPresentById(getFaceBookBtnId());
		driver.verifyElementPresentById(getGmailBtnId());
		driver.verifyElementPresentByName(getTxtOrSignInWithName());
		driver.verifyElementPresentByName(gettxtBySigningInName());
		
	}

	/**
	 * Recovery Scenario for all the screens if any of the test case fails
	 * 
	 * @throws Throwable
	 */
	@AfterMethod(alwaysRun = true)
	public void recoveryScenario(ITestResult rs) throws Throwable {
		if (rs.getStatus() == 2) {
			Reporter.log("Test case " + rs.getTestName() + " failed");
			driver.launchApp();
		}
	}
}
