package ua.com.shagit.awl;

import java.io.File;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * @author Sergii Shakun
 * The main class that starts thread to monitor RDP connections and reads commands from keyboard (quit - to stop the awl-client)
 *
 */
public class AwlClient {
	private static final Logger awlClientLogger = Logger.getLogger("awlClientLogger");

	/**
	 * @param args
	 * main method - no arguments needed
	 */
	public static void main(String[] args) {
		if (awlClientLogger.isInfoEnabled()) {
			awlClientLogger.info("Awl-drivers started.");
		}
		//Parsing config.xml
		Config.GetConfigInstance();
		//Creates new instance to keep lists of servers, users and IPs
		Lists lists = Lists.GetListsInstance();			
		if (!Config.printerSelected) {
			//Creates new instance to get a printer name
			new SelectPrinter().getDefaultPrinterName();		
		}
		if (Config.remminaUsing) {
			//Creates new instance to work with Remmina, read lists of users and servers from remmina config files
			new Remmina().parseRemminaConfFile(lists);				
		}
		if (!Config.localPdfFolderSelected) {
			//If local folder to store PDF files is not set in config.xml - let's do it here
			String username = System.getProperty("user.name");
			Config.localPdfFolder = File.separator+"home"+File.separator+username+File.separator+"PDFFiles"+File.separator;
			if (awlClientLogger.isInfoEnabled()) {
				awlClientLogger.info("Local PDF folder is set to "+Config.localPdfFolder);
			}
		}

		//Let's check if everything is ok with local PDF folder
		File pdfFolder = new File(Config.localPdfFolder); 
		if (!pdfFolder.exists()) {
			pdfFolder.mkdir();
			Config.localPdfFolderSelected = true;
		} else {
			if (pdfFolder.isFile()) {
				awlClientLogger.error("Local PDF folder is not a directory. Delete the file or set the name of directory in config.xml");
				Config.localPdfFolderSelected = false;
			} else {
				Config.localPdfFolderSelected = true;
			}
		}

	if ((lists.ipList.size()>0) && 
			//if we have a printer in local system
			!((Config.localPrinterName==null)||
					//if we have some servers and users 
					(Config.localPrinterName.length()==0)||
					//if a local folder for PDF files is set
					(!Config.localPdfFolderSelected))) {
		//lets start a new thread to monitor RDP connections appearance
		MonitorRdpConnections monitor = new MonitorRdpConnections(lists);	 
		monitor.setName("RDPMonitor");
		monitor.start();
		if (awlClientLogger.isInfoEnabled()) {
			awlClientLogger.info("RDP Monitor process started.");
		}

		Scanner sc = new Scanner(System.in);
		while (true) {
			//Reads a line from keyboard
			String command = sc.next();		
			//If it is "quit||exit||stop" - quits
			if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("stop")) {
				//Sets flag to stop thread that monitors RDP sessions
				monitor.finish(); 	
				break;
			}
		}
		sc.close();
	} else {
		awlClientLogger.error("No RDP presets found or No local printer is set. Fill in profile in your RDP client and setup the printer. Also check the local PDF folder is set correctly in config.xml.");
	}
	if (awlClientLogger.isInfoEnabled()) {
		awlClientLogger.info("Awl-drivers stopped.");
	}
}

}
