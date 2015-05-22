package xmlutils;

import org.json.simple.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class visualDataToJSON {

	public visualDataToJSON() {
		
		// Constructor

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
			
			
			//NodeList sectionNodes = 
		
		/*
		 * Iterate 
		 */
		
			
			
		
		}
		
		
		
		
		
		
		
		
		
		
		
		return null;
		
	}

}
