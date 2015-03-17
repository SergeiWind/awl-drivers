package ua.com.shagit.awl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author shagit.com.ua <br>
 * Listens to awl-client and runs a new thread when incoming connection detected
 */

public class Listnerer extends Thread {

	/**
	 * while isActive == true - runs the cycle
	 */
	private volatile boolean isActive = true;

	/**
	 *  the method to stop the cycle
	 */
	public void finish() {
		isActive = false;
	}

	public void run() {
		this.monitorAwlConnection();
	}

	public void monitorAwlConnection () {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(Integer.parseInt(AwlServer.remoteAwlPort));
			while (isActive) {
				System.out.println("Waiting for a client...");
				Socket socket = ss.accept();
				System.out.println("Got a client...");
				ClientConnection clientConnection = new ClientConnection (socket);
				clientConnection.setDaemon(true);
				clientConnection.start();
			}
		} catch (Exception x) {
			System.out.println(x);
		} finally {
			try {
				ss.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Monitor stopped.");
	}

}

