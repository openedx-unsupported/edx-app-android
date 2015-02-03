package org.edx.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileGenerator {

	public static void generateIndexHTML(String fileLocation) throws IOException {
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
				bw.write("<li><a href=\"" + list[i] + "/index.html\" target=\"_top\">" + list[i] + "</a><br>");
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

}
