package ua.com.shagit.awl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Shag-IT
 * Class sets the printer name - from config.xml or gets default if it is not set
 */
public class SelectPrinter {
	public void getPrinterNameFromConfig() { // setting printer name from config.xml
		File f = new File("config.xml");

		String XMLString = "";
		String [] elements = null;
		try {
			FileReader fr;
			fr = new FileReader(f);
			StringBuffer sb = new StringBuffer();
			int a = fr.read();
			System.out.print("Reading config.xml");
			while (a != -1) {
				sb.append((char) a);
				a = fr.read();
				System.out.print(".");
			}
			System.out.println("");
			XMLString = sb.toString();
			elements = "config/printer".split("/");
			for (String element : elements) {
				int start = XMLString.indexOf("<"+element+">");
				int end = XMLString.indexOf("</"+element+">", start+2+element.length());
				XMLString = XMLString.substring(start+2+element.length(), end);
			}
			if (!XMLString.equals("")) {
				System.out.println("Printer name is set to: "+XMLString);
				AwlClient.printerName = XMLString;
			} else {
				System.out.println("Printer name is not set in config.xml.");
				getDefaultPrinterName();
			}
			fr.close();
		} catch (FileNotFoundException e) {
			System.out.println("config.xml not found.");
		} catch (IOException e) {
			System.out.println("Can\'t read config.xml.");
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Can\'t parse config.xml or local printer name is not set in config.xml.");
			getDefaultPrinterName(); //if printer is not set in config.xml - take it from the CUPS
		}
	}
	
	public void getDefaultPrinterName () {//get default printer from CUPS
		ProcessBuilder procBuilder = new ProcessBuilder("lpstat","-d");
		procBuilder.redirectErrorStream(true);
		try {
			Process process = procBuilder.start();
			InputStream stdout = process.getInputStream();
			InputStreamReader isrStdout = new InputStreamReader(stdout);
			BufferedReader brStdout = new BufferedReader(isrStdout);
			String line = null;
			String prName = null;
			while((line = brStdout.readLine()) != null) {
				prName = line.substring(line.indexOf(": "), line.length());
			}
			if (!prName.equals("")) {
				AwlClient.printerName = prName;
			}
		} catch (IOException e) {
			System.out.println("Can\'t execute lpstat -d command.");
		}
		if (!AwlClient.printerName.equals("")) {
			System.out.println("Printer name is set to "+AwlClient.printerName+" as default in CUPS");
		} else {
			System.out.println("No local printer found.");
		}
	}
}
