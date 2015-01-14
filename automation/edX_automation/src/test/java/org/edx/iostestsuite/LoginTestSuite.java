package org.edx.iostestsuite;

import org.edx.basetest.LoginTestSuiteBase;

public class LoginTestSuite extends LoginTestSuiteBase {

	@Override
	public String getPasswordLocatorId() {
		return tbPasswordIdiOS;
	}

	@Override
	public boolean isAndroid() {
		return false;
	}

	@Override
	public String getSignInLocatorId() {
		return btnSigninIdiOS;
	}

	@Override
	public String getEmailLocatorId() {
		return tbEmailIdiOS;
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
		return webLinkIdiOS;
	}

	@Override
	public String getNewUserSignUpId() {
		return hlnkSignupIdiOS;
	}

	@Override
	public String getEULALinkId() {
		return hlnkEULAIdiOS;
	}

	@Override
	public String getMsgForgotPasswordWithWrongEmailId() {
		return msgForgotPasswordWithWrongEmailId;
	}

	@Override
	public String getOkPopUpId() {
		return btnOkPopupIdiOS;
	}

	@Override
	public String getForgotPasswordId() {
		return hlnkForgotPasswordIdiOS;
	}

	@Override
	public String getLogoutId() {
		return btnLogOutIdiOS;
	}

	@Override
	public String getHeaderId_Name() {
		return btnHeaderIdiOS;
	}

	@Override
	public String getMsgSignInWithNoEmailOrCredentials() {
		return msgSignInWithNoEmailOrCredentials;
	}

	@Override
	public String getForgotPasswordMailId_ClassName() {
		return tbForgotPasswordClassnameiOS;
	}

	@Override
	public String getCloseEULAId() {
		return btnCloseEULAIdiOS;
	}

	@Override
	public String getCancelPopUpId() {
		return null;
	}
}
