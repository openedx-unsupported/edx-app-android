package org.edx.androidtestsuite;

import org.edx.basetest.Offline_LoginTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups ="Android")
public class Offline_LoginTestSuite extends Offline_LoginTestSuiteBase {

	@Override
	public String getOfflineModeErrorMsg() {
		return offlineModeMsg;
	}

	@Override
	public String getSignInLocatorId() {
		return Android_btnSigninId;
	}

	@Override
	public String getEmailLocatorId() {
		return ANDROID_ID_EMAIL;
	}

	@Override
	public String getCloseEULAId() {
		return Android_btnCancelPopupId;
	}

	@Override
	public String getPasswordLocatorId() {
		return Android_tbPasswordId;
	}

	@Override
	public boolean isAndroid() {
		return true;
	}

	@Override
	public String getForgotPasswordId() {
		return Android_btnForgotPasswordId;
	}

	@Override
	public String getEULALinkId() {
		return Android_btnEULAId;
	}

	@Override
	public String getFaceBookBtnId() {
		return Android_btnfacebookId;
	}

	@Override
	public String getGmailBtnId() {
		return Android_btnGmailId;
	}

	@Override
	public String getNewUserSignUpId() {
		return Android_btnNeedAnAccountId;
	}

	@Override
	public String getForgotPasswordErrorMsgName() {
		return popUpForgotPasswordBodyName;
	}

	@Override
	public String getOkPopUpId() {
		return Android_btnOkPopupId;
	}

	@Override
	public String getTxtEULAHeaderName() {
		return txtEULAHeaderName;
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
	public String gettxtBySigningInName() {
		return txtBySigningInName;
	}

	@Override
	public String getTxtOrSignInWithName() {
		return txtOrSignInWithName;
	}
}
