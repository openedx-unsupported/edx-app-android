package org.edx.basetest;

import java.io.IOException;

import org.edx.nativeapp.NativeAppDriver;
import org.edx.utils.FileGenerator;
import org.edx.utils.PropertyLoader;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeTest;

public abstract class BaseTest {

	/*
	 * This unique string is used to give unique name in tests
	 */
	protected String uniqueID = (new UniqueTestId()).id;
	protected static NativeAppDriver driver;
	public String emailId = PropertyLoader.loadProperty("emailId").get();
	public String password = PropertyLoader.loadProperty("password").get();
	public String appPath= PropertyLoader.loadProperty("appPath").get();
	
	/**
	 * Initiate driver if null, else return the current driver
	 * 
	 * @return nativeAppDriver
	 * @throws Throwable
	 */
	@BeforeTest(groups = {"Android", "iOS"})
	public static NativeAppDriver getNativeAppDriver() throws Throwable {
		if (driver == null) {
			driver = new NativeAppDriver();
		}
		return driver;
	}

	/**
	 * 1) This will uninstall the app.
	 * 2) This will create an index.html file in output folder to list down the
	 * test runs.
	 * 
	 * @throws IOException
	 */
	@AfterSuite(alwaysRun = true, groups = {"Android", "iOS"})
	public void createIndexFile() throws IOException {
		//driver.uninstallApp();
		String root = PropertyLoader.loadProperty("output.path").get();
		String parentFolder = root + "/..";
		FileGenerator.generateIndexHTML(parentFolder.toString());
		String test_ReportDir = parentFolder + "/..";
		FileGenerator.generateIndexHTML(test_ReportDir);
	}
}
