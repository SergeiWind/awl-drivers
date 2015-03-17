package ua.com.shagit.awl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MonitorRdpConnections extends Thread {
	
	Lists lists;
	private volatile boolean isActive = true;
	
	public MonitorRdpConnections (Lists lists) {
		this.lists = lists;
	}
	
	public void run() {
		this.monitorRdpConnections(lists);
	}
	
	public void finish() {
		isActive = false;
	}
	
	public void monitorRdpConnections (Lists lists) {//runs "ss -ptn dport = :3389 to monitor RDP connections"
		while (isActive) {
			ProcessBuilder procBuilder = new ProcessBuilder("ss","-ptn","dport = :"+AwlClient.remoteRdpPort);
			procBuilder.redirectErrorStream(true);
			List<String> tempList = new ArrayList<String>(); // for storing ss util output; 
			try {
				Process process = procBuilder.start();
				InputStream stdout = process.getInputStream();
				InputStreamReader isrStdout = new InputStreamReader(stdout);
				BufferedReader brStdout = new BufferedReader(isrStdout);
				String line = null;
				String serverIp = null;
				while((line = brStdout.readLine()) != null) {
					if (line.contains("State")) {
						continue; //Skip the first line in ss output
					}
					serverIp = line.substring(0, line.indexOf(":"+AwlClient.remoteRdpPort));
					try {
						for (int i = serverIp.length()-1; i>=0; i--) {
							char currentChar = serverIp.charAt(i); 
							if (currentChar == 32) {
								serverIp = serverIp.substring(i+1, serverIp.length());
								break;
							}
						}
					} catch (Exception e) {
						System.out.println("Error parsing ServerIP adress. Something wrong is with ss utility.");
					}
					if (!tempList.contains(serverIp)) {//filling the tempList
						tempList.add(serverIp);
					}
					if (lists.ipList.contains(serverIp)) {//adding rdp session to list if new one is discovered
						if (!lists.rdpSessionsList.contains(serverIp)) {
							lists.rdpSessionsList.add(serverIp);
							System.out.print("Discovered new RDP session. Server IP address:"+serverIp+" ");
							System.out.println("Assuming user: "+lists.userList.get(lists.ipList.indexOf(serverIp)));
							AwlConnection awlConnection = new AwlConnection(serverIp, lists.userList.get(lists.ipList.indexOf(serverIp)));
							awlConnection.setDaemon(true);
							awlConnection.start();
						}
					}
				}
			} catch (IOException e) {
				System.out.println("Can\'t execute ss command.");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("Can\'t sleep for some reason.");
			}
			for (String rdpSessionsListElement : lists.rdpSessionsList) {// removing sessions if some disappeared
				if (!tempList.contains(rdpSessionsListElement)) {
					System.out.println("RDP session closed. Server IP:"+rdpSessionsListElement);
				}
			}
			lists.rdpSessionsList = tempList;
		}
	}

}
