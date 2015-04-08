package ua.com.shagit.awl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
//import java.io.BufferedWriter;
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

/**
 * @author sergei
 * Class that establishes connection to awl server for an RDP connection
 * @param serverIP - ip address of server
 * @param user - username
 */
public class AwlConnection extends Thread {
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

	public void interrupt(){
		this.isActive = false;
		try {
			this.awlServerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void receiveFileFromServer(PrintWriter outWriter, BufferedReader inBufferedReader, InputStream inStream) {
		try {
			String fileName = inBufferedReader.readLine();
			System.out.println("Receiving file "+fileName);
			outWriter.println(fileName);

			String fileSizeStr = inBufferedReader.readLine();
			System.out.println("Filesize "+fileSizeStr);
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
				System.out.println("Error reading file "+fileName);
				e.printStackTrace();
			};
			fileOS.close();
			System.out.println("File received.");

		} catch (IOException e) {
			System.out.println("Error reading Stream or socket closed");
		}
	}

	/**
	 * Try to establish connection with the awl-server according to detected RDP session
	 * @param serverIP - ip address of server
	 * @param user - username
	 */
	private void establishConnection(String serverIP, String user) {
		String data = null;
		System.out.println("Trying to establish awl-connection to "+serverIP+" as "+user);
		Integer remoteAwlPort = Integer.parseInt(Config.awlPort); 
		try (Socket awlServerSocket = new Socket(serverIP,remoteAwlPort)) {//Creating autocloseable socket

			this.awlServerSocket = awlServerSocket;
			OutputStream outStream = awlServerSocket.getOutputStream(); //Creating output stream to send bytes
			PrintWriter outWriter=new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"), true); //Creating a printWriter to send lines using println
			//BufferedWriter outBufferedWriter = new BufferedWriter(outWriter);

			InputStream inStream = awlServerSocket.getInputStream(); //Creating input stream to receive bytes
			InputStreamReader inStreamReader = new InputStreamReader(inStream, "UTF-8"); // Creating inputReader to read lines
			BufferedReader inBufferedReader=new BufferedReader(inStreamReader);// Make the inputReader Buffered

			outWriter.println("Hello-awl-client"); //Hello handshake
			data = inBufferedReader.readLine();
			if (data.equals("Hello-awl-server")) {
				System.out.println("Server hello OK.");
			} else {
				System.out.println("Unknown server type. Closing thread.");
				return;
			}

			outWriter.println(user); //Sending username
			data = inBufferedReader.readLine();
			if (data.equals("User-ok")) {
				System.out.println("Username sent.");
			} else {
				System.out.println("Error sending username. Closing thread.");
				return;
			}

			String hostName = InetAddress.getLocalHost().getHostName(); //Sending client hostname
			outWriter.println(hostName);
			data = inBufferedReader.readLine();
			if (data.equals("Hostname-ok")) {
				System.out.println("Local hostname "+hostName+" sent.");
				System.out.println("Ready to receive files.");
			} else {
				System.out.println("Error sending hostname. Closing thread.");
				return;
			}

			while (isActive) {
				receiveFileFromServer(outWriter, inBufferedReader, inStream);	//receiving files
			}


		} catch (UnknownHostException e) {
			System.out.println("Error in server IP adress");
			e.printStackTrace();
		} catch (SocketException e) {
			System.out.println("Problem accessing or creating Socket.");
		} catch (IOException e) {
			System.out.println("General IO Error or Socket closed");
			e.printStackTrace();
		};


	}
}
