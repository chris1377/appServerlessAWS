package dev.chris;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class main {

	public static void main (String[] args) {
		
		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048); // Key size (2048 bits is common)
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			PublicKey publicKey = keyPair.getPublic();
			PrivateKey privateKey = keyPair.getPrivate();
			System.out.println(publicKey);
			
			String pubsenzarn = Base64.getEncoder().encodeToString(publicKey.getEncoded()).replaceAll("(\\r|\\n)", "");
			byte[] publicKeyBytes = Base64.getDecoder().decode(pubsenzarn);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory;
            keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey2 = keyFactory.generatePublic(keySpec);
			
			
			String originalText = "This is a secret message.";
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey2);

			byte[] encryptedBytes = cipher.doFinal(originalText.getBytes());
			String crittato = Base64.getEncoder().encodeToString(encryptedBytes).replaceAll("(\\r|\\n)", "");
			
			System.out.println(encryptedBytes);
			System.out.println(crittato);
			
			
			String privsenzarn = Base64.getEncoder().encodeToString(privateKey.getEncoded()).replaceAll("(\\r|\\n)", "");
			byte[] privateKeyBytes = Base64.getMimeDecoder().decode(privsenzarn);
			KeyFactory keyFactory2 = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec2 = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey2 = keyFactory2.generatePrivate(keySpec2);
			
			
			Cipher cipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);

			byte[] ciphertext = Base64.getMimeDecoder().decode(crittato);
			
			byte[] decryptedBytes = cipherDecrypt.doFinal(ciphertext);
			String decryptedText = new String(decryptedBytes);
			System.out.println("Decrypted Text: " + decryptedText);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		}
	
}
