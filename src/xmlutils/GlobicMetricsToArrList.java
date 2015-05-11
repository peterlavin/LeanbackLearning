package xmlutils;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GlobicMetricsToArrList {

	/*
	 * Constructor
	 */
	public GlobicMetricsToArrList() {

	}

	/*
	 * Extracts the child nodes from the metrics elements returned from SSC and
	 * adds them to an arrayList, which is then returned. Further details are
	 * added to this arrayList back in SequenceServlet.
	 */
	public ArrayList<String> metricsToArrList(
			ArrayList<String> dataArrayGlobic, Document sAndCxml, boolean debug) {

		NodeList el = sAndCxml.getElementsByTagName("metrics");

		/*
		 * Firstly, it is necessary to check that there is an actual 'metrics' element
		 * found. E.g. some old versions of SSC may not produce an XML file with this data.
		 */

		if (el.item(0) == null) {
			
			System.out.println("\nNo 'metrics' element found in SSC XML\n");
			
		} else {

			/*
			 * If the 'metrics' element is present, there will only ever be one of them,
			 * so use the first (0) item in that nodeList
			 */
			NodeList metrics = el.item(0).getChildNodes();

			/*
			 * Each childNode is a metric to be logged to GLOBIC.
			 * An all lower-case version of the node name is used in the arrayList.
			 * 
			 * A string of ####### is used as a separator
			 */
			for (int i = 0; i < metrics.getLength(); i++) {

				Node node = metrics.item(i);
				
				/*
				 * In case of corruption of the XML, check it is an element node
				 */
				if (node.getNodeType() == Node.ELEMENT_NODE) {

					if (debug) {
						System.out.println("SSC GLOBIC data added: "
								+ node.getNodeName().toLowerCase() + "#######"
								+ node.getTextContent());
					}

					dataArrayGlobic.add(node.getNodeName().toLowerCase()
							+ "#######" + node.getTextContent());
				}
			}
		}

		return dataArrayGlobic;

	}

}
