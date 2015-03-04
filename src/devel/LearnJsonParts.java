package devel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import sequencer.SequenceServlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class LearnJsonParts {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		JSONObject obj = new JSONObject();
		
		obj.put("name", "mkyong.com");
		
		obj.put("age", new Integer(100));
		
		
		
		

		JSONArray list = new JSONArray();
		
		String msg1 = "msg 1";
		
		list.add(msg1);
		
		list.add("msg 2");
		
		list.add("msg 3");

		obj.put("messages", list);

		System.out.print("\n\n" + obj + "\n\n==========================\n\n");
		
		int numInPlaylist = 3;
		
		
		JSONObject[] jObjArray = new JSONObject[numInPlaylist];
		
		JSONArray overallList = new JSONArray();
		
		for(int i = 0; i < jObjArray.length; i++){
			
			jObjArray[i] = new JSONObject();
			
		}
		
		
		for(int j = 0; j < jObjArray.length; j++){
			
			jObjArray[j].put("title", "Part " + (j+1) + " of " + jObjArray.length);
			jObjArray[j].put("mp3", "http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_" + (j+1) + ".mp3");
			
			overallList.add(jObjArray[j]);	
			
		}
		
		/*
		 * Alternative...
		 */
		JSONArray overallList2 = new JSONArray();
		
		for(int k = 0; k < numInPlaylist; k++){
			
			JSONObject js = new JSONObject();
			
			js.put("title", "Part " + (k+1) + " of " + numInPlaylist);
			js.put("mp3", "http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_" + (k+1) + ".mp3");
			
			overallList2.add(js);
			
		}
		
		
//		JSONObject jsonObj = new JSONObject();
//		
//		jsonObj.put("title", "Part 1 of 2");
//		jsonObj.put("mp3", "http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_1.mp3");
//		
//		
//		
//		JSONObject jsonObj2 = new JSONObject();
//		
//		jsonObj2.put("title", "Part 2 of 2");
//		jsonObj2.put("mp3", "http://localhost/lbl/audio/1241_BelfastCityJSONTest_en_Part_2.mp3");
//		
//		overallList.add(jsonObj);
//		overallList.add(jsonObj2);
		
		
		println("1: " + overallList);
		println("2: " + overallList2);
		
		/*
		 * To print in pretty format...
		 */
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(overallList2.toJSONString());
		String prettyJsonString = gson.toJson(je);
		
		println("\nPretty...\n" + prettyJsonString);
		
		
		
		
		
		

		String errorDetails = "error_1";
		String errorDetails_a = "error_1" + "a";
		
		String errorAudioName = "";
		
		

		/*
		 * Create an JSONArray with the error feedback messages
		 */
		
		JSONArray overallErrList = new JSONArray();
		
		
			
			/*
			 * Create a new object for each iteration
			 */
			JSONObject js = new JSONObject();
			
			/*
			 * Populate this object with details for this iteration
			 */
			
			js.put("title", errorDetails);
			js.put("mp3", "http://");
			
			/*
			 * Add this object to the list
			 */
			overallErrList.add(js);	
			
			/*
			 * Create a new object for each iteration
			 */
			JSONObject js2 = new JSONObject();
			
			/*
			 * Populate this object with details for this iteration
			 */
			
			js2.put("title", errorDetails_a);
			js2.put("mp3", "http://");
			
			/*
			 * Add this object to the list
			 */
			overallErrList.add(js);
			
			
			
			
			/*
			 * Print in pretty format
			 */
			JsonElement je2 = jp.parse(overallErrList.toJSONString());
			String prettyJsonErrString = gson.toJson(je2);
		
			println("\n\n" + prettyJsonErrString);
			
			println(overallErrList);
		
		
		
		
		
		
		
		
		
		
		
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
