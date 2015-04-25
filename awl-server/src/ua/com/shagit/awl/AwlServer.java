package ua.com.shagit.awl;

import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * @author Sergii Shakun
 * This is main class that starts the server
 */
public class AwlServer {
	public static final Logger awlServerLogger = Logger.getLogger("awlServerLogger");
	/**
	 * @param args
	 * Main method
	 */
	public static void main(String[] args) {
		awlServerLogger.info("Awl server started.");
		ServerConfig.GetConfigInstance();
		Listnerer listnerer = new Listnerer ();
		listnerer.start();
		Scanner sc = new Scanner(System.in);
		while (true) {
			String command = sc.next();		//Reads a line from keyboard
			if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("stop")) {	//If it is "quit||exit||stop" - quits
					try {
						listnerer.finish();
					} catch (IOException e) {
						awlServerLogger.error("Can not close server socket.");
					}
				break;
			}
		}
		sc.close();
		awlServerLogger.info("Awl server stopped.");
	}
}
