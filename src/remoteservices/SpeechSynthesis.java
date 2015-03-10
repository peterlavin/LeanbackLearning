package remoteservices;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
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

import storageutils.FileToDisk;
import databaseutils.DbaseEntry;

/*
 * Class to provide the conversion of the summarised content in XML
 * format to audio file. 
 * 
 * This class recieves an XML file from the main application servlet.
 * This is POSTed to a remote webservice and receives an audio MP3
 * file in return. A byte array of this file is then returned to the
 * main application servlet.
 * 
 * The length of the file received and the time taken for the remote
 * processing (i.e. text to speech systheses) are recorded in the 
 * application database.
 * 
 * This class relies on database details from the service connection pool
 * which is configured in the file "WebContent/META-INF/context.xml".
 * 
 */

public class SpeechSynthesis {

	private DataSource dataSource;
	private Connection conn;
	private boolean debug;

	public SpeechSynthesis(boolean debug) {
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
	 * Main access method for this class, called by SequenceServlet,
	 * takes an integer (jobId), an XML file and a string. The XML file
	 * is passed to a remote Speech Synthesis service and the returned
	 * file is saved to disk. On success, the string passed is used to
	 * name the file. A boolean value is returned to indicate success/failure.
	 * 
	 * @param int, Document, String 
	 * 
	 * @return boolean
	 */
	public boolean processXmlFile(int jobId, Document sAndCTextWithConfig,
			String audioNamingDetails,  Properties prop) {

		boolean mp3Success = false;

		/*
		 * The byte[] array returend here should contain the bytes of the MP3
		 * audio file, however this is verified, if a text file is returned
		 * (i.e. an error has occurred), then this is detected.
		 */

		/*
		 *  Mock-up method to read an existing MP3 file from disk (for dev/testing)
		 */
//		byte[] fileBytes = MockUpCallSpeechService(jobId, sAndCTextWithConfig);
//		println("********* Mock-up service, reading file from disk is being used ************");
		
		/* Actual method to contact real Speech Syn Service */
		
		
		if(this.debug){
			println("File about to be sent to SS..\n" + convertXmlToString(sAndCTextWithConfig) + "\n");
		}
		
		
		byte[] fileBytes = CallSpeechService(jobId, sAndCTextWithConfig, prop);


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
	 * 
	 * HELPER METHODS - HELPER METHODS - HELPER METHODS - HELPER METHODS HELPER
	 * METHODS - HELPER METHODS - HELPER METHODS - HELPER METHODS
	 */
	
	 /* 
	 * Method which calls remote Speech Syntheses service
	 * 
	 * @param int, Document 
	 * 
	 * @return byte[]
	 */
	public byte[] CallSpeechService(int jobId, Document sAndCTextWithConfig, Properties prop) {

		byte[] mp3FileByteArray = null;
		
		/*
		 * Convert the received XML file to a string for POSTing to the remote
		 * service
		 */
		String xmlString = convertXmlToString(sAndCTextWithConfig);

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
			
			

//		} catch (FileNotFoundException e) {
//			println("FileNotFoundException ERROR reading properties file in TextToAudioFile");
//			if (this.debug) {
//				e.printStackTrace();
//			}
//			return mp3FileByteArray = new byte[0];
//
//		} catch (IOException e) {
//			System.err
//					.println("IOException ERROR reading properties file in TextToAudioFile");
//			if (this.debug) {
//				e.printStackTrace();
//			}
//
//			return mp3FileByteArray = new byte[0];
//		}

		/*
		 * Only attempt to call SS if it can be verifed as being available
		 */
		if (serviceIsAvailable(baseURL)) {

			/*
			 * Time this operation for logging to database and printing from exception catch
			 */
			long startTime = System.nanoTime();
			
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

				StringBody sb = new StringBody(xmlString, ct);

				MultipartEntityBuilder mpeb = MultipartEntityBuilder.create();

				mpeb.addPart("files", sb);

				if (this.debug) {
					println("MultipartEntityBuilder getContentType is...\n"
							+ mpeb.build().getContentType().getValue());
				}

				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				mpeb.build().writeTo(bytes);

				if (this.debug) {
					/* Prints up to 450 chars of the header */
					println("\n\nActual multipartEntity bytes to string for dbug...\n\n"
							+ bytes.toString()
									.substring(
											0,
											((bytes.toString().length() < 150) ? (bytes
													.toString().length())
													: 150))
							+ "     ...[ABRIDGED]\n");
				}

				httppost.setEntity(mpeb.build());

				// then fire the request, get a response...

				if (this.debug) {
					println("Calling the Speech Syn service now...\n");
				}

				HttpResponse httpResp = client.execute(httppost);

				String timeTaken = calculateTime(startTime, System.nanoTime());

				if (this.debug) {
					System.out.println("Seconds taken to do Speech Syn: "
							+ timeTaken + " sec");
				}

				HttpEntity httpEntity = httpResp.getEntity();

				InputStream is = httpEntity.getContent();

				mp3FileByteArray = IOUtils.toByteArray(is);

				/*
				 * Log time and array length to database
				 */
				DbaseEntry dbe = new DbaseEntry(this.debug);

				try {

					/*
					 * This call updates the column ssc_status in the table
					 * tbl_jobs
					 */
					conn = dataSource.getConnection();

					dbe.updateSpSynStatus(conn, jobId, timeTaken,
							mp3FileByteArray.length);

				} catch (Exception e1) {
					println("ERROR in TestToAudioFile Speech Syn d'base update");
					if (this.debug) {
						e1.printStackTrace();
					}

				} finally {
					try {
						if (conn != null)
							conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}

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
				println("Actual timeout time (from ex catch): " + calculateTime(startTime, System.nanoTime()));

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
	 * Method to print to log, all printed lines are recorded in catalina.out in
	 * the web service container.
	 * 
	 * @param Object 
	 */
	private static void println(Object obj) {
		System.out.println(obj);
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
	 * Method to convert an XML file to a string, formatting is used if the XML document
	 * is printed for debugging.
	 * 
	 * @param Document
	 * 
	 * @return String
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
	 * Method created during dev, reads a pre-made music MP3 file from disk for
	 * use in the absence of a working speech sysntesis service.
	 * 
	 * @param int, Document 
	 * 
	 * @return byte[]
	 */
	private byte[] MockUpCallSpeechService(int jobId,
			Document sAndCTextWithConfig) {

		// Read existing MP3 file from disk to byte[]
		String serverOS = System.getProperty("os.name");
		File mp3File = null;
		
		if (serverOS.startsWith("Windows")) {

			// file loc on dev windows system
			mp3File = new File("C:\\inetpub\\wwwroot\\jplayer\\audio\\UndertonesTeenageKicks.mp3");

		} else if (serverOS.startsWith("Linux")) {

			// file location for Linux system...
			mp3File = new File("//home/tomcat/audio/UndertonesTeenageKicks.mp3");

		} else {
			System.out.println("ERROR: OS other than Windows/Linux found");
		}
		
		byte[] fileData = new byte[(int) mp3File.length()];
		DataInputStream dis;
		try {
			dis = new DataInputStream(new FileInputStream(mp3File));
			dis.readFully(fileData);
			dis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		return fileData;
	}
	
	static void printLongerTrace(Throwable t){
		println("Full stack trace...\n");
		for(StackTraceElement e: t.getStackTrace()){
	    	println(e);
	    }
		println("\n End of stack trace...\n");
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
