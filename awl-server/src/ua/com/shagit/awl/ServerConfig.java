package ua.com.shagit.awl;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * @author shagit.com.ua
 * <br>
 * Gets <b>config.xml</b> file and sets <b>AwlServer.pdfFolder</b> - path to store <b>.pdf</b> files
 */
public class ServerConfig {
	private final Logger serverConfigLogger = Logger.getLogger("serverConfigLogger");
	protected static ServerConfig configXml;
	protected static String awlPort = "3390";	//default value is 3390
	protected static String bindIp = null;		//default value null - means binding to all IP interfaces
	protected static String pdfFolder = "./";

	/**
	 * Private constructor to avoid of duplicating configuration
	 * It reads config.xml and sets awlServerPort, localPrinterName (if it is described in config.xml), remminaConfigPath
	 */
	private ServerConfig() 
	{
		try {
			File f = new File("config.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(f);
			doc.getDocumentElement().normalize();
			String rootNodeName = doc.getDocumentElement().getNodeName();
			if (serverConfigLogger.isInfoEnabled()) {
				serverConfigLogger.info("Parsing config.xml, root NodeName is "+ rootNodeName);
			}
			String stringTmp = doc.getDocumentElement().getElementsByTagName("awlPort").item(0).getTextContent();
			if ((stringTmp!=null)&&(!"".equals(stringTmp))) {
				awlPort = stringTmp;
			}
			if (serverConfigLogger.isInfoEnabled()) {
				serverConfigLogger.info("Awl Port set to "+ awlPort);
			}
			pdfFolder = doc.getDocumentElement().getElementsByTagName("pdfFolder").item(0).getTextContent();
			if (serverConfigLogger.isInfoEnabled()&&(pdfFolder!=null)&&(!"".equals(pdfFolder))) {
				serverConfigLogger.info("pdfFolder set to "+ pdfFolder);
			} else {
				serverConfigLogger.warn("PdfFolder is not configured. Set as default ./");
			}
			bindIp = doc.getDocumentElement().getElementsByTagName("bindIp").item(0).getTextContent();
			if (serverConfigLogger.isInfoEnabled()&&(bindIp!=null)&&(!"".equals(bindIp))&&(!"*".equals(bindIp))) {
				serverConfigLogger.info("Server will be bind to "+ bindIp);
			} else {
				serverConfigLogger.warn("Server will be bind to all interfaces.");
			}
		} catch (ParserConfigurationException e) {
			serverConfigLogger.error("Error while creating a Document Builder");
		} catch (IOException e) {
			serverConfigLogger.error("Error while parsing file (I/O)");
		} catch (SAXException e) {
			serverConfigLogger.error("Error while parsing file (SAX)");
		}
	}
	/**
	 * Method to get configuration item
	 * @return configXml
	 */
	protected static ServerConfig GetConfigInstance() {
		if (configXml==null) {
			configXml = new ServerConfig();
		}
		return configXml;
	}
}
