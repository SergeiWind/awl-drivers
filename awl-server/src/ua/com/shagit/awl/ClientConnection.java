package ua.com.shagit.awl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * @author shagit.com.ua
 * a thread that works with awl client
 * @param socket - a socket with connection
 */
public class ClientConnection extends Thread {
	public static final Logger clientConnectionLogger = Logger.getLogger("clientConnectionLogger");
	private Socket socket;
	private boolean isActive = true;

	/**
	 * Constructor
	 * @param socket to be connected with a client
	 */
	public ClientConnection (Socket socket) {
		this.socket = socket;
	}

	/**
	 * Reads a line from DataInputStream
	 * @param in - DataInputStream to read a line
	 */
	void Echo (DataInputStream in) {
		String echo = null;
		try {
			echo = in.readUTF();
			if (clientConnectionLogger.isInfoEnabled()) {
				clientConnectionLogger.info("Echo: " + echo);
			}
		} catch (IOException e) {
			clientConnectionLogger.error("Can't get an echo answer");
		}
	}

	/**
	 * Sends one file to client
	 * @param file - file to send
	 * @param out - DataOutputStream to client
	 * @param in - DataInputStream from client
	 * 1) Sends a file name to client
	 * 2) Waits for answer
	 * 3) Sends a file size
	 * 4) Waits for answer
	 * 5) Sends file body using array of bytes as a buffer
	 */
	private void sendToClient (File file, DataOutputStream out, DataInputStream in) {
		if (clientConnectionLogger.isInfoEnabled()) {
			clientConnectionLogger.info("Sending file name "+file.getName());
		}
		try {
			out.writeUTF(file.getName());	//Sending file name
			out.flush();
			Echo (in);
			out.writeLong(file.length());	//Sending file size
			out.flush();
			Echo (in);
		} catch (IOException e) {
			clientConnectionLogger.error("Error while sending name or length of file");
		}
		if (clientConnectionLogger.isInfoEnabled()) {
			clientConnectionLogger.info("Sending file");
		}
		byte [] buffer = new byte [2048];	//Buffer size
		FileInputStream fileIS = null;
		try {
			fileIS = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			clientConnectionLogger.error("Error opening file "+file.getName());
		}

		try {
			Integer bytesRead;
			while((bytesRead=fileIS.read(buffer))>0) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush();
		} catch (IOException e) {
			clientConnectionLogger.error("Error reading file "+file.getName());
		}
		if (clientConnectionLogger.isInfoEnabled()) {
			clientConnectionLogger.info("File sent.");
		}
		try {
			fileIS.close();
		} catch (IOException e) {
			clientConnectionLogger.error("Error closing file "+file.getName());
		}
	}

	/**
	 * Stops sending files
	 */
	public void terminate () {
		this.isActive = false;
	};


	/* *
	 *	Gets DataInputStream and DataOutputStream with socket, then
	 *  1) Reads line - if result is "Hello-awl-client" - answers "Hello-awl-server"
	 *  2) Reads user name, sends "User-ok"
	 *  3) Reads Client host name, sends "Hostname-ok"
	 *  4) Monitors pdfFolder - folder with pdf files - sends them to client and deletes them
	 */
	public void run () {

		DataOutputStream out = null;
		DataInputStream in = null;
		String data = null;
		String user = null;
		String clientHostName = null;

		try {
			socket.setTcpNoDelay(true);
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			clientConnectionLogger.error("Can not get Output or Input Stream");
		}
		if (clientConnectionLogger.isInfoEnabled()) {
			clientConnectionLogger.info("New incoming connection thread starded.");
		}

		try {
			data = in.readUTF();
			if (data.equals("Hello-awl-client")) {
				System.out.println("Client hello OK.");
				out.writeUTF("Hello-awl-server");
				out.flush();
				
				user = in.readUTF();
				System.out.println("User: "+user);
				out.writeUTF("User-ok");
				out.flush();
				
				clientHostName = in.readUTF();
				System.out.println("Client hostname: "+clientHostName);
				System.out.println("Server hostname: "+InetAddress.getLocalHost().getHostName());
				out.writeUTF("Hostname-ok");
				out.flush();
				
				if (clientConnectionLogger.isInfoEnabled()) {
					clientConnectionLogger.info("Ready to send files.");
				}
				
				File file = new File(ServerConfig.pdfFolder+"//"+user);
				if (!file.isDirectory()) {
					clientConnectionLogger.error("Error in path to user directory. Exiting thread");
					return;
				}
				while (isActive) {
					File [] files = file.listFiles();
					if (files.length==0) Thread.sleep(1000); //If folder is empty - let's wait for a second
					for (File fileItem:files) {
						while (!fileItem.renameTo(fileItem)) {
							Thread.sleep(3000);	//waits while file is not busy to send it
						}; 

						sendToClient (fileItem, out, in);
						
						if (fileItem.delete()) {
							if (clientConnectionLogger.isInfoEnabled()) {
								clientConnectionLogger.info("File deleted.");
							}
						} else {
							if (clientConnectionLogger.isInfoEnabled()) {
								clientConnectionLogger.info("File not deleted");
							}
						}
						data = in.readUTF();
						if (clientConnectionLogger.isInfoEnabled()) {
							clientConnectionLogger.info("Received from client: " + data);
						}
					}
				}
			} else {
				clientConnectionLogger.error("Unknown connection type. Closing thread.");
				return;
			}
		} catch (IOException | InterruptedException e) {
			clientConnectionLogger.error("Connection error or can't sleep thread. Closing thread.");
			return;
		}
	}
}
