package org.edx.basetest;

import org.edx.nativeapp.NativeAppDriver;

public class CommonFunctionalities extends BaseTest {

	/**
	 * Login to the app
	 * 
	 * @param driver
	 *            - NativeAppDriver instance
	 * @param email
	 *            - Id of Email id field
	 * @param password
	 *            - Id of Password field
	 * @param signInButton
	 *            - Id of Full Screen Button
	 * @param isAndroid
	 *            - Whether the device is Android or iOS
	 */
	public void login(NativeAppDriver driver, String email, String password,
			String signInButton, boolean isAndroid) {
		driver.clearInputById(email);
		if (!(email.isEmpty())) {
			driver.clearInputById(email);
		}
		driver.clearInputById(password);
		if (!(password.isEmpty())) {
			driver.clearInputById(password);
		}
		driver.enterTextToElementById(email, emailId);
		if (isAndroid) {
			driver.hideKeyboard();
		}
		driver.enterTextToElementById(password, this.password);
		if (isAndroid) {
			driver.hideKeyboard();
		}
		driver.clickElementById(signInButton);
	}

	/**
	 * Logout from the app
	 * 
	 * @param driver
	 *            - NativeAppDriver instance
	 * @param header
	 *            - Id of header button of left Navigation Bar
	 * @param logoutButton
	 *            - Id of Logout button
	 * @param emailTextBox
	 *            - Id of Email Text box
	 * @param isAndroid
	 *            - Whether the device is Android or iOS
	 * @throws InterruptedException
	 */
	public void logout(NativeAppDriver driver, String header,
			String logoutButton, String emailTextBox, boolean isAndroid)
			throws InterruptedException {
		Thread.sleep(10000);
		driver.clickElementById(header);
		driver.clickElementById(logoutButton);
		driver.clearInputById(emailTextBox);
		if (isAndroid) {
			driver.hideKeyboard();
		}
	}

	// TODO - Ask the developers to add the code for popup
	// (https://discuss.appium.io/t/android-app-not-able-to-read-elements-from-pop-up-menu/2343/3)
	/**
	 * Video player functionality
	 * 
	 * @param driver
	 *            - NativeAppDriver instance
	 * @param fullscreenButton
	 *            - Id of Full Screen Button
	 * @param lmsButton
	 *            - Id of LMS Button
	 * @param settingsButton
	 *            - Id of Settings Button
	 * @param rewindButton
	 *            - Id of Rewind Button
	 * @param videoHeader
	 *            - Id of Video header.
	 * @param seekBar
	 *            - Id of seek bar
	 * @throws InterruptedException
	 */
	public void videoPlayer(NativeAppDriver driver, String fullscreenButton,
			String lmsButton, String settingsButton, String rewindButton,
			String seekBar, String playPauseButton, String videoPlayerId,
			boolean downloaded, boolean isAndroid) throws InterruptedException {

		if (!downloaded) {
			driver.insertWait(settingsButton);
		}
		Thread.sleep(3500);
		driver.clickElementById(videoPlayerId);
		driver.verifyElementPresentById(settingsButton); 
		Thread.sleep(3500);
		driver.clickElementById(videoPlayerId);
		driver.verifyElementPresentById(rewindButton);
		Thread.sleep(3500);
		driver.clickElementById(videoPlayerId);
		driver.verifyElementPresentById(lmsButton);
		Thread.sleep(3500);
		driver.clickElementById(videoPlayerId);
		driver.verifyElementPresentById(playPauseButton);
		Thread.sleep(3500);
		driver.clickElementById(videoPlayerId);
		driver.verifyElementPresentById(fullscreenButton);
		Thread.sleep(3500);
		if (isAndroid) {
			driver.clickElementById(videoPlayerId);
			driver.clickElementById(fullscreenButton);
			driver.clickElementById(videoPlayerId);
			driver.verifyElementPresentById(rewindButton);
			driver.verifyElementPresentById(lmsButton);
			driver.verifyElementPresentById(playPauseButton);
			driver.clickElementById(videoPlayerId);
			driver.clickElementById(fullscreenButton);
		}
	}

	/**
	 * Delete Functionality
	 * 
	 * @param driver
	 *            - NativeAppDriver instance
	 * @param editButton
	 *            - Id of Edit Button
	 * @param checkboxButton
	 *            - Id of Check Box Button
	 * @param deleteButton
	 *            - Id of Delete Button
	 * @param okButton
	 *            - Id of ok Button
	 * @throws InterruptedException
	 */
	public void deleteFuctionality(NativeAppDriver driver, String editButton,
			String checkboxButton, String deleteButton, String okButton,
			int videoNumber) throws InterruptedException {
		Thread.sleep(10000);
		driver.clickElementById(editButton);
		driver.clickElementWithIndexById(checkboxButton, videoNumber);
		driver.clickElementById(deleteButton);
		driver.clickElementById(okButton);

	}

	/**
	 * Verify videoName, video size, and video length
	 * 
	 * @param driver
	 *            - NativeAppDriver instance
	 * @param videoName
	 *            - Id of video name
	 * @param videoSize
	 *            - Id of video size
	 * @param videoLength
	 *            - Id of video length
	 */
	public void videoInformation(NativeAppDriver driver, String videoName, String videoLength) {
		driver.verifyElementPresentById(videoName);
		driver.verifyElementPresentById(videoLength);
	}

}
