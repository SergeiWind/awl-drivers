package ua.com.shagit.awl;

import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * @author Sergii Shakun
 * The main class that reads config.xml, starts thread to monitor RDP connections and reads commands from keyboard (quit - to stop the awl-client)
 *
 */
public class AwlClient {
	public static final String REMOTE_RDP_PORT = "3389"; 	//XXX this should be read from config.xml
	public static final int REMOTE_AWL_PORT = 3390; 		//XXX this should be read from config.xml
	public static final Logger awlClientLogger = Logger.getLogger("awlClientLogger");
	//	public static final Logger rootLogger = Logger.getRootLogger();
	public static boolean remminaUsing = false;
	public static String printerName = null;

	/**
	 * @param args
	 * main method - no arguments needed
	 */
	public static void main(String[] args) {
		boolean rdpPresetsFound = false;
		if (awlClientLogger.isInfoEnabled()) {
			awlClientLogger.info("Awl-drivers started.");
		}
		Remmina rem = new Remmina();				//Creates new instance to work with Remmina
		Lists lists = new Lists();					//Creates new instance to keep lists of servers, users and IPs
		SelectPrinter sp = new SelectPrinter();		//Creates new instance to get a printer name
		sp.getPrinterNameFromConfig();				//Sets the printer name
		//XXX This shoud be implemented in Remmina() constructor;
		rem.setRemminaConfPath();					//Sets Remmina's path 
		//XXX

		if (remminaUsing) {							//if remmina path is in config.xml
			rem.parseRemminaConfFile(lists);		//read lists of users and servers from remmina config file
		}

		if (lists.ipList.size()>0) {				//if RDP client config file is not empty
			rdpPresetsFound = true;
		}

		if (rdpPresetsFound && !((printerName==null)||(printerName.length()==0))) {		//if we have not empty remmina config and a printer in local system
			MonitorRdpConnections monitor = new MonitorRdpConnections(lists);			//lets start a new thread to monitor RDP connections appearance 
			monitor.setName("RDPMonitor");
			monitor.start();

			Scanner sc = new Scanner(System.in);
			while (true) {
				String command = sc.next();		//Reads a line from keyboard
				if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("stop")) {	//If it is "quit||exit||stop" - quits
					monitor.finish(); 	//Sets flag to stop thread that monitors RDP sessions
					break;
				}
			}
			sc.close();
		} else {
			awlClientLogger.error("No RDP presets found or No local printer is set. Fill in profile in your RDP client and setup the printer.");
		}
		if (awlClientLogger.isInfoEnabled()) {
			awlClientLogger.info("Awl-drivers stopped.");
		}
	}

}
