package ua.com.shagit.awl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.ini4j.Wini;

/**
 * @author Sergii Shakun
 * If there is a path to Remmina RDP client in config.xml, this class fills Lists with users and servers from Remmina presets
 */
public class Remmina {
	String remminaConfPath; 				//path to Remmina directory
	
	public String getRemminaConfPath() {	//If you need it - path to Remmina directory
		return this.remminaConfPath;
	}

	/**
	 * Method reads config.xml,parses it and if <remmina></remmina> section is found - sets the remminaConfPath and remminaUsing
	 * XXX 1. XML parsing shoud be implemented using javax or something like that
	 * XXX 2. This shoud be done in class constructor 
	 */
	public void setRemminaConfPath() { 		// Reading remminaConfPath from config.xml
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

	/**
	 * getting lists of users and servers from remmina config files
	 * @param lists - an instance of class Lists 
	 */
	public void parseRemminaConfFile (Lists lists) {
		File f = new File(getRemminaConfPath()); 			//Gets the remmina path
		if (f.isDirectory()) {								//if it is ok
			File [] fileList = f.listFiles();				//gets the list of files in it
			for (File file : fileList) {					//fore every file
				if (file.getName().endsWith(".remmina")) {	//with session presets
					try {
						Wini ini = new Wini(new File(file.getAbsolutePath()));			//Let's parse it
						String server = ini.get("remmina", "server", String.class);
						String username = ini.get("remmina", "username", String.class);

						if (!server.equals(null) && !username.equals(null)) {			//if server name and user name are found
							InetAddress iaRemoteAddress = null;
							String ipAddr = null;
							System.out.print("Server "+server+" found; User "+username+" found.");
							try {														//resolves FQDN into IP
								iaRemoteAddress = InetAddress.getByName(server);
								ipAddr = iaRemoteAddress.getHostAddress();
							} catch (UnknownHostException e) {
								ipAddr = "Unresolvable domain name. Chek the server name in Remmina configuration.";
							}
							if (!ipAddr.equals(null)) {									//if we have an IP and username - let's add them to lists
								lists.serverList.add(server);
								lists.userList.add(username);
								System.out.println(" FQDN resolved as IP adress: "+ ipAddr);
								lists.ipList.add(ipAddr);
							}
						}
					} catch (IOException e) {
						System.out.println("Can\'t read remmina config file"); 			//somthing wrong is with remmina config file
						e.printStackTrace();
					}
				}
			}

		} else {
			System.out.println("Wrong path for remmina config file");		//not a directory in config.xml in <remmna></remmina> section
		}
	}

}
