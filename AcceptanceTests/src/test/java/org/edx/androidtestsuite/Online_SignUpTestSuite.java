package org.edx.androidtestsuite;

import org.edx.basetest.SignUpTestSuiteBase;
import org.testng.annotations.Test;

@Test(groups ="Android")
public class Online_SignUpTestSuite extends SignUpTestSuiteBase {

	@Override
	public String getEdxLogoById() {
		return Android_edXLogoId;
	}

	@Override
	public String getSignUpButtonByName() {
		return Android_signUpButtonName;
	}

	@Override
	public String getSignUpTextByName() {
		return Android_signUpTextByName;
	}

	@Override
	public String getCloseButtonById() {
		return Android_closeBtnById;
	}

	@Override
	public String getCreateMyAccountById() {
		return Android_createAccountById;
	}

	@Override
	public String getCreateMyAccountByName() {
		return Android_createAccountByName;
	}

	@Override
	public String getShowOptionalFieldByName() {
		return Android_showOptionalFieldByName;
	}

	@Override
	public String getAgreeToEULAById() {
		return Android_txtAgreeToEULAById;
	}

	@Override
	public String getEULAById() {
		return Android_EULAById;
	}

	@Override
	public String getOfflineMessageHeaderByName() {
		return Android_offlineModeheaderMsg;
	}

	@Override
	public String getOfflineMessageByName() {
		return Android_offlineModeMsg;
	}

}
