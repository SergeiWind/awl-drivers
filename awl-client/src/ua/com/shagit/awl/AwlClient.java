package ua.com.shagit.awl;

import java.util.Scanner;

public class AwlClient {
	public static final String remoteRdpPort = "3389";
	public static final int remoteAwlPort = 3390;
	public static boolean remminaUsing = false;
	public static boolean rdpPresetsFound = false;
	public static String printerName = null;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Awl-drivers started.");
		Remmina rem = new Remmina();
		Lists lists = new Lists();
		SelectPrinter sp = new SelectPrinter();
		sp.getPrinterNameFromConfig();
		rem.setRemminaConfPath();
		
		if (remminaUsing) {
			rem.parseRemminaConfFile(lists);
		}
		if (lists.ipList.size()>0) {
			rdpPresetsFound = true;
		}
		
		if (rdpPresetsFound && (printerName!=null)) {
			MonitorRdpConnections monitor = new MonitorRdpConnections(lists);
			monitor.start();

			Scanner sc = new Scanner(System.in);
			while (true) {
				String command = sc.next();
				if (command.equals("quit")) {
					monitor.finish();
					break;
				}
			}
			sc.close();
		} else {
			System.out.println("No RDP presets found or No local printer is set.");
		}
		System.out.println("Awl-drivers stopped.");
	}

}
