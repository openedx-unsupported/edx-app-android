package org.edx;

import org.edx.utils.PropertyLoader;
import org.edx.utils.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * This class holds configurations for Android and iOS platforms.
 * @author rohan
 *
 */
public final class Config {

	/**
	 * Configurations for Android.
	 * @author rohan
	 *
	 */
	public static final class Android {
		
		public static final String OS_NAME = "android";
		
		public static DesiredCapabilities getCapabilities() {
			String appPath = PropertyLoader.loadProperty("appPath").get();
			String osVersion = PropertyLoader.loadProperty("osVersion") .get();
			String deviceName = PropertyLoader.loadProperty("deviceName").get();
			
			DesiredCapabilities cap = new DesiredCapabilities();
			
			int result = StringUtils.compareAndroidVersion(osVersion, String.valueOf(4.2));
			if (result == -1) {
				cap.setCapability("automationName", "Selendroid");
			} else {
				cap.setCapability("automationName", "Appium");
			}

			cap.setCapability("appium-version", "1.3.4");
			cap.setCapability("platformName", "Android");
			cap.setCapability("deviceName", deviceName);
			cap.setCapability("app", appPath);
			cap.setCapability("platformVersion", osVersion);
			
			cap.setCapability("capture-html", true);
			cap.setCapability("name","edX-Android-test");
			
			cap.setCapability("appPackage","org.edx.mobile");
			cap.setCapability("appActivity","org.edx.mobile.view.SplashActivity");
			cap.setCapability("newCommandTimeout", 10000);
			cap.setCapability("command-timeout", 600);
			cap.setCapability("idle-timeout", 800);
			cap.setCapability("max-duration", 10800);
			
			return cap;
		}
	}


	/**
	 * Configurations for iOS.
	 * @author rohan
	 *
	 */
	public static final class iOS {

		public static final String OS_NAME = "ios";

		public static DesiredCapabilities getCapabilities() {
			String appPath = PropertyLoader.loadProperty("appPath").get();
			String osVersion = PropertyLoader.loadProperty("osVersion") .get();
			String deviceName = PropertyLoader.loadProperty("deviceName").get();
			String appPackage = PropertyLoader.loadProperty("appPackage").get();
			String udid = PropertyLoader.loadProperty("udid").get();
			
			DesiredCapabilities cap = new DesiredCapabilities();
			
			cap.setCapability("platformName", "iOS");
			cap.setCapability("deviceName", deviceName);
			cap.setCapability("name","edX-IOS-test");
			cap.setCapability("bundleId", appPackage);
			if (appPath.contains(".ipa")) {
				cap.setCapability("udid", udid);
			}
			cap.setCapability("platformVersion", osVersion);
			cap.setCapability("app", appPath);
			cap.setCapability("newCommandTimeout", 10000);
			
			return cap;
		}
		
	}
}
