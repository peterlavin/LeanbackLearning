package xmlutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BalanceSentencesUtils {

	boolean debug;

	public BalanceSentencesUtils(boolean debug) {

		this.debug = debug;

	}

	/*
	 * method which
	 */
	public ArrayList<String> balanceSentences(Document xmlForBalancing) {

		/*
		 * Variables used for sentence lengths
		 */
		int minSentLen = 30;
		int maxSentLen = minSentLen + 50;

		// int initialSet = 5; // MUST BE BETWEEN 5 AND 11, must be an odd
		// number, if not it is made to be
		// int blockSize = 5; // MUST BE AN EVEN NUMBE, IF NOT, IT WILL BE
		// ROUNDED UP TO BE
		// int numberOf50WParts = 30; // arbitary testing number, actually
		// decided by length of text

		NodeList nodeList = xmlForBalancing.getElementsByTagName("sentence");

		/*
		 * Create an array of the sentence contents.
		 */
		String[] sentenceContent = new String[nodeList.getLength()];

		/*
		 * Iterate over the node list and populate these two arrays
		 */
		for (int i = 0; i < nodeList.getLength(); i++) {

			Node node = nodeList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				sentenceContent[i] = node.getTextContent().trim();

			}

		}

		println("sentenceLengths: " + sentenceContent.length + "\n");

		/*
		 * For debug/before-after comparsion only
		 */
		// for (int h = 0; h < sentenceContent.length; h++) {
		// println(sentenceContent[h].split(" ").length + ": "
		// + sentenceContent[h]);
		// }

		/*
		 * Three passes through process to join all under minSentLen word
		 * sentences Two is often enough, needs a recursive call (ideally)
		 */
		String[] pass1Array = joinShortSentences(minSentLen, sentenceContent);
		String[] pass2Array = joinShortSentences(minSentLen, pass1Array);
		String[] finalArray1 = joinShortSentences(minSentLen, pass2Array);

		println();

		/*
		 * DEBUG DEBUG DEBUG Prints the final balanced array
		 */
		for (int h = 0; h < finalArray1.length; h++) {
			println(finalArray1[h].split(" ").length + ": " + finalArray1[h]);
		}

		println("\nfinalArray1 Length: " + finalArray1.length);

		/*
		 * Now that all sentences are over minSentLen words, all sentences over
		 * minSentLen + 50 are chopped in half using the comma (,) nearest to the
		 * middle, or in the absence of a comma, the space nearest the middle
		 */
		ArrayList<String> balancedArrayList = new ArrayList<String>();

		for (int a = 0; a < finalArray1.length; a++) {

			balancedArrayList.add(finalArray1[a]);

		}

		for (int a = 0; a < balancedArrayList.size(); a++) {

			if (balancedArrayList.get(a).split(" ").length > maxSentLen) {

				String splitSencences = splitLongSentence(balancedArrayList
						.get(a));

				/*
				 * One string is returned, delimited by #######, this is parsed
				 * into two
				 */
				String firstPart = splitSencences.split("#######")[0];

				String secondPart = splitSencences.split("#######")[1];

				// reinsert these to the arrayList
				balancedArrayList.set(a, firstPart);
				balancedArrayList.add(a + 1, secondPart);

				a++;
			}

		}

		return balancedArrayList;

	}

	/*
	 * Helper methods Helper methods Helper methods
	 * 
	 * Method which splits up a long sentence using the comma (if present)
	 * nearest the middle of that sentence. If no commas is found in the
	 * sentence, the median space is used as the middle.
	 */
	private static String splitLongSentence(String longSentence) {

		/*
		 * Get the overall number of sentences in the target sentence
		 */
		String[] wordsFromSentence = longSentence.split(" ");

		// println("Sentence length: " + wordsFromSentence.length);

		int medianOfSentence = wordsFromSentence.length / 2;
		// println("\n\nMedian is: " + medianOfSentence + ", numberOfCommas: "
		// + numberOfCommas);

		HashMap<Integer, String> wordsWithCommas = new HashMap<Integer, String>();

		/*
		 * This needs a hashmap to store the index of the word in the original
		 * array. Find all the words that have a comma after them and put them
		 * in the hashmap, using their position in the original array as the key
		 */

		for (int j = 0; j < wordsFromSentence.length; j++) {

			if (wordsFromSentence[j].endsWith(",")) {

				wordsWithCommas.put(j, wordsFromSentence[j]);

			}
		}

		/*
		 * Set initial value to be a very (and unlikely) high value
		 */
		int distanceFromMedian = 5000;
		int posOfBreakValue = 0;

		/*
		 * Check that 'some' commas were found, if not, then use the middle of
		 * the sentence to split it.
		 */

		if (wordsWithCommas.size() != 0) {

			/*
			 * Iterate over the hashmap and find the value which is closest to
			 * middle of the original array (i.e. the original sentence).
			 */
			for (Entry<Integer, String> entry : wordsWithCommas.entrySet()) {

				Integer key = entry.getKey();

				if (Math.abs(medianOfSentence - key) < distanceFromMedian) {

					distanceFromMedian = Math.abs(medianOfSentence - key);
					posOfBreakValue = key;
				}
			}
		} else {
			posOfBreakValue = medianOfSentence;
		}

		String firstPart = "";

		/*
		 * Using the index of the value with a comma after it which was found to
		 * be closes to the middle of the sentence, reconstruct the first part
		 * of the original sentence.
		 */
		for (int k = 0; k < (posOfBreakValue + 1); k++) {
			firstPart = firstPart + wordsFromSentence[k] + " ";
		}

		/*
		 * Again, using the value nearest the middle, reconstruct the second
		 * part of the sentence.
		 */
		String secondPart = "";

		for (int k = (posOfBreakValue + 1); k < wordsFromSentence.length; k++) {
			secondPart = secondPart + wordsFromSentence[k] + " ";
		}

		return firstPart + "#######" + secondPart;
	}

	// /*
	// * Method which converts the balanced sentences from the XML file,
	// * now in an ArrayList to be XML in the same format as it was
	// * received in.
	// */
	// private Document arrayListToXML(ArrayList<String> balancedArrayList) {
	//
	// DocumentBuilderFactory docFactory = DocumentBuilderFactory
	// .newInstance();
	// DocumentBuilder docBuilder = null;
	// try {
	// docBuilder = docFactory.newDocumentBuilder();
	// } catch (ParserConfigurationException e) {
	// e.printStackTrace();
	// }
	//
	// Document balancedXmlDoc = docBuilder.newDocument();
	//
	// // presentation is the root element
	// Element presentation = balancedXmlDoc.createElement("presentation");
	// balancedXmlDoc.appendChild(presentation);
	//
	// // content is below the root element
	// Element content = balancedXmlDoc.createElement("content");
	// presentation.appendChild(content);
	//
	// // within a presentation, there are (multiple) slides
	// Element slide = balancedXmlDoc.createElement("slide");
	// content.appendChild(slide);
	//
	// for(String s: balancedArrayList){
	//
	// Element sentence = balancedXmlDoc.createElement("sentence");
	// sentence.appendChild(balancedXmlDoc.createTextNode(s));
	// slide.appendChild(sentence);
	// }
	//
	// return balancedXmlDoc;
	// }

	/*
	 * Method which takes sentences under a set minimum length and adds then to
	 * the next sentence.
	 */

	private static String[] joinShortSentences(int minSentLen,
			String[] sentenceContent) {

		/*
		 * Create an Arraylist which will contain all sentences which are to be
		 * used for parts of the broken-up XML file.
		 */
		ArrayList<String> parts = new ArrayList<String>();

		/*
		 * Iterating across (any) one of the above arrays...
		 */
		for (int j = 0; j < sentenceContent.length; j++) {

			/*
			 * If the sentence is less than minSentLen words long, join it to
			 * the sentence immediately after it.
			 * 
			 * Special treatment is needed for the last sentence, if under
			 * minSentLen words, it is added to the sentence immediately
			 * previous to it.
			 */
			if (sentenceContent[j].split(" ").length < minSentLen
					&& j < (sentenceContent.length - 1)) {

				parts.add(sentenceContent[j] + " " + sentenceContent[j + 1]);
				/*
				 * Now make an additional increment to j to skip the next
				 * sentence, if the next sentence is still too short, it will be
				 * caught on a subsequent pass, if it has become too long, it
				 * will be shortened by a later process.
				 */
				j++;
			} else if (sentenceContent[j].split(" ").length < minSentLen
					&& j == sentenceContent.length) {

				/*
				 * If last sentence in the array is LT minSentLen value in
				 * words, append it to the existing last item in the parts
				 * ArrayList
				 */
				String tempSentence = parts.get(parts.size() - 1);
				tempSentence = tempSentence + " " + sentenceContent[j];
				parts.add("Found last" + sentenceContent[j]);

			} else {
				// Used only for normal sentences, anywhere in the array
				parts.add(sentenceContent[j]);
			}

		}

		// println("\nparts length: " + parts.size());
		println();

		// for (String s : parts) {
		// println(s.split(" ").length + ": " + s);
		// }

		String[] returnedList = new String[parts.size()];

		returnedList = (String[]) parts.toArray(returnedList);

		return returnedList;

	}

	/*
	 * Method to determine what position/location a line would be located in for
	 * a given line number, takes an integer, returns an integer.
	 * 
	 * This method is used for two different purposes, one to get the position of
	 * each line, and finally to get the position of the last line, which is
	 * also the number of parts in the array.
	 */
	public int getPartLocation(int lineNumber, int initialSet, int blockSize) {

		int totalPlayListSize = 0;

		/*
		 * if number of 50W parts (sentences) is less than the initialSet size,
		 * just break them up using 1, 2, 2, etc.
		 */
		if (lineNumber <= initialSet && lineNumber != 0) {

			totalPlayListSize = getInitialSetPartsOnly(lineNumber);

		} else { // i.e. number of 50W parts is greated than the initialSet

			int modulusPartOnly = getInitialSetPartsOnly(initialSet);

			int blockPartsSize = getPlayListNumberOfBlocks(blockSize,
					lineNumber, initialSet);

			totalPlayListSize = modulusPartOnly + blockPartsSize;

		}

		/*
		 * needs block size, intital set size also
		 */
		return totalPlayListSize;

	}

	/*
	 * Method to determine the number of parts in the initial set only when
	 * the the initial set variable is greater than the number of sentences
	 * returned from SSC (i.e. a very short presentation).
	 */
	public int getInitialSetPartsOnly(int initialSet, int numberOfSentences) {
		
		/*
		 * This methods counts the number of parts which will be created using
		 * a 1 - 2 - 2 - 2... breakdown of sentences into parts. E.g. for an 
		 * initialSet variable of 5,  3 parts are calculated, 1, 2 and 2.
		 * 
		 * NB the variable initialSet refers to the number of sentences processed,
		 * not the number of parts.
		 * 
		 * The first part is always ONE sentence, so subtract this from the
		 * numberOfSentences
		 */
		
		int remainder = numberOfSentences - 1;

		/*
		 * initialSet will never be < 1, number of sentence will never be < 1
		 * (as this would have been detected earlier).
		 * 
		 * If ZERO is the remainder, then the first initial sentence makes up
		 * one part, then calculate how many parts are needed for the remaining
		 * sentences (i.e. the remainder) 
		 */

		/*
		 * Division here needs to be by x.0 to force Java to not do integer
		 * division. The result is then rounded up.
		 */
		
		float resultFl = 0;
		
		if(remainder != 0){
			resultFl = (float) ((remainder) / 2.0);
		}
		else {
			println("Zero remainder found for num < iset");
			resultFl = 0;
		}
		
		println("resultFl for remainder div is: " + resultFl);
		
		int resultInt = (int) (Math.ceil(resultFl)); 
		
		println("remainder initialSetPartsOnly result for num < iset: " + resultInt);
		
		/*
		 * 1 is (re)added here as it was removed before
		 */
		return resultInt + 1 ;


	}
	
	/*
	 * Method to determine the number of parts in the initial set only where
	 * the number of sentences is greater than the size if the initial set varaible
	 */
	public int getInitialSetPartsOnly(int initialSet) {
		

		if (initialSet == 1) {
			return 1;
		} else {

			/*
			 * Division here needs to be by x.0 to force Java to not do integer
			 * division. The result is then rounded up.
			 */
			float resultFl = (float) ((initialSet - 1) / 2.0) + 1;
			
			int resultInt = (int) (Math.ceil(resultFl)); 
			
			return resultInt;

		}

	}

	/*
	 * Method which determines the number of playlist items for the XML lines
	 * over and above the 'initialSet' quantity. The total for the number within
	 * the initial set needs to be added to this.
	 */
	public int getPlayListNumberOfBlocks(int blockSize, int numberOfSentences,
			int initialSet) {

		int numPartsLessInitSet = numberOfSentences - initialSet;

		if(numPartsLessInitSet==0){
			/*
			 * i.e. if numberOfSentences = initialSet
			 */
			return 0;
			
		} else if (numPartsLessInitSet <= blockSize) {
			/*
			 * numberOfParts is between 1 and 2 multiples of the blockSize
			 */

			return 1;

		} else if (numPartsLessInitSet > blockSize
				&& numPartsLessInitSet <= blockSize * 2) {
			/*
			 * numberOf50WParts is between 2 and 3 multiples of the blockSize
			 */
			return 2;

		} else if (numPartsLessInitSet > blockSize * 2
				&& numPartsLessInitSet <= blockSize * 3) {

			return 3;

		} else if (numPartsLessInitSet > blockSize * 3
				&& numPartsLessInitSet <= blockSize * 5) {
			/*
			 * numberOf50WParts is between 4 and 6 multiples of the blockSize
			 */
			return 4;

		} else if (numPartsLessInitSet > blockSize * 5
				&& numPartsLessInitSet <= blockSize * 10) {
			/*
			 */
			return 5;
		} else if (numPartsLessInitSet > blockSize * 10
				&& numPartsLessInitSet <= blockSize * 15) {
			/*
			 * 
			 */
			return 6;
		}  else if (numPartsLessInitSet > blockSize * 15
				 && numPartsLessInitSet <= blockSize * 20) {
			/*
			 * 
			 */
			return 7;
		} else if (numPartsLessInitSet > blockSize * 20
				&& numPartsLessInitSet <= blockSize * 25) {
			/*
			 * 
			 */
			return 8;
		} else if (numPartsLessInitSet > blockSize * 25
				&& numPartsLessInitSet <= blockSize * 30) {
			/*
			 * 
			 */
			return 9;
		} else if (numPartsLessInitSet > blockSize * 25
				&& numPartsLessInitSet <= blockSize * 30) {
			/*
			 * 
			 */
			return 10;
		} else if (numPartsLessInitSet > blockSize * 30
				&& numPartsLessInitSet <= blockSize * 35) {
			/*
			 * 
			 */
			return 11;
		} else if (numPartsLessInitSet > blockSize * 35
				&& numPartsLessInitSet <= blockSize * 40) {
			/*
			 * 
			 */
			return 12;
		} else if (numPartsLessInitSet > blockSize * 40
				&& numPartsLessInitSet <= blockSize * 45) {
			/*
			 * 
			 */
			return 13;
		} else if (numPartsLessInitSet > blockSize * 45
				&& numPartsLessInitSet <= blockSize * 50) {
			/*
			 * 
			 */
			return 14;
		} else if (numPartsLessInitSet > blockSize * 50
				&& numPartsLessInitSet <= blockSize * 55) {
			/*
			 * 
			 */
			return 15;
		} else if (numPartsLessInitSet > blockSize * 55
				&& numPartsLessInitSet <= blockSize * 60) {
			/*
			 * 
			 */
			return 16;
		} else if (numPartsLessInitSet > blockSize * 60
				&& numPartsLessInitSet <= blockSize * 65) {
			/*
			 * 
			 */
			return 17;
		} else if (numPartsLessInitSet > blockSize * 65
				&& numPartsLessInitSet <= blockSize * 70) {
			/*
			 * 
			 */
			return 18;
		} else if (numPartsLessInitSet > blockSize * 70
				&& numPartsLessInitSet <= blockSize * 75) {
			/*
			 * 
			 */
			return 19;
		} else if (numPartsLessInitSet > blockSize * 75
				&& numPartsLessInitSet <= blockSize * 80) {
			/*
			 * 
			 */
			return 20;
		} else if (numPartsLessInitSet > blockSize * 80
				&& numPartsLessInitSet <= blockSize * 85) {
			/*
			 * 
			 */
			return 21;
		} else if (numPartsLessInitSet > blockSize * 85
				&& numPartsLessInitSet <= blockSize * 90) {
			/*
			 * 
			 */
			return 22;
		} else {
			/*
			 * The remainder of parts over this ammount are included in one
			 * large (final) part
			 */
			return 23;
		} 
		/*
		 * TODO, this does not scale indefinitely, needs a better algorithm
		 * Also, check with Mostafa on largest file likely/possible
		 * 
		 * Update, 4300 words ~ 9 parts, longest file is 40K words TODO
		 */
		
		

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

	/*
	 * Method to determine the number of unique numbers in the partsLocation
	 * array
	 */
	public int getNumUniquePartNums(int[] partsLocation) {

		Set<Integer> uniqueNums = new HashSet<Integer>();

		for (int i : partsLocation) {
			uniqueNums.add(i);
		}

		return uniqueNums.size();
	}

}
