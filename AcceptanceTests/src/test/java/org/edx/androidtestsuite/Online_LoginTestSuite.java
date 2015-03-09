package org.edx.androidtestsuite;

import org.edx.basetest.LoginTestSuiteBase;
import org.testng.annotations.Test;


@Test(groups = "Android", priority=-1)
public class Online_LoginTestSuite extends LoginTestSuiteBase {

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
		return Android_tbPasswordId;
	}

	@Override
	public String getSignInLocatorId() {
		return Android_btnSigninId;
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
		return Android_webLinkId;
	}

	@Override
	public String getEULALinkId() {
		return Android_hlnkEULAId;
	}

	@Override
	public String getMsgForgotPasswordWithWrongEmailId() {
		return msgForgotPasswordWithWrongEmailId;
	}

	@Override
	public String getOkPopUpId() {
		return Android_btnOkPopupId;
	}

	@Override
	public String getForgotPasswordMailId_ClassName() {
		return Android_tbForgotPasswordId;
	}

	@Override
	public String getForgotPasswordId() {
		return Android_hlnkForgotPasswordId;
	}

	@Override
	public String getLogoutId() {
		return Android_btnLogOutId;
	}

	@Override
	public String getHeaderId_Name() {
		return Android_btnHeaderId;
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
		return Android_btnCancelPopupId;
	}

	@Override
	public String getFacebookBtnId() {
		return Android_btnfacebookId;
	}

	@Override
	public String getGmailBtnId() {
		return Android_btnGmailId;
	}

	@Override
	public String getNeedAnAccountId() {
		return Android_hlnkNeedAnAccountId;
	}

	@Override
	public String getWebViewClassName() {
		return Android_WebViewClassName;
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
		return ANDROID_ID_btnSIGNINId;
	}

	@Override
	public String getSignInTextName() {
		return Android_Name_signinText;
	}


}
