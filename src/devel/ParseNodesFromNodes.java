package devel;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseNodesFromNodes {

	public ParseNodesFromNodes() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		System.out.println();
		
		
		File file = new File("//var/www/html/lbl/content/700_CorkCity.xml");
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document docFromDisk = null;

		try {

			dBuilder = dbFactory.newDocumentBuilder();
			dBuilder = dbFactory.newDocumentBuilder();
			docFromDisk = dBuilder.parse(file);

		} catch (ParserConfigurationException | SAXException | IOException e3) {
			e3.printStackTrace();
		}

		ParseNodesFromNodes pnfp = new ParseNodesFromNodes();
		
		JSONArray visualJSON = pnfp.convertXmlToJson(docFromDisk);
		
		

	}

public JSONArray convertXmlToJson(Document sAndCxml){
		
		
		/*
		 * Get the 'visual' element from the XML
		 */
		
		NodeList el = sAndCxml.getElementsByTagName("visual");

		/*
		 * Firstly, it is necessary to check that there is an actual 'metrics' element
		 * found. E.g. some old versions of SSC may not produce an XML file with this data.
		 */

		if (el.item(0) == null) {
			
			System.out.println("\nNo 'visual' element found in SSC XML\n");
			
		} else {
						
			NodeList secNodeList = sAndCxml.getElementsByTagName("section");
			
			
			for(int visualNlIndex = 0;visualNlIndex < secNodeList.getLength();visualNlIndex++){
				
				NodeList nodeList = secNodeList.item(visualNlIndex).getChildNodes();
				
				// each child node has 5 elements, 1 and 3 have the actual content.
				// Also checking for random nodes called 'section' which are not in 'visual'
				if (nodeList instanceof Element && secNodeList.item(visualNlIndex).getParentNode().getNodeName()=="visual" ){
					
					
					/*
					 * TODO, pop these values into the required JOSN format and return
					 */
					System.out.print(nodeList.item(1).getTextContent() + " - ");
					System.out.println(nodeList.item(3).getTextContent());
				}
					
			}
	
		}
		
				
		return null;
		
	}
	
	
	
}
