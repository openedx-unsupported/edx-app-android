package org.edx.elementlocators;

public interface ILoginLocators_Offline {
	
	public String getOfflineModeErrorMsg();

	public String getSignInLocatorId();

	public String getEmailLocatorId();

	public String getCloseEULAId();

	public String getPasswordLocatorId();

	public boolean isAndroid();
	
	public String getLogoutId();
	
	public String getHeaderId_Name();
	
	public String getForgotPasswordId();
	
	public String getEULALinkId();
	
	public String getFaceBookBtnId();
	
	public String getGmailBtnId();
	
	public String getNewUserSignUpId();
	
	public String getForgotPasswordErrorMsgName();
	
	public String getOkPopUpId();
	
	public String getTxtEULAHeaderName();
	
	public String gettxtBySigningInName();
	public String getTxtOrSignInWithName();
	
	/* Android Locator Ids */
	String Android_btnLogOutId = "org.edx.mobile:id/logout_button";
	String Android_btnHeaderId = "android:id/up";
	String Android_btnOkPopupId = "org.edx.mobile:id/positiveButton";
	String ANDROID_ID_EMAIL = "org.edx.mobile:id/email_et";
	String Android_tbPasswordId = "org.edx.mobile:id/password_et";
	String Android_btnSigninId = "org.edx.mobile:id/login_button_layout";
	String Android_btnForgotPasswordId = "org.edx.mobile:id/forgot_password_tv";
	String Android_btnEULAId = "org.edx.mobile:id/end_user_agreement_tv";
	String Android_btnNeedAnAccountId = "org.edx.mobile:id/new_user_tv";
	String Android_lbSignupId = "org.edx.mobile:id/by_signing_up_tv";
	String Android_lbNewUserId = "org.edx.mobile:id/signup_text";
	String Android_btnCancelPopupId = "org.edx.mobile:id/negativeButton";
	String Android_txtEULAHeaderId = "org.edx.mobile:id/tv_dialog_title";
	String Android_btnfacebookId="org.edx.mobile:id/img_facebook";
	String Android_btnGmailId="org.edx.mobile:id/img_google";
	

	/* iOS Locator Ids */
	String tbEmailIdiOS = "tbUserName";
	String tbPasswordIdiOS = "tbPassword";
	String btnSigninIdiOS = "btnSignIn";
	String hlnkForgotPasswordIdiOS = "lnforgot";
	String hlnkEULAIdiOS = "lnEULA";
	String hlnkSignupIdiOS = "btnNeedAnAccount";
	String btnCloseEULAIdiOS = "btnClose";
	String webLinkIdiOS = "URL";
	String btnOkPopupIdiOS = "OK";
	String btnHeaderIdiOS = "btnNavigation";
	String btnLogOutIdiOS = "btnLogout";
	String btnfacebookIdiOS = "btnFacebook";
	String btnGmailIdiOS = "btnGoogle";
	
	//Common locators
	String offlineModeMsg = "You are not connected to the Internet.";
	String popUpForgotPasswordHeaderName="Connection Error";
	String popUpForgotPasswordBodyName="You are not connected to the Internet. Please check your Internet connection.";
	String txtEULAHeaderName="END USER LICENSE AGREEMENT";
	String txtOrSignInWithName="Or Sign in with";
	String txtBySigningInName="By signing in to this app, you agree to the";
	
}
