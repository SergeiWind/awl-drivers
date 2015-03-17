package ua.com.shagit.awl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.ini4j.Wini;

public class Remmina {
	String remminaConfPath; //path to Remmina directory
	
	public String getRemminaConfPath () {
		return this.remminaConfPath;
	}

	public void setRemminaConfPath() { // setting remminaConfPath from config.xml
		File f = new File("config.xml");

		String XMLString = "";
		String [] elements = null;
		try {
			FileReader fr;
			fr = new FileReader(f);
			StringBuffer sb = new StringBuffer();
			int a = fr.read();
			System.out.print("Reading config.xml");
			while (a != -1) {
				sb.append((char) a);
				a = fr.read();
				System.out.print(".");
			}
			System.out.println("");
			XMLString = sb.toString();
			elements = "config/remmina".split("/");
			for (String element : elements) {
				int start = XMLString.indexOf("<"+element+">");
				int end = XMLString.indexOf("</"+element+">", start+2+element.length());
				XMLString = XMLString.substring(start+2+element.length(), end);
			}
			if (!XMLString.equals("")) {
				System.out.println("Remmina configuration file is in "+XMLString);
				AwlClient.remminaUsing = true;
			} else {
				System.out.println("Remmina is not configured in config.xml.");
			}
			fr.close();
			this.remminaConfPath = XMLString;
		} catch (FileNotFoundException e) {
			System.out.println("config.xml not found.");
		} catch (IOException e) {
			System.out.println("Can\'t read config.xml.");
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Can\'t parse config.xml.");
		}
	}

	public void parseRemminaConfFile (Lists lists) {//getting lists of users and servers from remmina config files
		File f = new File(this.remminaConfPath);
		if (f.isDirectory()) {
			File [] fileList = f.listFiles();
			for (File file : fileList) {
				if (file.getName().endsWith(".remmina")) {
					try {
						Wini ini = new Wini(new File(file.getAbsolutePath()));
						String server = ini.get("remmina", "server", String.class);
						String username = ini.get("remmina", "username", String.class);

						if (!server.equals(null) && !username.equals(null)) {
							InetAddress iaRemoteAddress = null;
							String ipAddr = null;
							System.out.print("Server "+server+" found; User "+username+" found.");
							try {
								iaRemoteAddress = InetAddress.getByName(server);
								ipAddr = iaRemoteAddress.getHostAddress();
							} catch (UnknownHostException e) {
								ipAddr = "Unresolvable domain name. Chek the server name in Remmina configuration.";
							}
							if (!ipAddr.equals(null)) {
								lists.serverList.add(server);
								lists.userList.add(username);
								System.out.println(" FQDN resolved as IP adress: "+ ipAddr);
								lists.ipList.add(ipAddr);
							}
						}
					} catch (IOException e) {
						System.out.println("Can\'t read remmina config file");
						e.printStackTrace();
					}
				}
			}

		} else {
			System.out.println("Wrong path for remmina config file");
		}
	}

}
