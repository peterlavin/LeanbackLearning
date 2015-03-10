package storageutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class FileToDisk {

	private boolean debug;

	public FileToDisk(boolean debug) {
		// constructor
		this.debug = debug;
	}

	/*
	 * Method receives an XML Document object and saves it to disk in a location from
	 * which it can be fetched over HTML
	 * 
	 *   @param Document, String, Properties
	 *   
	 *   @return boolean
	 */

	public boolean storeContentFilesToDisk(Document doc, String fileName,
			Properties prop) {

		/*
		 * Firstly, get the path details from config/properties file for where
		 * file is to be saved.
		 * 
		 * 
		 * Determine OS of underlying system and set file path accordingly
		 */
		String filePath = "";
		String serverOS = System.getProperty("os.name");
		boolean fileCreated = false;

		if (serverOS.startsWith("Windows")) {

			if (this.debug) {
				System.out.println("Windows OS found");
			}

			filePath = prop.getProperty("contentFileStorageWindows");

		} else if (serverOS.startsWith("Linux")) {
			if (this.debug) {
				System.out.println("Linux OS found");
			}
			filePath = prop.getProperty("contentFileStorageLinux");

		} else {

			System.out.println("ERROR: OS other than Windows/Linux found");

			filePath = null;

			return false;
		}

		/*
		 * create a new instance of a TransformerFactory, to create a
		 * Transformer instance
		 */
		TransformerFactory tff = TransformerFactory.newInstance();
		Transformer tf = null;

		try {
			tf = tff.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}

		DOMSource source = new DOMSource(doc);

		// StreamResult result = new StreamResult(System.out);
		StringWriter stW = new StringWriter();

		try {
			tf.transform(source, new StreamResult(stW));

			/*
			 * Sets indentation details
			 */
			tf.setOutputProperty(OutputKeys.METHOD, "xml");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");

			/*
			 * 5 here means five whitespaces for each indent
			 */
			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
					"5");

			/*
			 * Write the DOM to file using the StreamWriter
			 */
			DOMSource xmlSource = new DOMSource(doc);
			// StreamResult outputTarget = new StreamResult(filePath + "/" +
			// fileName);

			if (this.debug) {
				println("Content file path and name... " + filePath + "/"
						+ fileName);
			}

			StreamResult outputTarget = new StreamResult(filePath + "/"
					+ fileName);

			tf.transform(xmlSource, outputTarget);

		} catch (TransformerException e) {
			e.printStackTrace();
		}

		/*
		 * now check that the file is actually there (belt & braces check)
		 */

		/*
		 * Check created file now exist
		 */
		File fileToCheck = new File(filePath + "/" + fileName);

		if (fileToCheck.exists() && fileToCheck.length() > 0) {
			fileCreated = true;
		}

		if (this.debug) {
			System.out.println("Boolean fileCreated (returned) in "
					+ this.getClass() + " is " + fileCreated);
		}

		return fileCreated;
	}

	/*
	 * Method receives a byte array of the MP3 file and writes it to disk storage. This
	 * operation is operating system dependent and attempts to determine the OS.
	 * A boolean is returned if the written file is detectable and is not of
	 * zero length.
	 * 
	 * @param byte[], String, Properties
	 * 
	 * @return boolean
	 */
	public boolean storeAudioFileToDisk(byte[] bytes, String filename,
			Properties prop) {

		/*
		 * Determine OS of underlying system and set file path accordingly
		 */
		String filePath = "";
		String serverOS = System.getProperty("os.name");
		boolean fileCreated = false;

		if (serverOS.startsWith("Windows")) {

			if (this.debug) {
				System.out.println("Windows OS found");
			}

			filePath = prop.getProperty("audioFileStorageWindows");

		} else if (serverOS.startsWith("Linux")) {
			if (this.debug) {
				System.out.println("Linux OS found");
			}
			filePath = prop.getProperty("audioFileStorageLinux");

		} else {

			System.out.println("ERROR: OS other than Windows/Linux found");

			filePath = null;

			return false;
		}

		try {

			// check if file exists (it shouldn't), create as needed
			File outFile = new File(filePath + "/" + filename);

			FileOutputStream fout = new FileOutputStream(outFile);

			if (!outFile.exists()) {
				outFile.createNewFile();
			}

			fout.write(bytes);
			fout.flush();
			fout.close();

			/*
			 * Check created file now exist
			 */
			if (outFile.exists() && outFile.length() > 0) {

				fileCreated = true;

			}

			if (this.debug) {
				System.out.println("MP3 file length is... " + outFile.length());
				System.out.println("Boolean fileCreated (returned) in "
						+ this.getClass() + " is " + fileCreated);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileCreated;

	}
	
	/*
	 * Method is used to delete files from disk for housekeeping, is used
	 * for both the content file and its metadata file.
	 * 
	 * @param String, prop
	 */

	public void deleteContentFiles(String fileName, Properties prop) {

		/*
		 * Firstly, get the path details from config/properties file for where
		 * file is to be saved.
		 * 
		 * 
		 * Determine OS of underlying system and set file path accordingly
		 */
		String filePath = "";
		String serverOS = System.getProperty("os.name");

		if (serverOS.startsWith("Windows")) {

			if (this.debug) {
				System.out.println("Windows OS found");
			}

			filePath = prop.getProperty("contentFileStorageWindows");

		} else if (serverOS.startsWith("Linux")) {
			if (this.debug) {
				System.out.println("Linux OS found");
			}
			filePath = prop.getProperty("contentFileStorageLinux");

		} else {

			if (this.debug) {
				println("ERROR: OS other than Windows/Linux found");
			}

			filePath = null;

		}

		File fileToDelete = new File(filePath + "/" + fileName);

		if (fileToDelete.exists()) {

			if (this.debug) {
				println("Deleting content file " + filePath + "/" + fileName);
			}
			fileToDelete.delete();
		}

		if (!fileToDelete.exists()) {
			println("File " + filePath + "/" + fileName + " no longer exists.");
		}

	}

	/*
	 * Log printing method
	 * 
	 *   @param Object
	 */
	private static void println(Object obj) {
		System.out.println(obj);
	}

}
