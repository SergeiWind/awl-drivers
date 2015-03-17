package ua.com.shagit.awl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * @author shagit.com.ua
 * <br>
 * Gets <b>config.xml</b> file and sets <b>AwlServer.pdfFolder</b> - path to store <b>.pdf</b> files
 */
public class ServerConfig {

	public void ParseConfig () {
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
			elements = "config/pdffolder".split("/");
			for (String element : elements) {
				int start = XMLString.indexOf("<"+element+">");
				int end = XMLString.indexOf("</"+element+">", start+2+element.length());
				XMLString = XMLString.substring(start+2+element.length(), end);
			}
			if (!XMLString.equals("")) {
				System.out.println("PDF folder is set on "+XMLString);
			} else {
				System.out.println("PDF folder is not configured in config.xml.");
			}
			fr.close();
			AwlServer.pdfFolder = XMLString;
		} catch (FileNotFoundException e) {
			System.out.println("config.xml not found.");
		} catch (IOException e) {
			System.out.println("Can\'t read config.xml.");
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Can\'t parse config.xml.");
		}
	}
}
