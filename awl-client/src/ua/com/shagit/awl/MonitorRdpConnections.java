package ua.com.shagit.awl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shagit.com.ua
 * Monitors RDP sessions and initiates or drops connections to awl-server
 *
 */
public class MonitorRdpConnections extends Thread {
	Lists lists; //lists of servers, users and connections
	private volatile boolean isActive = true; //client works while isActive = true
	
	/**
	 * Constructor
	 * @param lists
	 */
	public MonitorRdpConnections(Lists lists) {
		this.lists = lists;
	}
	
	/**
	 * Runs new thread
	 */
	@Override
	public void run() {
		this.monitorRdpConnections(lists);
	}
	
	/**
	 *Use this method to stop a client 
	 */
	public void finish() {
		isActive = false;
	}
	
	/**
	 * Terminates thread by name
	 * @param threadName - thread name to terminate in username@serverIP format
	 */
	void TerminateThread(String threadName) {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		Thread[] threads = new Thread[threadGroup.activeCount()];
		int numberOfThreads = threadGroup.enumerate(threads, false);
		for (int i=0; i<numberOfThreads; i++) {
			if (threads[i].getName().equals(threadName)) {
				threads[i].interrupt();
			}
		}
	}
	/**
	 * Runs "ss -ptn dport = :3389 to monitor RDP connections"
	 * @param lists
	 */
	public void monitorRdpConnections(Lists lists) {
		while (isActive) {
			ProcessBuilder procBuilder = new ProcessBuilder("ss","-ptn","dport = :"+AwlClient.REMOTE_RDP_PORT);
			procBuilder.redirectErrorStream(true);
			List<String> tempList = new ArrayList<String>(); // for storing ss util output; 
			try {
				Process process = procBuilder.start();// runs ss util
				InputStream stdout = process.getInputStream(); //create inputstream
				InputStreamReader isrStdout = new InputStreamReader(stdout);//create inputstreamreader to use readln
				BufferedReader brStdout = new BufferedReader(isrStdout);//make it buffered
				String line = null;
				String serverIp = null;
				while((line = brStdout.readLine()) != null) {//parsing ss output
					if (line.contains("State")) {
						continue; //Skip the first line in ss output
					}
					serverIp = line.substring(0, line.indexOf(":"+AwlClient.REMOTE_RDP_PORT));
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
							awlConnection.setDaemon(true);//make the future thread a daemon
							awlConnection.setName(lists.userList.get(lists.ipList.indexOf(serverIp))+"@"+serverIp);//sets the name of the thread
							awlConnection.start();// starts new thread
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
					String threadName = lists.userList.get(lists.ipList.indexOf(rdpSessionsListElement))+"@"+rdpSessionsListElement;
					System.out.println("RDP session closed. Terminating thread:"+threadName);
					TerminateThread(threadName);
				}
			}
			lists.rdpSessionsList = tempList;
		}
	}

}
