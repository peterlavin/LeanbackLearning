package sequencer;

import globicutils.LogToGlobic;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import config.ErrorTypes;
import remoteservices.SearchSummAndCombine;
import remoteservices.SpeechSynthesisParts;
import storageutils.FileToDisk;
import xmlutils.BalanceSentencesUtils;
import xmlutils.GetVisualDataTitleFromXML;
import xmlutils.GlobicMetricsToArrList;
import xmlutils.visualDataToJSON;
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
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Second call to SSC
		 * 
		 * This (doGet) method is called from the UI of Leanback Learning when the
		 * user has finalised their time and 'level of detail' preferences.
		 * 
		 * SuppressWarnings is added above as JSONObject cannot be parameterised 
		 */
		
		
		/*
		 * Boolean variable used to catch success/failure throughout. All
		 * actions are predicated this remaining as true.
		 */
		boolean overallSuccess = false;

		/*
		 * Boolean variables to decide (in conjunction with overallSuccess) if
		 * data is to be sent to GLOBIC. Variable globicStorageSuccess becomes
		 * true if the content file is successfully saved for fetching by
		 * GLOBIC. Variable globicDataFetchSuccess becomes true if a '200' is
		 * returned from GLOBIC, indicating that details of an individual job
		 * are processed by it, thereby allowing the deletion of the files which
		 * were stored for use by GLOBIC.
		 */
		boolean globicStorageSuccess = false;
		boolean globicDataFetchSuccess = false;

		/*
		 * String which can be set to appropriate text when and if the different
		 * types of errors occurr. The String errorType is used for user
		 * feedback and debug
		 */
		String errorType = ErrorTypes.error_0;

		/*
		 * Create the Properties object for reading from disk, this is used for
		 * multiple file reads throughout this method and methods it calls.
		 * 
		 * It is initialised later. Failure to initialise this terminates a job
		 */
		prop = new Properties();

		/*
		 * Initialise the properties object, failure or error while to do so
		 * sets overallSuccess to false
		 */

		try {
			InputStream inStrm = SequenceServlet.class.getClassLoader()
					.getResourceAsStream("/config/properties");
			prop.load(inStrm);
			println("Properties file read successfully in doGet()");
			overallSuccess = true;
		} catch (IOException | NullPointerException e) {

			overallSuccess = false;

			println("Error initialising properties inputstream");

			/*
			 * Writes a hard coded error that does not rely on the 'failed' prop
			 * object
			 */
			writePropErrorToResponse(response);

			e.printStackTrace();
		}

		// ///////////////////////////////////////////////
		//
		// PROCESSING OF THE JOB STARTS HERE...
		//
		// ///////////////////////////////////////////////

		/*
		 * Gets input variable(s) from the request (entered on the index.jsp
		 * form), uses this to create a database entry, this returns an job
		 * number.
		 * 
		 * Trim possible leading/trailing white spaces from user created strings
		 */
		
		
		
		String strIdNum = request.getParameter("idnum").trim();
		String strName = request.getParameter("name").trim();
		String strJobId = request.getParameter("jobid").trim();
		String strTopics = request.getParameter("topics").trim();
		String strTime = request.getParameter("time");
		String strFinalDetail = request.getParameter("final_detail");
		String strOutputLang = request.getParameter("outputlang");

		if (this.debug) {
			println("Parameters...\n" + 
					strIdNum + " : " +
					strName + " : " +
					strJobId + " : " +
					strTopics + " : " +
					strTime + " : " + 
					strFinalDetail + " : " + 
					strOutputLang);
		}

		// ////////////////////////////////////////////////////////////
		//
		// UPDATE FINAL USER DETAIL AND TIME PREFERENCES TO THE DATABASE
		//
		// /////////////////////////////////////////////////////////////


		DbaseEntry dbe = new DbaseEntry(this.debug);

		
		if (overallSuccess) {
			
		try {

				conn = dataSource.getConnection();
				dbe.updateFinalUserPrefs(conn, strJobId, strFinalDetail, strTime);
			
		} catch (Exception e1) {
			System.err
					.println("ERROR in Final Prefs DB UPDATE: " + e1.getMessage());
				if (debug) {
					e1.printStackTrace();
				}
			}
						
		} // end of overallSuccess if-stm for initial jobId database call

		
		/*
		 * Check that the topics string is not empty, all other fields may be
		 * empty or have default values.
		 * 
		 * This is ALSO validated in the web page.
		 */

		if (strTopics.equalsIgnoreCase("") && overallSuccess) {

			overallSuccess = false;

			errorType = ErrorTypes.error_3;

			writeErrorToResponse(response, errorType, prop);
			/*
			 * Sequence for a job will end here if this code is executed
			 */

		}
		
		
		// /////////////////////////////////////////////////
		//
		// CALL THE SEARCH, SUMMARISE & COMBINE SERVICE
		//
		// //////////////////////////////////////////////////

		/*
		 * Call the remote service (NB, dependent on overallSuccess) and update
		 * database with the status of the job after the SCC web service call
		 * 
		 * Pass the above variables to the Search-Summarise-Compile (WS)
		 * component in an ArrayList of strings.
		 * 
		 * NB: name of user is not passed.
		 */
		String namingDetails = "";
		String contentNamingDetails = "";

		String metaContentNamingDetails = "";
		Document sAndCxml = null;

		String sAndCText = "";
		JSONArray visDataArrayJs = null;
		/*
		 * String title is extracted from the visual element
		 * in the sAndCxml from SSC and passed to the UI in
		 * the returned JSON array for use in the Treemap.
		 */
		String title = "";
				
		/* Create an arrayList containing the data which it will
		* contain.
		*/
		ArrayList<String> dataArrayGlobic = new ArrayList<String>();

		/*
		 * At this point, if overallSuccess is true, proceed to prepare for and
		 * call the Search-Summarise-Combine service
		 */
		if (overallSuccess) {

			/*
			 * Integer variable used for word count of returned SSC XML document,
			 * recorded in database if SSC part of job is successful
			 */

			// ArrayList<String> dataForSC = new ArrayList<String>();
			//
			// dataForSC.add("topics:" + strTopics);
			// dataForSC.add("time:" + strTime);
			// dataForSC.add("detail:" + strDetail);
			// dataForSC.add("langentered:" + strLangEntered);
			// dataForSC.add("outputlang:" + strOutputLang);

			String strTopicsWithUnderScrs = strTopics.replace(" ", "_");

			/*
			 * Changed to now only use 4 arguments, 'Language entered' no longer
			 * used
			 */
			String strDataForSC = strTopicsWithUnderScrs + "&" + strTime + "&"
					+ strFinalDetail + "&" + strOutputLang;

			/*
			 * ACTUAL CALL FOR SSC SERVICE
			 */
			SearchSummAndCombine sac = new SearchSummAndCombine(this.debug);

			sAndCText = sac.getSscText(jobId, strDataForSC, prop);
			
		} // end of overallSuccess if stm for calling SSC
		
		
		/*
		 * Check that the String returned from SSC converts to valid XML
		 */
		if(overallSuccess){
			
		
			/*
			 * Check XML is valid, if not abort job
			 */
			if(validStringToXMLConversion(sAndCText)){
				
				/*
				 * Convert the returned string to XML for storage to disk 
				 * and ready for being accessible to GLOBIC)
				 */
				sAndCxml = convertStringToDocument(sAndCText);
				
				overallSuccess = true; 
				
				if (this.debug) {
					println("Valid XML returned from SearchAndCombine class"); 
					}
				
				
				
				/*
				 * Pass returned XML doc to class to convert the (GLOBIC) metrics
				 * to an arrayList for insertion later to the meta...xml file
				 */
				
				GlobicMetricsToArrList globArrList = new GlobicMetricsToArrList();
				dataArrayGlobic = globArrList.metricsToArrList(dataArrayGlobic, sAndCxml, this.debug);
				
				/*
				 * Now pass the same XML file to a class to convert the visual data
				 * to a JSON array for insertion to the JSON returned to the UI.
				 * 
				 * This JSONArray is added to the JSON array that is returned to 
				 * the UI if there are no errors and overallSuccess stays true. 
				 */
				visualDataToJSON vdj = new visualDataToJSON();
				visDataArrayJs =  vdj.convertXmlToJson(sAndCxml, debug);
				
				/*
				 * Get the title from the XL file, this is used in the JSON returned
				 * to the UI 
				 */
				GetVisualDataTitleFromXML gvdt = new GetVisualDataTitleFromXML();
				title = gvdt.getTitleFromXml(sAndCxml, debug);
				
				
			}
			else {
				
				overallSuccess = false;
				errorType = ErrorTypes.error_4;
				writeErrorToResponse(response, errorType, prop);
				println("XML returned from SSC was INVALID\n" + sAndCText);
				
			}
		
		}
		
		
		/*
		 * Check if (for any reason) word count of this XML 
		 * is zero (it may still be valid XML and be empty)
		 */
		int xmlWordCount = 0;
		
		if(overallSuccess){
			
			/*
			 * Get word-count for XML file returned.
			 */
			xmlWordCount = getWordCountXml(sAndCxml);
			
			if(xmlWordCount == 0){
				overallSuccess = false;
				errorType = ErrorTypes.error_4;
				writeErrorToResponse(response, errorType, prop);
				println("XML returned from SSC contained no words/content");
				
			}
			
		}
			
		/*
		 * Proceed with processing the returned XML file
		 */

		if (overallSuccess && sAndCText.toLowerCase().contains("no result found")) {
			overallSuccess = false;
			errorType = ErrorTypes.error_5;

			writeErrorToResponse(response, errorType, prop);

			if (this.debug) {
				println("String returned is: " + sAndCText);
			}
		}

		else if (overallSuccess && sAndCText.toLowerCase().contains("disambiguation")) {
			overallSuccess = false;
			errorType = ErrorTypes.error_7;

			writeErrorToResponse(response, errorType, prop);

			if (this.debug) {
				println("String returned is: " + sAndCText);
			}
		}
		
		else if (overallSuccess) {  // For now, no other option is handled FIXME

			/*
			 * First, generate names for the (XML content and audio) files
			 * to be saved to disk. These are used later to create the URLs
			 * for access to these files by GLOBIC and the media player.
			 */
			namingDetails = concatNameDetails(strJobId, strTopics);

			/*
			 * Add appropriate extensions to make a file name
			 */
			contentNamingDetails = addContentExtn(namingDetails, prop);

			/* Variable used when creating the playlist Hashmap */
			audioNamingDetailsParts = addAudioParts(namingDetails,
					strOutputLang, prop);

			// audioNamingDetails = addAudioExtn(namingDetails,
			// strOutputLang, prop);

			metaContentNamingDetails = prependMetaToContentName(
					contentNamingDetails, prop);

			/*
			 * Save the XML document to disk, available to be served
			 * over HTML to GLOBIC (at end of this method).
			 * 
			 * True/False returned here, depending on success or not of
			 * saving of the XML text content and metadata file to disk for
			 * GLOBIC. Failure is NOT fatal to the over all job success. A
			 * false value prevents GLOBIC logging being attempted at the
			 * end of the sequence.
			 */
			FileToDisk ftd = new FileToDisk(this.debug);
			globicStorageSuccess = ftd.storeContentFilesToDisk(sAndCxml,
					contentNamingDetails, prop);

			
			/*
			 * Create an XML file which contains metadata for this job, this
			 * is then stored to disk, available for GLOBIC to fetch it from
			 * this location.
			 *
			 * Get audio file URL parts from properties
			 */
			String strContentServerIP = "";
			String strContentUrlPath = "";
			String strContentUrl = "";

			strContentServerIP = prop.getProperty("contentServerIP");
			strContentUrlPath = prop.getProperty("contentUrlPath");

			strContentUrl = "http://" + strContentServerIP + "/"
					+ strContentUrlPath + "/" + contentNamingDetails;

			if (this.debug) {
				println("Content URL is... " + strContentUrl);
			}
			
			/*
			 * Add further data to the arrayList for GLOBIC loggin
			 */
			dataArrayGlobic.add("contenturl#######" + strContentUrl);
			dataArrayGlobic.add("topics#######" + strTopics);
			dataArrayGlobic.add("outputlang#######" + strOutputLang);

			/*
			 * Convert this ArrayList<String> to XML, this approach is used
			 * so that further meta data can be added here without changing
			 * the code used to create the XML
			 */
			Document metaSAndCxml = metaDataToXML(dataArrayGlobic);

			/*
			 * Store this file to disk, a boolean is returned to indicate if
			 * the storage operation was successful.
			 */
			globicStorageSuccess = ftd.storeContentFilesToDisk(
					metaSAndCxml, metaContentNamingDetails, prop);

			if (globicStorageSuccess) {
				println("XML content file stored successfully for GLOBIC");
			} else {
				println("False returned from XML content file storage for GLOBIC");
			}

		}
	
		
		/*
		 * At the end of the interaction with the SSC service, the database
		 * is updated with the status of the SSC step
		 */
		
		if(overallSuccess){
		
			try {
	
				/*
				 * Update the column ssc_status in the table tbl_jobs
				 */
				conn = dataSource.getConnection();
				dbe.updateSscStatus(conn, jobId, overallSuccess, xmlWordCount);
	
			} catch (Exception e1) {
				System.err
						.println("ERROR in SSC DB UPDATE: " + e1.getMessage());
				if (debug) {
					e1.printStackTrace();
				}
			} 
			
		}
		
		/////////////////////////////////////////////////////////
		//
		// CONVERT THE RETURNED FROM SSC TO SPEECH AUDIO FILE(s)
		//
		/////////////////////////////////////////////////////////
		
		int numberOfBalSentences = 0;
		
		/*
		 * Need number of parts here to make the playlist response
		 */
		BalanceSentencesUtils bal = new BalanceSentencesUtils(this.debug);
		
		
		if (overallSuccess) {

			/*
			 * The SSC XML is now available for conversion to audio.
			 * 
			 * This method processes the SSC XML in parts
			 */

			/*
			 * First, balance the sentence lengths in the XML file
			 */
			balancedArrayList = bal.balanceSentences(sAndCxml);
			
			
			numberOfBalSentences = balancedArrayList.size();
			
			if(numberOfBalSentences==0){
				/*
				 * Although, the method should not get called if there are zero
				 * values for wordcount (obtained in the doPost method), this
				 * checks that there is at least one sentence in the arrayList
				 * to be processed, if not, abort using the overallSuccess boolean
				 */
				errorType = ErrorTypes.error_4;
				writeErrorToResponse(response, errorType, prop);
				overallSuccess = false;
			}
			
		}
		
		if(overallSuccess) {

			if (this.debug) {
				println("numberOfBalSentences is: " + numberOfBalSentences);
			}

			/*
			 * Read initialSet and blockSize from properties file, with default
			 * values also set
			 * 
			 * NOTES: initialSet is the number of sentences (taken from the
			 * balance array list) which will be processed as an 'initial set'.
			 * This number must be odd, so for initialSet = 5, there will be
			 * three blocks processed, one of 1, the first sentence, and two
			 * further blocks of 2 sentences. Three blocks for 5 sentences in
			 * 1-2-2 chunks.
			 * 
			 * The blockSize is the number of sentences which are processed each
			 * call to SS after the initial set has been done.
			 */
			String strInitialSet = prop.getProperty("initialSet", "7");
			String strBlockSize = prop.getProperty("blockSize", "6");

			/*
			 * Convert string from properties file to useful integer
			 */
			initialSet = Integer.parseInt(strInitialSet);
			blockSize = Integer.parseInt(strBlockSize);

			/*
			 * Following two methods implement logic to change initialSet and
			 * blockSize to be within predefined limits and to make sure they
			 * are even/odd as required.
			 */
			initialSet = validateInitialSet(initialSet);
			blockSize = validateBlockSize(blockSize);

			if (this.debug) {
				println("initialSet determined to be: " + initialSet);
				println("blockSize determined to be: " + blockSize);
			}

			/*
			 * Get the number of parts which will be in the playlist for this
			 * XML file.
			 */
			
			int numberInPlaylist = 0;
			
			// <=, less than or equal, means that the initialSet caters for all of the sentences
			if(numberOfBalSentences <= initialSet){
				
				numberInPlaylist = bal.getInitialSetPartsOnly(initialSet, numberOfBalSentences);
				
			}
			
			// >, greater than, not equal, means there is at least one sentence left over when the intitialSet is taken away
			if(numberOfBalSentences > initialSet){
				
				numberInPlaylist = bal.getPlayListNumberOfBlocks(blockSize,numberOfBalSentences, initialSet) + bal.getInitialSetPartsOnly(initialSet);
				
			}
			
			/*
			 * Create an array which contains the part location of each line in
			 * the balancedXML file. The array is equal in size to the number of
			 * sentences in the balancedXML file and each entry contains a
			 * number referring to which part that line is in
			 */
			partsLocation = new int[numberOfBalSentences];

			for (int b = 0; b < numberOfBalSentences; b++) {

				/*
				 * for each b, call a method to determining what number of parts
				 * there would be for a presentation with that number of lines,
				 * this gives the location of each line
				 */
				partsLocation[b] = bal.getPartLocation(b + 1, initialSet,
						blockSize);

			}
			/*
			 * Get number of unique numbers in this array and check it against
			 * the number of parts in the array
			 */

			int numUniquePartLocations = bal.getNumUniquePartNums(partsLocation);

			if (debug) {
				println(numUniquePartLocations + " : " + numberInPlaylist
						+ " These numbers should be the same");

				if (numUniquePartLocations != numberInPlaylist) {
					println("Difference - PROBLEM with number of parts in playlist\n");
				}
			}
			
			/*
			 * Create and populate JSON for playlist, contains a title and full
			 * URL to each of the MP3 files in the playlist.
			 * 
			 * Make a JSON object instead of a hashmap
			 */
			JSONArray jsonArrayPlist = createPlaylistJson(numberInPlaylist,audioNamingDetailsParts, prop);
			
			/*
			 * Create and popluate a JSON for the word count of the original XML content
			 * file retruned from SSC. This is used to estimate the number of seconds
			 * in the presentation.
			 */
			String wcMultipleStr = prop.getProperty("wordcountMultiple");

			float wcMultipleFloat = Float.parseFloat(wcMultipleStr);

			/*
			 * Calculate the predicted seconds using a constant multiple
			 */
			int predictedSeconds = (int) ((xmlWordCount / wcMultipleFloat));
			
			
			if(debug){
				println("WC from original XML is: " + xmlWordCount);			
				println("predictedSeconds is: " + predictedSeconds);
			}
			
								
			JSONObject jsWC = new JSONObject();
			
			jsWC.put("seconds", new Integer(predictedSeconds));
			
			JSONArray returnedJsonArray = new JSONArray();
					
			JSONObject jsTitle = new JSONObject();
			
			jsTitle.put("title", title);

			returnedJsonArray.add(0, jsWC);
			returnedJsonArray.add(1, jsonArrayPlist);
			returnedJsonArray.add(2, visDataArrayJs);
			returnedJsonArray.add(3, jsTitle);
			
			
			/*
			 * Create an ArrayList which contains the required output language
			 * as received from the user's entry at the UI.
			 */
			configData = new ArrayList<String>();
			configData.add("outputlang:" + strOutputLang);

			/*
			 * Part number is always 1 for this part
			 */
			int partNumberOne = 1;

			/*
			 * Add the partNumber and file extn to the audioNamingDetailsParts
			 * for file storage
			 */
			String audioFileExtension = prop.getProperty("audioFileExtension");
			String fileName = audioNamingDetailsParts + partNumberOne + "."
					+ audioFileExtension;

			/*
			 * First process the first item (first XML line) of
			 * balancedArrayList, this string is placed in a single item
			 * ArrayList to allow universal use of the method called here
			 */

			ArrayList<String> firstSentenceAlist = new ArrayList<String>();

			String firstSentence = balancedArrayList.get(0);
			
			/*
			 * Calls improvised code to remove first brackets from the first sentence
			 * as these often contain non-ascii chars which cause problems in
			 * the sp-syn service.
			 */

			String cleanedSentence = cleanTranslationAids(firstSentence);
			
			/*
			 * This cleaned sentence is then added to the array list which is sent for speech syn
			 */
			firstSentenceAlist.add(cleanedSentence);

			/*
			 * Get the word count for this (small) array list
			 */
			int partWc = getWordCountArList(firstSentenceAlist);

			/*
			 * Note start time, and calculate after call to speech synthesis (3
			 * lines below)
			 */
			long startTime = System.nanoTime();

			SpeechSynthesisParts spSynPts = new SpeechSynthesisParts(this.debug);

			/*
			 * The boolean returned is crucial to the overall success of the job
			 */
			try {
				if(conn.isClosed()){
					conn = dataSource.getConnection();
				}
			} catch (SQLException e3) {
				e3.printStackTrace();
			}
			
			overallSuccess = spSynPts.processXmlFirstLine(jobId, partNumberOne, firstSentenceAlist, fileName, conn, prop, configData);
//			overallSuccess = false;

			String ssTimeTaken = calculateTime(startTime, System.nanoTime());

			int mp3PlayTime = spSynPts.getMP3FileDuration(fileName, prop);

			/*
			 * Deal with the returned boolean from Speech Synthesis
			 */
			if (!overallSuccess) {

				/*
				 * Return string of error for feedback to the user
				 */
				errorType = ErrorTypes.error_6;

				writeErrorToResponse(response, errorType, prop);

				/*
				 * Add overallSuccess failure status (at this point) to
				 * database,
				 */
				try { // for failure of speech syn

					conn = dataSource.getConnection();
					dbe.updateAudioFileStatus(jobId, conn, numberInPlaylist,
							overallSuccess);

				} catch (Exception e1) {
					System.err.println("ERROR in Audio status UPDATE: "
							+ e1.getMessage());
					if (debug) {
						e1.printStackTrace();
					}
				} 
				
				/*
				 * With failure of SS, update the tbl_parts table for part 1
				 */
				try {
					
					if(conn.isClosed()){
						conn = dataSource.getConnection();
					}
					dbe.updateSpSynStatusFirstPart(jobId, conn, partNumberOne,
							blockSize, initialSet, firstSentenceAlist.size(),
							partWc, ssTimeTaken, overallSuccess);
				} catch (SQLException e2) {
					e2.printStackTrace();
				} 
				

			} else { // i.e. overallSuccess is true after call to speech syn

				/*
				 * Call method to write response, using the partNumber
				 */
				println("Writing (first part) response for: "
						+ audioNamingDetailsParts + partNumberOne + "\n");

				String json = new Gson().toJson(returnedJsonArray);
				response.setContentType("text/plain");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(json);

				/*
				 * Get the duration of the file created, then update the
				 * database, adding the file, duration, name and value of
				 * overallSuccess
				 */
				int mp3Duration = 0;
				mp3Duration = spSynPts.getMP3FileDuration(
						audioNamingDetailsParts + partNumberOne + "."
								+ audioFileExtension, prop);

				/*
				 * Database update, updates an entry in tbl_jobs and details for
				 * the first part in tbl_parts
				 */
				try {

					/*
					 * Update the main DB table (tbl_jobs) for the overall job
					 */
					conn = dataSource.getConnection();
					dbe.updateAudioFileStatus(jobId, conn, overallSuccess,
							audioNamingDetailsParts, mp3Duration,
							numberInPlaylist);



				} catch (Exception e1) {
					System.err.println("ERROR in Audio status UPDATE: "
							+ e1.getMessage());
					if (debug) {
						e1.printStackTrace();
					}
				} 
				
				
				/*
				 * Database update, updates an entry in tbl_parts and details for
				 * the first part in tbl_parts
				 */
				try {

				/*
				 * Update the tbl_parts table for this particular part (i.e.
				 * part 1)
				 */
				if(conn.isClosed()){
					conn = dataSource.getConnection();
				}
					
				dbe.updateSpSynStatusFirstPart(jobId, conn, partNumberOne,
						blockSize, initialSet, firstSentenceAlist.size(),
						partWc, ssTimeTaken, overallSuccess, mp3PlayTime);



				} catch (Exception e1) {
					System.err.println("ERROR in Audio status UPDATE: "
							+ e1.getMessage());
					if (debug) {
						e1.printStackTrace();
					}
				}
				
			} // End of if-stm for dealing with outcome of Speech Syn

		} // End of overallSuccess if-stm for calling Speech Syn

		// /////////////////////////////////////////////////
		//
		// LOG OVERALL DETAILS OF THE JOB TO GLOBIC
		//
		// //////////////////////////////////////////////////
		if (overallSuccess && globicStorageSuccess) {

			String strContentServerIP = "";
			String strContentUrlPath = "";

			/*
			 * Get file extension from config.properties file
			 */
			strContentServerIP = prop.getProperty("contentServerIP");
			strContentUrlPath = prop.getProperty("contentUrlPath");

			/*
			 * Create a URL to send to GLOBIC from which it can fetch the
			 * metadata file.
			 */
			String strContentMetaUrl = "http://" + strContentServerIP + "/"
					+ strContentUrlPath + "/" + metaContentNamingDetails;

			if (this.debug) {
				println("Link passed to GLOBIC call method is: "
						+ strContentMetaUrl);
			}

			/*
			 * Pass the jobId and the URL (in string format) to a class which
			 * sends it to GLOBIC.
			 */
			LogToGlobic ltg = new LogToGlobic(this.debug);
//			globicDataFetchSuccess = true; 
			
			globicDataFetchSuccess = ltg.logDataWithGlobic(strContentMetaUrl,prop);

		}

		/*
		 * TODO, decide if these files are to be deleted up anyway, success or
		 * not???
		 */
		if (globicDataFetchSuccess) {

			// TODO uncomment these lines
			// TODO
			// TODO
			// TODO
			// TODO
			// TODO

			FileToDisk ftdDel = new FileToDisk(this.debug);
			//ftdDel.deleteContentFiles(contentNamingDetails, prop);
			//ftdDel.deleteContentFiles(metaContentNamingDetails, prop);

		}

		/*
		 * Final task, print to log, the status and job number to log the end of
		 * the job.
		 */
		if (this.debug) {
			println("Job number " + jobId + " finished:\n"
					+ "overallSuccess is: " + overallSuccess + "\n"
					+ "globicDataFetchSuccess is: " + globicDataFetchSuccess
					+ "\n\n-------------------- End Step one of job " + jobId
					+ " ---------------------");
		}

		/*
		 * Process remainder of parts in a different thread
		 */
		Thread thr = new Thread(new Runnable() {

			public void run() {

				processRemainderOfXML(partsLocation, balancedArrayList, jobId,
						audioNamingDetailsParts, initialSet, blockSize, prop,
						configData, dataSource);
			}
		});
		
		
		
		/*
		 * Only if overallSuccess is still true, continue to process the
		 * remaining parts of the XML content
		 */
		
		if (overallSuccess) {

			/*
			 * This thread continues to run after the first piece of audio has been returned
			 * and is playing.
			 */
			thr.start();
		}


		
	} ///////////////////////////////// End of the doGet() method

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Initial (word-count call)
		 * 
		 * This (doPost) method is called from the UI when the user first enters their
		 * topic and their initial 'level of detail' preferences. It returns a short
		 * XML file with three word-count values to the UI for generation of time
		 * options for the user.
		 */
		
		
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
		
		DbaseEntry dbe = new DbaseEntry(this.debug);
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
		
		
	
		/*
		 * First, try to carry out a transaction which can fail without causing
		 * the job fail (i.e. a simple transaction which has no impact).
		 */
		try {
			
			if(this.debug){
				
				println("\n\nNew job...\nCarrying out a test/waking transaction on the DBase");
				
			}
			
			conn = dataSource.getConnection();
			
			dbe.WakeUpConnection(conn);
						
		} catch (Exception e2) {
			
			println(" -- Test/waking transaction threw an error - connection was asleep --");
		
			/*
			 * Set jobId to -1 so it can be detected for error feedback. If the connection
			 * was only asleep, jobId will re-assigned to a positive number in the next
			 * stage 
			 */
			jobId = -1;
						
		}
			
			
			/*
			 * Create a DB entry for this job and get a new job ID number from the database
			 */
		try {
			
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
				
				jsWc.put("level_1", "ssc_failure");
				
				
				if(debug){
					println("jsWc (failure) is: " + jsWc);
				}
				
				JSONArray jsArray = new JSONArray();
				
				jsArray.add(0,jsJobId);
				jsArray.add(1,jsWc);
				
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
			
			jsSscFail.put("level_1", new String("db_failure"));
			
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
	 * Converts the SSC three word counts to seconds using an empirically obtained
	 * constant (words per second) and puts them in a JSON object.
	 * 
	 * NB, seconds are returned to the UI, not the actual word counts XML file
	 */

	@SuppressWarnings("unchecked")
	private JSONObject convertWcToSecToJson(Document wcXmlDoc, Properties prop) {

		/*
		 * Get constant from config/properties file for WC to seconds conversion
		 */
		String wcMultipleStr = prop.getProperty("wordcountMultiple");

		float wcMultipleFloat = Float.parseFloat(wcMultipleStr);

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
				int secondsPlaytime = (int) ((Integer.parseInt(nList.item(i).getTextContent()) / wcMultipleFloat));

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
	
	
	/*
	 * Method to create a JSONArray which contains the numbers and URLs to he
	 * audio files in the playlist
	 */
	@SuppressWarnings("unchecked")
	private JSONArray createPlaylistJson(int numberInPlaylist,
			String audioNamingDetailsParts, Properties prop) {

		/*
		 * Get Strings from the properties file for use in file names, etc.
		 */
		String mediaServerIP = prop.getProperty("mediaServerIP");
		String urlPath = prop.getProperty("audioUrlPath");
		String audioFileExtension = prop.getProperty("audioFileExtension");
		String audioWebProto = prop.getProperty("audioWebProto");

		/*
		 * Allows debuging on Windows dev m/c and use on Linux VM for demo
		 */
		if (System.getProperty("os.name").startsWith("Windows")) {
			// localhost used if on Windows during dev
			mediaServerIP = "localhost";
		}

		/*
		 * Create an JSONArray (for a number of JSONObjects)
		 */

		JSONArray overallList = new JSONArray();

		for (int j = 0; j < numberInPlaylist; j++) {

			/*
			 * Create a new object for each iteration
			 */
			JSONObject js = new JSONObject();

			/*
			 * Populate this object with details for this iteration
			 */

			js.put("title", "Part " + (j + 1) + " of " + numberInPlaylist);
			js.put("mp3", audioWebProto + "://" + mediaServerIP + "/" + urlPath + "/"
					+ audioNamingDetailsParts + (j + 1) + "."
					+ audioFileExtension);

			/*
			 * Add this object to the list
			 */
			overallList.add(js);

		}

		return overallList;

	}
	
	
	/*
	 * Method used to write properties error
	 */
	@SuppressWarnings("unchecked")
	private void writePropErrorToResponse(HttpServletResponse response) {

		println("writePropErrorToResponse called");

		/*
		 * Create an JSONArray with the error feedback messages
		 */
		JSONArray overallErrList = new JSONArray();

		/*
		 * Create a new object for each iteration
		 */
		JSONObject jsOne = new JSONObject();

		/*
		 * Populate this object with details
		 */

		jsOne.put("title", "Properties file problem");
		jsOne.put("mp3", "");

		/*
		 * Add this object to the list
		 */
		overallErrList.add(jsOne);

		/*
		 * Create another new object for the second error message
		 */
		JSONObject jsTwo = new JSONObject();

		/*
		 * Populate this object with details
		 */
		jsTwo.put("title", "Please contact admin");
		jsTwo.put("mp3", "");

		/*
		 * Add this object to the list
		 */
		overallErrList.add(jsTwo);

		overallErrList.toJSONString();

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");

		println("There's something wrong: " + overallErrList.toJSONString()
				+ "\n");

		try {
			response.getWriter().write(overallErrList.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/*
	 * Method used to write all responses when there has been an error in the
	 * doGet method call
	 */
	private void writeErrorToResponse(HttpServletResponse response,
			String errorType, Properties prop) {

		println("writeErrorToResponse called with " + errorType);

		String errorJsonStr = createErrorJson(errorType, prop);

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");

		println("There's something wrong: " + errorJsonStr + "\n");

		try {
			response.getWriter().write(errorJsonStr);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	/*
	 * Method to create a JSONArray which contains the numbers and URLs to he
	 * audio files in the playlist
	 */
	@SuppressWarnings("unchecked")
	private String createErrorJson(String errorType, Properties prop) {

		/*
		 * Get Strings from the properties file for the error type
		*/
		
		String errorDetails = prop.getProperty(errorType);
		String errorDetails_a = prop.getProperty(errorType + "a");

		String errorAudioName = "";

		/*
		 * Create an JSONArray with the error feedback messages
		 * in the same form as a normal playlist would be created
		 */
		JSONArray overallErrList = new JSONArray();

		/*
		 * Create a new object for each iteration
		 */
		JSONObject jsErrPlaylistItemOne = new JSONObject();

		/*
		 * Populate this object with details
		 */

		jsErrPlaylistItemOne.put("title", errorDetails);
		jsErrPlaylistItemOne.put("mp3", errorAudioName);

		/*
		 * Add this object to the array
		 */
		overallErrList.add(jsErrPlaylistItemOne);

		/*
		 * Create another new object for the second error message
		 */
		JSONObject jsErrPlaylistItemTwo = new JSONObject();

		/*
		 * Populate this object with details
		 */
		jsErrPlaylistItemTwo.put("title", errorDetails_a);
		jsErrPlaylistItemTwo.put("mp3", errorAudioName);

		/*
		 * Add this object to the array, this is now the equivalent
		 * of a playlist with two items
		 */
		overallErrList.add(jsErrPlaylistItemTwo);
		
		/*
		 * Now create a 'seconds' object using a default val 
		 * of 0 (zero) as there is an error. This 0 value is
		 * used to detect a failure at the UI
		 */

		JSONObject jsWcErrorValue = new JSONObject();
		
		jsWcErrorValue.put("seconds", new Integer(0));
		
		JSONArray returnedJsonArray = new JSONArray();
				
		/*
		 * Now put the 'seconds,0' object and the 'errors' playlist into 
		 * another array.
		 */
		returnedJsonArray.add(0, jsWcErrorValue);
		returnedJsonArray.add(1, overallErrList);
		
		/*
		 * Finally, create a mock visual data array which contains 
		 * 'No data' and '0' values.
		 */
		
		JSONArray visualDataErrArr = new JSONArray();
		
		// need this...    [{name: 'No data available',value: 1}]

		JSONObject visualDataErrObj = new JSONObject();
		
		/*
		 * Populate this object with details for this iteration
		 */
		
		visualDataErrObj.put("name", "No Data");
		visualDataErrObj.put("value", 1);
		
		/*
		 * Add this object to the list
		 */
		visualDataErrArr.add(visualDataErrObj);
		
		returnedJsonArray.add(2, visualDataErrArr);
				
		/*
		 * The JSON array returned here is in the same format as the usual
		 * 'wordcoung + playlist" format. However, it contains failure information
		 * which is displayed to the user.
		 */
			
		return returnedJsonArray.toJSONString();

	}
	
	/*
	 * Method takes an XML document and returns a count of all the text
	 * contained in the elements named <sentence> within that document.
	 */
	private int getWordCountXml(Document sAndCxml) {

		/*
		 * Iterate across this file for <sentence> elements
		 */
		int wordCount = 0;

		/*
		 * Try/catch to gracefully catch no sentence elements in XML
		 */
		
		NodeList nodeList = sAndCxml.getElementsByTagName("sentence");

		if (this.debug) {
			println("Sentence-count of SSC XML document is: "
					+ nodeList.getLength());
		}

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				wordCount += node.getTextContent().split(" ").length;
			}
		}

		if (this.debug) {
			println("Word-count of SSC XML document is: " + wordCount);
		}

		return wordCount;
	}
	
	/*
	 * Method to use the current jobId and the topics to create a naming string
	 * for MP3 files created.
	 */
	private String concatNameDetails(String strJobId, String strTops) {

		String name = "";

		/*
		 * Parse this string into multiple individual strings, then capitalise
		 * and join to form the basis of the name
		 */
		String[] individualWords = strTops.split("\\s+");

		/*
		 * Capitalise each work, then concatenate them
		 */
		for (String topicTerm : individualWords) {

			topicTerm = topicTerm.substring(0, 1).toUpperCase()
					+ topicTerm.substring(1);

			name = name + topicTerm;

		}

		/*
		 * Remove all occurrences of non alpha-numeric chars, chars unusable as
		 * file names are stripped \ / : ; * ? " < > |
		 */
		name = name.replaceAll("[^a-zA-Z0-9]", "");

		/* Prepend the job ID number */
		name = strJobId + "_" + name;

		if (this.debug) {
			println("Name of saved file(s)  will be: " + name
					+ " + <extension>");
		}

		return name;

	}
	
	
	/*
	 * Method which gets the content file extn (e.g. xml) and adds it to the
	 * nameingDetails string
	 */
	private String addContentExtn(String namingDetails, Properties prop) {

		/*
		 * Get file extension (e.g. xml) from config.properties file and appends
		 * this to the file name
		 */

		String fileExtn = "";

		fileExtn = prop.getProperty("contentFileExtension");

		return namingDetails + "." + fileExtn;
	}
	
	
	/*
	 * NEW: Methods which adds a '_Part_' string to the audioNamingDetails
	 * string for use in the Playlist creation
	 * 
	 * NB, file extension is NOT added here
	 */
	private String addAudioParts(String namingDetails, String strOutputLang,
			Properties prop) {

		/*
		 * Get file extension from config.properties file
		 */

		// String fileExtn = "";
		String strParts = "";

		// fileExtn = prop.getProperty("audioFileExtension");
		strParts = prop.getProperty("parts", "_Part_");

		return namingDetails + "_" + strOutputLang + strParts;
	}

	
	/*
	 * Method to prepends a the string "meta_" the content file name, used when
	 * posting data to GLOBIC. The string "meta_" is read from the properties
	 * file.
	 */
	private String prependMetaToContentName(String contentNamingDetails,
			Properties prop) {

		/*
		 * Get file extension from config.properties file
		 */
		String prependix = "";
		prependix = prop.getProperty("contentFilePrependix");

		if (this.debug) {
			println("Content meta data filename: " + prependix + "_"
					+ contentNamingDetails);
		}

		return prependix + "_" + contentNamingDetails;
		
	}

	
	/*
	 * Method takes an ArrayList and converts it to XML, the number of elements
	 * and their names is dictated by the contents of the ArrayList.
	 */
	private Document metaDataToXML(ArrayList<String> dataArrayGlobic) {

		// split string in the array on ####### (# x 7)
		if (this.debug) {

			for (String strData : dataArrayGlobic) {
				println(strData);
			}
		}

		/*
		 * Convert ArrayList contents (using iteration) to create XML to store,
		 * making it available for GLOBIC to fetch for logging.
		 */
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		Document globicDocToSend = docBuilder.newDocument();

		/*
		 * Root Element, all others are appended to this within the for loop...
		 */
		Element rootElement = globicDocToSend.createElement("globicdata");

		/*
		 * Create an element under the root element for metadata, below the root
		 * element
		 */

		Element metadataElem = globicDocToSend.createElement("metadata");

		rootElement.appendChild(metadataElem);

		/*
		 * Iterate across the ArrayList, using what is before and after the
		 * ####### demarker in each element to become the element name and
		 * element contents
		 */
		for (String s : dataArrayGlobic) {

			// create an element for "this" item in the arrayList
			Element itemElement = globicDocToSend.createElement(s.substring(0,
					s.indexOf("#######")));

			// 7 used here is the number of # chars in the demarker
			itemElement.appendChild(globicDocToSend.createTextNode(s.substring(
					(s.indexOf("#######") + 7), s.length())));

			// append it to the metadata element
			metadataElem.appendChild(itemElement);
		}

		/*
		 * Add the root element (and its childern) to the document
		 */
		globicDocToSend.appendChild(rootElement);

		if (this.debug) {
			println("");
			println(convertXmlToString(globicDocToSend));
		}

		return globicDocToSend;
	}
	
	
	/*
	 * Method which converts an XML file to a string, formatting is used if the
	 * XML document is printed for debugging.
	 */
	private String convertXmlToString(Document xmlDoc) {

		String xmlAsString = "";
		try {

			Transformer transformer;
			transformer = TransformerFactory.newInstance().newTransformer();

			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
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
	 * Two methods which ensure that initialSet and blockSize are odd/even as
	 * required, and are with practical limits
	 */
	private int validateInitialSet(int initialSet) {

		/*
		 * Check that initialSet variable is an odd number and is between 3 and
		 * 11, if not make it so
		 */
		if (initialSet < 3) {
			initialSet = 3;
		} else if (initialSet > 11) {
			initialSet = 11;
		}

		if (initialSet % 2 != 1) {
			initialSet = initialSet + 1;
			println("initialSet var has been changed to become " + initialSet);
		}

		return initialSet;
	}
	
	private int validateBlockSize(int blockSize) {

		if (blockSize < 2) {
			blockSize = 2;
			println("blockSize variable has been reset to be within program limits");
		} else if (blockSize > 10) {
			blockSize = 10;
			println("blockSize variable has been reset to be within program limits");
		}

		/*
		 * Check to ensure that the blockSize variable is an even number
		 */
		if ((blockSize % 2) != 0) {
			blockSize = blockSize + 1;
			println("blockSize rounded up to be: " + blockSize);
		} else {
			println("BlockSize is: " + blockSize);
		}

		return blockSize;
	}
	
	
	/*
	 * Method takes an ArrayList of sentences and returns a count of all the
	 * text contained in it
	 */
	private int getWordCountArList(ArrayList<String> arrayListPart) {

		/*
		 * Iterate across the Strings in the ArrayList
		 */
		int wordCount = 0;

		for (String s : arrayListPart) {

			wordCount += s.split(" ").length;

		}

		return wordCount;
	}
	
	
	/*
	 * Method used for calculating time between two long nanosecond values. It
	 * recieves two long numbers which are nanoseconds timestamps, their
	 * difference is returned in seconds as a string.
	 */
	private String calculateTime(long startTime, long endTime) {

		DecimalFormat df = new DecimalFormat("#.00");

		double timeTaken = ((endTime - startTime) / 1000000000.0);

		String timeTakenStr = df.format(timeTaken);

		return timeTakenStr;
	}
	
	/*
	 * Removed brackets and translation/pronouncation aids in text
	 * TODO Refactor this out as it should be replaced with something
	 * more robust/better
	 */
	private static String cleanTranslationAids(String firstSentence){
		
		String roundBracketsCleaned_1 = "";

		if (firstSentence.contains("(") && firstSentence.contains(")")) {
			roundBracketsCleaned_1 = firstSentence.substring(0,
					firstSentence.indexOf("(")).trim()
					+ firstSentence.substring(
							(firstSentence.indexOf(")") + 1),
							firstSentence.length());
			println("\n\n******\nSentence cleaned of ( and ) is now...\n"
					+ roundBracketsCleaned_1);
			println("This was removed...\n"
					+ firstSentence.substring(firstSentence.indexOf("("),(firstSentence.indexOf(")") + 1)) + "\n******\n");
		} else {
			roundBracketsCleaned_1 = firstSentence;
		}
		
		/*
		 * Second round on round brackets (occasionally needed)
		 */
		String roundBracketsCleaned_2 = "";
		if (roundBracketsCleaned_1.contains("(") && roundBracketsCleaned_1.contains(")")) {
			roundBracketsCleaned_2 = roundBracketsCleaned_1.substring(0,
					roundBracketsCleaned_1.indexOf("(")).trim()
					+ roundBracketsCleaned_1.substring(
							(roundBracketsCleaned_1.indexOf(")") + 1),
							roundBracketsCleaned_1.length());
			println("\n\n******\nSentence cleaned of ( and ) is now...\n"
					+ roundBracketsCleaned_2);
			println("This was removed...\n"
					+ firstSentence.substring(firstSentence.indexOf("("),(firstSentence.indexOf(")") + 1)) + "\n******\n");
		} else {
			roundBracketsCleaned_2 = roundBracketsCleaned_1;
		}
		
		/*
		 * 2nd round of cleaning to remove any text in [ ] brackets (often
		 * found in non-english articles but outside of the round brackets)
		 */
		String squareBracketsCleaned = "";

		if (roundBracketsCleaned_2.contains("[")&& roundBracketsCleaned_2.contains("]")) {
			squareBracketsCleaned = roundBracketsCleaned_2.substring(0,roundBracketsCleaned_2.indexOf("[")).trim()
					+ roundBracketsCleaned_2.substring((roundBracketsCleaned_2.indexOf("]") + 1),roundBracketsCleaned_2.length());
			println("\n\n******\nSentence cleaned of [ and ] is now...\n"
					+ squareBracketsCleaned);
			println("This was removed...\n"
					+ roundBracketsCleaned_2.substring(roundBracketsCleaned_2.indexOf("["),(roundBracketsCleaned_2.indexOf("]") + 1)) + "\n******\n");
		} else {
			squareBracketsCleaned = roundBracketsCleaned_2;
		}

		/*
		 * Attempt to remove un-bracketed pronouncation chars, e.g.
		 * like Yorkshire /ˈjɔrkʃə/ (non-rhotic) (or /ˈjɔrkʃɪər/) is a historic county...
		 * 
		 * TODO, needs proper regex
		 */
//		String forwardSlashCleaned = "";
//		if (squareBracketsCleaned.contains("/")) {
//			forwardSlashCleaned = roundBracketsCleaned_2.substring(0,roundBracketsCleaned_2.indexOf("/")).trim()
//					+ roundBracketsCleaned_2.substring(roundBracketsCleaned_2.indexOf("/", roundBracketsCleaned_2.indexOf("/")),roundBracketsCleaned_2.length());
//			println("Sentence cleaned of / and / is now...\n"
//					+ forwardSlashCleaned);
//			println("This was removed...\n"
//					+ roundBracketsCleaned_2.substring(roundBracketsCleaned_2.indexOf("/"),(roundBracketsCleaned_2.lastIndexOf("/") + 1)));
//		} else {
//			forwardSlashCleaned = roundBracketsCleaned_2;
//		}
			
		return squareBracketsCleaned;
	}
	
	
	 /* 
	 * Method which runs as a different thread, only if 'overallSuccess' is true
	 * at end of job
	 */
	private void processRemainderOfXML(int[] partsLocation,
			ArrayList<String> balancedArrayList, int jobId,
			String audioNamingDetailsParts, int initialSet, int blockSize,
			Properties prop, ArrayList<String> configData, DataSource ds) {

		println("\n========= Start of remaining parts of " + jobId + " for "
				+ audioNamingDetailsParts + " ===========\n");
		println("initialSet: " + initialSet + "\nblockSize: " + blockSize);
		
		/*
		 * A new database connection is needed as the one used in the different
		 * thread is now closed. This try-catch spans all the code here, for all
		 * parts as this ensures that the DB connection is closed in the event
		 * of any exception
		 */
		try {
			
			conn = ds.getConnection();
			
			/*
			 * Array for use inside the iteration across partsLocation array.
			 */
			ArrayList<String> thisPartForSS = new ArrayList<String>();
	
			/*
			 * Part Number, initially set to 1, incremented after each part is sent
			 * to for speech synthesis
			 */
			int partNumber = 1;
	
			/*
			 * Get the audio file extn (e.g. mp3), used for file naming, etc
			 *
			 * The file name is created later using the audioNamingDetailsParts argument 
			 * and by adding the part number and extension
			 */
			String audioFileExtension = prop.getProperty("audioFileExtension");
	
			SpeechSynthesisParts spSynPts2 = new SpeechSynthesisParts(this.debug);
	
			
			/*
			 * This loop iterates over every sentence. It ignores the first as it
			 * is already processed. As subsequent each sentence is iterated across,
			 * it's position in relation to where it falls in the blocks of sentences
			 * assigned to each part determines if it is simply added to a 'current'
			 * arrayList, or if it is added as a final sentence and then sent to SS.
			 */
			for (int c = 0; c < partsLocation.length; c++) {
				
				/*
				 * RESET ALL VARIABLES NEEDED FOR EACH PART
				 * 
				 * Boolean used to decide what database entry is made after each
				 * part is sent for speech synthesis
				 */
				boolean thisPartSuccess = false;
	
				/*
				 * Word count part for each part, used as input to database
				 */
				int partWc = 0;
	
				/*
				 * Filename used for each part, reset to "" each time
				 */
				String fileName = "";
	
				/*
				 * Time taken for speech syn to complete, float is used (hence
				 * String type)
				 */
				String ssTimeTaken = "";
	
				/*
				 * Seconds of duration of the mp3 file produced
				 */
				int mp3PlayTime = 0;
	
				/*
				 * First condition checks that the position is not at the end of the
				 * array (causing an out of bounds error when the part number in the
				 * 'next' position is checked. Second condition checks that the
				 * entry in partsLocation matches the partNumber being processed.
				 * Third condition checks that the entry is not the last entry for
				 * that partNumber in the partsLocation array. This applies to all 
				 * sentences in a 'part' except the last one, which is treated differently.
				 */
				if (partNumber != partsLocation[partsLocation.length - 1]
						&& partNumber == partsLocation[c]
						&& partNumber == partsLocation[c + 1]) {
	
					// when these conditions are met, the sentence is added to an
					// arrayList (in the expectation that there are more sentences
					// to be added)
					thisPartForSS.add(balancedArrayList.get(c));
	
				}
				/*
				 * First and second conditions are the same as above, the third
				 * condition detects that this sentence is the last one with this
				 * part number. When this condition is met, all the sentences in a
				 * part are in the thisPartForSS arrayList and is ready for speech
				 * synthesis. This applies to the last sentence of any given part.
				 */
				else if (partNumber != partsLocation[partsLocation.length - 1]
						&& partNumber == partsLocation[c]
						&& partNumber != partsLocation[c + 1]) {
	
					thisPartForSS.add(balancedArrayList.get(c));
	
					/*
					 * This is where the call is made to SS, however, if partNumber
					 * is 1, skip this part as it has already been processed
					 */
					if (partNumber != 1) {
	
						/*
						 * Add the partNumber and file extn to the
						 * audioNamingDetailsParts for file storage
						 */
						fileName = audioNamingDetailsParts + partNumber + "."
								+ audioFileExtension;
	
						long startTime = System.nanoTime();
						thisPartSuccess = spSynPts2.processXmlRemLines(jobId,partNumber, thisPartForSS, fileName, prop, configData);
	
						ssTimeTaken = calculateTime(startTime, System.nanoTime());
	
						mp3PlayTime = spSynPts2.getMP3FileDuration(fileName, prop);
	
						/*
						 * Get the wordcount for the entire thisPartforSS array list.
						 */
						partWc = getWordCountArList(thisPartForSS);
	
						/*
						 * DATABASE entry...
						 * 
						 * Record success/failure of this part synthesis in
						 * tbl_parts table in the application database
						 */
						if (thisPartSuccess) { // SS for success
	
							DbaseEntry dbe2 = new DbaseEntry(debug);
	
							dbe2.updateSpSynStatusPart(jobId, conn, partNumber,
									blockSize, initialSet, thisPartForSS.size(),
									partWc, ssTimeTaken, thisPartSuccess,
									mp3PlayTime);
	
						} else { // for SS failure
	
							DbaseEntry dbe2 = new DbaseEntry(debug);
	
							dbe2.updateSpSynStatusPart(jobId, conn, partNumber,
									blockSize, initialSet, thisPartForSS.size(),
									partWc, ssTimeTaken, thisPartSuccess);
	
						}
	
					} // end of NOT partNumber 1 if filter
	
					/*
					 * Now that this part is processed (or NOT if it is part 1, the
					 * arrayList is cleared for the next part
					 */
					thisPartForSS.clear();
	
					/*
					 * The last sentence in this part has been reached so increment
					 * partNumber for the next iteration.
					 */
					partNumber++;
	
				}
	
				/*
				 * === SPECIAL CASE === this deals with the sentences which have the
				 * last part number.
				 * 
				 * However, if partNumber equals 1, and there was only
				 * one sentence in the original array, and this has already been
				 * processed, then skip this sentence here
				 */
				else if (partNumber == partsLocation[partsLocation.length - 1]
						&& partNumber != 1) {
	
					thisPartForSS.add(balancedArrayList.get(c));
	
					/*
					 * Detect the end of balancedArrayList and send the final part
					 * to SS for processing, this is the same method call as is used
					 * for all other parts
					 */
					if (balancedArrayList.indexOf(balancedArrayList.get(c)) == balancedArrayList
							.size() - 1) {
	
						/*
						 * Add the partNumber and file extn to the
						 * audioNamingDetailsParts for file storage
						 */
						fileName = audioNamingDetailsParts + partNumber + "."
								+ audioFileExtension;
	
						long startTime = System.nanoTime();
						thisPartSuccess = spSynPts2.processXmlRemLines(jobId,
								partNumber, thisPartForSS, fileName, prop,
								configData);
	
						ssTimeTaken = calculateTime(startTime, System.nanoTime());
	
						mp3PlayTime = spSynPts2.getMP3FileDuration(fileName, prop);
	
						/*
						 * Get the wordcount for thisPartforSS
						 */
						partWc = getWordCountArList(thisPartForSS);
	
						/*
						 * DATABASE entry...
						 * 
						 * Record success/failure of this part synthesis in
						 * tbl_parts table in the application database
						 */
						if (thisPartSuccess) { // for SS success
	
							DbaseEntry dbe2 = new DbaseEntry(debug);
	
							dbe2.updateSpSynStatusPart(jobId, conn, partNumber,
									blockSize, initialSet, thisPartForSS.size(),
									partWc, ssTimeTaken, thisPartSuccess,
									mp3PlayTime);
	
						} else { // for SS failure
	
							DbaseEntry dbe2 = new DbaseEntry(debug);
	
							dbe2.updateSpSynStatusPart(jobId, conn, partNumber,
									blockSize, initialSet, thisPartForSS.size(),
									partWc, ssTimeTaken, thisPartSuccess);
	
						}
					}
				}
	
			} // end of main for loop over partsLocation array length
	
		
		} catch (SQLException e) {
			println("=== DataSource ERROR in Thread for processRemainderOfXML method ===\n");
			e.printStackTrace();
		} finally {
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		

		println("\n=== *** === End of all processing for job " + jobId
				+ " === *** ===");
	}
	
	
	
	
}



