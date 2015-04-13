package ua.com.shagit.awl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * @author Sergeii Shakun
 * Class gets default printer name from system
 */
public class SelectPrinter {
	private static final Logger selectPrinterLogger = Logger.getLogger("selectPrinterLogger");

	/**
	 * get default printer from CUPS
	 */
	protected void getDefaultPrinterName () {
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
				if (line.contains(": ")){
					prName = line.substring(line.indexOf(": "), line.length());
				} else {
					prName = "";
				}
			}
			if (!((prName==null)||(prName.length()==0))) {
				Config.localPrinterName = prName;
			}
		} catch (IOException e) {
			selectPrinterLogger.error("Can\'t execute lpstat -d command.");
		}
		if (!((Config.localPrinterName==null)||(Config.localPrinterName.length()==0))) {
			Config.printerSelected = true;
			if (selectPrinterLogger.isInfoEnabled()) {
				selectPrinterLogger.info("Printer name is set to "+Config.localPrinterName+" as default in CUPS");
			}
		} else {
			selectPrinterLogger.error("No local printer found.");
		}
	}
}
