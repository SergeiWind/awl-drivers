package ua.com.shagit.awl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.ini4j.Wini;

/**
 * @author Sergii Shakun
 * If there is a path to Remmina RDP client in config.xml, this class fills Lists with users and servers from Remmina presets
 */
public class Remmina {
	public static final Logger remminaLogger = Logger.getLogger("remminaLogger");

	/**
	 * @return Config.remminaConfigPath
	 * If you need it - path to Remmina directory
	 */
	private String getRemminaConfPath() {
		return Config.remminaConfigPath;
	}

	/**
	 * getting lists of users and servers from remmina config files
	 * @param lists - an instance of class Lists 
	 */
	protected void parseRemminaConfFile (Lists lists) {
		File f = new File(getRemminaConfPath()); 			//Gets the remmina path
		if (f.isDirectory()) {								//if it is ok
			File [] fileList = f.listFiles();				//gets the list of files in it
			for (File file : fileList) {					//fore every file
				if (file.getName().endsWith(".remmina")) {	//with session presets
					try {
						Wini ini = new Wini(new File(file.getAbsolutePath()));			//Let's parse it
						String server = ini.get("remmina", "server", String.class);
						String username = ini.get("remmina", "username", String.class);

						if ((server != null) && (username != null)) {			//if server name and user name are found
							InetAddress iaRemoteAddress = null;
							String ipAddr = null;
							if (remminaLogger.isInfoEnabled()) {
								remminaLogger.info("Server "+server+" found; User "+username+" found.");
							}
							try {														//resolves FQDN into IP
								iaRemoteAddress = InetAddress.getByName(server);
								ipAddr = iaRemoteAddress.getHostAddress();
							} catch (UnknownHostException e) {
								remminaLogger.warn("Unresolvable domain name. (" + server + ") Chek the server name in Remmina configuration.");
							}
							if (ipAddr!=null) {									//if we have an IP and username - let's add them to lists
								lists.serverList.add(server);
								lists.userList.add(username);
								lists.ipList.add(ipAddr);
								remminaLogger.info("FQDN resolved as IP adress: "+ ipAddr);
							}
						}
					} catch (IOException e) {
						remminaLogger.error("Can\'t read remmina config files");
					}
				}
			}

		} else {
			remminaLogger.error("Wrong path for remmina config files");
		}
	}

}
