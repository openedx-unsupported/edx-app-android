package org.edx.nativeapp;

import java.util.List;

import org.edx.utils.PropertyLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;


public class NativeAppElement extends RemoteWebElement{
	public RemoteWebElement getWebElement() {
		return this.webElement;
	}

	public NativeAppDriver getCateredWebDriver() {
		return this.nativeAppDriver;
	}

	private RemoteWebElement webElement;
	private final By byLocator;
	private final NativeAppDriver nativeAppDriver;
	private final long maxWaitTime = Long.parseLong(PropertyLoader.loadProperty("max.wait.time").get());

	public NativeAppElement(NativeAppDriver nativeAppDriver, By byLocator, WebElement webElement) {
		this.webElement = (RemoteWebElement) webElement;
		this.nativeAppDriver = nativeAppDriver;
		this.byLocator = byLocator;
	}

	public By getByLocator() {
		return byLocator;
	}

	@Override
	public void clear() {
		webElement.clear();
	}

	@Override
	public void click() {
		try {
			WebDriverWait wait = new WebDriverWait(this.nativeAppDriver, maxWaitTime, 500);
			wait.until(ExpectedConditions.presenceOfElementLocated(this.getByLocator()));
			webElement.click();
		} catch (Exception e) {
			Reporter.log("Unable to click element by " + this.getByLocator().toString());
			this.getCateredWebDriver().captureScreenshot();
			throw new WebDriverException(e);
		}
	}

	@Override
	public WebElement findElement(By loc) {
		WebElement innerElement = webElement.findElement(loc);
		
		((JavascriptExecutor) nativeAppDriver.getDriver()).executeScript("arguments[0].scrollIntoView();", innerElement);

		return innerElement;
	}

	@Override
	public List<WebElement> findElements(By loc) {
		return webElement.findElements(loc);
	}

	@Override
	public String getAttribute(String atr) {
		long maxWaitTimeInMilis = maxWaitTime * 1000;
		String innerText = webElement.getAttribute(atr);
		while (innerText.isEmpty() && maxWaitTimeInMilis > 0) {
			try {
				Thread.sleep(500);
				System.out.println("Time remaining " + maxWaitTimeInMilis);
				maxWaitTimeInMilis = maxWaitTimeInMilis - 500;
			} catch (Exception e) {
				e.printStackTrace();
			}
			innerText = webElement.getAttribute(atr);
		}
		return innerText;
	}

	@Override
	public String getCssValue(String atr) {
		return webElement.getCssValue(atr);
	}

	@Override
	public Point getLocation() {
		return webElement.getLocation();
	}

	@Override
	public Dimension getSize() {
		return webElement.getSize();
	}

	@Override
	public String getTagName() {
		return webElement.getTagName();
	}

	@Override
	public String getText() {
		return webElement.getText();
	}

	public String readInnerText() {
		long maxWaitTimeInMilis = maxWaitTime * 1000;
		boolean textRead = false;
		String innerText = "";
		while (!textRead && maxWaitTimeInMilis > 0 && innerText.isEmpty()) {
			try {
				innerText = webElement.getText();
				textRead = true;
			} catch (Exception e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				maxWaitTimeInMilis = maxWaitTimeInMilis - 500;
				System.out.println("Time remaining..." + maxWaitTimeInMilis);
			}
		}
		return innerText;
	}

	@Override
	public boolean isDisplayed() {
		return webElement.isDisplayed();
	}

	@Override
	public boolean isEnabled() {
		return webElement.isEnabled();
	}

	@Override
	public boolean isSelected() {
		return webElement.isSelected();
	}

	@Override
	public void sendKeys(CharSequence... arg0) {
		int attemp = 1;
		boolean typeSucess = false;
		while (!typeSucess && attemp <= 5) {
			try {
				WebDriverWait wait = new WebDriverWait(this.nativeAppDriver, maxWaitTime, 500);
				wait.until(ExpectedConditions.presenceOfElementLocated(this.getByLocator()));
				attemp++;
				webElement.sendKeys(arg0);
				typeSucess = true;
			} catch (Exception e) {
				if (attemp > 5) {
					Reporter.log("Unable to enter text into element by " + this.getByLocator().toString());
					this.getCateredWebDriver().captureScreenshot();
					throw e;
				}
			}
		}
	}

	@Override
	public void submit() {
		webElement.submit();
	}
	
	
	
}
