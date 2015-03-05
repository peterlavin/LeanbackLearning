package sequencer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;








import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import config.ErrorTypes;
import remoteservices.SearchSummAndCombine;
import databaseutils.DbaseEntry;

/**
 * Servlet implementation class SequenceServlet
 */
@WebServlet(description = "Main LBL Servlet", urlPatterns = { "/SequenceServlet" }, loadOnStartup=1)
public class SequenceServlet extends HttpServlet {
	
	
	/*
	 * The following variables have to be declared class scope as they need to
	 * be passed to a process which is run in a thread
	 */
	private static final long serialVersionUID = 1L;
	private DataSource dataSource;
	private Connection conn;
	private boolean debug;
	private int jobId;
	int[] partsLocation;
	ArrayList<String> balancedArrayList;
	String audioNamingDetailsParts;
	Properties prop;
	ArrayList<String> configData;
	
	// these will be moved to external class ? TODO
	int initialSet;
	int blockSize;
	
	
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SequenceServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {

		System.out.println("init() has been called in LeanbackLearning SequenceServlet");
		
		/*
		 * Calling super.init means that config can be used anywhere in this
		 * class
		 */
		super.init(config);

		try {

			/*
			 * Get DataSource using details in context.xml
			 * 
			 * maxConnectionAge = "14400" and maxIdleTimeExcessConnections =
			 * "1800" in this file
			 */
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			dataSource = (DataSource) envContext.lookup("jdbc/lbldb");

		} catch (NamingException e) {
			if (true) {
				e.printStackTrace();
			}
		}
		
		
		
		
		Properties prop = new Properties();
		/*
		 * Initialise the properties object for all methods in this servlet
		 */
		try {
			InputStream inStrm = SequenceServlet.class.getClassLoader()
					.getResourceAsStream("/config/properties");
			prop.load(inStrm);
			println("Properties file read successfully in init()");
		} catch (IOException | NullPointerException e) {

			println("Error Initialising properties inputstream");
			e.printStackTrace();
		}

		/*
		 * Read debug (true/false) from properties file
		 */
		debug = Boolean.parseBoolean(prop.getProperty("debug"));
		println("Debug mode is set to '" + debug + "' in init()");
				
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		String responseJsStr = " --- From Servlet --- " + request.getParameter("testArg2");

		System.out.println("doGet in SequenceServlet has been invoked " + responseJsStr);
		
		/*
		 * Write string to response
		 */
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(responseJsStr);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		
		/*
		 * Gets input variable(s) from the request (entered on the index.jsp
		 * form), uses this to create a database entry, this returns an job
		 * number.
		 * 
		 * Trim possible leading/trailing white spaces from user created strings
		 */
		String strIDnum = request.getParameter("idnum").trim();
		String strName = request.getParameter("name").trim();
		String strTopics = request.getParameter("topics").trim();
		String strInitDetail = request.getParameter("init_detail");
		String strOutputLang = request.getParameter("outputlang");
		
		/*
		 * Check that the database is available and that the connection is 'awake'
		 */
		DbaseEntry dbe = new DbaseEntry(this.debug);
		String strJobId = "";
		String responseJsStr = "";
		
		/*
		 * Add the user input parameters to an arraylist for database entry,
		 * then pass to the DbaseEntry object for sending to database.
		 */
		ArrayList<String> dataForDB = new ArrayList<String>();
		dataForDB.add(strIDnum);
		dataForDB.add(strName);
		dataForDB.add(strTopics);
		dataForDB.add(strInitDetail);
		dataForDB.add(strOutputLang);
		
		
		try {
			
			/*
			 * Create a DB entry for this job and get a new job ID number from the database
			 */
			conn = dataSource.getConnection();
			jobId = dbe.CreatInitialDbEntry(conn, dataForDB);

		} catch (Exception e1) {
			
			/*
			 * jobId may be returned as -1 if there is an error with the DB,
			 * or needs to be set to -1 here if the database is unreachable. 
			 */
			jobId = -1;
			
			System.err
					.println("ERROR in initial DB call for new Job ID number: " + e1.getMessage());
			if (debug) {
				e1.printStackTrace();
			}
		} 
		
		if (jobId != -1) {
			
			/*
			 * If the jobId is not -1, a new jobId has been created successfully, therefore
			 * proceed to call SSC to get word-count information about the topics
			 */
			
			if (this.debug) {
				println("jobId created: " + jobId);
			}
			
			/*
			 * Replace all spaces with underscores
			 */
			String strTopicsWithUnderScrs = strTopics.replace(" ", "_");
			
			SearchSummAndCombine ssc = new SearchSummAndCombine(this.debug);

			/*
			 * Initialise the properties object to read word-count multiplier from properties file.
			 * Properties prop is created once and passed as required
			 */
			Properties prop = new Properties();
			try {
				InputStream inStrm = SequenceServlet.class.getClassLoader()
						.getResourceAsStream("/config/properties");
				prop.load(inStrm);
				println("Properties file read successfully in doPost()");
			} catch (IOException | NullPointerException e) {

				println("Error Initialising properties inputstream");
				e.printStackTrace();
			}

			String sscWordCountXML = ssc.getSscWc(strTopicsWithUnderScrs,prop);

			/*
			 * Check that the returned string is valid XML and apply logic to determine
			 * what response is sent. This effectively determines if the SSC response is OK.
			 */
			if (validStringToXMLConversion(sscWordCountXML)) {

				if (this.debug) {
					println("XML WC returned...\n" + prettyFormat(sscWordCountXML));
				}

				/*
				 * Create a JSON object to contain the jobId
				 */
				JSONObject jsJobId = new JSONObject();
				jsJobId.put("jobid", new Integer(jobId));
				
				/*
				 * Convert the word-count XML to another JSON object
				 */
				Document wcXmlDoc = convertStringToDocument(sscWordCountXML);

				JSONObject jsWc = new JSONObject();

				jsWc = convertWcToSecToJson(wcXmlDoc, prop);

				
				JSONArray jsArray = new JSONArray();
				
				jsArray.add(jsJobId);
				jsArray.add(jsWc);
				
				
				if(debug){
					println("jsArray (success) is: " + jsArray);
				}
				
				responseJsStr = jsArray.toJSONString();
				
				/*
				 * TODO, print pretty here to debug
				 */

			}
			else {
				
				// put default SSC failure response into an array with the new jobid
				
				/*
				 * Create a JSON object to contain the jobId
				 */
				JSONObject jsJobId = new JSONObject();
				jsJobId.put("jobid", new Integer(jobId));
				
				if(debug){
					println("jsJobId is: " + jsJobId);
				}
				
				JSONObject jsWc = new JSONObject();
				
				jsWc.put("level_1", "failure");
				
				
				if(debug){
					println("jsWc (failure) is: " + jsWc);
				}
				
				JSONArray jsArray = new JSONArray();
				
				jsArray.add(jsJobId);
				jsArray.add(jsWc);
				
				if(debug){
					println("jsArray (failure) is: " + jsArray);
				}
				
				responseJsStr = jsArray.toJSONString();
				
				
			}

		} else if (jobId == -1) {
			/*
			 * -1 means failure to generate a new jobId in the database.
			 */
			
			JSONObject jsJobIdFail = new JSONObject();
			
			jsJobIdFail.put("jobid", new Integer(jobId));
			
			JSONArray jsArray = new JSONArray();
			
			JSONObject jsSscFail = new JSONObject();
			
			jsSscFail.put("level_1", new String("failure"));
			
			jsArray.add(jsJobIdFail);
			jsArray.add(jsSscFail);
			
			if(debug){
				println("jsArray (DB failure) is: " + jsArray);
			}
			
			responseJsStr = jsArray.toJSONString();
			
		}
		
		/*
		 * Write string to response
		 */
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(responseJsStr);
		
	}

	
	
	
	/*
	 * ///////////////////// Helper methods /////////////////////////
	 * ///////////////////// Helper methods /////////////////////////
	 * ///////////////////// Helper methods /////////////////////////
	 * 
	 *
	 * 
	 * 
	 * 
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
	 * Method which receives a string and returns a boolean depending of whether
	 * the string converts to an XML document.
	 */
	private Boolean validStringToXMLConversion(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Boolean stringIsXML = false;
		try {
			builder = factory.newDocumentBuilder();
			@SuppressWarnings("unused")
			Document newDoc = builder.parse(new InputSource(new StringReader(
					xmlStr)));
			stringIsXML = true;
		} catch (Exception e) {
			if (this.debug) {
				println("String returned from SSC (below) did not convert to XML");
				println(xmlStr);
				stringIsXML = false;
			}
		}
		return stringIsXML;
	}
	
	/*
	 * Method to pretty print an XML string
	 */
	@SuppressWarnings("restriction")
	private String prettyFormat(String unformattedXml) {
		try {
			final Document document = parseXmlFile(unformattedXml);

			OutputFormat format = new OutputFormat(document);
			format.setLineWidth(65);
			format.setIndenting(true);
			format.setIndent(5);
			Writer out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);

			return out.toString();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * Method which receives a string and returns an XML document which is
	 * created from that string.
	 * 
	 * If the string cannot be converted to XML (due to form, etc), a null# is
	 * returned. This is detected by the calling method.
	 */
	private Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document newDoc = builder.parse(new InputSource(new StringReader(
					xmlStr)));

			/*
			 * Required XML file is returned here
			 */
			return newDoc;
		} catch (Exception e) {
			System.out.println("Error with form of XML in SearchAndCombine\n");
			if (this.debug) {
				e.printStackTrace();
			}
		}
		/*
		 * Only if error is found, return null
		 */
		return null;
	}
	
	/*
	 * Used by the above method
	 */
	private Document parseXmlFile(String in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * Converts the SSC Wordcount to Seconds using an empirically obtained
	 * constant (words per second) and puts them in a JSON object
	 */

	@SuppressWarnings("unchecked")
	private JSONObject convertWcToSecToJson(Document wcXmlDoc, Properties prop) {

		/*
		 * Get constant from config/properties file for WC to seconds conversion
		 */
		String wcMultipleStr = prop.getProperty("wordcountMultiple");

		float wcMultipleInt = Float.parseFloat(wcMultipleStr);

		NodeList nList = wcXmlDoc.getElementsByTagName("*");
		println();

		JSONObject jsob = new JSONObject();

		for (int i = 0; i < nList.getLength(); i++) {

			if (nList.item(i).getNodeName().startsWith("level_")) {

				/*
				 * Check that the value to be parsed as an Int is not a NaN string
				 */
				String wcValue = nList.item(i).getTextContent();
				
				if(isStringInteger(wcValue, 10)){
					
				/*
				 * Convert the word-count value to seconds playtime, rounding
				 * happens automatically as the result is an integer
				 */
				int secondsPlaytime = (int) ((Integer.parseInt(nList.item(i).getTextContent()) / wcMultipleInt));

				jsob.put(nList.item(i).getNodeName(), secondsPlaytime);

				}
				else {
					
					jsob.put(nList.item(i).getNodeName(), 0);
					
				}
			} // end for loop

		}

		return jsob;
	}
	
	/*
	 * Check is a string can parse to an Integer, minus numbers not expected
	 * but are dealt with
	 */
	private boolean isStringInteger(String s, int radix) {
			
		    if(s.isEmpty()){

		    	return false;
		    	
		    }
		    
		    for(int i = 0; i < s.length(); i++) {
		    	
		    	/*
		    	 * Allows for a string to start with -, e.g. -123
		    	 */
		        if(i == 0 && s.charAt(i) == '-') {
		        	
		        	/*
		        	 * But if it's only '-', then it's not a nunber
		        	 */
		            if(s.length() == 1) {
		            	return false;
		            }
		            else {
		            	continue;
		            }
		        }
		        
		        /*
		         * All alpha chars return -1, all numeric chars return the actual number
		         */
		        if(Character.digit(s.charAt(i),radix) < 0) {
		        	
		        	if(debug){
		        		println(" This NON-numeric char found in word-count XML: " + s.charAt(i));
		        	}
		        	
		        	return false;
		        }
		    }
		    
		    return true;
		    
		}
	
}



