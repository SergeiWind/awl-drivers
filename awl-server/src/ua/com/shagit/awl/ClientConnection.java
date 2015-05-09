package ua.com.shagit.awl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

/**
 * @author shagit.com.ua
 * a thread that works with awl client
 * @param socket - a socket with connection
 */
public class ClientConnection extends Thread {

	private Socket socket;
	private boolean isActive = true;

	public ClientConnection (Socket socket) {
		this.socket = socket;
	}

	void Echo (DataInputStream in) {
		String echo = null;
		try {
			echo = in.readUTF();
			System.out.println("Echo:"+echo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendToClient (File file, DataOutputStream out, DataInputStream in) {
		System.out.println("Sending file name "+file.getName());
		try {

			out.writeUTF(file.getName());
			out.flush();
			Echo (in);
			out.writeLong(file.length());
			out.flush();
			Echo (in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sending file");
		byte [] buffer = new byte [2048];
		FileInputStream fileIS = null;
		try {
			fileIS = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("Error opening file "+file);
			e.printStackTrace();
		}

		try {
			Integer bytesRead;
			while((bytesRead=fileIS.read(buffer))>0) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush();
		} catch (IOException e) {
			System.out.println("Error reading file "+file);
			e.printStackTrace();
		};
		System.out.println("File sent.");
		try {
			fileIS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void terminate () {
		this.isActive = false;
	};


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
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("New incoming connection thread starded.");

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
				System.out.println("Ready to send files.");
				while (isActive) {
					File file = new File(ServerConfig.pdfFolder+"//"+user);
					if (!file.isDirectory()) {
						System.out.println("Error in path to user directory. Exiting thread");
						return;
					}
					File [] files = file.listFiles();
					for (File fileItem:files) {
						while (!fileItem.renameTo(fileItem)) {
							Thread.sleep(1000);
						}; //waits while file is not busy to send it
						if (socket.isOutputShutdown()) {
							out = new DataOutputStream(socket.getOutputStream());
						}
						sendToClient (fileItem, out, in);
						if (fileItem.delete()) {
							System.out.println("File deleted.");
						} else {
							System.out.println("File not deleted.");
						}
						data = in.readUTF();
						System.out.println(data);
					}
				}
			} else {
				System.out.println("Unknown connection type. Closing thread.");
				return;
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Connection error or can't sleep thread. Closing thread.");
			return;
		}

	}
}
