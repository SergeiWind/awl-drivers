package ua.com.shagit.awl;

public class AwlServer {
	public static final String remoteAwlPort = "3390";
	public static String pdfFolder = null;

	public static void main(String[] args) {
		System.out.println("Awl-drivers started.");
		
		ServerConfig cfg = new ServerConfig();
		cfg.ParseConfig();
		if (pdfFolder==null) {
			System.out.println("Error. Stopping awl-driver.");
			return;
		}
		
		Listnerer listnerer = new Listnerer ();
		listnerer.start();
		
		
	}

}
