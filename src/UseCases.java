import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
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


public class UseCases {
	
	private byte[] privkey_user = null;

	//Registrierung
	public int register(String name, char[] password) {
		
		System.out.println("------------Registrierung------------");
		System.out.println("");

		//salt_masterkey bilden
		final Random r = new SecureRandom();
		byte[] salt_masterkey = new byte[64];
		r.nextBytes(salt_masterkey);

		//Aufruf PBKDF2 Funktion um masterkey aus password und salt_masterkey zu bilden
		byte[] masterkey = pbkf2(password, salt_masterkey);
		
		//KeyPair bilden
		KeyPairGenerator kpg = null;
		//KeyPairGenerator erzeugen --> Algorithmus: RSA 2048
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			// Auto-generated catch block
			e.printStackTrace();
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
		String publickey64 = new String(DatatypeConverter.printBase64Binary(publicKeyByte));
		
		//privkey_user zu privkey_user_enc verschlüsseln
		byte[] privkey_user_enc = encryptAESECB(masterkey, privateKeyByte);
		
		String value = "{\"name\":\"" +name+ "\",\"salt_master_key\":\"" +new BigInteger(1, salt_masterkey).toString(16)+ "\",\"public_key\":\"" +publickey64+ "\",\"private_key_enc\":\"" +new BigInteger(1, privkey_user_enc).toString(16)+ "\"}";
		System.out.println(value);
		
		//Verbindung zum Server herstellen
		HttpConnection con = new HttpConnection();
		String success = "";
		try {
			success = con.sendPost("/users", value);
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		
		//Logausgabe
		System.out.println("Übergabestring: " + value);
		System.out.println("PrivateKey vor der Verschlüsselung: " + new BigInteger(1, privateKeyByte).toString(16));
		System.out.println("PrivateKey nach der Verschlüsselung: " + new BigInteger(1, privkey_user_enc).toString(16));
		
		//Rückgabe eines Statuscodes
		if(success.equals("{\"status\":\"1\"}")) {
			return 1;
		}
		if(success.equals("{\"status\":\"3\"}"))	{
			return 3;
		}
		else {
			return 2;
		}
	}
	
	//Anmeldung
	public int login(String name, char[] password) {
		System.out.println("");
		System.out.println("------------Anmeldung------------");

		//Verbindung zum Server herstellen
		HttpConnection con = new HttpConnection();
		String success = "";
		try {
			success = con.sendGet("/users/"+name);
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ÜbergabeString: " + success);
		
		//String in JSON umwandeln und Daten extrahieren
		JsonHandler jHandler = new JsonHandler();	
		String salt_masterkeyString = jHandler.extraxtString(jHandler.convert(success), "salt_master_key");
		String pubkey_userString = jHandler.extraxtString(jHandler.convert(success), "public_key");
		String privkey_user_encString = jHandler.extraxtString(jHandler.convert(success), "private_key_enc");
		byte[] salt_masterkey = DatatypeConverter.parseHexBinary(salt_masterkeyString);
		byte[] privkey_user_enc = DatatypeConverter.parseHexBinary(privkey_user_encString);		
		byte[] masterkey = pbkf2(password, salt_masterkey);	
		privkey_user = decryptAESECB(masterkey, privkey_user_enc);
		
		//Logausgabe
		System.out.println("PrivateKey vor der Entschlüsselung: " + new BigInteger(1, privkey_user_enc).toString(16));
		System.out.println("PrivateKey nach der Entschlüsslung: " + new BigInteger(1, privkey_user).toString(16));
		
		//Rückgabe eines Statuscodes
		if(success.equals("{\"status\":\"2\"}")) {
			return 2;
		}
		if(success.equals("{\"status\":\"3\"}"))	{
			return 3;
		}
		else {
			return 1;
		}
	}
	
	//Public Key anfordern
	public String getPubkey(String recipientName) {
		System.out.println("");
		System.out.println("------------Pubkey anforden------------");
		//Verbindung zum Server herstellen
		HttpConnection con = new HttpConnection();
		String success = "";
		JsonHandler jHandler = new JsonHandler();
		try {
			success = con.sendGet("/users/" + recipientName + "/pubkey");
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		if (jHandler.extraxtString(jHandler.convert(success), "public_key").equals("")) {
			String status = jHandler.extraxtString(jHandler.convert(success), "status");
			return status;
		}
		else {
			String pubkey_recipient = jHandler.extraxtString(jHandler.convert(success), "public_key");
			byte[] b = DatatypeConverter.parseBase64Binary(pubkey_recipient);
			return new BigInteger(1, b).toString(16);
		}
	}
	
	//Nachrichtenversand
	public int sendMessage(String name, String recipientName, String nachrichtparam) {	
		String status = getPubkey(recipientName); 
		if (status.equals("2")) {
			System.out.println("Sonstiger Fehler");
			return 2;
		}
		if (status.equals("3")) {
			System.out.println("Empfänger Existiert nicht");
			return 3;
		}
		else {
			byte[] nachricht = nachrichtparam.getBytes();
			byte[] pubkey_recipient = DatatypeConverter.parseHexBinary(status);
//			System.out.println("PublicKey von " + recipientName + ": " + status);
//			System.out.println("Nachricht: " + nachrichtparam);
			
			//Symmetrischen Schlüssel bilden
			KeyGenerator kg = null;
			try {
				kg = KeyGenerator.getInstance("AES");
			} catch (NoSuchAlgorithmException e) {
				// Auto-generated catch block
				e.printStackTrace();
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
			byte[] cipher = encryptAESCBC(nachricht, pubkey_recipient, iv, key_recipient_secret);
			
			//key_recipient mit Public Key verschlüsseln
			byte[] key_recipient_enc = encryptRSAPubKey(pubkey_recipient, key_recipient);
			
			//Bildung von SHA-256 Hash für sig_recipient
			String text = name + new BigInteger(1, cipher).toString(16) + new BigInteger(1, iv).toString(16) + new BigInteger(1, key_recipient_enc).toString(16);
			byte [] textBytes = text.getBytes();
			
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			md.update(textBytes); // Change this to "UTF-16" if needed
			byte[] sig_recipient = md.digest();
			System.out.println("-sig_recipient: " + new BigInteger(1, sig_recipient).toString(16));
			//Verschlüsselung des Hashes mit dem Private Key
			byte[] sig_recipient_enc = encryptRSAPrivKey(privkey_user, sig_recipient);
			System.out.println("-sig_recipient_enc: " + new BigInteger(1, sig_recipient_enc).toString(16));
			
			//Unix-Zeit
			Long unixTime = System.currentTimeMillis() / 1000L;
			String timestamp = unixTime.toString();
			//System.out.println("Timestamp: " + timestamp);
			
			//Bildung von SHA-256 Hash für sig_service
			String text1 = text + timestamp + recipientName;
			byte [] text1Bytes = text1.getBytes();
			MessageDigest md1 = null;
			try {
				md1 = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			md.update(text1Bytes); // Change this to "UTF-16" if needed
			byte[] sig_service = md1.digest();
			
			//Verschlüsselung des Hashes mit dem Private Key
			byte[] sig_service_enc = encryptRSAPrivKey(privkey_user, sig_service);
			System.out.println("-sig_service: " + new BigInteger(1, sig_service_enc).toString(16));
			String sig_service_enc64 = new String(DatatypeConverter.printBase64Binary(sig_service_enc));
			System.out.println("-sig_service_enc64: " + sig_service_enc64);
			
			String value = "{\"name\":\"" +name+ "\",\"cipher\":\"" +new BigInteger(1, cipher).toString(16)+ "\",\"iv\":\"" +new BigInteger(1, iv).toString(16)+ "\",\"key_recipient_enc\":\"" +new BigInteger(1, key_recipient_enc).toString(16)+ "\",\"sig_recipient\":\"" +new BigInteger(1, sig_recipient_enc).toString(16)+ "\",\"timestamp\":\"" +timestamp+ "\",\"recipientname\":\"" +recipientName+ "\",\"sig_service\":\"" +sig_service_enc64+ "\"}";
			
			//Verbindung zum Server herstellen
			HttpConnection con = new HttpConnection();
			String success = "";
			try {
				success = con.sendPost("/messages", value);
			} catch (Exception e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println(success);
			
			return 1;
		}
	}
	
	//Nachrichtenabruf
	public String receiveMessage(String name) {
		return "x";
	}
	
	//User anzeigen
	public ArrayList<String> showUsers() {
		ArrayList<String> x = new ArrayList<>();
		return x;
	}
	
	//pbkf2 Funktion
	public byte[] pbkf2(char[] password, byte[]salt) {
		PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
		try {
			gen.init(new String(password).getBytes("UTF-8"), salt, 10000);
		} catch (UnsupportedEncodingException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		byte[] masterkey = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();
		
		return masterkey;
	}
	
	//AESECB Funktion encrypt
	public byte[] encryptAESECB(byte[] masterkey, byte[] privateKeyByte) {
		SecretKeySpec key = new SecretKeySpec(masterkey, "AES");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
			// Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		byte[] privkey_user_enc = null;
		try {
			privkey_user_enc = cipher.doFinal(privateKeyByte);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return privkey_user_enc;
	}
	
	//AESECB Funktion decrypt
	public byte[] decryptAESECB (byte[] masterkey, byte[] privkey_user_enc) {
		SecretKeySpec key = new SecretKeySpec(masterkey, "AES");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
			// Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		byte[] privkey_user = null;
		try {
			privkey_user = cipher.doFinal(privkey_user_enc);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return privkey_user;
	}
	
	//AESCBC Funktion encrypt
	public byte[] encryptAESCBC (byte[] nachricht, byte[] pubkey_recipient, byte[] iv, SecretKey key_recipient) {
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Cipher c = null;
		try {
			c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, key_recipient, ivspec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] cipher = null;
		try {
			cipher = c.doFinal(nachricht);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cipher;		
	}
	
	//RSA Verschlüsselung mit Public Key
	public byte[] encryptRSAPubKey (byte[] pubkey, byte[] text) {
		PublicKey publicKey = null;
		try {
			publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubkey));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Cipher c = null;
		try {
			c = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, publicKey);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] text_enc = null;
		try {
			text_enc = c.doFinal(text);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text_enc;
	}
	
	//RSA Verschlüsselung mit Private Key
	public byte[] encryptRSAPrivKey (byte[] privkey, byte[] text) {
		PrivateKey privKey = null;
		try {
			privKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privkey));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Cipher c = null;
		try {
			c = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, privKey);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] text_enc = null;
		try {
			text_enc = c.doFinal(text);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text_enc;
	}
}