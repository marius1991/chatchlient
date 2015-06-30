import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonHandler {
	
	public JSONObject convert (String input) {
		
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(input);
		} catch (JSONException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return jObject;
		
	}
	
	public String extractString (JSONObject input, String key) {

		String output = "";
		if (input.has(key)) {
		try {
			output = input.getString(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return output;
	}
	
	public int extractInt (JSONObject input, String key) {

		int output = 98;
		if (input.has(key)) {
		try {
			output = input.getInt(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return output;
	}
	
	public JSONArray extractArray (JSONObject input, String key) {

		JSONArray output = null;
		if (input.has(key)) {
		try {
			output = input.getJSONArray(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return output;
	}
}
