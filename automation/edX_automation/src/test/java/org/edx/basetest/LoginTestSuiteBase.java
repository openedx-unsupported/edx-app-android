package org.edx.basetest;

import java.io.IOException;

import org.edx.elementlocators.ILoginLocators;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class LoginTestSuiteBase extends BaseTest implements ILoginLocators {

	boolean recovery = false;

	@BeforeMethod
	public void recoveryMethod() throws Throwable {
		if (recovery)
			recoveryScenario();
	}

	/**
	 * Verify that error message is shown if user tries to login with Invalid
	 * credentials
	 */
	@Test
	public void loginWithInvalidCredentials() {
		try {
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
			driver.clearInputById(getEmailLocatorId());
			driver.clearInputById(getPasswordLocatorId());

			driver.enterTextToElementById(getEmailLocatorId(), "zzz");
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			driver.enterTextToElementById(getPasswordLocatorId(), "");
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			driver.clickElementById(getSignInLocatorId());
			driver.findElementByName(getMsgSignInWithNoPassword());
			driver.clearInputById(getEmailLocatorId());

			driver.enterTextToElementById(getEmailLocatorId(), "");
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			driver.enterTextToElementById(getPasswordLocatorId(), "zzz");
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			driver.clickElementById(getSignInLocatorId());
			driver.findElementByName(getMsgSignInWithNoEmailOrCredentials());
			driver.clearInputById(getPasswordLocatorId());

			driver.enterTextToElementById(getEmailLocatorId(), "");
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			driver.enterTextToElementById(getPasswordLocatorId(), "");
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			driver.clickElementById(getSignInLocatorId());
			driver.findElementByName(getMsgSignInWithNoEmailOrCredentials());
			recovery = false;
		} catch (Throwable e) {
			recovery = true;
			throw e;
		}
	}

	/**
	 * Verify that user is able to logout of the app
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void loginWithValidCredentialsTest() throws InterruptedException {

		try {
			driver.clearInputById(getEmailLocatorId());
			driver.enterTextToElementById(getEmailLocatorId(), "divakar51");
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			driver.enterTextToElementById(getPasswordLocatorId(), "edx");
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			driver.clickElementById(getSignInLocatorId());
			Thread.sleep(10000);
			if (isAndroid()) {
				driver.clickElementById(getHeaderId_Name());
			} else {
				driver.clickElementById(getHeaderId_Name());
			}
			driver.clickElementById(getLogoutId());
			driver.clearInputById(getEmailLocatorId());
			if (isAndroid()) {
				driver.hideKeyboard();
			}
			recovery = false;
		} catch (Throwable e) {
			recovery = true;
			throw e;
		}
	}

	/**
	 * Verify that error is displayed if user enters invalid password
	 */
	@Test
	public void verifyForgotYourPasswordWithInvalidEmailIdTest() {

		try {
			driver.clickElementById(getForgotPasswordId());
			if (isAndroid()) {
				driver.enterTextToElementById(
						getForgotPasswordMailId_ClassName(), "zzz");
			} else {
				driver.enterTextToElementByClassname(
						getForgotPasswordMailId_ClassName(), "zzz");
			}
			if (isAndroid()) {
				driver.clickElementById(getOkPopUpId());
			} else {
				driver.clickElementById(getOkPopUpId());
			}
			driver.findElementByName(getMsgForgotPasswordWithWrongEmailId());
			if(isAndroid()){
				driver.clickElementById(getCancelPopUpId());
			}
			recovery = false;
		} catch (Throwable e) {
			recovery = true;
			throw e;
		}

	}

	/**
	 * Verify that user is able to reset his password from app
	 */
	@Test
	public void verifyForgotYourPasswordWithValidEmailIdTest() {
		try {
			if(isAndroid()){
				driver.hideKeyboard();
			}
			driver.clickElementById(getForgotPasswordId());
			if (isAndroid()) {
				driver.enterTextToElementById(
						getForgotPasswordMailId_ClassName(),
						"divakar.patil@claricetechnologies.com");
			} else {
				driver.enterTextToElementByClassname(
						getForgotPasswordMailId_ClassName(),
						"divakar.patil@claricetechnologies.com");
			}
			driver.clickElementById(getOkPopUpId());
			driver.findElementByName("PASSWORD RESET E-MAIL SENT");
			driver.clickElementById(getOkPopUpId());
			recovery = false;
		} catch (Throwable e) {
			recovery = true;
			throw e;
		}
	}

	/**
	 * Verify that EULA page opens after tapping on EULA link
	 */
	@Test
	public void verifyEULALinkTest() {
		try {
			driver.clickElementById(getEULALinkId());
			if (isAndroid()) {
				driver.clickElementById(getOkPopUpId());
			} else {
				driver.clickElementById(getCloseEULAId());
			}
			recovery = false;
		} catch (Throwable e) {
			recovery = true;
			throw e;
		}
	}

	/**
	 * Verify that correct link opens after tapping on New User? sign up link
	 */
	@Test
	public void verifyNewUserSignupTest() throws IOException {
		try {
			driver.clickElementById(getNewUserSignUpId());
			if (isAndroid()) {
				driver.clickElementById(getWebLinkId());
				driver.verifyElementTextById(getWebLinkId(),
						"https://courses.edx.org/register");
			} else {
				// work in progress
			}
			driver.launchApp();
			recovery = false;
		} catch (Throwable e) {
			recovery = true;
			throw e;
		}
	}


}
