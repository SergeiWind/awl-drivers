package ua.com.shagit.awl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

/**
 * @author shagit.com.ua <br>
 * Listens to awl-client and runs a new thread when incoming connection detected
 */

public class Listnerer extends Thread {
	private static final Logger listnererLogger = Logger.getLogger("listnererLogger");
	private ServerSocket socket;

	/**
	 * while isActive == true - runs the cycle
	 */
	private volatile boolean isActive = true;

	/**
	 *  the method to stop the cycle by closing ServerSocket and setting isActive to false
	 * @throws IOException 
	 */
	public void finish() throws IOException {
		isActive = false;
		this.socket.close();
	}

	/* 
	 * runs new thread
	 */
	@Override
	public void run() {
		this.monitorAwlConnection();
	}

	/**
	 * This method creates a ServerSocket, waits for a client, accepts connection and starts a new thread to handle that connection
	 * you may quit the endless cycle by typing exit, stop or quit
	 */
	private void monitorAwlConnection () {
		ServerSocket ss = null;
		try {
			InetAddress ipAddress = null;
			if ((ServerConfig.bindIp==null)||("*".equals(ServerConfig.bindIp))||("".equals(ServerConfig.bindIp))) {
				ipAddress = null;
			} else {
				ipAddress = InetAddress.getByName(ServerConfig.bindIp);
			}
			ss = new ServerSocket(Integer.parseInt(ServerConfig.awlPort),0,ipAddress);
			this.socket = ss;
			while (isActive) {
				if (listnererLogger.isInfoEnabled()) {
					listnererLogger.info("Waiting for a client...");
				}
				Socket socket = ss.accept();
				if (listnererLogger.isInfoEnabled()) {
					listnererLogger.info("Got a client...");
				}
				ClientConnection clientConnection = new ClientConnection (socket);
				clientConnection.setDaemon(true);
				clientConnection.setName(socket.getRemoteSocketAddress().toString());
				clientConnection.start();
			}
		} catch (UnknownHostException e) {
			listnererLogger.error("Unknown host to bind");
		} catch (IOException e){
			listnererLogger.warn("I/O error while accepting connection from a client");
		} finally {
			try {
				ss.close();
			} catch (IOException e) {
				listnererLogger.error("Unable to close socket to client");
			}
		}
		System.out.println("Monitor stopped.");
	}

}

