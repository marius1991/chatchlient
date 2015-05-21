import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONException;
import org.json.JSONObject;


public class UseCases {

	//Registrierung
	public int register(String name, char[] password) {
		
		//Logger print name
		//System.out.println(name);
		
		//salt_masterkey bilden
		final Random r = new SecureRandom();
		byte[] salt_masterkey = new byte[64];
		r.nextBytes(salt_masterkey);

		
		//Aufruf PBKDF2 Funktion um masterkey aus password und salt_masterkey zu bilden
		byte[] masterkey = pbkf2(password, salt_masterkey);
		
		//KeyPair bilden
		KeyPairGenerator kpg = null;
		//KeyPairGenerator erzugen --> Algorithmus: RSA 2048
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		kpg.initialize(2048);
		//KeyPair erzeugen
		KeyPair kp = kpg.genKeyPair();
		//publickey und privatekey in Variablen speichern
		Key pubkey_user = kp.getPublic();
		Key privkey_user = kp.getPrivate();
		byte[] privateKeyByte = privkey_user.getEncoded();
		byte[] publicKeyByte = pubkey_user.getEncoded();
		
		
		//privkey_user zu privkey_user_enc verschlüsseln
		byte[] privkey_user_enc = encryptAES(masterkey, privateKeyByte);

		
		
		 
		
		
		String value = "{\"name\":\"" +name+ "\",\"salt_master_key\":\"" +new BigInteger(1, salt_masterkey).toString(16)+ "\",\"public_key\":\"" +new BigInteger(1, publicKeyByte).toString(16)+ "\",\"private_key_enc\":\"" +new BigInteger(1, privkey_user_enc).toString(16)+ "\"}";
		//System.out.println(value);
		
		
		HttpConnection con = new HttpConnection();
		String success = "";
		try {
			success = con.sendPost("/users", value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Logger
		//System.out.println(s);
		//System.out.println(new String(password));
		//System.out.println("salt_mastekey: " + new BigInteger(1, salt_masterkey).toString(16));
		//System.out.println("pubKey: " + new BigInteger(1, publicKeyByte).toString(16));
		System.out.println("privKey: " + new BigInteger(1, privateKeyByte).toString(16));
		System.out.println("privKeyenc: " + new BigInteger(1, privkey_user_enc).toString(16));
		//System.out.println("masterkey: " + new BigInteger(1, masterkey).toString(16));
		//System.out.println("masterkey1: " + new BigInteger(1, key.getEncoded()).toString(16));
		//System.out.println(success);
		
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
		HttpConnection con = new HttpConnection();
		String success = "";
		try {
			success = con.sendGet("/users/"+name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(success);
		
		JsonHandler jHandler = new JsonHandler();
		
			
		String salt_masterkeyString = jHandler.extraxtString(jHandler.convert(success), "salt_master_key");
		String pubkey_userString = jHandler.extraxtString(jHandler.convert(success), "public_key");
		String privkey_user_encString = jHandler.extraxtString(jHandler.convert(success), "private_key_enc");
		byte[] salt_masterkey = DatatypeConverter.parseHexBinary(salt_masterkeyString);
		byte[] privkey_user_enc = DatatypeConverter.parseHexBinary(privkey_user_encString);
		
		byte[] masterkey = pbkf2(password, salt_masterkey);
		
		byte[] privkey_user = decryptAES(masterkey, privkey_user_enc);
		
		
		System.out.println("privKey: " + new BigInteger(1, privkey_user).toString(16));
		System.out.println("privKeyenc: " + new BigInteger(1, privkey_user_enc).toString(16));
		
		
		//Logger
		//System.out.println(new String(password));
		//System.out.println(salt_masterkeyString);
		//System.out.println(pubkey_userString);
		//System.out.println(privkey_user_encString);
		//System.out.println("masterkey: " + new BigInteger(1, masterkey).toString(16));
		//System.out.println("salt_mastekey: " + new BigInteger(1, salt_masterkey).toString(16));
		
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
		HttpConnection con = new HttpConnection();
		String success = "";
		try {
			success = con.sendGet("/users/" + recipientName + "/pubkey");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(success);
		return "x";
	}
	
	//Nachrichtenversand
	public String sendMessage(String name, String recipientName) {
		return "x";
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
	//Passwort und salt_masterkey übergeben
	public byte[] pbkf2(char[] password, byte[]salt) {
		PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
		try {
			gen.init(new String(password).getBytes("UTF-8"), salt, 10000);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] masterkey = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();
		
		return masterkey;
	}
	
	//AES Funktion encrypt
	public byte[] encryptAES(byte[] masterkey, byte[] privateKeyByte) {
		SecretKeySpec key = new SecretKeySpec(masterkey, "AES");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] privkey_user_enc = null;
		try {
			privkey_user_enc = cipher.doFinal(privateKeyByte);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return privkey_user_enc;
	}
	
	//AES Funktion decrypt
	public byte[] decryptAES (byte[] masterkey, byte[] privkey_user_enc) {
		SecretKeySpec key = new SecretKeySpec(masterkey, "AES");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] privkey_user = null;
		try {
			privkey_user = cipher.doFinal(privkey_user_enc);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return privkey_user;
	}
}




//KeySpec spec = new PBEKeySpec(password, salt_masterkey, 1000, 256);
//SecretKeyFactory f = null;
//try {
//	f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//} catch (NoSuchAlgorithmException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//SecretKey masterkey = null;
//try {
//	masterkey = f.generateSecret(spec);
//} catch (InvalidKeySpecException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}



////parameterSpec erzeugen mit password, salt_masterkey, Interationen=1000 und Länge=256Bit
//		PBEKeyAndParameterSpec parameterSpec = new PBEKeyAndParameterSpec(new String(password).getBytes(), salt, 1000, 256);
//		//KeyGenerator erzeugen --> Verschlüsselungsalgorithmus: SHA-256
//		KeyGenerator pbkdf2 = null;
//		try {
//			pbkdf2 = KeyGenerator.getInstance("PBKDF2WithHmacSHA256", "IAIK");
//		} catch (NoSuchAlgorithmException | NoSuchProviderException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} 
//		//parameterSpec an KeyGenerator übergeben
//		try {
//			pbkdf2.init(parameterSpec);
//		} catch (InvalidAlgorithmParameterException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		//masterkey bilden
//		SecretKey masterkey = pbkdf2.generateKey();
//		