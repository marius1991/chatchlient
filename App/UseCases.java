import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UseCases {
	
	private byte[] privkey_user = null;
	private String name;
	private HttpConnection httpConnection = new HttpConnection();
	private Security security = new Security();
	private JsonHandler jsonHandler = new JsonHandler();
	private static final String TAG = UseCases.class.getName();

	/**
	 * Registrierung
	 * @param name Name 
	 * @param password Passwort
	 * @return Statuscode 98: Sonstiger Fehler, 99: Keine Verbidung
	 */
	public int register(String name, char[] password) {
		privkey_user = null;

		//salt_masterkey bilden
		final Random r = new SecureRandom();
		byte[] salt_masterkey = new byte[64];
		r.nextBytes(salt_masterkey);

		//Aufruf PBKDF2 Funktion um masterkey aus password und salt_masterkey zu bilden
		byte[] masterkey = security.pbkf2(password, salt_masterkey);
		if(masterkey == null) {
			return 98;
		}
		
		//KeyPair bilden
		KeyPairGenerator kpg = null;
		
		//KeyPairGenerator erzeugen --> Algorithmus: RSA 2048
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			return 98;
		}
		SecureRandom securerandom = new SecureRandom();
	    byte bytes[] = new byte[20];
	    securerandom.nextBytes(bytes);
		kpg.initialize(2048, securerandom);
		
		//KeyPair erzeugen
		KeyPair kp = kpg.genKeyPair();
		
		//publickey und privatekey in Variablen speichern
		Key pubkey_user = kp.getPublic();
		Key privkey_user = kp.getPrivate();
		byte[] privateKeyByte = privkey_user.getEncoded();
		byte[] publicKeyByte = pubkey_user.getEncoded();
		String publickey = security.writePublicKey(publicKeyByte);
		String privatekey = security.writePrivateKey(privateKeyByte);
		String publickey64 = Base64.toBase64String(publickey.getBytes());
		
		//privkey_user zu privkey_user_enc verschlüsseln
		byte[] privkey_user_enc = security.encryptAESECB(masterkey, privateKeyByte);
		if(privkey_user_enc == null) {
			return 98;
		}
		
		//Übergabestring erstellen
		String value = "{\"id\":\"" + name + "\",\"salt_masterkey\":\"" + DatatypeConverter.printHexBinary(salt_masterkey) + "\",\"pubkey_user\":\"" + publickey64 + "\",\"privkey_user_enc\":\"" + DatatypeConverter.printHexBinary(privkey_user_enc) + "\"}";
		
		//Verbindung zum Server herstellen
		String success = "";
		try {
			success = httpConnection.sendPost("/User", value);
		} catch (Exception e) {
			return 99;
		}
		
		//Logausgabe
//		Log.d(TAG, "Übergabestring: " + value);
//		Log.d(TAG, "Rückgabestring: " + success);
		
		//Rückgabe eines Statuscodes
		if(!success.equals("")) {
			return jsonHandler.extractInt(jsonHandler.convert(success), "fehlercode");
		}
		else return 98;
	}
	
	/**
	 * Anmeldung
	 * @param name Name
	 * @param password Passwort
	 * @return Statuscode 98: Sonstiger Fehler, 99: Keine Verbidung
	 */
	public int login(String name, char[] password) {
		privkey_user = null;
		this.name = name;

		//Verbindung zum Server herstellen
		String value = "{\"id\":\"" + name + "\"}";
		String success = "";
		try {
			success = httpConnection.sendGetWithBody("/User", value);
		} catch (Exception e) {
			return 99;
		}
		
		//String in JSON umwandeln und Daten extrahieren
		JsonHandler jHandler = new JsonHandler();	
		String salt_masterkeyString = jHandler.extractString(jHandler.convert(success), "salt_masterkey");
		String privkey_user_encString = jHandler.extractString(jHandler.convert(success), "privkey_user_enc");
		byte[] salt_masterkey = DatatypeConverter.parseHexBinary(salt_masterkeyString);
		byte[] privkey_user_enc = DatatypeConverter.parseHexBinary(privkey_user_encString);
		
		//Masterkey bilden
		byte[] masterkey = security.pbkf2(password, salt_masterkey);
		if(masterkey == null) {
			return 98;
		}
		
		//PrivateKey entschlüsseln
		privkey_user = security.decryptAESECB(masterkey, privkey_user_enc);
		if(privkey_user == null) {
			return 98;
		}
		
		//Logausgabe
//		Log.d(TAG, "Übergabestring: " + value);
//		Log.d(TAG, "Rückgabestring: " + success);
		
		//Rückgabe eines Statuscodes
		if(!success.equals("")) {
			return jsonHandler.extractInt(jsonHandler.convert(success), "fehlercode");
		}
		else return 98;
	}
	
	/**
	 * PublicKey anfordern
	 * @param recipientName Empfängername
	 * @return Statuscode 98: Sonstiger Fehler, 99: Keine Verbidung
	 */
	public String getPubkey(String recipientName) {
		//Id_enc bilden
		//Unix-Zeit
		Long unixTime = System.currentTimeMillis() / 1000L;
		String timestamp = unixTime.toString();
		
		//Bildung von MD5 Hash für Id_enc
		String text = name + timestamp;
		byte [] textBytes = text.getBytes();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return "98";
		}
		md.update(textBytes); // Change this to "UTF-16" if needed
		byte[] id = md.digest();
		String idHex = DatatypeConverter.printHexBinary(id).toLowerCase();
		
		//Verschlüsselung dnulles Hashes mit dem Private Key
		byte[] id_RSA = security.encryptRSAPrivKey(privkey_user, idHex.getBytes());
		if(id_RSA == null) {
			return "98";
		}

		String id_enc = DatatypeConverter.printBase64Binary(id_RSA);
		
		//Verbindung zum Server herstellen
		String success = "";
		String value = "{\"empfaengerID\":\"" + recipientName + "\",\"id\":\"" + name + "\",\"timestamp\":\"" + timestamp + "\",\"id_enc\":\"" + id_enc + "\"}";
		JsonHandler jHandler = new JsonHandler();
		try {
			success = httpConnection.sendGetWithBody("/User/Pubkey", value );
		} catch (Exception e) {
			return "99";
		}

		if (jHandler.extractString(jHandler.convert(success), "pubkey_recipient").equals("")) {
			String status = jHandler.extractString(jHandler.convert(success), "fehlercode");
			return status;
		}
		else {
			String pubkey_recipient = jHandler.extractString(jHandler.convert(success), "pubkey_recipient");
			byte[] b = DatatypeConverter.parseBase64Binary(pubkey_recipient);
			return new String(b);
		}
	}

	/**
	 * Nachrichtenversand
	 * @param name Name
	 * @param recipientName Empfängername
	 * @param nachrichtparam Nachricht
	 * @return Statuscode 98: Sonstiger Fehler, 99: Keine Verbidung
	 */
	public int sendMessage(String name, String recipientName, String nachrichtparam) {
		String status = getPubkey(recipientName);
		if(status.equals("99")) {
			return 99;
		}
		if(status.length() < 10) {
			return 98;
		}
		else {
			byte[] nachricht = nachrichtparam.getBytes();
			String pubKey = status.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
			byte[] pubkey_recipient = DatatypeConverter.parseBase64Binary(pubKey);

			
			//Symmetrischen Schlüssel bilden
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance("AES");
			} catch (NoSuchAlgorithmException e) {
				return 98;
			}
			SecureRandom securerandom = new SecureRandom();
		    byte bytes[] = new byte[20];
		    securerandom.nextBytes(bytes);
			kg.init(128, securerandom);
			SecretKey key_recipient_secret = kg.generateKey();
			byte[] key_recipient = key_recipient_secret.getEncoded();
			
			//Initialisierungsvektor erzeugen
			final Random r = new SecureRandom();
			byte[] iv = new byte[16];
			r.nextBytes(iv);

			//Nachricht verschlüsseln
			byte[] cipher = security.encryptAESCBC(nachricht, pubkey_recipient, iv, key_recipient_secret);
			if(cipher == null) {
				return 98;
			}
			
			//key_recipient mit Public Key verschlüsseln
			byte[] key_recipient_enc = security.encryptRSAPubKey(pubkey_recipient, key_recipient);
			if(key_recipient_enc == null) {
				return 98;
			}
			
			//Bildung von SHA-256 Hash für sig_recipient
			String text = name + DatatypeConverter.printBase64Binary(cipher) + DatatypeConverter.printBase64Binary(iv) + DatatypeConverter.printBase64Binary(key_recipient_enc);
			byte [] textBytes = text.getBytes();
			
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				return 98;
			}
			md.update(textBytes); // Change this to "UTF-16" if needed
			byte[] sig_recipient = md.digest();
			String sig_recipientHex = DatatypeConverter.printHexBinary(sig_recipient).toLowerCase();
			
			//Verschlüsselung des Hashes mit dem Private Key
			byte[] sig_recipient_enc = security.encryptRSAPrivKey(privkey_user, sig_recipientHex.getBytes());

			if(sig_recipient_enc == null) {
				return 98;
			}
			
			//Unix-Zeit
			Long unixTime = System.currentTimeMillis() / 1000L;
			String timestamp = unixTime.toString();
			
			//Bildung von SHA-256 Hash für sig_service
			String text1 = "{\"Id\":\"" + name + "\",\"Cipher\":\"" + DatatypeConverter.printBase64Binary(cipher) + "\",\"Iv\":\"" + DatatypeConverter.printBase64Binary(iv) + "\",\"key_recipient_enc\":\"" + DatatypeConverter.printBase64Binary(key_recipient_enc) + "\",\"sig_recipient\":\"" + DatatypeConverter.printBase64Binary(sig_recipient_enc) + "\"}" + timestamp + recipientName;
			String text3 = text1.replace("/","\\/");
			byte [] text1Bytes = text3.getBytes();
			MessageDigest md1 = null;
			try {
				md1 = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				return 98;
			}
			md1.update(text1Bytes); // Change this to "UTF-16" if needed
			byte[] sig_service = md1.digest();
			String sig_serviceHex = DatatypeConverter.printHexBinary(sig_service).toLowerCase();
			
			//Verschlüsselung des Hashes mit dem Private Key
			byte[] sig_service_enc = security.encryptRSAPrivKey(privkey_user, sig_serviceHex.getBytes());
			if(sig_service_enc == null) {
				return 98;
			}
			
			//Id_enc bilden
			//Bildung von MD5 Hash für Id_enc
			String text2 = name + timestamp;
			byte [] text2Bytes = text2.getBytes();
			MessageDigest md2 = null;
			try {
				md2 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				return 98;
			}
			md2.update(text2Bytes); // Change this to "UTF-16" if needed
			byte[] id = md2.digest();
			String idHex = DatatypeConverter.printHexBinary(id).toLowerCase();
			
			//Verschlüsselung des Hashes mit dem Private Key
			byte[] id_enc = security.encryptRSAPrivKey(privkey_user, idHex.getBytes());
			if(id_enc == null) {
				return 98;
			}
			
			String value = "{\"Innerer_Umschlag\":{\"Id\":\"" + name + "\",\"Cipher\":\"" + DatatypeConverter.printBase64Binary(cipher) + "\",\"Iv\":\"" + DatatypeConverter.printBase64Binary(iv) + "\",\"key_recipient_enc\":\"" + DatatypeConverter.printBase64Binary(key_recipient_enc) + "\",\"sig_recipient\":\"" + DatatypeConverter.printBase64Binary(sig_recipient_enc) + "\"},\"Empfaenger\":\"" + recipientName + "\",\"timestamp\":" + timestamp + ",\"id_enc\":\"" + DatatypeConverter.printBase64Binary(id_enc) + "\",\"sig_service\":\"" + DatatypeConverter.printBase64Binary(sig_service_enc) + "\"}";
			
			//Verbindung zum Server herstellen
			String success = "";
			try {
				success = httpConnection.sendPost("/Msg", value);
			} catch (Exception e) {
				return 99;
			}

			//Logausgabe
//			Log.d(TAG, "Übergabestring: " + value);
//			Log.d(TAG, "Rückgabestring: " + success);
			
			//Rückgabe eines Statuscodes
			if(!success.equals("")) {
				return jsonHandler.extractInt(jsonHandler.convert(success), "fehlercode");
			}
			else return 98;
		}
	}
	
	//Nachrichtenabruf
	public ArrayList<String[]> receiveMessage(String name) {
		//Unix-Zeit
		Long unixTime = System.currentTimeMillis() / 1000L;
		String timestamp = unixTime.toString();
		
		//Id_enc bilden
		//Bildung von MD5 Hash für Id_enc
		String text2 = name + timestamp;
		byte [] text2Bytes = text2.getBytes();
		MessageDigest md2 = null;
		try {
			md2 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md2.update(text2Bytes); // Change this to "UTF-16" if needed
		byte[] id = md2.digest();
		String idHex = DatatypeConverter.printHexBinary(id).toLowerCase();
		
		//Verschlüsselung des Hashes mit dem Private Key
		byte[] id_enc = security.encryptRSAPrivKey(privkey_user, idHex.getBytes());
		if(id_enc == null) {
			return null;
		}
	
		//Verbindung zum Server herstellen
		String success = "";
		JsonHandler jHandler = new JsonHandler();
		String value = "{\"id\":\"" + name + "\",\"timestamp\":\"" + timestamp + "\",\"id_enc\":\"" + DatatypeConverter.printBase64Binary(id_enc) + "\"}";
		String url = "/Msg";
		HttpConnection http = new HttpConnection();
		try {
			success = http.sendGetWithBody(url, value);
		} catch (IOException e1) {
			return null;
		}
		JSONObject json = jHandler.convert(success);
		
		int anzahl = jHandler.extractInt(jHandler.convert(success), "AnzahlNachrichten");
		String fehlercode = jHandler.extractString(jHandler.convert(success), "fehlercode");
		JSONArray nachrichten = jHandler.extractArray(json, "Nachrichten");
		ArrayList<JSONObject> nachrichtenList = new ArrayList<>();
		try {
			for(int i=0; i<anzahl; i++) {
				nachrichtenList.add(nachrichten.getJSONObject(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<String[]> messages = new ArrayList<>();
		
		for(int i=0; i<nachrichtenList.size(); i++) {
			//Bildung von SHA-256 Hash für sig_recipient
			JSONObject innererUmschlag = new JSONObject();
			try {
				innererUmschlag = nachrichtenList.get(i).getJSONObject("Innerer_Umschlag");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String status = getPubkey(jHandler.extractString(innererUmschlag,"Sender"));
			if(status.length() < 10) {
				return null;
			}
			else {
				String pubKey = status.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
				byte[] pubkey_recipient = DatatypeConverter.parseBase64Binary(pubKey);
				byte[] signature = DatatypeConverter.parseBase64Binary(jHandler.extractString(innererUmschlag,"sig_recipient"));
				byte[] sig_recipientByte = security.decryptRSAPubKey(pubkey_recipient, signature);
				String sig_recipientString = new String(Base64.decode(DatatypeConverter.printBase64Binary(sig_recipientByte)));
				String text = jHandler.extractString(innererUmschlag,"Sender") + jHandler.extractString(innererUmschlag,"Cipher") + jHandler.extractString(innererUmschlag,"Iv") + jHandler.extractString(innererUmschlag,"key_recipient_enc");
				byte [] textBytes = text.getBytes();
				
				MessageDigest md = null;
				try {
					md = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					return null;
				}
				md.update(textBytes); // Change this to "UTF-16" if needed
				byte[] sig_recipient = md.digest();
				String sig_recipientHex = DatatypeConverter.printHexBinary(sig_recipient).toLowerCase();
				if(!sig_recipientHex.equals(sig_recipientString)) {
					return null;
				}
				else {
					//key_recipient_enc entschlüsseln
					byte[] key_recipient = security.decryptRSAPrivKey(privkey_user, DatatypeConverter.parseBase64Binary(jHandler.extractString(innererUmschlag,"key_recipient_enc")));
					
					byte[] nachricht = security.decryptAESCBC(DatatypeConverter.parseBase64Binary(jHandler.extractString(innererUmschlag,"Cipher")), key_recipient, DatatypeConverter.parseBase64Binary(jHandler.extractString(innererUmschlag,"Iv")));
					String message[] = {jHandler.extractString(innererUmschlag,"Sender"),new String(nachricht)};
					messages.add(message);
				}
			}
		}

		return messages;
	}
	
	//User anzeigen
	public ArrayList<String> showUsers() {
		ArrayList<String> x = new ArrayList<>();
		return x;
	}
	
}