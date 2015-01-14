package org.edx.basetest;

import java.io.IOException;

import org.edx.nativeapp.NativeAppDriver;
import org.edx.utils.FileGenerator;
import org.edx.utils.PropertyLoader;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeTest;


public class BaseTest{

	/*
	 * This unique string is used to give unique name in tests
	 */
	protected String uniqueID = (new UniqueTestId()).id;
	protected static NativeAppDriver driver;

	/**
	 * Initiate driver if null, else return the current driver 
	 * @return nativeAppDriver
	 * @throws Throwable
	 */
	@BeforeTest
	public static NativeAppDriver getNativeAppDriver() throws Throwable {
		if (driver == null) {
			driver = new NativeAppDriver();
		}
		return driver;
		
	}
	
	
	/**
	 * This will create an index.html file in output folder to list down the
	 * test runs.
	 * 
	 * @throws IOException
	 */
	@AfterSuite(alwaysRun = true)
	public void createIndexFile() throws IOException {
		String root = PropertyLoader.loadProperty("output.path").get();
		String parentFolder = root + "/..";
		FileGenerator.generateIndexHTML(parentFolder.toString());
		String test_ReportDir = parentFolder + "/..";
		FileGenerator.generateIndexHTML(test_ReportDir);
	}
	
	/**
	 * Recovery Scenario for all the screens if any of the test case fails
	 * @throws Throwable
	 */
	public void recoveryScenario() throws Throwable {
		driver.close();
		driver=new NativeAppDriver();

	}

}
