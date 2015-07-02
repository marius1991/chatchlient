import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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
	
	public String extraxtString (JSONObject input, String key) {

		String output = "";
		if (input.has(key)==true) {
		try {
			output = input.getString(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return output;
	}
}
