
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

import javax.net.ssl.HttpsURLConnection;
 
public class HttpConnection {
 
	private final String USER_AGENT = "Mozilla/5.0";
	private final String SERVER = "http://localhost:3000";
	private final String SERVER1 = "localhost";
 
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
 
		//add request header
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
	
	public String sendGetWithBody(String param_url, String value) throws IOException {
		String modifiedSentence;
		String result = "";
		Socket clientSocket = new Socket(SERVER1, 3000);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String content = value;
		outToServer.writeBytes("GET " + SERVER1 + param_url + " HTTP/1.1" + "\n" +
		"Host: " + SERVER1 + "\n" +
		"Content-Type: application/json" + "\n" +
		"Content-Length: " + content.length() + "\n" +
		"\n" +
		content
		);
//        while ((modifiedSentence = inFromServer.readLine()) != null) {
//        	//System.out.println("FROM SERVER: " + modifiedSentence);
//        	if(modifiedSentence.startsWith("[")) {
//        		result = modifiedSentence;
//        	}
//        }
        StringBuilder reply = new StringBuilder();

        while ((modifiedSentence = inFromServer.readLine()) != null) {
        	if(modifiedSentence.startsWith("[")) {
        		reply.append(modifiedSentence);
        	}
        }
        inFromServer.close();
        
		clientSocket.close();
		
		System.out.println(reply.toString());
        return reply.toString();
    }
}