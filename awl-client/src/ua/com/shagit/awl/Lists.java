package ua.com.shagit.awl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sergei <br>
 * Data class that keeps lists of servers, users and IPs form client config files, also list of active RDP sessions
 */
public class Lists {
	public List<String> serverList = new ArrayList<String>(); //list of servers from config files
	public List<String> userList = new ArrayList<String>(); //list of users from config files
	public List<String> ipList = new ArrayList<String>(); //list of resolved ip of servernames
	public List<String> rdpSessionsList = new ArrayList<String>(); //list of current RDP sessions

}
