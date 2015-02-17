package org.edx.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.log4j.Logger;

public class FileGenerator {
	
	 public final static Logger logger = Logger.getLogger(FileGenerator.class);
	
	public static void generateIndexHTML(String fileLocation)
			throws IOException {
		File file;
		File dir = new File(fileLocation);

		File indexFile = new File(fileLocation + "/index.html");

		if (!indexFile.exists()) {
			indexFile.createNewFile();
		}
		FileWriter fw = new FileWriter(indexFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("<html>");
		bw.newLine();
		bw.write("<BODY>");
		bw.newLine();
		bw.write("<h1>edX Automation</h1>");
		bw.newLine();
		bw.write("<h2>Test Results</h2>");
		bw.newLine();
		bw.write("<ul>");
		bw.newLine();

		String[] list = dir.list();
		for (int i = list.length - 1; i >= 0; i--) {
			file = new File(fileLocation + "/" + list[i]);
			if (file.isDirectory()) {
				bw.write("<li><a href=\"" + list[i]
						+ "\\index.html\" target=\"_top\">" + list[i]
						+ "</a><br>");
				bw.newLine();
			}
		}
		bw.write("</ul>");
		bw.newLine();
		bw.write("</BODY>");
		bw.newLine();
		bw.write("</html>");
		bw.close();
	}

	public static void appendTextToBeginingOfFile(String location,
			String fileName, String text) throws IOException {
		String existingText = "";
		String eol = System.getProperty("line.separator");
		File oldFile = new File(location + "/" + fileName);
		if (!oldFile.exists()) {
			oldFile.createNewFile();
		}
		BufferedReader objBufferedReader = new BufferedReader(new FileReader(
				oldFile.getAbsolutePath()));
		String line;
		while ((line = objBufferedReader.readLine()) != null) {
			existingText = existingText + line + eol;
		}
		objBufferedReader.close();
		String finalText = text + eol + existingText;
		oldFile.delete();
		File newFile = new File(location + "/" + fileName);
		newFile.createNewFile();

		FileWriter fw = new FileWriter(newFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(finalText);
		bw.close();
	}

	public static void generateBarGraphIndexHTML(String fileLocation)
			throws IOException {
		File indexFile = new File(fileLocation + "/index.html");
		if (!indexFile.exists()) {
			indexFile.createNewFile();
		}
		File[] fileArray = { new File("src/main/resources/index_part1.txt"),
				new File(fileLocation + "/runSummary.csv"),
				new File("src/main/resources/index_part2.txt") };
		FileWriter output = new FileWriter(indexFile);
		try {
			for (int i = 0; i < fileArray.length; i++) {
				BufferedReader objBufferedReader = new BufferedReader(
						new FileReader(fileArray[i].getAbsolutePath()));
				String line;
				boolean firstLine = true;
				while ((line = objBufferedReader.readLine()) != null) {
					if (fileArray[i].getName().contains("runSummary.csv")) {
						String[] values = line.split(",");
						values[0] = "'" + values[0] + "'";
						if (firstLine) {
							line = "["
									+ Arrays.toString(values)
											.substring(
													1,
													Arrays.toString(values)
															.length() - 1)
									+ ",'']";
							firstLine = false;
						} else {
							line = ",["
									+ Arrays.toString(values)
											.substring(
													1,
													Arrays.toString(values)
															.length() - 1)
									+ ",'']";
						}
					}
					appendTextToFile(fileLocation, "/index.html", line);
				}
				objBufferedReader.close();
			}
			output.close();
		} catch (Exception e) {
			throw e;
		}
	}
	
	 public static void appendTextToFile(String location, String fileName, String text) {
		  try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(location + "/" + fileName, true)))) {
		   out.println(text);
		  } catch (IOException e) {
		   logger.error(e.getStackTrace());
		  }
		 }

}
