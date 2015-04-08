package ua.com.shagit.awl;

import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * @author Sergii Shakun
 * The main class that starts thread to monitor RDP connections and reads commands from keyboard (quit - to stop the awl-client)
 *
 */
public class AwlClient {
	public static final Logger awlClientLogger = Logger.getLogger("awlClientLogger");

	/**
	 * @param args
	 * main method - no arguments needed
	 */
	public static void main(String[] args) {
		if (awlClientLogger.isInfoEnabled()) {
			awlClientLogger.info("Awl-drivers started.");
		}
		Config.GetConfigInstance();						//Parsing config.xml
		Lists lists = new Lists();						//Creates new instance to keep lists of servers, users and IPs
		if (!Config.printerSelected) {
			SelectPrinter sp = new SelectPrinter();		//Creates new instance to get a printer name
			sp.getDefaultPrinterName();
		}
		if (Config.remminaUsing) {						
			Remmina rem = new Remmina();				//Creates new instance to work with Remmina
			rem.parseRemminaConfFile(lists);			//read lists of users and servers from remmina config file
		}

		if ((lists.ipList.size()>0) && 
				!((Config.localPrinterName==null)||
						(Config.localPrinterName.length()==0))) {				//if we have some servers and users and a printer in local system
			MonitorRdpConnections monitor = new MonitorRdpConnections(lists);	//lets start a new thread to monitor RDP connections appearance 
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
