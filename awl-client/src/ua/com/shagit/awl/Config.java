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
 * @author Sergii Shakun
 * Class that contains configuration from Config.xml 
 */
public class Config {
	public static final Logger configLogger = Logger.getLogger("configLogger");
	protected static Config configXml;
	protected static String rdpPort = "3389";		//default value is 3389
	protected static String awlPort = "3390";		//default value is 3390
	protected static String localPrinterName;
	protected static String remminaConfigPath;
	protected static boolean remminaUsing;
	protected static boolean printerSelected;
	/**
	 * Private constructor to avoid of duplicating configuration
	 * It reads config.xml and sets rdpPort, awlPort, localPrinterName (if it is described in config.xml), remminaConfigPath
	 */
	private Config() 
	{
		try {
			File f = new File("config.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(f);
			doc.getDocumentElement().normalize();
			String rootNodeName = doc.getDocumentElement().getNodeName();
			if (configLogger.isInfoEnabled()) {
				configLogger.info("Parsing config.xml, root NodeName is "+ rootNodeName);
			}
			String stringTmp = doc.getDocumentElement().getElementsByTagName("RdpPort").item(0).getTextContent();
			if ((stringTmp!=null)&&(stringTmp!="")) {
				rdpPort=stringTmp;
			}
			if (configLogger.isInfoEnabled()) {
				configLogger.info("RdpPort set to "+ rdpPort);
			}
			stringTmp = doc.getDocumentElement().getElementsByTagName("AwlPort").item(0).getTextContent();
			if ((stringTmp!=null)&&(stringTmp!="")) {
				awlPort=stringTmp;
			}
			if (configLogger.isInfoEnabled()) {
				configLogger.info("AwlPort set to "+ awlPort);
			}
			localPrinterName = doc.getDocumentElement().getElementsByTagName("LocalPrinterName").item(0).getTextContent();
			if (configLogger.isInfoEnabled()&&(localPrinterName!=null)&&(localPrinterName!="")) {
				configLogger.info("LocalPrinterName set to "+ localPrinterName);
			}
			if (localPrinterName==null || localPrinterName=="") {
				configLogger.warn("Local Printer is not configured in config.xml");
			} else {
				printerSelected = true;
			}
			remminaConfigPath = doc.getDocumentElement().getElementsByTagName("RemminaConfigPath").item(0).getTextContent();
			if (configLogger.isInfoEnabled()&&(remminaConfigPath!=null)&&(remminaConfigPath!="")) {
				configLogger.info("Remmina config path set to "+ remminaConfigPath);
			}
			if (remminaConfigPath==null || remminaConfigPath=="") {
				configLogger.warn("Remmina config path is not configured in config.xml");
			} else {
				remminaUsing = true;
			}
		} catch (ParserConfigurationException e) {
			configLogger.error("Error while creating a Document Builder");
		} catch (IOException e) {
			configLogger.error("Error while parsing file (I/O)");
		} catch (SAXException e) {
			configLogger.error("Error while parsing file (SAX)");
		}
	}
	/**
	 * Method to get configuration item
	 * @return configXml
	 */
	protected static Config GetConfigInstance() {
		if (configXml==null) {
			configXml = new Config();
		}
		return configXml;
	}
}
