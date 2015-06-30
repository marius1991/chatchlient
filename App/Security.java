import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;


public class Security {
	/**
	 * PBKDF2 Funktion Algorithmus: sha-256, Länge: 256 Bit, Iterationen: 10000
	 * @param password Passwort
	 * @param salt Salt
	 * @return
	 */
	public byte[] pbkf2(char[] password, byte[]salt) {
		PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
		byte[] masterkey;
		try {
			gen.init(new String(password).getBytes("UTF-8"), salt, 10000);
			masterkey = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();
		} catch (UnsupportedEncodingException e) {
			masterkey = null;
		}
		return masterkey;
	}
	
	/**
	 * AES-ECB-128 Verschlüsselungsfunktion
	 * @param masterkey Masterkey
	 * @param privateKeyByte PrivateKey
	 * @return
	 */
	public byte[] encryptAESECB(byte[] masterkey, byte[] privateKeyByte) {
		SecretKeySpec key = new SecretKeySpec(masterkey, "AES");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
			return null;
			//e1.printStackTrace();
		}
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			return null;
			//e.printStackTrace();
		}
		byte[] privkey_user_enc = null;
		try {
			privkey_user_enc = cipher.doFinal(privateKeyByte);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			return null;
			//e.printStackTrace();
		}
		return privkey_user_enc;
	}
	
	/**
	 * AES-ECB-128 Entschlüsselungsfunktion
	 * @param masterkey Masterkey
	 * @param privkey_user_enc PrivateKey
	 * @return
	 */
	public byte[] decryptAESECB (byte[] masterkey, byte[] privkey_user_enc) {
		SecretKeySpec key = new SecretKeySpec(masterkey, "AES");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
			return null;
			//e.printStackTrace();;
		}
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			return null;
			//e.printStackTrace();
		}
		byte[] privkey_user = null;
		try {
			privkey_user = cipher.doFinal(privkey_user_enc);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			return null;
			//e.printStackTrace();
		}
		return privkey_user;
	}
	
	/**
	 * AES-CBC-128 Verschlüsselungsfunktion
	 * @param nachricht Nachricht
	 * @param pubkey_recipient PubKey
	 * @param iv Initialisierungsvektor
	 * @param key_recipient Key_recipient
	 * @return
	 */
	public byte[] encryptAESCBC (byte[] nachricht, byte[] pubkey_recipient, byte[] iv, SecretKey key_recipient) {
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Cipher c = null;
		try {
			c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			return null;
			//e.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, key_recipient, ivspec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			return null;
			//e.printStackTrace();
		}
		byte[] cipher = null;
		try {
			cipher = c.doFinal(nachricht);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			return null;
			//e.printStackTrace();
		}
		return cipher;		
	}
	
	/**
	 * AES-CBC-128 Entschlüsselungsfunktion
	 * @param cipher Cipher
	 * @param key_recipient Key_recipient
	 * @param iv Initialisierungsvektor
	 * @return
	 */
	public byte[] decryptAESCBC (byte[] cipher, byte[] key_recipient, byte[] iv) {
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		SecretKeySpec key = new SecretKeySpec(key_recipient, "AES");
		Cipher c = null;
		try {
			c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		try {
			c.init(Cipher.DECRYPT_MODE, key, ivspec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		byte[] nachricht = null;
		try {
			nachricht = c.doFinal(cipher);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return nachricht;		
	}
	
	/**
	 * RSA Verschlüsselung mit Public Key
	 * @param pubkey PubKey
	 * @param text Text
	 * @return
	 */
	public byte[] encryptRSAPubKey (byte[] pubkey, byte[] text) {
		PublicKey publicKey = null;
		try {
			publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubkey));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {
			return null;
			//e.printStackTrace();
		}
		Cipher c = null;
		try {
			c = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			return null;
			//e.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, publicKey);
		} catch (InvalidKeyException e) {
			return null;
			//e.printStackTrace();
		}
		byte[] text_enc = null;
		try {
			text_enc = c.doFinal(text);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			return null;
			//e.printStackTrace();
		}
		return text_enc;
	}
	
	/**
	 * RSA Entschlüsselung mit Public Key
	 * @param pubkey PubKey
	 * @param text Text
	 * @return
	 */
	public byte[] decryptRSAPubKey (byte[] pubkey, byte[] text) {
		PublicKey publicKey = null;
		try {
			publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubkey));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {
//			return null;
			e1.printStackTrace();
		}
		Cipher c = null;
		try {
			c = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//			return null;
			e.printStackTrace();
		}
		try {
			c.init(Cipher.DECRYPT_MODE, publicKey);
		} catch (InvalidKeyException e) {
//			return null;
			e.printStackTrace();
		}
		byte[] text_dec = null;
		try {
			text_dec = c.doFinal(text);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
//			return null;
			e.printStackTrace();
		}
		return text_dec;
	}

	/**
	 * RSA Verschlüsselung mit Private Key
	 * @param privkey PrivKey
	 * @param text Text
	 * @return
	 */
	public byte[] encryptRSAPrivKey (byte[] privkey, byte[] text) {
		PrivateKey privKey = null;
		try {
			privKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privkey));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {
			return null;
			//e.printStackTrace();
		}
		Cipher c = null;
		try {
			c = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			return null;
			//e.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, privKey);
		} catch (InvalidKeyException e) {
			return null;
			//e.printStackTrace();
		}
		byte[] text_enc = null;
		try {
			text_enc = c.doFinal(text);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			return null;
			//e.printStackTrace();
		}
		return text_enc;
	}
	
	/**
	 * RSA Entschlüsselung mit Private Key
	 * @param privkey PrivKey
	 * @param key_recipient_enc Key_recipient_enc
	 * @return
	 */
	public byte[] decryptRSAPrivKey (byte[] privkey, byte[] key_recipient_enc) {
		PrivateKey privKey = null;
		try {
			privKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privkey));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		Cipher c = null;
		try {
			c = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		try {
			c.init(Cipher.DECRYPT_MODE, privKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		byte[] text_dec = null;
		try {
			text_dec = c.doFinal(key_recipient_enc);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return text_dec;
	}
	
	public String writePublicKey(byte[] publicKeyByte) {
		StringWriter writer = new StringWriter();
		PemWriter pemWriter = new PemWriter(writer);
		try {
			pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKeyByte));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			pemWriter.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			pemWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return writer.toString();
	}
	
	public String writePrivateKey(byte[] privateKeyByte) {
		StringWriter writer = new StringWriter();
		PemWriter pemWriter = new PemWriter(writer);
		try {
			pemWriter.writeObject(new PemObject("PRIVATE KEY", privateKeyByte));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			pemWriter.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			pemWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    return writer.toString();
	}
	
}
