package dev.chris;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Map<String,String>, Risultato> {
	
    @Override
    public Risultato handleRequest(Map<String,String> map, Context context) {
        
    	Risultato result = new Risultato();
    	String username = map.get("destinatario");
    	String id = map.get("id");
    	String privata = map.get("chiave").replaceAll("(\\r|\\n)", "");
    	
    	DynamoDbClient ddb = DynamoDbClient.builder().build();
    	
    	// Creazione della chiave primaria (usata sia per GET che per UPDATE)
    	Map<String, AttributeValue> primaryKey = new HashMap<>();
        primaryKey.put("destinatario", AttributeValue.builder().s(username).build());
        primaryKey.put("id", AttributeValue.builder().s(id).build());

    	GetItemRequest request = GetItemRequest.builder()
                .tableName("messaggi")
                .key(primaryKey)
                .build();

        // Recupero del messaggio cifrato dal database
        GetItemResponse response = ddb.getItem(request);
        Map<String, AttributeValue> item = response.item();
        
        byte[] privateKeyBytes = Base64.getMimeDecoder().decode(privata);
        
        try {
            // Generazione della chiave privata per la decifratura
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
			
			// Decifratura del messaggio
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            String mex = item.get("messaggio").s().replaceAll("(\\r|\\n)", "");
            byte[] ciphertext = Base64.getMimeDecoder().decode(mex);
            byte[] decryptedMessageBytes = cipher.doFinal(ciphertext);
            String decryptedMessage = new String(decryptedMessageBytes, "UTF-8");
            
            result.setResult("Il messaggio e': " + decryptedMessage);
            
            // Aggiornamento dello stato del messaggio a "letto"
            UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                    .tableName("messaggi")
                    .key(primaryKey)
                    .updateExpression("SET #attrName = :attrValue")
                    .expressionAttributeNames(Collections.singletonMap("#attrName", "letto"))
                    .expressionAttributeValues(Collections.singletonMap(":attrValue", AttributeValue.builder().s("si").build()))
                    .build();
            
            ddb.updateItem(updateRequest);
            
		} catch (Exception e) {
			result.setResult("Errore decript: " + e.getMessage());
			e.printStackTrace();
		}
        
        return result;
    }
}