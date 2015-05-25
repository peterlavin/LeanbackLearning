package devel;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
		
		JSONArray visualJSON = pnfp.convertXmlToJson(docFromDisk, true);
		
		

	}

@SuppressWarnings("unchecked")
public JSONArray convertXmlToJson(Document sAndCxml, Boolean debug){
		
		
		/*
		 * Get the 'visual' element from the XML
		 */
		
		NodeList el = sAndCxml.getElementsByTagName("visual");

		/*
		 * Firstly, it is necessary to check that there is an actual 'metrics' element
		 * found. E.g. some old versions of SSC may not produce an XML file with this data.
		 */

		/*
		 * Create a JSONArray object to hold all the JSONObjects which
		 * are created within the below for loop
		 */
		
		JSONArray visualDataArray = new JSONArray();
		
		if (el.item(0) == null) {
			
			System.out.println("\nNo 'visual' element found in SSC XML\n");
			
			return null;
			
		} else {
			
			if(debug){
				System.out.println("\nNo 'visual' element found in SSC XML\n");
			}
						
			NodeList secNodeList = sAndCxml.getElementsByTagName("section");
			
			
			for(int visualNlIndex = 0;visualNlIndex < secNodeList.getLength();visualNlIndex++){
				
				NodeList nodeList = secNodeList.item(visualNlIndex).getChildNodes();
				
				
				// each child node has 5 elements, 1 and 3 have the actual content.
				// Also checking for random nodes called 'section' which are not in 'visual'
				if (nodeList instanceof Element && secNodeList.item(visualNlIndex).getParentNode().getNodeName()=="visual" ){
					
					JSONObject nameValuePair = new JSONObject();
					
					nameValuePair.put("name", nodeList.item(1).getTextContent());
					nameValuePair.put("value", new Integer(Integer.parseInt(nodeList.item(3).getTextContent())));
					
					visualDataArray.add(nameValuePair);
			
				}
					
			}
	
		}
		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(visualDataArray.toJSONString());
		String prettyJsonString = gson.toJson(je);
		
		System.out.println("\ndata: " + prettyJsonString);
				
		return visualDataArray;
		
	}
	
	
	
}
