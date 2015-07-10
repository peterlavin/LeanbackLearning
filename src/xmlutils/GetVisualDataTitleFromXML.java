package xmlutils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import devel.ParseNodesFromNodes;

public class GetVisualDataTitleFromXML {

	public GetVisualDataTitleFromXML() {
		// Constructor stub
	}

	public String getTitleFromXml(Document sAndCxml, boolean debug) {

		/*
		 * Get the 'visual' element from the XML
		 */
		NodeList ndl = sAndCxml.getElementsByTagName("visual");
		String title = "";

		/*
		 * Check that there is actually a visual element (there is only one item
		 * in this NodeList so 0 can be used)
		 */
		if (ndl.item(0) == null) {

			System.out.println("\nNo 'visual' element (and no title) found in SSC XML\n");

			return "";

		} else {

			NodeList ndl2 = ndl.item(0).getChildNodes();

			if (ndl2.item(0) != null) {

				for (int i = 0; i < ndl2.getLength(); i++) {

					if (ndl2.item(i).getNodeName().equalsIgnoreCase("title")
							&& ndl2.item(i).getParentNode().getNodeName() == "visual") {

						title = ndl2.item(i).getTextContent();
					}

				}

				if (debug) {
					System.out.println("\nTitle in visual elem of SSC XML found to be: " + title);
				}
			}
			return title;
		}
	}
}
