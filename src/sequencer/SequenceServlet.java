package sequencer;

import java.io.IOException;
import java.io.InputStream;
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

import org.w3c.dom.Document;

import databaseutils.DbaseEntry;
//import remoteservices.SearchAndCombine;

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

			/*
			 * Get Boolean debug varialbe from context (set in
			 * WebContent/WEB-INF/web.xml)
			 */
			ServletContext context = getServletContext();
			debug = Boolean.valueOf(context.getInitParameter("debug"));

			println("Init() method has been called, debug mode is set to "
					+ debug);

		} catch (NamingException e) {
			if (debug) {
				e.printStackTrace();
			}
		}
		
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		/*
		 * This method receives the topic selection from the user and gets three
		 * different word-counts from SSC for three different levels of detail.
		 * These are then returned to the UI (JSP) page for further
		 * 'summarising/time' details to be selected.
		 */
		Properties prop = new Properties();
		/*
		 * Initialise the properties object
		 */
		try {
			InputStream inStrm = SequenceServlet.class.getClassLoader()
					.getResourceAsStream("/config/properties");
			prop.load(inStrm);
			println("Properties file read successfully in doPost()");
		} catch (IOException | NullPointerException e) {

			println("Error Initialising properties inputstream");
			e.printStackTrace();
		}

		
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
		String strDetail = request.getParameter("detail");
		String strOutputLang = request.getParameter("outputlang");
		
		/*
		 * Check that the database is available and that the connection is 'awake'
		 */
		DbaseEntry dbe = new DbaseEntry(this.debug);
		
		/*
		 * Add the user input parameters to an arraylist for database entry,
		 * then pass to the DbaseEntry object for sending to database.
		 */
		ArrayList<String> dataForDB = new ArrayList<String>();
		dataForDB.add(strIDnum);
		dataForDB.add(strName);
		dataForDB.add(strTopics);
		dataForDB.add(strOutputLang);
		dataForDB.add(strDetail);
		

		
		
		
		
		try {
			
			/*
			 * Update the column ssc_status in the table tbl_jobs
			 */
			conn = dataSource.getConnection();
			jobId = dbe.CreatInitialDbEntry(conn, dataForDB);

		} catch (Exception e1) {
			System.err
					.println("ERROR in SSC DB UPDATE: " + e1.getMessage());
			if (debug) {
				e1.printStackTrace();
			}
		} 
		
		
		
		
		/*
		 * Get a new job ID number from the database
		 */
		
		
		
		
		
		
		
		
		
		
//		String strPrelimTopics = request.getParameter("prelimTopics");
//
//		/*
//		 * Replace all spaces with underscores
//		 */
//		String strPrelimTopicsWithUnderScrs = strPrelimTopics.replace(" ", "_");
//
//		SearchAndCombine sac = new SearchAndCombine(this.debug);
//
//		// String sscWordCountXML =
//		// "<wordcount><level_1>0</level_1><level_2>0</level_2><level_3>0</level_3></wordcount>";
//		String sscWordCountXML = sac.getSscWc(strPrelimTopicsWithUnderScrs,
//				prop);
//
//		/*
//		 * Default string to return (signals a failure at this stage)
//		 */
//		String responseJsStr = "{\"level_1\":\"failure\"}";
//
//		/*
//		 * Check returned string is valid XML and apply logic to determine what
//		 * response is sent
//		 */
//		if (validStringToXMLConversion(sscWordCountXML)) {
//
//			if (this.debug) {
//				println("XML WC returned...\n" + prettyFormat(sscWordCountXML));
//			}
//
//			/*
//			 * Now, convert this to JSON and send back to UI.
//			 * 
//			 * First convert the XML string to be an XML Document
//			 */
//			Document wcXmlDoc = convertStringToDocument(sscWordCountXML);
//
//			JSONObject js = new JSONObject();
//
//			js = convertWcToSecToJson(wcXmlDoc, prop);
//
//			responseJsStr = js.toJSONString();
//
//		}
//
//		/*
//		 * Write string to response
//		 */
//		response.setContentType("text/plain");
//		response.setCharacterEncoding("UTF-8");
//		response.getWriter().write(responseJsStr);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
	
}



