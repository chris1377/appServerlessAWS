package dev.chris;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Map<String,String>, Risultato> {
	
    @Override
    public Risultato handleRequest(Map<String,String> map, Context context) {
    	Risultato result = new Risultato();
    	
    	try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048); // Dimensione chiave RSA
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			PublicKey publicKey = keyPair.getPublic();
		    PrivateKey privateKey = keyPair.getPrivate();
		    
		    // Codifica in Base64 e rimozione ritorni a capo
	        result.setPrivata(Base64.getMimeEncoder().encodeToString(privateKey.getEncoded()).replaceAll("(\\r|\\n)", ""));
	        result.setPubblica(Base64.getMimeEncoder().encodeToString(publicKey.getEncoded()).replaceAll("(\\r|\\n)", ""));
	        result.setResult("Ok");
		} catch (Exception e) {
			result.setResult("Fail");
			result.setPrivata("Fail creazione chiave " + e.getMessage());
	        result.setPubblica("Fail");
	        return result;
		}
    	
    	Map<String, AttributeValue> valori = new HashMap<>();
    	String username = map.get("username");
    	valori.put("username", AttributeValue.builder().s(username).build());
    	// Utilizza la chiave già ripulita salvata nell'oggetto result
    	valori.put("chiave", AttributeValue.builder().s(result.getPubblica()).build());
    	
    	PutItemRequest request = PutItemRequest.builder()
    			.tableName("chiavi")
    			.item(valori)
    			.build();
    			
    	try {
    		DynamoDbClient ddb = DynamoDbClient.builder().region(Region.EU_NORTH_1).build();
    		ddb.putItem(request);
    	} catch(Exception e) {
    		result.setResult("Fail");
			result.setPrivata("Fail salvataggio in database: "+ e.getMessage());
	        result.setPubblica("Fail");
	        return result;
    	}
    	
        return result; 
    }
}