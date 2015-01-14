/**
 * 
 */
package org.edx.elementlocators;

/**
 * @author divakarpatil
 * 
 */
public interface ILoginLocators {

	public String getWebLinkId();

	public String getNewUserSignUpId();

	public String getEULALinkId();

	public String getMsgForgotPasswordWithWrongEmailId();

	public String getOkPopUpId();

	public String getForgotPasswordMailId_ClassName();

	public String getForgotPasswordId();

	public String getLogoutId();

	public String getHeaderId_Name();

	public String getMsgSignInWithNoEmailOrCredentials();

	public String getMsgSignInWithNoPassword();

	public String getMsgInvalidCredentials();

	public String getPasswordLocatorId();

	public boolean isAndroid();

	public String getSignInLocatorId();

	public String getEmailLocatorId();

	public String getCloseEULAId();

	public String getCancelPopUpId();

	String ANDROID_ID_EMAIL = "org.edx.mobile:id/email_et";
	String tbPasswordId = "org.edx.mobile:id/password_et";
	String btnSigninId = "org.edx.mobile:id/login_button_layout";
	String hlnkForgotPasswordId = "org.edx.mobile:id/forgot_password_tv";
	String btnHeaderId = "android:id/action_bar_title";
	String hlnkEULAId = "org.edx.mobile:id/end_user_agreement_tv";
	String hlnkSignupId = "org.edx.mobile:id/new_user_tv";
	String lbSignupId = "org.edx.mobile:id/by_signing_up_tv";
	String lbNewUserId = "org.edx.mobile:id/signup_text";//
	String btnLogOutId = "org.edx.mobile:id/logout_button";
	String tbForgotPasswordId = "org.edx.mobile:id/email_edit";
	String btnOkPopupId = "org.edx.mobile:id/positiveButton";
	String btnCancelPopupId = "org.edx.mobile:id/negativeButton";
	String txtEULAHeaderId = "org.edx.mobile:id/tv_dialog_title";
	String webLinkId = "com.android.chrome:id/url_bar";

	/* iOS Locator Ids */
	String tbEmailIdiOS = "tbUserName";
	String tbPasswordIdiOS = "tbPassword";
	String btnSigninIdiOS = "btnSignIn";
	String hlnkForgotPasswordIdiOS = "lnforgot";
	String hlnkEULAIdiOS = "lnEULA";
	String hlnkSignupIdiOS = "btnNewUser";
	String btnCloseEULAIdiOS = "btnCloseEULA";
	String btnOkPopupIdiOS = "OK";
	String btnCancelPopupIdiOS = "Cancel";
	String tbForgotPasswordClassnameiOS = "UIATextField";
	String webLinkIdiOS = "URL";
	String btnHeaderIdiOS = "btn";
	String btnLogOutIdiOS = "btnLogout";
	
	String msgInvalidCredentials = "Please make sure that your user name or e-mail address and password are correct and try again.";
	String msgSignInWithNoPassword = "Please enter your password and try again.";
	String msgSignInWithNoEmailOrCredentials = "Please enter your user name or e-mail address and try again.";
	String msgForgotPasswordWithWrongEmailId = "Please make sure your e-mail address is formatted correctly and try again.";

}
