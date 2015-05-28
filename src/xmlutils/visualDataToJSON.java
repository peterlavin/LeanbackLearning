package xmlutils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class visualDataToJSON {

	public visualDataToJSON() {
		
		// Constructor

	}
	
	@SuppressWarnings("unchecked")
	public JSONArray convertXmlToJson(Document sAndCxml, Boolean debug){
			
			
			/*
			 * Get the 'visual' element from the XML
			 */
			
			NodeList ndl = sAndCxml.getElementsByTagName("visual");

			/*
			 * Firstly, it is necessary to check that there is an actual 'metrics' element
			 * found. E.g. some old versions of SSC may not produce an XML file with this data.
			 */

			/*
			 * Create a JSONArray object to hold all the JSONObjects which
			 * are created within the below for loop
			 */
			
			JSONArray visualDataArray = new JSONArray();
			
			if (ndl.item(0) == null) {
				
				System.out.println("\nNo 'visual' element found in SSC XML\n");
				
				return null;
				
			} else {
				
				if(debug){
					System.out.println("\n'Visual' element found in SSC XML, now processing...\n");
				}
							
				NodeList secNodeList = sAndCxml.getElementsByTagName("section");
				
				
				for(int i = 0; i < secNodeList.getLength(); i++){
					
					NodeList nodeList = secNodeList.item(i).getChildNodes();
					
					
					// each child node has 5 elements, 1 and 3 have the actual content.
					// Also checking for random nodes called 'section' which are not in 'visual'
					if (nodeList instanceof Element && secNodeList.item(i).getParentNode().getNodeName()=="visual" ){
						
						JSONObject nameValuePair = new JSONObject();
						
						nameValuePair.put("name", nodeList.item(1).getTextContent());
						
						// The value needs to be inserted as an integer to avoid quotes, i.e. "333"
						nameValuePair.put("value", new Integer(Integer.parseInt(nodeList.item(3).getTextContent())));
						
						/*
						 * Add this JSONObject to the array to be returned
						 */
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
