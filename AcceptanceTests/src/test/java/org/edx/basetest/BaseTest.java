package org.edx.basetest;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.edx.nativeapp.NativeAppDriver;
import org.edx.utils.FileGenerator;
import org.edx.utils.PropertyLoader;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeTest;



public abstract class BaseTest {

	public final static Logger testLogger = Logger.getLogger(BaseTest.class);
	/*
	 * This unique string is used to give unique name in tests
	 */
	protected String uniqueID = (new UniqueTestId()).id;
	protected static NativeAppDriver driver;
	public String emailId = PropertyLoader.loadProperty("emailId").get();
	public String password = PropertyLoader.loadProperty("password").get();
	public String appPath = PropertyLoader.loadProperty("appPath").get();

	/**
	 * Initiate driver if null, else return the current driver
	 * 
	 * @return nativeAppDriver
	 * @throws Throwable
	 */
	@BeforeTest
	public static  NativeAppDriver getNativeAppDriver() throws Throwable {
		if (driver == null) {
			driver = new NativeAppDriver();
		}
		return driver;
	}

	/**
	 * 1) This will uninstall the app. 2) This will create an index.html file in
	 * output folder to list down the test runs.
	 * 
	 * @throws IOException
	 * @throws MessagingException
	 * @throws AddressException
	 */
	@AfterSuite(alwaysRun = true, groups = { "Android", "iOS" })
	public void createIndexFile(ITestContext testContext) throws IOException{

		String root = PropertyLoader.loadProperty("output.path").get();
		testLogger.info("report path " + root);
		int pathLevel = root.split("/").length;
		String reportFolder = root.split("/")[pathLevel - 1];
		int numberOfPassedTests = testContext.getPassedTests().size();
		int numberOfFailedTests = testContext.getFailedTests().size();
		int numberOfSkippedTests = testContext.getSkippedTests().size();
		String summaryFileName = "runSummary.csv";
		String summaryFilePath = root + "/..";
		String summaryReport = reportFolder + "," + numberOfPassedTests + ","
				+ numberOfFailedTests + "," + numberOfSkippedTests;
		FileGenerator.appendTextToBeginingOfFile(summaryFilePath,
				summaryFileName, summaryReport);
		testLogger.info(reportFolder);
		testLogger.info("Passed: " + numberOfPassedTests);
		testLogger.info("Failed: " + numberOfFailedTests);
		testLogger.info("Skipped: " + numberOfSkippedTests);
		FileGenerator.generateBarGraphIndexHTML(summaryFilePath);
		String parentFolder = root + "/../..";
		for (int i = 1; i < pathLevel; i++) {
			FileGenerator.generateIndexHTML(parentFolder.toString());
			parentFolder = parentFolder + "/..";
		}
	}
}
