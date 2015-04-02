package ua.com.shagit.awl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * @author Sergeii Shakun
 * Class gets the printer name - from config.xml or gets default if it is not set in the file
 */
public class SelectPrinter {
	public static final Logger selectPrinterLogger = Logger.getLogger("selectPrintreLogger");

	public void getPrinterNameFromConfig() { // setting printer name from config.xml
		File f = new File("config.xml");

		String XMLString = "";
		String [] elements = null;
		try {
			FileReader fr;
			fr = new FileReader(f);
			StringBuffer sb = new StringBuffer();
			int a = fr.read();
			if (selectPrinterLogger.isInfoEnabled()) {
				selectPrinterLogger.info("Reading config.xml");
			}
			while (a != -1) {
				sb.append((char) a);
				a = fr.read();
				if (selectPrinterLogger.isInfoEnabled()) {
					System.out.print(".");
				}
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
				if (selectPrinterLogger.isInfoEnabled()) {
					selectPrinterLogger.info("Printer name is set to: "+XMLString);
				}
				AwlClient.printerName = XMLString;
			} else {
				selectPrinterLogger.warn("Printer name is not set in config.xml.");
				getDefaultPrinterName();
			}
			fr.close();
		} catch (FileNotFoundException e) {
			selectPrinterLogger.error("Config.xml not found.");
		} catch (IOException e) {
			selectPrinterLogger.error("Can\'t read config.xml.");
		} catch (StringIndexOutOfBoundsException e) {
			selectPrinterLogger.warn("Can\'t parse config.xml or local printer name is not set in config.xml. Trying to get a printer from CUPS.");
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
			String line = "";
			String prName = "";
			while((line = brStdout.readLine()) != null) {
				if (line.contains(": ")){
					prName = line.substring(line.indexOf(": "), line.length());
				} else {
					prName = "";
				}
			}
			if (!((prName==null)||(prName.length()==0))) {
				AwlClient.printerName = prName;
			}
		} catch (IOException e) {
			selectPrinterLogger.error("Can\'t execute lpstat -d command.");
		}
		if (!((AwlClient.printerName==null)||(AwlClient.printerName.length()==0))) {
			if (selectPrinterLogger.isInfoEnabled()) {
				selectPrinterLogger.info("Printer name is set to "+AwlClient.printerName+" as default in CUPS");
			}
		} else {
			selectPrinterLogger.error("No local printer found.");
		}
	}
}
