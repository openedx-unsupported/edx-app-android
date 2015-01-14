package org.edx.androidtestsuite;

import org.edx.basetest.LoginTestSuiteBase;

public class LoginTestSuite extends LoginTestSuiteBase {

	@Override
	public String getEmailLocatorId() {
		return ANDROID_ID_EMAIL;
	}

	@Override
	public boolean isAndroid() {
		return true;
	}

	@Override
	public String getPasswordLocatorId() {
		return tbPasswordId;
	}

	@Override
	public String getSignInLocatorId() {
		return btnSigninId;
	}

	@Override
	public String getMsgInvalidCredentials() {
		return msgInvalidCredentials;
	}

	@Override
	public String getMsgSignInWithNoPassword() {
		return msgSignInWithNoPassword;
	}

	@Override
	public String getWebLinkId() {
		return webLinkId;
	}

	@Override
	public String getNewUserSignUpId() {
		return hlnkSignupId;
	}

	@Override
	public String getEULALinkId() {
		return hlnkEULAId;
	}

	@Override
	public String getMsgForgotPasswordWithWrongEmailId() {
		return msgForgotPasswordWithWrongEmailId;
	}

	@Override
	public String getOkPopUpId() {
		return btnOkPopupId;
	}

	@Override
	public String getForgotPasswordMailId_ClassName() {
		return tbForgotPasswordId;
	}

	@Override
	public String getForgotPasswordId() {
		return hlnkForgotPasswordId;
	}

	@Override
	public String getLogoutId() {
		return btnLogOutId;
	}

	@Override
	public String getHeaderId_Name() {
		return btnHeaderId;
	}

	@Override
	public String getMsgSignInWithNoEmailOrCredentials() {
		return msgSignInWithNoEmailOrCredentials;
	}

	@Override
	public String getCloseEULAId() {
		return null;
	}

	@Override
	public String getCancelPopUpId() {
		return btnCancelPopupId;
	}

}
