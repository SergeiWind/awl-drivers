package ua.com.shagit.awl;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Sergii Shakun
 * Class that keeps lists of servers, users and IPs form RDP client config files, also list of active RDP sessions
 */
public class Lists {
	public List<String> serverList = new LinkedList<String>(); 		//list of FQDN from config files
	public List<String> userList = new LinkedList<String>(); 		//list of users from config files
	public List<String> ipList = new LinkedList<String>(); 			//list of resolved ip from FQDN
	public List<String> rdpSessionsList = new LinkedList<String>(); //list of current RDP sessions

}
