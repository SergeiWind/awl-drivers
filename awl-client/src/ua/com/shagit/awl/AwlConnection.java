package ua.com.shagit.awl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

/**
 * @author sergei
 * Class that establishes connection to awl server for an RDP connection
 * @param serverIP - ip address of server
 * @param user - username
 */
public class AwlConnection extends Thread {
	private static final Logger awlConnectionLogger = Logger.getLogger("awlConnectionLogger");
	private String serverIP;
	private String user;
	private boolean isActive;
	private Socket awlServerSocket;

	/**
	 * Constructor
	 * @param serverIP
	 * @param user
	 */
	public AwlConnection(String serverIP, String user) {
		this.serverIP = serverIP;
		this.user = user;
	}

	/* 
	 * Starts new thread
	 */
	@Override
	public void run() {
		this.isActive = true;
		this.establishConnection(serverIP, user);
	}

	/* 
	 * Method to interrupt process
	 */
	public void interrupt(){
		this.isActive = false;
		try {
			this.awlServerSocket.close();
		} catch (IOException e) {
			awlConnectionLogger.error("Unable to close socket while terminating thread ");
		}
	}

	/**
	 * Prints the file to selected printer
	 * @param fileName - full path to file (String)
	 */
	private void printFile(String fileName) {
		ProcessBuilder procBuilder = new ProcessBuilder("lpr", "-P", Config.localPrinterName, fileName);
		if (awlConnectionLogger.isInfoEnabled()) {
			awlConnectionLogger.info("Executing "+procBuilder.command());
		}
		procBuilder.redirectErrorStream(true);
		try {
			procBuilder.start();
		} catch (IOException e) {
			awlConnectionLogger.error("Can\'t execute lpr command.");
		}
	}

	/**
	 * Method receives a file from server
	 * @param outWriter
	 * @param inBufferedReader
	 * @param inStream
	 * 
	 */
	private void receiveFileFromServer(DataOutputStream out, DataInputStream in) {
		try {
			String fileName = in.readUTF();
			if (awlConnectionLogger.isInfoEnabled()) {
				awlConnectionLogger.info("Receiving file "+fileName);
			}
			out.writeUTF(fileName);
			out.flush();

			Long fileSize = in.readLong();
			if (awlConnectionLogger.isInfoEnabled()) {
				awlConnectionLogger.info("Filesize "+fileSize);
			}

			out.writeUTF(fileSize.toString());
			out.flush();


			File localFile = new File(Config.localPdfFolder+fileName);
			FileOutputStream fileOS = new FileOutputStream(localFile);

			byte[] buffer = new byte [2048];
			try {
				for (int i = 0; i<fileSize/buffer.length; i++) {
					in.readFully(buffer, 0, buffer.length);
					fileOS.write(buffer,0, buffer.length);
				}
				in.readFully(buffer, 0, (int)(fileSize%buffer.length));
				fileOS.write(buffer,0, (int)(fileSize%buffer.length));

			} catch (IOException e) {
				awlConnectionLogger.error("Error writing file "+fileName);
			};
			fileOS.close();
			if (awlConnectionLogger.isInfoEnabled()) {
				awlConnectionLogger.info("File received.");
			}

			if (Config.autoPrintFiles) {
				printFile (Config.localPdfFolder+fileName);
			}
		} catch (IOException e) {
			awlConnectionLogger.error("Error reading Stream or socket closed");

		}
	}

	/**
	 * Method sends a @param command to server, waits for answer, then if answer is not null and equals @param answer it logs @param messageOk else @param messageError
	 * returns true if everything is ok, else returns false
	 * @param outWriter
	 * @param inBufferedReader
	 * @throws IOException
	 */
	private boolean sendCommandReceiveAnswer(String command, String answer, String messageOk,String messageError, 
			DataOutputStream out, DataInputStream in) throws IOException {
		out.writeUTF(command); //Hello handshake
		out.flush();
		String data = in.readUTF();
		if ((data!=null)&&(data.equals(answer))) {
			if (awlConnectionLogger.isInfoEnabled()) {
				awlConnectionLogger.info(messageOk);
			}
			return true;
		} else {
			awlConnectionLogger.error(messageError);
			return false;
		}
	}

	/**
	 * Try to establish connection with the awl-server according to detected RDP session
	 * @param serverIP - ip address of server
	 * @param user - username
	 */
	private void establishConnection(String serverIP, String user) {
		if (awlConnectionLogger.isInfoEnabled()) {
			awlConnectionLogger.info("Trying to establish awl-connection to "+serverIP+" as "+user);
		}
		Integer remoteAwlPort = Integer.parseInt(Config.awlPort); 
		try (Socket awlServerSocket = new Socket(serverIP,remoteAwlPort)) {//Creating autocloseable socket

			this.awlServerSocket = awlServerSocket;
			DataOutputStream out = new DataOutputStream(awlServerSocket.getOutputStream());
			DataInputStream in = new DataInputStream(awlServerSocket.getInputStream());

			if (!sendCommandReceiveAnswer("Hello-awl-client", "Hello-awl-server", "Server hello OK.", 
					"Unknown server type. Closing thread.", out, in)) {
				return;
			}; //Hello handshake - return if not successful
			if (!sendCommandReceiveAnswer(user, "User-ok", "Username sent.", "Error sending username. Closing thread.", out, in)) {
				return;
			}	//Sending username
			String hostName = InetAddress.getLocalHost().getHostName(); //Getting client local hostname
			if (!sendCommandReceiveAnswer(hostName, "Hostname-ok", "Local hostname "+hostName+" sent. Ready to receive files.", 
					"Error sending hostname. Closing thread.", out, in)) {
				return;
			} //Sending hostname
			awlServerSocket.setTcpNoDelay(true);
			while (isActive) {
				receiveFileFromServer(out, in);	//receiving files
				out.writeUTF("Ready to receive next file.");
				out.flush();
			}

		} catch (UnknownHostException e) {
			awlConnectionLogger.error("Error in server IP adress");
		} catch (SocketException e) {
			awlConnectionLogger.error("Problem accessing or creating Socket.");
		} catch (IOException e) {
			awlConnectionLogger.error("General IO Error or Socket closed");
		};
	}
}
