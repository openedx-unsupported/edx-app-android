package org.edx.nativeapp;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.edx.basetest.UniqueTestId;
import org.edx.utils.PropertyLoader;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.mobile.NetworkConnection;
import org.openqa.selenium.mobile.NetworkConnection.ConnectionType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;


public class NativeAppDriver extends RemoteWebDriver{

	protected AppiumDriver appiumDriver;
	private final long maxWaitTime = Long.parseLong(PropertyLoader
			.loadProperty("max.wait.time").get());
	private final String testPackageName = PropertyLoader.loadProperty(
			"test.package.name").get();
	private final String deviceOS = PropertyLoader.loadProperty("deviceOS")
			.get();
	private final String appPath = PropertyLoader.loadProperty("appPath").get();
	private final String osVersion = PropertyLoader.loadProperty("osVersion")
			.get();
	private final String deviceName = PropertyLoader.loadProperty("deviceName")
			.get();
	private final String appPackage = PropertyLoader.loadProperty("appPackage")
			.get();
	private final String udid = PropertyLoader.loadProperty("udid").get();
	
	
	@SuppressWarnings("static-access")
	public NativeAppDriver() throws Throwable {
		super();
		
		try {
			DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
			if (deviceOS.equalsIgnoreCase("ios")) {
				desiredCapabilities.setCapability("platformName", "ios");
				desiredCapabilities.setCapability("deviceName", deviceName);
				desiredCapabilities.setCapability("bundleId", appPackage);
				desiredCapabilities.setCapability("udid", udid);
				desiredCapabilities.setCapability("platformVersion", osVersion);
				desiredCapabilities.setCapability("app", appPath);
				//desiredCapabilities.setCapability("autoAcceptAlerts", true);
				desiredCapabilities.safari();
				desiredCapabilities.setCapability("newCommandTimeout", 1000);
			} else if (deviceOS.equalsIgnoreCase("android")) {
				desiredCapabilities.setCapability("platformName", "android");
				desiredCapabilities.setCapability("deviceName", deviceName);
				desiredCapabilities.setCapability("app", appPath);
				desiredCapabilities.setCapability("platformVersion", osVersion);
			}
			URL serverAddress = new URL("http://127.0.0.1:4723/wd/hub");
			//driver = new RemoteWebDriver(serverAddress,desiredCapabilities);
			
			appiumDriver = new AppiumDriver(serverAddress, desiredCapabilities) {
				@Override
				public MobileElement scrollToExact(String arg0) {
					return null;
				}

				@Override
				public MobileElement scrollTo(String arg0) {
					return null;
				}
				
			};
			

		} catch (Throwable e) {
			e.printStackTrace();
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
		String outputPath = null;
		// if tests are running by maven then
		if (!PropertyLoader.loadProperty("output.path").get()
				.contains("timestamp")) {
			outputPath = PropertyLoader.loadProperty("output.path").get();
		} else {
			// It is running by main function
			
		}
		String screenShotPath = outputPath + "\\ScreenShots\\";
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
	private String generateFileName() {
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
	 * @param id
	 * @return
	 */
	public NativeAppElement findElementByName(String name) {
		return findElement(By.name(name));
	}

	/**
	 * Find element by css selector
	 * 
	 * @param id
	 * @return
	 */
	public NativeAppElement findElementByCSS(String css) {
		return findElement(By.cssSelector(css));
	}

	/**
	 * Find element by Xpath
	 * 
	 * @param id
	 * @return
	 */
	public NativeAppElement findElementByXpath(String xpath) {
		return findElement(By.xpath(xpath));
	}

	/**
	 * Find element by Class name
	 * 
	 * @param id
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

	public boolean verifyElementTextByCss(String css, String text) {
		return this.verifyElementText(By.cssSelector(css), text);
	}

	public boolean verifyElementTextByXpath(String xpath, String text) {
		return this.verifyElementText(By.xpath(xpath), text);
	}

	public boolean verifyElementTextByClassName(String className, String text) {
		return this.verifyElementText(By.className(className), text);
	}

	public boolean verifyElementNotPresntById(String id) {
		boolean found = false;
		try {
			this.findElementById(id);
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
			Reporter.log("Element is present with name " + name);
			throw new AssertionError("Element is present with name " + name);
		} catch (Exception e) {
		}
		return found;
	}

	public boolean verifyElementNotPresntByXpath(String xpath) {
		boolean notFound = true;
		try {
			this.findElementByXpath(xpath);
			notFound = false;
			Reporter.log("Element is present with xpath " + xpath);
			throw new AssertionError("Element is present with xpath " + xpath);
		} catch (Exception e) {
		}
		return notFound;
	}

	public boolean verifyElementNotPresntByCss(String css) {
		boolean found = false;
		try {
			this.findElementByCSS(css);
			found = true;
			Reporter.log("Element is present with css " + css);
			throw new AssertionError("Element is present with css " + css);
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
			(new WebDriverWait(appiumDriver, maxWaitTime)).until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
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
		//List<NativeAppElement> elements = findAllElements(By.id(id));
		try {
			findElementsById(id).get(index).click();
			//elements.get(index - 1).click();
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

	public void clickElementByName(String name) {
		findElementByName(name).click();
	}

	public void clickElementByCss(String css) {
		findElementByCSS(css).click();
	}

	public void clickElementByXpath(String xpath) {
		findElementByXpath(xpath).click();
	}

	public void clickElementByClassname(String className) {
		findElementByClassName(className).click();
	}

	public void clickElementByTagName(String tag) {
		findElementByTagName(tag).click();
	}

	public void clickElementByLinkText(String link) {
		findElementByLinkText(link).click();
	}

	public void clickElementByPartialLinkText(String link) {
		findElementByPartialLinkText(link).click();
	}

	public void enterTextToElementById(String id, String text) {
		findElementById(id).sendKeys(text);
		//appiumDriver.hideKeyboard();
	}

	public void enterTextToElementByName(String name, String text) {
		findElementByName(name).sendKeys(text);
	}

	public void enterTextToElementByCss(String css, String text) {
		findElementByCSS(css).sendKeys(text);
	}

	public void enterTextToElementByXpath(String xpath, String text) {
		findElementByXpath(xpath).sendKeys(text);
	}

	public void enterTextToElementByClassname(String className, String text) {
		findElementByClassName(className).sendKeys(text);
	}

	/**
	 * Enter text in the input in the specified index. Index starts from 1.
	 * 
	 * @param xpath
	 * @param text
	 * @param index
	 */
	public void enterTextToElementInIndexByXpath(String xpath, String text,
			int index) {
		findElements(By.xpath(xpath)).get(index - 1).clear();
		findElements(By.xpath(xpath)).get(index - 1).sendKeys(text);
	}

	
	public void verifyElementPresentByName(String name) {
		findElementByName(name);
	}

	public void verifyElementPresentByCss(String css) {
		findElementByCSS(css);
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

	public void verifyPresenceOfTextLocatedByXpath(String xpath, String text,
			long waitTime) {
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, waitTime, 500);
			wait.until(ExpectedConditions.textToBePresentInElementLocated(
					By.xpath(xpath), text));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By
					.xpath(xpath)));
		} catch (StaleElementReferenceException sRExp) {
			this.navigate().refresh();
			verifyPresenceOfTextLocatedByXpath(xpath, text, waitTime);
		} catch (WebDriverException wbExp) {
			Reporter.log("text " + text + " not present in xpath " + xpath
					+ "after " + waitTime + "secs");
			captureScreenshot();
			throw wbExp;
		} catch (Throwable t) {
			Reporter.log("text " + text + " not present in xpath " + xpath
					+ "after " + waitTime + "secs");
			captureScreenshot();
			throw t;
		}
	}

	public void verifyPresenceOfTextLocatedByXpath(String xpath, String text) {
		verifyPresenceOfTextLocatedByXpath(xpath, text, maxWaitTime);
	}

	public void verifyPresenceOfTextLocatedByCss(String css, String text) {
		// findElementByCSS(css);
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.textToBePresentInElementLocated(
					By.cssSelector(css), text));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By
					.cssSelector(css)));
		} catch (Throwable t) {
			Reporter.log("text " + text + " not present in css selector " + css
					+ ". Found: " + findElementByCSS(css).readInnerText());
			captureScreenshot();
			throw t;
		}
	}

	public void verifyPresenceOfTextLocatedByClassName(String className,
			String text) {
		// findElementByClassName(className);
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.textToBePresentInElementLocated(
					By.className(className), text));
		} catch (Throwable t) {
			Reporter.log("text " + text + " not present in Class Name "
					+ className);
			captureScreenshot();
			throw t;
		}
	}

	public void acceptAlert() {
		WebDriverWait wait = (WebDriverWait) new WebDriverWait(appiumDriver,
				maxWaitTime, 500);
		wait.until(ExpectedConditions.alertIsPresent());
		Alert alrt = appiumDriver.switchTo().alert();
		alrt.accept();
	}

	public void enterTextInPrompt(String text) {
		WebDriverWait wait = (WebDriverWait) new WebDriverWait(appiumDriver,
				maxWaitTime, 500);
		wait.until(ExpectedConditions.alertIsPresent());
		Alert prmpt = appiumDriver.switchTo().alert();
		prmpt.sendKeys(text);
		prmpt.accept();
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
	 * @param xpath
	 */
	public void verifyVisibilityOfElementLocatedByXpath(String xpath) {
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By
					.xpath(xpath)));
		} catch (Throwable t) {
			Reporter.log("Elment by xpath " + xpath + " not visible");
			captureScreenshot();
			throw t;
		}
	}

	/**
	 * Verifies element is visible
	 * 
	 * @param xpath
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

	/**
	 * Verifies element is visible
	 * 
	 * @param xpath
	 */
	public void verifyVisibilityOfElementLocatedByCss(String selector) {
		try {
			WebDriverWait wait = (WebDriverWait) new WebDriverWait(
					appiumDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By
					.cssSelector(selector)));
		} catch (Throwable t) {
			Reporter.log("Elment by Css " + selector + " not visible");
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

	public void clearInputByXpath(String xpath) {
		findElementByXpath(xpath).clear();
	}

	public void clearInputByCss(String css) {
		findElementByCSS(css).clear();
	}

	public void clearInputByClassname(String className) {
		findElementByClassName(className).clear();
	}

	/**
	 * Verify element present by xpath
	 * 
	 * @param linkText
	 */
	public void verifyElementPresentByXpath(String xpath) {
		findElementByXpath(xpath);
	}

	public WebDriver getDriver() {
		return appiumDriver;
	}
	
	
	public void setNetworkConnection(boolean wifi, boolean data) {
		NetworkConnection connection=(NetworkConnection) appiumDriver;
			if(wifi&&!data){
				connection.setNetworkConnection(ConnectionType.WIFI);	
			}else if(!wifi&&data){
				connection.setNetworkConnection(ConnectionType.DATA);
			}else if(!wifi&&!data){
				connection.setNetworkConnection(ConnectionType.AIRPLANE_MODE);
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
	 * Wait till the element is displayed and swipe till the particular time slot
	 * 
	 * @param name
	 */
	public void swipe(String name) {
		try{
		new WebDriverWait(appiumDriver, 20).until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
		appiumDriver.swipe(360, 500, 360, 1000, 80000);
		} catch(Throwable e){
			throw e;
		}
		}
	

	/**
	 * Hide Keyboard
	 */
	public void hideKeyboard() {
		appiumDriver.hideKeyboard();
		//appiumDriver.switchTo().
	}
	
	/**
	 * verifying that text is present or not
	 * 
	 * @param id
	 *          - id of the element whose text is to be checked for.
	 * @param text
	 *          - text use for comparison.
	 * @return
	 */
	public boolean verifyElementText(String id, String text) {
		try {
			(new WebDriverWait(appiumDriver, 20)).until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
		} catch (Exception e) {
			Reporter.log("Element not found: " + e.toString());
			captureScreenshot();
			throw e;
		}
		if (!(appiumDriver.findElementById(id).getText()).equals(text)) {
			return true;
		} else {
			Reporter.log("Expected: \"" + text + "\" found \"");
			captureScreenshot();
			throw new AssertionError("Expected: \"" + text + "\" found \"" + Thread.currentThread().getStackTrace().toString());
		}
	}


	public void insertWait(String id) {
	
		try {
			new WebDriverWait(appiumDriver, 300).until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
		} catch (Throwable e) {
			Reporter.log("Element not found: " + e.toString());
			captureScreenshot();
			throw e;
		}
		
	}
	
}
