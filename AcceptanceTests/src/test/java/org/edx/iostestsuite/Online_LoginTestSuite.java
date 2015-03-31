package org.edx.iostestsuite;

import org.edx.basetest.LoginTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups = "iOS")
public class Online_LoginTestSuite extends LoginTestSuiteBase {

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
		return btnCloseIdiOS;
	}

	@Override
	public String getCancelPopUpId() {
		return btnCancelPopupIdiOS;
	}

	@Override
	public String getFacebookBtnId() {
		return btnfacebookIdiOS;
	}

	@Override
	public String getGmailBtnId() {
		return btnGmailIdiOS;
	}

	@Override
	public String getNeedAnAccountId() {
		return hlnkNeedAnAccountIdiOS;
	}

	@Override
	public String getWebViewClassName() {
		return webViewClassNameiOS;
	}

	@Override
	public String getMyCoursesName() {
		return txtMyCoursesName;
	}

	@Override
	public String getSignINButtonChangeText() {
		return btnSignInChangingName;
	}

	@Override
	public String getPasswordResetEmailMessage() {
		return msgForgotPasswordWithCorrectEmailId;
	}

	@Override
	public String getSignInButtonId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSignInTextName() {
		// TODO Auto-generated method stub
		return null;
	}
}
