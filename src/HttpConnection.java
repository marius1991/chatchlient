
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

import javax.net.ssl.HttpsURLConnection;
 
public class HttpConnection {
 
	private final String USER_AGENT = "Mozilla/5.0";
	private final String SERVER = "http://localhost:3000";
 
	// HTTP GET request
	public String sendGet(String param_url) throws Exception {
 
		String url = SERVER + param_url;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		return response.toString();
	}
 
	// HTTP POST request
	public String sendPost(String param_url, String param_body) throws Exception {
 
		String body = param_body;
		String url = SERVER + param_url;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod( "POST" );
		con.setDoInput( true );
		con.setDoOutput( true );
		con.setUseCaches( false );
		con.setRequestProperty( "Content-Type","application/json" );
		con.setRequestProperty( "Content-Length", String.valueOf(body.length()) );
 
		// Send post request
		OutputStreamWriter writer = new OutputStreamWriter( con.getOutputStream() );
		writer.write( body );
		writer.flush();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		//System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		writer.close();
		in.close();
 
		//print result
		System.out.println("Rückgabestring: " + response.toString());
		
		return response.toString();
	}
	
 
}