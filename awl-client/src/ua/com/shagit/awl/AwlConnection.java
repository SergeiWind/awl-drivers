package ua.com.shagit.awl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
	public static final Logger awlConnectionLogger = Logger.getLogger("awlConnectionLogger");
	String serverIP;
	String user;
	boolean isActive;
	Socket awlServerSocket;

	public AwlConnection(String serverIP, String user) {
		this.serverIP = serverIP;
		this.user = user;
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Method receives a file from server
	 * @param outWriter
	 * @param inBufferedReader
	 * @param inStream
	 * 
	 */
	void receiveFileFromServer(PrintWriter outWriter, BufferedReader inBufferedReader, InputStream inStream) {
		try {
			String fileName = inBufferedReader.readLine();
			if (awlConnectionLogger.isInfoEnabled()) {
				awlConnectionLogger.info("Receiving file "+fileName);
			}
			outWriter.println(fileName);

			String fileSizeStr = inBufferedReader.readLine();
			if (awlConnectionLogger.isInfoEnabled()) {
				awlConnectionLogger.info("Filesize "+fileSizeStr);
			}
			Integer fileSize = Integer.parseInt(fileSizeStr);
			outWriter.println(fileSizeStr);

			File localFile = new File(fileName);
			FileOutputStream fileOS = new FileOutputStream(localFile);

			byte [] buffer = new byte [2048];
			int bytesRead = 0;

			try {
				for (int i = 0; i<=fileSize/buffer.length; i++) {
					bytesRead = inStream.read(buffer, 0, buffer.length);
					fileOS.write(buffer,0, bytesRead);
				}
			} catch (IOException e) {
				awlConnectionLogger.error("Error reading file "+fileName);
			};
			fileOS.close();
			if (awlConnectionLogger.isInfoEnabled()) {
				awlConnectionLogger.info("File received.");
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
											PrintWriter outWriter, BufferedReader inBufferedReader) throws IOException {
		outWriter.println(command); //Hello handshake
		String data = inBufferedReader.readLine();
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
			OutputStream outStream = awlServerSocket.getOutputStream(); //Creating output stream to send bytes
			PrintWriter outWriter=new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"), true); //Creating a printWriter to send lines using println

			InputStream inStream = awlServerSocket.getInputStream(); //Creating input stream to receive bytes
			InputStreamReader inStreamReader = new InputStreamReader(inStream, "UTF-8"); // Creating inputReader to read lines
			BufferedReader inBufferedReader=new BufferedReader(inStreamReader);// Make the inputReader Buffered

			if (!sendCommandReceiveAnswer("Hello-awl-client", "Hello-awl-server", "Server hello OK.", 
											"Unknown server type. Closing thread.", outWriter, inBufferedReader)) {
				return;
			}; //Hello handshake - return if not successful
			if (!sendCommandReceiveAnswer(user, "User-ok", "Username sent.", "Error sending username. Closing thread.", outWriter, inBufferedReader)) {
				return;
			}	//Sending username
			String hostName = InetAddress.getLocalHost().getHostName(); //Getting client local hostname
			if (!sendCommandReceiveAnswer(hostName, "Hostname-ok", "Local hostname "+hostName+" sent. Ready to receive files.", 
											"Error sending hostname. Closing thread.", outWriter, inBufferedReader)) {
				return;
			} //Sending hostname

			while (isActive) {
				receiveFileFromServer(outWriter, inBufferedReader, inStream);	//receiving files
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
