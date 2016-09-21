package org.edx.basetest;

import org.edx.elementlocators.ILoginLocators;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public abstract class LoginTestSuiteBase extends CommonFunctionalities
		implements ILoginLocators {
	
	/**
	 * Verify that error message is shown if user tries to login with Invalid
	 * credentials
	 */
	@Test
	public void loginWithInvalidCredentialsTest() {
		
		driver.clickElementById(getSignInButtonId());
		driver.verifyElementPresentByName(getSignInTextName());
		
		
		driver.enterTextToElementById(getEmailLocatorId(), "zzz");
		if (isAndroid()) {
			driver.hideKeyboard();
		}
		driver.enterTextToElementById(getPasswordLocatorId(), "zzz");
		if (isAndroid()) {
			driver.hideKeyboard();
		}
		driver.clickElementById(getSignInLocatorId());
		driver.findElementByName(getMsgInvalidCredentials());
		driver.clearInputById(getPasswordLocatorId());
		if (!(getPasswordLocatorId().isEmpty())) {
			driver.clearInputById(getPasswordLocatorId());
			driver.clearInputById(getPasswordLocatorId());
			driver.clearInputById(getPasswordLocatorId());
		}
		
		if (isAndroid()) {
			driver.hideKeyboard();
		}

		driver.clickElementById(getSignInLocatorId());
		driver.findElementByName(getMsgSignInWithNoPassword());
		driver.clearInputById(getEmailLocatorId());

		driver.enterTextToElementById(getPasswordLocatorId(), "zzz");
		if (isAndroid()) {
			driver.hideKeyboard();
		}
		driver.clickElementById(getSignInLocatorId());
		driver.findElementByName(getMsgSignInWithNoEmailOrCredentials());
		driver.clearInputById(getPasswordLocatorId());
		if (isAndroid()) {
			driver.hideKeyboard();
		}

		driver.clickElementById(getSignInLocatorId());
		driver.findElementByName(getMsgSignInWithNoEmailOrCredentials());
	}

	/**
	 * Verify that user is able to login to the app and logout.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void loginWithValidCredentialsTest() throws InterruptedException {
		login(driver, getEmailLocatorId(), getPasswordLocatorId(), getSignInLocatorId(), isAndroid());
		//failed on sauce lab
		driver.verifyElementText(getSignInLocatorId(), getSignINButtonChangeText());
		Thread.sleep(10000);
		logout(driver, getHeaderId_Name(), getLogoutId(), getEmailLocatorId(), isAndroid());
	}


	/**
	 * Verify that error is displayed if user enters invalid password
	 */
	@Test
	public void verifyForgotYourPasswordWithInvalidEmailIdTest() {
		driver.clickElementById(getForgotPasswordId());
		if (isAndroid()) {
			driver.enterTextToElementById(getForgotPasswordMailId_ClassName(),
					"zzz");
		} else {
			driver.enterTextToElementByClassname(
					getForgotPasswordMailId_ClassName(), "zzz");
		}
		if (isAndroid()) {
			driver.hideKeyboard();
		}
		driver.clickElementById(getOkPopUpId());
		driver.findElementByName(getMsgForgotPasswordWithWrongEmailId());
		if (isAndroid()) {
			driver.clickElementById(getCancelPopUpId());
		}

	}

	/**
	 * Verify that user is able to reset his password from app
	 * @throws InterruptedException 
	 */
	@Test
	public void verifyForgotYourPasswordWithValidEmailIdTest() throws InterruptedException {
		
		driver.clickElementById(getForgotPasswordId());
		if (isAndroid()) {
			driver.enterTextToElementById(getForgotPasswordMailId_ClassName(),
					emailId);
		} else {
			driver.enterTextToElementByClassname(
					getForgotPasswordMailId_ClassName(), emailId);
		}
		Thread.sleep(5*1000);
		driver.clickElementById(getOkPopUpId());
		driver.findElementByName(getPasswordResetEmailMessage());
		driver.clickElementById(getOkPopUpId());

	}

	

	/**
	 * Verify that EULA page opens after tapping on EULA link
	 */
	@Test
	public void verifyEULALinkTest() {
		driver.clickElementById(getEULALinkId());
		driver.verifyElementPresentByClassName(getWebViewClassName());
		if (isAndroid()) {
			driver.clickElementById(getOkPopUpId());
		} else {
			driver.clickElementById(getCloseEULAId());
		}
	}

	/**
	 * Verify that Facebook and Google buttons are present on login page
	 */
	@Test
	public void verifyFacebookAndGoogleButtonsTest() {
		driver.verifyElementPresentById(getFacebookBtnId());
		driver.verifyElementPresentById(getGmailBtnId());
	}

//	/**
//	 * Verify that a web view opens after tapping on Need an account button
//	 * @throws Throwable 
//	 */
//	@Test
//	private void verifyNeedAnAccountTest() throws Throwable {
//		//driver.scrollList("Need an account?");//(getNeedAnAccountId());
//		driver.clickElementById(getNeedAnAccountId());
//		driver.verifyElementPresentByClassName(getWebViewClassName());
//		if (isAndroid()) {
//			driver.clickElementById(getOkPopUpId());
//		} else {
//			driver.clickElementById(getCloseEULAId());
//		}
//
//	}
//
	/**
	 * Recovery Scenario for all the screens if any of the test case fails
	 * 
	 * @throws Throwable
	 */
	@AfterMethod(alwaysRun = true)
	public void recoveryScenario(ITestResult rs) throws Throwable {
		if (rs.getStatus() == 2) {
			Reporter.log("Test case "+rs.getTestName()+" failed");
			driver.launchApp();
		}
	}

}
