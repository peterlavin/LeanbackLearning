package globicutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sequencer.SequenceServlet;

public class LogToGlobic {

	private boolean debug;

	/*
	 * Class which deals supports logging to GLOBIC
	 */

	public LogToGlobic(boolean debug) {
		// Constructor
		this.debug = debug;

	}

	/*
	 * Method receives a String URL which is posted to GLOBIC.
	 * GLOBIC uses this URL to fetch two XML file for parsing/storage.
	 * The HTTP response code is used to determine overall success of
	 * GLOBIC logging attempt. The response code (200 or otherwise) is
	 * dependent on the GLOBIC implementation.
	 * 
	 * @param String, String, Properties
	 * 
	 * @return boolean
	 */
	public boolean logDataWithGlobic(String strContentMetaUrl, Properties prop) {

		boolean globicLogSuccess = false;

		/*
		 * Get GLOBIC server details from config.properties file
		 */
		String globicServerIP = "";
		String globicServerPath = "";

		globicServerIP = prop.getProperty("globicServerIP");
		globicServerPath = prop.getProperty("globicServerPath");
		
		String globicUrl = "http://" + globicServerIP + "/" + globicServerPath + "?";
		
        try {

        	/*
        	 * New version of constructor (with same signature) is called here
        	 */
            @SuppressWarnings("deprecation")
			DefaultHttpClient httpClient = new DefaultHttpClient();
            
            // Post the URL to GLOBIC, the hard-coded "contentConsumed1" string is a variable required by GLOBIC
            String query = URLEncoder.encode("contentConsumed1", "UTF-8") + "=" + URLEncoder.encode(strContentMetaUrl, "UTF-8");
            

            
            
            if(this.debug){
            	println("GLOBIC URL target... " + globicUrl + query);
            }
            
            // Actually post the response...
            HttpPost postRequest = new HttpPost(globicUrl + query);
            
            /*
             *  GLOBIC reacts to this post by fetching the meta_xxxxx.xml and content XML file from the Lbl host machine,
             *  if successful, it returns a 200 response. 
             */  

            // Get the response code
            HttpResponse response = httpClient.execute(postRequest);
            
            println("Print of resp code from phad: " + response.getStatusLine().getStatusCode());
            
            // GLOBIC response is checked, a boolean is set and used to record success in the DB 
            if (response.getStatusLine().getStatusCode() == 200) {
            	System.out.println("\nSUCCESS " + response.getStatusLine().getStatusCode() + " returned from GLOBIC server.\n");
            	globicLogSuccess = true;
            } else {
            	println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            	globicLogSuccess = false;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            if(this.debug){
            	println("GLOBIC call ended, globicLogSuccess is: " + globicLogSuccess);
            }
            
            httpClient.getConnectionManager().shutdown();

        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

		return globicLogSuccess;
		
	}

	/*
	 * HELPER METHODS - HELPER METHODS - HELPER METHODS - HELPER METHODS HELPER
	 * METHODS - HELPER METHODS - HELPER METHODS - HELPER METHODS
	 * 
	 * 
	 * Converts an XML file to a string, formatting is used if the XML document
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
