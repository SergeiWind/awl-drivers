package ua.com.shagit.awl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author shagit.com.ua
 * a thread that works with awl client
 * @param socket - a socket with connection
 */
public class ClientConnection extends Thread {

	Socket socket;

	public ClientConnection (Socket socket) {
		this.socket = socket;
	}

	private void sendToClient (File file, PrintWriter out, OutputStream outStream, BufferedReader in) {
		System.out.println("Sending file name "+file.getName());
		out.println(file.getName());
		System.out.println("Sending file size "+file.length());
		out.println(file.length());
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
			while(fileIS.read(buffer, 0, 2048)!=-1) {
				outStream.write(buffer);
			}
		} catch (IOException e) {
			System.out.println("Error reading file "+file);
			e.printStackTrace();
		};
		System.out.println("File sent.");

	}

	public void run () {

		PrintWriter out = null;
		BufferedReader in = null;
		OutputStream outStream = null;
		String data = null;
		String user = null;
		String clientHostName = null;

		try {
			outStream = socket.getOutputStream();
			out = new PrintWriter(outStream, true);
			in = new BufferedReader(new InputStreamReader( socket.getInputStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("New incoming connection thread starded.");

		try {
			data = in.readLine();
			if (data.equals("Hello-awl-client")) {
				System.out.println("Client hello OK.");
				out.println("Hello-awl-server");
				user = in.readLine();
				System.out.println("User: "+user);
				out.println("User-ok");
				clientHostName = in.readLine();
				System.out.println("Client hostname: "+clientHostName);
				System.out.println("Server hostname: "+InetAddress.getLocalHost().getHostName());
				out.println("Hostname-ok");
				System.out.println("Ready to send files.");
				while (true) {
					File file = new File(AwlServer.pdfFolder+"//"+user);
					if (!file.isDirectory()) {
						System.out.println("Error in path to user directory. Exiting thread");
						return;
					}
					File [] files = file.listFiles();
					for (File fileItem : files) {
						sendToClient (fileItem, out, outStream, in);
						fileItem.delete();
						System.out.println("File deleted.");
					}
				}
			} else {
				System.out.println("Unknown connection type. Closing thread.");
				return;
			}
		} catch (IOException e) {
			System.out.println("Connection error. Closing thread.");
			return;
		}

	}
}
