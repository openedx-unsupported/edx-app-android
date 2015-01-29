package org.edx.iostestsuite;

import org.edx.basetest.Offline_LoginTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups ="iOS" )
public class Offline_LoginTestSuite extends Offline_LoginTestSuiteBase {

	@Override
	public String getOfflineModeErrorMsg() {
		return offlineModeMsg;
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
	public String getCloseEULAId() {
		return btnCloseEULAIdiOS;
	}

	@Override
	public String getPasswordLocatorId() {
		return tbPasswordIdiOS;
	}

	@Override
	public boolean isAndroid() {
		return false;
	}

	@Override
	public String getForgotPasswordId() {
		return hlnkForgotPasswordIdiOS;
	}

	@Override
	public String getEULALinkId() {
		return hlnkEULAIdiOS;
	}

	@Override
	public String getFaceBookBtnId() {
		return btnfacebookIdiOS;
	}

	@Override
	public String getGmailBtnId() {
		return btnGmailIdiOS;
	}

	@Override
	public String getNewUserSignUpId() {
		return hlnkSignupIdiOS;
	}

	@Override
	public String getForgotPasswordErrorMsgName() {
		return popUpForgotPasswordBodyName;
	}

	@Override
	public String getOkPopUpId() {
		return btnOkPopupIdiOS;
	}

	@Override
	public String getTxtEULAHeaderName() {
		return txtEULAHeaderName;
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
	public String gettxtBySigningInName() {
		return txtBySigningInName;
	}

	@Override
	public String getTxtOrSignInWithName() {
		return txtOrSignInWithName;
	}

}
