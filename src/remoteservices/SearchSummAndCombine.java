package remoteservices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import databaseutils.DbaseEntry;

/*
 * Class to process the details entered at the user interface.
 * 
 * The details entered by the user are passed here from the SequenceServlet
 * class in string format. This is them posted in a URL
 * to a remote webservice which returns an XML file containing
 * text which summarises web articles on the topics chosen by the user.
 * 
 * This XML file is then returned to the main application servlet.
 * 
 */
public class SearchSummAndCombine {

	private DataSource dataSource;
	private Connection conn;
	private boolean debug;

	public SearchSummAndCombine(boolean debug) {
		// Constructor

		this.debug = debug;

		try {
			/*
			 * Get DataSource using details in context.xml
			 */
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			dataSource = (DataSource) envContext.lookup("jdbc/lbldb");

		} catch (NamingException e) {
			e.printStackTrace();
		}

	}

	
	/*
	 * Method which is called by the SequenceServlet to call SSC to
	 * obtain three different word-counts for three different
	 * levels of detail. These are then returned to the UI (JSP) page
	 * for further 'summarising/time' details to be selected, all before
	 * the final job details are submitted
	 */
	public String getSscWc(String strDataForScWc, Properties prop) {
		
		/*
		 * TODO implement call to SSC for WC
		 */
		if(this.debug){
			println("getSscWc in SearchAndCombine called with " + strDataForScWc);
		}
		
		String ipAddress = "";
		String servicePath = "";
		String servicePort = "";
		String serviceTestPath = "";
		String serviceWordC = "";
		String strTimeOut = "";
		String strUrl = "";
		String strUrlTest = "";
		
    	/*
	     * Get required details of IP addresses, etc from properties file.
	     */
		ipAddress = prop.getProperty("sAndCIpAddress");
		servicePath = prop.getProperty("sAndCServicePath");
		servicePort = prop.getProperty("sAndCServicePort");
		serviceTestPath = prop.getProperty("sAndCServiceTestPath");
		serviceWordC = prop.getProperty("sAndCServiceWordCountPath");
		strTimeOut = prop.getProperty("sscTimeoutValue");
		
		/*
		 * Convert string from properties file to useful integer
		 */
		int timeout = 0;
		timeout = Integer.parseInt(strTimeOut);
		
		if(this.debug){
			println("Time-out value for SSC Word-count call is: " + timeout + " ms");
		}	
				
		/*
		 * Construct a URL to first test the remote service for availability
		 */
		strUrlTest = "http://" + ipAddress + ":" + servicePort + "/"
				+ servicePath + "/" + serviceTestPath;

		/*
		 * Construct the URL to call the remote word-count service,
		 * prepend the 'topics with underscores' (passed here as arg)
		 * to a fixed string from the properties file... &null&null&null 
		 */
		strUrl = "http://" + ipAddress + ":" + servicePort + "/"
				+ servicePath + "/" + strDataForScWc + serviceWordC;

		if (this.debug) {
			println("URL for SSC TEST is: " + strUrlTest);
			println("URL for SSC is: " + strUrl);
		}

		/*
		 * Call the remote SSC word-count service
		 */
		if(serviceIsAvailable(strUrlTest)){
			
			long startTime = System.nanoTime();
			
			if (this.debug) {
				println("Calling the Search-Summarise-Combine service now...\n");
			}
			
			String xmlStrSscWCToReturn = CallSummaryService(strUrl, timeout);
		
			println("SSC WC time taken: " + calculateTime(startTime, System.nanoTime()) + " sec");
			
			return xmlStrSscWCToReturn;
			
		}
		else {
			
			return "";
			
		}
		
	}
	
	/*
	 * Method which is called by the SequenceServlet, takes a string which is appended to 
	 * a URL made from variables in the properties file and passes it to a
	 * remote Search-Summarise-Combine (SSC) service. A string of the XML file
	 * is returned.
	 * 
	 * @param int, String, Properties 
	 * 
	 * @return String
	 */
	public String getSscText(int jobId, String strDataForSC, Properties prop) {

		/*
		 * Get 'Search and Combine' service info from properties file for
		 * sending this.
		 */
		String ipAddress = "";
		String servicePath = "";
		String servicePort = "";
		String strUrl = "";
		String strUrlTest = "";
		String serviceTestPath = "";
		String strTimeOut = "";
		int timeout = 0;

		/*
		 * Get required details of IP addresses, etc from properties file.
		 */
			ipAddress = prop.getProperty("sAndCIpAddress");
			servicePath = prop.getProperty("sAndCServicePath");
			servicePort = prop.getProperty("sAndCServicePort");
			serviceTestPath = prop.getProperty("sAndCServiceTestPath");
			strTimeOut = prop.getProperty("sscTimeoutValue");
			
			/*
			 * Convert string from properties file to useful integer
			 */
			timeout = Integer.parseInt(strTimeOut);

			
			if(this.debug){
				println("Time-out value for SSC call is: " + timeout + " ms");
			}	
					
			/*
			 * Construct a URL to first test the remote service for availability
			 */
			strUrlTest = "http://" + ipAddress + ":" + servicePort + "/"
					+ servicePath + "/" + serviceTestPath;

			/*
			 * Construct the URL to call the remote service
			 */
			strUrl = "http://" + ipAddress + ":" + servicePort + "/"
					+ servicePath + "/" + strDataForSC;

			if (this.debug) {
				println("URL for SSC TEST is: " + strUrlTest);
				println("URL for SSC is: " + strUrl);
			}


		if (serviceIsAvailable(strUrlTest)) {

			long startTime = System.nanoTime();
			
			if (this.debug) {
				println("Calling the Search-Summarise-Combine service now...\n");
			}

//			String xmlStrSscToReturn = createLocalDocument();
			println("******** Reading Content XML document from local disk ********");
			String xmlStrSscToReturn = readDocumentFromFile();
			
//			String xmlStrSscToReturn = CallSummaryService(strUrl, timeout);

			String timeTaken = calculateTime(startTime, System.nanoTime());

			/*
			 * Log timeTaken and returnedStr.length() If the XML document is
			 * null, time taken is logged, file length is recorded as 0 (zero).
			 */
			DbaseEntry dbe = new DbaseEntry(this.debug);

			try {

				/*
				 * This call updates the column ssc_status in the table tbl_jobs
				 */
				conn = dataSource.getConnection();

				if (xmlStrSscToReturn != null) {
					dbe.updateSearchAndCombStatus(jobId, conn, timeTaken, xmlStrSscToReturn.length());
				} else {
					dbe.updateSearchAndCombStatus(jobId, conn, timeTaken, 0);
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				System.err
						.println("ERROR in SSC UPDATE in SearchAndCombine class: "
								+ e1.getMessage());

			} finally {
				try {
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

			return xmlStrSscToReturn;

		} else {
			/*
			 * Return an empty string if remote SSC service is not available
			 */
			return "";
		}

	}

	/*
	 * HELPER METHODS - HELPER METHODS - HELPER METHODS HELPER METHODS - HELPER
	 * METHODS - HELPER METHODS
	 * 
	 * Method to print to log, all printed lines are recorded in catalina.out in
	 * the web service container.
	 * 
	 * @param Object 
	 */
	
	
	
	/*
	 * Log printing method
	 */
	private static void println(Object... obj) {
		if (obj.length == 0) {
			System.out.println();
		} else {
			System.out.println(obj[0]);
		}
	}

	/*
	 * Method to convert an XML file to a string, formatting is used if the XML document
	 * is printed for debugging.
	 * 
	 *  @param Document 
	 *  
	 *  @return String
	 */
	private String convertXmlToString(Document xmlDoc) {

		String xmlAsString = "";
		try {

			Transformer transformer;
			transformer = TransformerFactory.newInstance().newTransformer();

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "5");

			StreamResult result = new StreamResult(new StringWriter());

			DOMSource source = new DOMSource(xmlDoc);

			transformer.transform(source, result);

			xmlAsString = result.getWriter().toString();

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return xmlAsString;

	}

		/*
		 * Method to create an XML files during dev,
		 * file is created here.
		 * 
		 * @return Document
		 */
	private Document createLocalDocument() {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		Document contentDoc = docBuilder.newDocument();

		// presentation is the root element
		Element presentation = contentDoc.createElement("presentation");
		contentDoc.appendChild(presentation);

		// content is below the root element
		Element content = contentDoc.createElement("content");
		presentation.appendChild(content);

		// within a presentation, there are (multiple) slides
		Element slide = contentDoc.createElement("slide");
		content.appendChild(slide);

		// within each slide, there are (multiple) sentences
		Element sentence = contentDoc.createElement("sentence");
		sentence.appendChild(contentDoc
				.createTextNode("This is a sample sentence."));
		slide.appendChild(sentence);

		Element sentence2 = contentDoc.createElement("sentence");
		sentence2.appendChild(contentDoc
				.createTextNode("This is another sample sentence."));
		slide.appendChild(sentence2);

		Element sentence3 = contentDoc.createElement("sentence");
		sentence3.appendChild(contentDoc
				.createTextNode("Yet another sample sentence."));
		slide.appendChild(sentence3);

		// then print it...

		if (this.debug) {
			println(convertXmlToString(contentDoc));
		}

		return contentDoc;
	}

	/*
	 * Method which reads an XML document from a file on the
	 * local disk. This is used only during debug/devel and 
	 * when the 'real' remote SSC service is unavailable.
	 *  
	 *  @return String
	 */
	private String readDocumentFromFile() {

//		File mockFile = new File("C:\\Users\\Peter\\workspace\\aaa_temp\\mostafasample.xml");
		
		/*
		 * 0699_NorthStradbrokeIsland.xml
		 * 0723_Obama.xml
		 * 0990_TheSun_rename.xml
		 * 0731_Obama.xml
		 * 
		 */
		
		File mockFile = new File("/var/www/html/lbl/content/212_Waterford.xml");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document docFromDisk = null;

		try {

			dBuilder = dbFactory.newDocumentBuilder();
			dBuilder = dbFactory.newDocumentBuilder();
			docFromDisk = dBuilder.parse(mockFile);

		} catch (ParserConfigurationException | SAXException | IOException e3) {
			e3.printStackTrace();
		}

//		if (this.debug) {
//			println(convertXmlToString(docFromDisk));
//		}

		return convertXmlToString(docFromDisk);
	}

	/*
	 * Method used for calculating time between two long nanosecond
	 * values. It recieves two long numbers which are nanoseconds timestamps, their
	 * difference is returned in seconds as a string.
	 * 
	 *   @param long, long 
	 *   
	 *   @return String
	 */
	private String calculateTime(long startTime, long endTime) {

		DecimalFormat df = new DecimalFormat("#.00");

		double timeTaken = ((endTime - startTime) / 1000000000.0);

		String timeTakenStr = df.format(timeTaken);

		return timeTakenStr;
	}



	/*
	 * Method which checks if the service at the end of a URL is available.
	 * Receives a URL/URI and returns true if response code 200 is receieved
	 * from that address.
	 * 
	 *   @param String
	 *   
	 *   @return boolean
	 */
	private boolean serviceIsAvailable(String urlStrTest) {

		int respCode = 0;
		URL url;

		/*
		 * Time out set to 3 seconds, checked at 4 seconds, response should
		 * be almost instant so this can be hardcoded in this method.
		 */
		int testingTimeout = 3000;
		
		try {
			url = new URL(urlStrTest);

			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("GET");
			huc.setConnectTimeout(testingTimeout);
			huc.setReadTimeout(testingTimeout + 1000);
			huc.connect();
			

			if(this.debug){
				println("Calling test URL now");
			}
			
			respCode = huc.getResponseCode();

			if (this.debug) {
				println("Response code from " + urlStrTest + " is:   "
						+ respCode);
			}

		} catch (MalformedURLException e) {
			
			println("Problem connecting to SSC service: " + e.getMessage());
			
		} catch (SocketTimeoutException s) {
			
			println("Timeout occurred when checking availability of SSC service.");
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}

		if (respCode == 200) {
			return true;
		} else {
			return false;
		}

	}

	/*
	 * Method to call the remote web service. 
	 * 
	 *   @param String, int
	 *   
	 *   @return String
	 */
	private String CallSummaryService(String strUrl, int timeout) {

		String returnedStr = "";
		InputStream in = null;
		URL url;

		try {

			url = new URL(strUrl);

			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("GET");
			
			/*
			 * Timeout is set in the properites file, the set timeout is
			 * then read 1 sec (arbitary) after this to determine if it has been 
			 * exceeded. 
			 */
			huc.setConnectTimeout(timeout);
			huc.setReadTimeout(timeout + 1000);
			
			
			huc.connect();

			/*
			 * Get the content from the huc connection
			 */
			returnedStr = IOUtils.toString(huc.getInputStream(), "UTF-8");

			/*
			 * TODO, removed this when the \" escape chars are removed from the
			 * XML prolog (see Mostafa)
			 */
			returnedStr = returnedStr.replace("\\", "");

		} catch (MalformedURLException e) {
			println("MalformedURLException in CallSummaryService(...) in SSC for URL\n"
					+ strUrl + "\n");
			if (this.debug) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			println("IOException in CallSummaryService(...) in SSC for URL\n"
					+ strUrl + "\n");
			if (this.debug) {
				e.printStackTrace();
			}
		}

		IOUtils.closeQuietly(in);

		if (this.debug) {
			println("Start of XML is\n"
					+ returnedStr.substring(
									0,
									((returnedStr.toString().length() < 150) ? (returnedStr
											.toString().length()) : 150)));
		}

		return returnedStr;

	}
}
