package remoteservices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tika.Tika;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import databaseutils.DbaseEntry;
import storageutils.FileToDisk;

public class SpeechSynthesisParts {

	private DataSource dataSource;
	private Connection conn;
	private boolean debug;

	public SpeechSynthesisParts(boolean debug) {
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
			println("NamingException in TextToAudioFile");
			if (this.debug) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	/*
	 * Method to process the first line (part) of an XML part. This differs from
	 * the method that processes the remainder of the XML content file in how
	 * the database is updated. 
	 */
	public boolean processXmlFirstLine(int jobId, int partNumber,
			ArrayList<String> sentences, String audioNamingDetails, Connection conn, Properties prop,
			ArrayList<String> configData) {

		if(this.debug){
			println("\nCalling processXmlFirstLine called, part number: " + partNumber + "\n");
		}

		/*
		 * Declare the boolean to be eventually returned
		 */
		boolean mp3Success = false;

		/*
		 * The byte[] array returend here should contain the bytes of the MP3
		 * audio file, however this is verified, if a text file is returned
		 * (i.e. an error has occurred), then this is detected.
		 */

		/*
		 * Convert the arrayList to be XML in the format needed for 
		 * the SpeechSynthesis service. The configData is also passed here.
		 */
		String sAndCXmlWithConfig = convertStrToXmlString(sentences, configData);
		
		println("XML about to be sent to SS...\n" + sAndCXmlWithConfig + "\n");
		
		/*
		 * Time this operation for logging to database
		 */
		long startTime = System.nanoTime();
		
		/* Actual method to contact real Speech Syn Service */
		byte[] fileBytes = CallSpeechService(jobId, sAndCXmlWithConfig, partNumber, prop);
		
		/*
		 * Collect time data for DB entry
		 */
		String timeTaken = calculateTime(startTime, System.nanoTime());
		
		/*
		 * Log time and array length to database
		 */
		DbaseEntry dbe = new DbaseEntry(this.debug);
		
		try {

			/*
			 * This call updates the length of the Byte[] returned in table tbl_jobs
			 */
			dbe.updateSpSynStatus(conn, jobId, timeTaken, fileBytes.length);

		} catch (Exception e1) {
			println("ERROR in TestToAudioFile Speech Syn d'base update");
			if (this.debug) {
				e1.printStackTrace();
			}

		} 
		
		
//		finally {
//			try {
//				if (conn != null)
//					conn.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//
//		}
		
		
		
		
		
		
		
		/*
		 * The logic is as follows... 
		 * 
		 * If the file is null, return false.
		 * If it's an valid MP3 format, write it to disk, return true.
		 * If it contains text (caused by the speech service returning
		 * a message such as 'Unsupported Media Type', then log this
		 * and return mp3success as false.
		 * 
		 * TODO, this is to be improved to better catch the various possible
		 * responses from Mostafa's service.
		 */

		if (fileBytes.length == 0) {
			mp3Success = false;

			if (this.debug) {
				println("Zero length byte[] array returned to processXmlFile method.");
			}

			/*
			 * Return now, regardless of the following two tests below
			 */
			return mp3Success;

		} else if (fileIsAnMp3Format(fileBytes)) {
			// all ok, write to disk

			/*
			 * boolean mp3success get set to true here if file is saved and
			 * confirmed as saved
			 */
			FileToDisk ftd = new FileToDisk(this.debug);
			mp3Success = ftd
					.storeAudioFileToDisk(fileBytes, audioNamingDetails, prop);

			if (this.debug) {
				println(mp3Success
						+ " returned from storeFileToDisk method in TextToAudioFile class.");
			}

		} else if (fileIsInTextFormat(fileBytes)) {

			/*
			 * If in debug mode, prints the first 150 chars of the returned text
			 * file array
			 */
			if (this.debug) {

				println("Text file/array was returned from the Speech Syntheses service.");
				for (int i = 0; i < Math.min(fileBytes.length, 150); i++) {
					System.out.print(fileBytes[i]);
				}
				println("");

			}

			mp3Success = false;
		}


		return mp3Success;
	}
	
	
	/*
	 * Method to process lines (parts) of an XML part, other than the first one.
	 * This differs from the method that processes the remainder of the XML
	 * content file in how the database is updated. 
	 */
	public boolean processXmlRemLines(int jobId, int partNumber,
			ArrayList<String> sentences, String audioNamingDetails, Properties prop,
			ArrayList<String> configData) {

		if(this.debug){
			println("\nCalling processXmlLines called, part number: " + partNumber + "\n");
		}

		/*
		 * Declare the boolean to be eventually returned
		 */
		boolean mp3Success = false;

		/*
		 * The byte[] array returend here should contain the bytes of the MP3
		 * audio file, however this is verified, if a text file is returned
		 * (i.e. an error has occurred), then this is detected.
		 */

		/*
		 * Convert the arrayList to be XML in the format needed for 
		 * the SpeechSynthesis service. The configData is also passed here.
		 */
		String sAndCXmlWithConfig = convertStrToXmlString(sentences, configData);
		
		println("XML about to be sent to SS...\n" + sAndCXmlWithConfig + "\n");
		
		/* Actual method to contact real Speech Syn Service */
		byte[] fileBytes = CallSpeechService(jobId, sAndCXmlWithConfig, partNumber, prop);


		/*
		 * The logic is as follows... 
		 * 
		 * If the file is null, return false.
		 * If it's an valid MP3 format, write it to disk, return true.
		 * If it contains text (caused by the speech service returning
		 * a message such as 'Unsupported Media Type', then log this
		 * and return mp3success as false.
		 * 
		 * TODO, this is to be improved to better catch the various possible
		 * responses from Mostafa's service.
		 */

		if (fileBytes.length == 0) {
			mp3Success = false;

			if (this.debug) {
				println("Zero length byte[] array returned to processXmlFile method.");
			}

			/*
			 * Return now, regardless of the following two tests below
			 */
			return mp3Success;

		} else if (fileIsAnMp3Format(fileBytes)) {
			// all ok, write to disk

			/*
			 * boolean mp3success get set to true here if file is saved and
			 * confirmed as saved
			 */
			FileToDisk ftd = new FileToDisk(this.debug);
			mp3Success = ftd
					.storeAudioFileToDisk(fileBytes, audioNamingDetails, prop);

			if (this.debug) {
				println(mp3Success
						+ " returned from storeFileToDisk method in TextToAudioFile class.");
			}

		} else if (fileIsInTextFormat(fileBytes)) {

			/*
			 * If in debug mode, prints the first 150 chars of the returned text
			 * file array
			 */
			if (this.debug) {

				println("Text file/array was returned from the Speech Syntheses service.");
				for (int i = 0; i < Math.min(fileBytes.length, 150); i++) {
					System.out.print(fileBytes[i]);
				}
				println("");

			}

			mp3Success = false;
		}


		return mp3Success;
	}
	
	

	
	/*
	 * 
	 * HELPER METHODS - HELPER METHODS - HELPER METHODS - HELPER METHODS HELPER
	 * METHODS - HELPER METHODS - HELPER METHODS - HELPER METHODS
	 */
	
	/*
	 * Method takes a simple string and an ArrayList with some
	 * config data. Using these, an XML file is created, then
	 * converted back to a string again (for passing to SS service).
	 */
	 private String convertStrToXmlString(ArrayList<String> LinesAList,
			ArrayList<String> configData) {

		 /*
		  * First make the XML for the single sentence string passed
		  */
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

			
			for(String s: LinesAList){
				
				/* Within each slide, there may be one or multiple sentences */
				Element sentence = contentDoc.createElement("sentence");
				sentence.appendChild(contentDoc
					.createTextNode(s));
				slide.appendChild(sentence);
				
			}

			
			
			/*
			 * Now insert the config data to it
			 */
			Document contentDocWithConfig = insertConfigDetails(contentDoc, configData);
			
			/*
			 * Conver this XML doucment to a string for return
			 */
			
			String contDocWtConfigStr =  convertXmlToString(contentDocWithConfig);
			
			
		 
		return contDocWtConfigStr;
	}

	/* 
	 * Method which calls remote Speech Syntheses service
	 * 
	 * @param int, Document 
	 * 
	 * @return byte[]
	 */
	public byte[] CallSpeechService(int jobId, String sAndCTextWithConfig, int partNumber, Properties prop) {

		byte[] mp3FileByteArray = null;
		
		/*
		 * Get 'Speech Synthesis' service information from properties file
		 */
		String ipAddress = "";
		String servicePath = "";
		String baseURL = "";
		String strTimeOut = "";
		int timeout = 0;

			ipAddress = prop.getProperty("speechserviceIpAddress");
			servicePath = prop.getProperty("speechserviceUriPath");
			strTimeOut = prop.getProperty("speechserviceTimeoutValue");
			
			/*
			 * Convert string from properties file to useful integer
			 */
			timeout = Integer.parseInt(strTimeOut);
			
			/*
			 * Create the URL in string format
			 */
			baseURL = "http://" + ipAddress + ":8080/" + servicePath;
			

		/*
		 * Only attempt to call SS if it can be verifed as being available
		 */
		if (serviceIsAvailable(baseURL)) {

			try {

				/*
				 * Now that the service is available, create the objects to post
				 * the XML to the remote service.
				 * 
				 * Log the time taken for this timeout. This code is not reached unless the Speech Syn
				 * service is available
				 */
				RequestConfig.Builder requestBuilder = RequestConfig.custom();
				requestBuilder = requestBuilder.setConnectTimeout(timeout);
				requestBuilder = requestBuilder.setConnectionRequestTimeout(timeout);
				
				HttpClientBuilder builder = HttpClientBuilder.create();     
				builder.setDefaultRequestConfig(requestBuilder.build());
				HttpClient client = builder.build();
				
				HttpPost httppost = new HttpPost(baseURL);

				ContentType ct = ContentType.TEXT_XML;

				StringBody sb = new StringBody(sAndCTextWithConfig, ct);

				MultipartEntityBuilder mpeb = MultipartEntityBuilder.create();

				mpeb.addPart("files", sb);

				if (this.debug) {
					println("MultipartEntityBuilder getContentType is...\n"
							+ mpeb.build().getContentType().getValue());
				}

				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				mpeb.build().writeTo(bytes);

				if (this.debug) {
					/* Prints up to n chars of the header */
					
					int len = 450;
					
					println("\n\nFirst " + len + " chars of actual multipartEntity bytes to string for sending...\n\n"
							+ bytes.toString()
									.substring(
											0,
											((bytes.toString().length() < len) ? (bytes
													.toString().length())
													: len))
							+ "     ...[ABRIDGED]\n");
				}

				httppost.setEntity(mpeb.build());

				// then fire the request, get a response...

				if (this.debug) {
					println("Calling the Speech Syn service for job " + jobId + ", part " + partNumber + "\n");
				}

				HttpResponse httpResp = client.execute(httppost);

				HttpEntity httpEntity = httpResp.getEntity();

				InputStream is = httpEntity.getContent();

				mp3FileByteArray = IOUtils.toByteArray(is);

				if (this.debug) {
					println("MP3 array len: " + mp3FileByteArray.length);
					println("Start of array... (20 chars)");

					for (int j = 0; j < Math.min(mp3FileByteArray.length, 20); j++) {
						System.out.print(mp3FileByteArray[j]);
					}
					println("");
				}
				
				

			} catch (IOException e) {
				/*
				 * Return a zero length byte[], this is caught in the calling method.
				 */
				println("IOException connecting or getting data from Speech Syn service in TestToAudioFile: " + e.getMessage());
				println("Printing timeout exception...\n");
				e.printStackTrace();

				if (this.debug) {
					e.getMessage();
				}

				return mp3FileByteArray = new byte[0];
				
			}

		}
		/*
		 * If something has gone wrong, this array will be returned zero length, this
		 * is caught by the calling method
		 */
		else {
			return mp3FileByteArray = new byte[0];
		}

		return mp3FileByteArray;

	}

	

	/*
	 * Method to  verify/detect if a file (in byte[] array format) is text/html
	 * 
	 * @param byte[]
	 * 
	 * @return boolean
	 */
	private boolean fileIsInTextFormat(byte[] fileBytesToVerify) {

		boolean isOk = false;
		Tika tka = new Tika();
		
		isOk = tka.detect(fileBytesToVerify).equalsIgnoreCase("text/html");

		if (this.debug) {
			println(isOk + " found in fileIsInTextFormat in TextToAudioFile");
			println("Contents of text file returned...");
			try {
				println(new String(fileBytesToVerify, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return isOk;
	}
	
	/*
	 * Method to verify that a file (in byte[] array format) is audio/mpeg
	 * 
	 * @param byte[]
	 * 
	 * @return
	 */
	private boolean fileIsAnMp3Format(byte[] fileBytesToVerify) {

		boolean isOk = false;
		Tika tk = new Tika();

		isOk = tk.detect(fileBytesToVerify).equalsIgnoreCase("audio/mpeg");

		if (this.debug) {
			println(isOk + " found in fileIsAnMp3Format in TextToAudioFile");
		}

		return isOk;
	}
	
	
	/*
	 * Method which receives a URL/URI and returns true if response code 200 is receieved
	 * from that address.
	 * 
	 * @param String 
	 * 
	 * @return boolean
	 */
	private boolean serviceIsAvailable(String urlStr) {

		int respCode = 0;
		URL url;
		int testingTimeout = 3000;
		
		try {
			url = new URL(urlStr);

			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("POST");

			/*
			 * Time out set to 3 seconds, checked at 4 seconds, response should
			 * be almost instant so this can be hardcoded in this method.
			 */
			huc.setConnectTimeout(testingTimeout);
			huc.setReadTimeout(testingTimeout + 1000);
			huc.connect();
			
			respCode = huc.getResponseCode();

			if (this.debug) {
				println("Response code from " + urlStr + " is " + respCode);
			}

		} catch (MalformedURLException e) {
			println("MalformedURLException from " + urlStr
					+ " : Response code is " + respCode);
			return false;
		} catch (SocketTimeoutException s) {
			println("Timeout occurred when checking availability of Speech Synthesis service at...\n"
					+ urlStr);
			return false;
		}
		catch (IOException e) {
			println("IOException (other than Connection Timeout) from " + urlStr
					+ " is " + respCode);
			return false;
		}

		if (respCode == 200) {
			return true;
		} else {
			return false;
		}

	}
	
	/*
	 * Method used for calculating time between two long nanosecond
	 * values. It recieves two long numbers which are nanoseconds timestamps, their
	 * difference is returned in seconds as a string.
	 */
	private String calculateTime(long startTime, long endTime) {

		DecimalFormat df = new DecimalFormat("#.00");

		double timeTaken = ((endTime - startTime) / 1000000000.0);

		String timeTakenStr = df.format(timeTaken);

		return timeTakenStr;
	}
	
	/*
	 * Method which takes a DOM document and an ArrayList, the arraylist
	 * contents are inserted into the XML.
	 */
	private Document insertConfigDetails(Document doc,
			ArrayList<String> configDetails) {

		/*
		 * Create the root element (presentation) and append a child (config) to
		 * it
		 */
		Element presentation = doc.getDocumentElement();
		Element config = doc.createElement("config");
		presentation.appendChild(config);

		/*
		 * Iterates over the ArrayList, add an new node within the config
		 * element for each of item in the ArrayList
		 */

		for (String s : configDetails) {

			/*
			 * The array contains strings in the format elemName:elemText
			 * 
			 * Get and split these strings, within the 'config' elemnt, there
			 * may be multiple pieces of data
			 */
			String elemName = s.substring(0, s.indexOf(":"));
			String elemText = s.substring((s.indexOf(":") + 1), s.length());

			Element anElement = doc.createElement(elemName);
			anElement.appendChild(doc.createTextNode(elemText));
			config.appendChild(anElement);

		}

		return doc;
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
	 * Public method of the class to get duration of the MP3 file created
	 */
	public int getMP3FileDuration(String audioNamingDetails, Properties prop) {

		/*
		 * Get file path details, same as was used for creating the target
		 * MP3 file
		 *
		 * Determine OS of underlying system and set file path accordingly
		 */
		String filePath = "";
		String serverOS = System.getProperty("os.name");

		if (serverOS.startsWith("Windows")) {

			if (this.debug) {
				System.out.println("\nWindows OS found");
			}

			filePath = prop.getProperty("audioFileStorageWindows");

		} else if (serverOS.startsWith("Linux")) {
			if (this.debug) {
				System.out.println("Linux OS found");
			}
			filePath = prop.getProperty("audioFileStorageLinux");

		} 

		/*
		 * Now access the file and get its duration
		 */
		int dur = 0;

		AudioFile audioFile;
		try {
			
			audioFile = AudioFileIO.read(new File(filePath + "/" + audioNamingDetails));
			dur = audioFile.getAudioHeader().getTrackLength();
		
		} catch (Exception e) {
			return -1;
		} 
		  
		if(this.debug) {
			println("\nPlaying time of " + audioNamingDetails + " is " + dur + " sec\n");
		}	
		
		return dur;
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
