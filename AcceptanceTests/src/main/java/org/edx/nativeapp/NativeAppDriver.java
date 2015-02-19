package org.edx.nativeapp;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.NetworkConnectionSetting;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.edx.Config;
import org.edx.basetest.UniqueTestId;
import org.edx.utils.PropertyLoader;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;

/**
 * @author divakarpatil
 * 
 */
public class NativeAppDriver extends RemoteWebDriver {

	protected AppiumDriver appiumDriver;
	private final long maxWaitTime = Long.parseLong(PropertyLoader
			.loadProperty("max.wait.time").get());
	private final long downloadWaitTime = Long.parseLong(PropertyLoader
			.loadProperty("download.wait.time").get());
	private final String testPackageName = PropertyLoader.loadProperty(
			"test.package.name").get();
	private final String deviceOS = PropertyLoader.loadProperty("deviceOS")
			.get();
	private final String appPath = PropertyLoader.loadProperty("appPath").get();

	private final String deviceName = PropertyLoader.loadProperty("deviceName")
			.get();
	private final String appPackage = PropertyLoader.loadProperty("appPackage")
			.get();
	private final String sauceUserName = PropertyLoader.loadProperty(
			"sauceUserName").get();
	private String sauceKey = PropertyLoader.loadProperty("sauceKey").get();

	URL serverAddress;

	DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

	/**
	 * Constructor which sets the required desired capabilities for the Appium
	 * driver
	 * 
	 * @throws Throwable
	 */
	public NativeAppDriver() throws Throwable {
		super();
		if (sauceKey.isEmpty()) {
			serverAddress = new URL("http://0.0.0.0:4723/wd/hub");// appium:
																	// 4723,
																	// 4475
		} else {
			serverAddress = new URL("http://" + sauceUserName + ":" + sauceKey
					+ "@ondemand.saucelabs.com:80/wd/hub");
		}

		try {

			if (isiOS()) {
				desiredCapabilities = Config.iOS.getCapabilities();
				appiumDriver = new IOSDriver(serverAddress, desiredCapabilities);
			} else if (isAndroid()) {
				desiredCapabilities = Config.Android.getCapabilities();
				appiumDriver = new AndroidDriver(serverAddress,
						desiredCapabilities);
				
			}

		} catch (Throwable e) {
			System.out.println(">> Error while initiating Driver " + deviceOS);
			throw e;
		}

	}

	/**
	 * Find element by the given locator after proper wait
	 */
	@Override
	public NativeAppElement findElement(final By locator) {
		WebElement appElement = null;
		try {
			appElement = (new WebDriverWait(appiumDriver, maxWaitTime))
					.until(ExpectedConditions.presenceOfElementLocated(locator));

		} catch (Throwable te) {
			Reporter.log("Unable to find the element by locator: "
					+ locator.toString() + " within " + maxWaitTime + " secs");
			captureScreenshot();
			throw new TimeoutException(te);
		}
		return new NativeAppElement(this, locator, appElement);

	}

	public boolean verifyElementText(By by, String text) {
		boolean result = false;
		String actualText = "";
		int attempt = 0;
		NativeAppElement webElment = findElement(by);
		while (!result && attempt <= 5) {
			try {

				attempt++;
				WebDriverWait wait = (WebDriverWait) new WebDriverWait(
						appiumDriver, maxWaitTime / 5, 500);
				wait.until(ExpectedConditions.presenceOfElementLocated(by));
				wait.until(ExpectedConditions.visibilityOfElementLocated(by));
				// Adding support for textarea
				if (webElment.getTagName().equalsIgnoreCase("input")
						|| webElment.getTagName().equalsIgnoreCase("textarea")) {
					actualText = webElment.getAttribute("value");
				} else {
					actualText = webElment.readInnerText();
				}

			} catch (Exception e) {
				System.err.println("attempt " + attempt + "...");
				if (attempt >= 5) {
					Reporter.log("Unable to get the text by locator "
							+ by.toString());
					captureScreenshot();
					throw new WebDriverException(e);
				}
			}

			result = actualText.equals(text);
		}
		if (result) {
			return true;
		} else {
			Reporter.log("Expected: \"" + text + "\" but found: \""
					+ actualText + "\"");

			captureScreenshot();
			throw new AssertionError("Expected: \"" + text + "\" but found: \""
					+ actualText + "\"" + "\n"
					+ Thread.currentThread().getStackTrace().toString());
		}
	}

	/**
	 * To check the given text contains by the element located by the locator
	 * 
	 * @param by
	 * @param text
	 * @return
	 */
	public boolean verifyElementTextContains(By by, String text) {
		boolean result = false;
		String actualText = "";
		int attempt = 0;
		NativeAppElement webElment = findElement(by);
		while (!result && attempt <= 5) {
			try {
				attempt++;
				WebDriverWait wait = (WebDriverWait) new WebDriverWait(
						appiumDriver, maxWaitTime, 500);
				wait.until(ExpectedConditions.presenceOfElementLocated(by));
				wait.until(ExpectedConditions.visibilityOfElementLocated(by));
				if (webElment.getTagName().equalsIgnoreCase("input")
						|| webElment.getTagName().equalsIgnoreCase("textarea")) {
					actualText = webElment.getAttribute("value");
				} else {
					actualText = webElment.readInnerText();
				}
			} catch (Exception e) {
				System.err.println("attempt " + attempt + "...");
				if (attempt > 5) {
					Reporter.log("Unable to get the text by locator "
							+ by.toString());
					captureScreenshot();
					throw new WebDriverException(e);
				}
			}
			result = actualText.contains(text);
		}
		if (result) {
			return true;
		} else {
			Reporter.log("\"" + text + "\" not found in \"" + actualText + "\"");
			captureScreenshot();
			throw new AssertionError("\"" + text + "\" not found in \""
					+ actualText + "\"" + "\n"
					+ Thread.currentThread().getStackTrace().toString());
		}
	}

	/**
	 * Captures the screenshot
	 */
	public void captureScreenshot() {
		String outputPath = PropertyLoader.loadProperty("output.path").get();

		String screenShotPath = outputPath + "/ScreenShots/";
		String fileName = generateFileName() + ".jpg";
		// Take the screenshot
		File scrFile = ((TakesScreenshot) (this.appiumDriver))
				.getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(screenShotPath + fileName));
			Reporter.log("<br> Module name: " + getCurrentTestClassName()
					+ "<br>");
			Reporter.log(" Refer to <a href=\"ScreenShots/" + fileName
					+ "\" target = \"_blank\"><img src=\"ScreenShots/"
					+ fileName + "\" width=\"50\" height=\"50\"></a><br>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates the filename for screenshot
	 * 
	 * @return
	 */
	public String generateFileName() {
		return new UniqueTestId().id + "_" + getCurrentTestClassName() + "_"
				+ getCurrentTestMethodName() + "_"
				+ getCurrentTestMethodLineNumber();
	}

	/**
	 * This will return the current test method name it is executing
	 * 
	 * @return String Test case name
	 */
	public String getCurrentTestMethodName() {
		int stackIndex = 0;
		while (stackIndex < Thread.currentThread().getStackTrace().length) {
			if (Thread.currentThread().getStackTrace()[stackIndex]
					.getMethodName().endsWith("Test")
					&& Thread.currentThread().getStackTrace()[stackIndex]
							.getClassName().startsWith(testPackageName)) {
				break;
			}
			stackIndex++;
		}
		return Thread.currentThread().getStackTrace()[stackIndex]
				.getMethodName();
	}

	/**
	 * This will return the current test class name it is executing
	 * 
	 * @return String test class name
	 */
	public String getCurrentTestClassName() {
		int stackIndex = 0;
		while (stackIndex < Thread.currentThread().getStackTrace().length) {
			if (Thread.currentThread().getStackTrace()[stackIndex]
					.getMethodName().endsWith("Test")
					&& Thread.currentThread().getStackTrace()[stackIndex]
							.getClassName().startsWith(testPackageName)) {
				break;
			}
			stackIndex++;
		}
		return Thread.currentThread().getStackTrace()[stackIndex]
				.getClassName();
	}

	/**
	 * This will return the current test method line number it is executing
	 * 
	 * @return int: Test method line number
	 */
	public int getCurrentTestMethodLineNumber() {
		int stackIndex = 0;
		while (stackIndex < Thread.currentThread().getStackTrace().length) {
			if (Thread.currentThread().getStackTrace()[stackIndex]
					.getMethodName().endsWith("Test")
					&& Thread.currentThread().getStackTrace()[stackIndex]
							.getClassName().startsWith(testPackageName)) {
				break;
			}
			stackIndex++;
		}
		return Thread.currentThread().getStackTrace()[stackIndex]
				.getLineNumber();
	}

	/**
	 * AppiumDriver close
	 */
	@Override
	public void close() {
		appiumDriver.quit();
	}

	/**
	 * Find element by ID
	 * 
	 * @param id
	 * @return
	 */
	public NativeAppElement findElementById(String id) {
		return findElement(By.id(id));
	}

	/**
	 * Find element by Name
	 * 
	 * @param name
	 * @return
	 */
	public NativeAppElement findElementByName(String name) {
		return findElement(By.name(name));
	}

	/**
	 * Find element by Xpath
	 * 
	 * @param xpath
	 * @return
	 */
	public NativeAppElement findElementByXpath(String xpath) {
		return findElement(By.xpath(xpath));
	}

	/**
	 * Find element by Class name
	 * 
	 * @param className
	 * @return
	 */
	public NativeAppElement findElementByClassName(String className) {
		return findElement(By.className(className));
	}

	public boolean verifyElementTextByName(String name, String text) {
		return this.verifyElementText(By.name(name), text);
	}

	public boolean verifyElementTextById(String id, String text) {
		return this.verifyElementText(By.id(id), text);
	}

	public boolean verifyElementNotPresntById(String id) {
		boolean found = false;
		try {
			appiumDriver.findElementById(id);
			found = true;
			Reporter.log("Element is present with id " + id);
			throw new AssertionError("Element is present with id " + id);
		} catch (Exception e) {
		}
		return found;
	}

	public boolean verifyElementNotPresntByName(String name) {
		boolean found = false;
		try {
			this.findElementByName(name);
			found = true;
		} catch (Exception e) {
		}
		return found;
	}

	/**
	 * Overridden webDriver find Elements with proper wait.
	 */
	@Override
	public List<WebElement> findElements(By locator) {

		try {
			(new WebDriverWait(appiumDriver, maxWaitTime))
					.until(ExpectedConditions
							.presenceOfAllElementsLocatedBy(locator));
		} catch (ElementNotVisibleException e) {
			Reporter.log("Element not found: " + locator.toString());
			captureScreenshot();
			throw e;
		}
		return appiumDriver.findElements(locator);

	}

	@Override
	public List<WebElement> findElementsById(String using) {
		return findElements(By.id(using));
	}

	public void clickElementWithIndexById(String id, int index) {
		// List<NativeAppElement> elements = findAllElements(By.id(id));
		try {
			findElementsById(id).get(index).click();
			// elements.get(index - 1).click();
		} catch (Exception exp) {
			captureScreenshot();
			Reporter.log("Unable to click element index: " + index
					+ " with id: " + id);
			throw exp;
		}
	}

	public void clickElement(WebElement element) {
		element.click();
	}

	public void clickElement(NativeAppElement element) {
		((JavascriptExecutor) appiumDriver).executeScript(
				"arguments[0].scrollIntoView();", element.getWebElement());
		element.getWebElement().click();
	}

	public void clickElementById(String id) {
		findElementById(id).click();
	}

	public void clickElementByXpath(String xpath) {
		findElementByXpath(xpath).click();
	}

	public void clickElementByName(String name) {
		findElementByName(name).click();
	}

	public void enterTextToElementById(String id, String text) {
		findElementById(id).sendKeys(text);
	}

	public void enterTextToElementByName(String name, String text) {
		findElementByName(name).sendKeys(text);
	}

	public void enterTextToElementByClassname(String className, String text) {
		findElementByClassName(className).sendKeys(text);
	}

	public void verifyElementPresentByName(String name) {
		findElementByName(name);
	}

	public void verifyElementPresentByXpath(String name) {
		findElementByXpath(name);
	}

	public void verifyElementPresentById(String id) {
		findElementById(id);
	}

	public void verifyElementPresentByClassName(String className) {
		findElementByClassName(className);
	}

	public void verifyPresenceOfTextLocatedById(String id, String text) {
		// findElementById(id);
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.textToBePresentInElementLocated(
					By.id(id), text));
		} catch (Throwable t) {
			Reporter.log("text " + text + " not present in id " + id);
			captureScreenshot();
			throw t;
		}
	}

	public void verifyPresenceOfTextLocatedByName(String name, String text) {
		// findElementByName(name);
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.textToBePresentInElementLocated(
					By.name(name), text));
		} catch (Throwable t) {
			Reporter.log("text " + text + " not present in name " + name);
			captureScreenshot();
			throw t;
		}
	}

	/**
	 * Accept the alert
	 */
	public void acceptAlert() {
		WebDriverWait wait = (WebDriverWait) new WebDriverWait(appiumDriver,
				maxWaitTime, 500);
		wait.until(ExpectedConditions.alertIsPresent());
		Alert alrt = appiumDriver.switchTo().alert();
		alrt.accept();
	}

	/**
	 * Verify the element is visible
	 * 
	 * @param element
	 */
	public void verifyVisibilityOfElement(WebElement element) {
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.visibilityOf(element));
		} catch (Throwable t) {
			captureScreenshot();
			throw t;
		}
	}

	/**
	 * Verifies element is visible
	 * 
	 * @param id
	 */
	public void verifyVisibilityOfElementLocatedById(String id) {
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
		} catch (Throwable t) {
			Reporter.log("Elment by Id " + id + " not visible");
			captureScreenshot();
			throw t;
		}
	}

	public void clearInputById(String id) {
		findElementById(id).clear();
	}

	public void clearInputByName(String name) {
		findElementByName(name).clear();
	}

	public void clearInputByClassname(String className) {
		findElementByClassName(className).clear();
	}

	/**
	 * Method returns the appium driver instance
	 * 
	 * @return
	 */
	public WebDriver getDriver() {
		return appiumDriver;
	}

	/**
	 * Switch between data and wifi(specific to android).
	 * 
	 * @param wifi
	 * @param data
	 * @throws InterruptedException
	 */
	public void setNetworkConnection(boolean wifi, boolean data,
			boolean airplane) throws InterruptedException {
		if (deviceOS.equalsIgnoreCase("android")) {
			NetworkConnectionSetting connectionSetting = new NetworkConnectionSetting(
					0);
			connectionSetting.setWifi(wifi);
			connectionSetting.setData(data);
			connectionSetting.setData(airplane);
			((AndroidDriver) appiumDriver)
					.setNetworkConnection(connectionSetting);
			connectionSetting = ((AndroidDriver) appiumDriver)
					.getNetworkConnection();
		} else {
			HashMap<String, Double> coords = new HashMap<String, Double>();
			JavascriptExecutor js = (JavascriptExecutor) appiumDriver;
			Dimension devicediam = appiumDriver.manage().window().getSize();
			int height = devicediam.getHeight();
			// int width = devicediam.getWidth();
			if (deviceName.equals("iPhone 6")) {
				appiumDriver.swipe(100, height, 100, 100, 500);
				Thread.sleep(3 * 1000);
				coords.put("x", new Double("" + 120));
				coords.put("y", new Double("" + 290));
				coords.put("duration", 0.5);
				js.executeScript("mobile: tap", coords);
				Thread.sleep(3 * 1000);
				coords.put("x", new Double("" + 100));
				coords.put("y", new Double("" + 100));
				coords.put("duration", 0.5);
				js.executeScript("mobile: tap", coords);

			} else {// For iPhone 5
				appiumDriver.swipe(100, height, 100, 100, 500);
				Thread.sleep(3 * 1000);
				coords.put("x", new Double("" + 100));
				coords.put("y", new Double("" + 180));
				coords.put("duration", 0.5);
				js.executeScript("mobile: tap", coords);
				Thread.sleep(3 * 1000);
				coords.put("x", new Double("" + 100));
				coords.put("y", new Double("" + 100));
				coords.put("duration", 0.5);
				js.executeScript("mobile: tap", coords);
			}
		}
	}

	/**
	 * Navigating back
	 */
	public void back() {
		appiumDriver.navigate().back();
	}

	/**
	 * Launch the app
	 */
	public void launchApp() {
		appiumDriver.closeApp();
		appiumDriver.launchApp();
		
	}

	/**
	 * Remove app(Uninstall)
	 */
	public void uninstallApp() {
		appiumDriver.removeApp(appPackage);
	}

	/**
	 * Wait till the element is displayed and swipe till the particular time
	 * slot
	 * 
	 * @param id
	 *            - id of the element
	 */
	public void swipe(String id) {
		try {
			if (isAndroid()) {
				new WebDriverWait(appiumDriver, 20).until(ExpectedConditions
						.presenceOfElementLocated(By.id(id)));
				Dimension dimension = appiumDriver.manage().window().getSize();
				int ht = dimension.height;
				int width = dimension.width;
				appiumDriver.swipe((width / 2), (ht / 4), (width / 2),
						(ht / 2), 1000);
			} else {
				new WebDriverWait(appiumDriver, 20).until(ExpectedConditions
						.presenceOfElementLocated(By.id(id)));
				if (deviceName.equalsIgnoreCase("iphone 5")) {
					appiumDriver.swipe((int) 0.1, 557, 211, 206, 500);
				} else if (deviceName.equalsIgnoreCase("iphone 6")) {
					appiumDriver.swipe((int) 0.1, 660, 50, 50, 500);
				}
			}
		} catch (Throwable e) {
			Reporter.log("Element by Id " + id + " not visible");
			captureScreenshot();
			throw e;
		}
	}

	/**
	 * Hide Keyboard (specific for android)
	 */
	public void hideKeyboard() {
		// if (sauceKey.equals("")) {
		appiumDriver.hideKeyboard();
		// }
	}

	/**
	 * verifying that text is present or not
	 * 
	 * @param id
	 *            - id of the element whose text is to be checked for.
	 * @param text
	 *            - text use for comparison.
	 * @return
	 */
	public boolean verifyElementText(String id, String text) {
		try {
			(new WebDriverWait(appiumDriver, 20)).until(ExpectedConditions
					.presenceOfElementLocated(By.id(id)));
		} catch (Exception e) {
			Reporter.log("Element not found: " + e.toString());
			captureScreenshot();
			throw e;
		}
		try {
			if (!(appiumDriver.findElementById(id).getText()).equals(text)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Reporter.log("Element not found: " + e.toString());
			captureScreenshot();
			throw e;
		}
	}

	/**
	 * Inserting wait till the element to be found is present.
	 * 
	 * @param id
	 *            : id of the element.
	 */
	public void insertWait(String id) {
		try {
			new WebDriverWait(appiumDriver, downloadWaitTime)
					.until(ExpectedConditions.invisibilityOfElementLocated(By
							.id(id)));
		} catch (Throwable e) {
			Reporter.log("Element found: " + e.toString());
			captureScreenshot();
			throw e;
		}

	}

	/**
	 * Scroll the list till item found by name
	 * 
	 * @param name
	 *            - name of the element till the scroll is to be performed
	 * @throws Throwable
	 */
	public void scrollList(String name) throws Throwable {
		int i = 0;
		Dimension dimension = appiumDriver.manage().window().getSize();
		int ht = dimension.height;
		int width = dimension.width;
		if (isAndroid()) {
			while (!verifyElementIsDisplayedById(name)) {
				appiumDriver.swipe((width / 2), (ht / 2), (width / 4),
						(ht / 4), 500);
			}
		} else {
			try {
				if (appPath.contains(".ipa")) {
					while (!(verifyElementIsDisplayedById(name))) {
						appiumDriver.swipe((width / 2), (ht / 2), (width / 4),
								(ht / 4), 500);
						i++;
						if (i > 20) {
							throw new Exception();
						}
					}
				}
			} catch (Throwable e) {
				Reporter.log("Element not found by name:" + name);
				throw e;
			}

		}
	}

	public boolean verifyElementIsDisplayedById(String id) {
		boolean found = false;
		try {
			found = appiumDriver.findElementById(id).isDisplayed();
		} catch (Exception e) {

		}
		return found;

	}

	/**
	 * Tap on wifi(only for ios)
	 */
	public void tapOnWifi() {
		System.out.println("Size of iphone 6 is "
				+ appiumDriver.manage().window().getSize());
		if (deviceName.equalsIgnoreCase("iphone 5")) {
			appiumDriver.tap(85, 85, 175, 0);
		} else if (deviceName.equalsIgnoreCase("iphone 6")) {
			appiumDriver.tap(120, 120, 278, 0);
		}
	}

	/**
	 * Verify that whether the text is present or not
	 * 
	 * @param courseWareErrorText
	 * @param text
	 * @return
	 */
	public boolean textIsPresent(String courseWareErrorText, String text) {
		if (courseWareErrorText.equals(text)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Verify that whether the element is present or not
	 * 
	 * @param id
	 * @return
	 */
	public boolean verifyElementId(String id) {
		boolean found = false;
		try {
			appiumDriver.findElementById(id);
			found = true;
		} catch (Exception e) {

		}
		return found;

	}

	private boolean isAndroid() {
		return deviceOS.equalsIgnoreCase(Config.Android.OS_NAME);
	}

	private boolean isiOS() {
		return deviceOS.equalsIgnoreCase(Config.iOS.OS_NAME);
	}

}
