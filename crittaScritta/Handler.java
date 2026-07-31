package dev.chris;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class Handler implements RequestHandler<Map<String,String>, Risultato> {
	
    @Override
    public Risultato handleRequest(Map<String,String> map, Context context) {
        
    	Risultato result = new Risultato();
    	String res = "";
    	Gson gson = new Gson();
    	String username = map.get("username");
    	String messaggio = map.get("messaggio");
    	String[] destinatari = (map.get("destinatari")).split("@");
    	
    	DynamoDbClient ddb = DynamoDbClient.builder().build();
    	LambdaClient lambdaClient = LambdaClient.create();
    	
    	// Recupera il nome della Lambda esterna dalle variabili di ambiente
    	String lambdaCercaChiave = System.getenv("LAMBDA_CERCA_CHIAVE");
    	
    	for(String dest : destinatari) {
    		try {
    		    // 1. Recupero chiave pubblica tramite invocazione di un'altra Lambda
    		    String payloadString = "{\"username\": \""+dest+"\"}";  		
                InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(lambdaCercaChiave)
                    .payload(SdkBytes.fromUtf8String(payloadString))
                    .build();
                
                InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
                String responsePayload = invokeResponse.payload().asUtf8String();
                ChiavePubblica pub = gson.fromJson(responsePayload, ChiavePubblica.class);
                
                byte[] publicKeyBytes = Base64.getMimeDecoder().decode(pub.getChiave());
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(keySpec);
				
                // 2. Cifratura del messaggio
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				byte[] encryptedBytes = cipher.doFinal(messaggio.getBytes());
				String crittato = Base64.getEncoder().encodeToString(encryptedBytes).replaceAll("(\\r|\\n)", "");
				
				// 3. Salvataggio su DynamoDB
				Map<String, AttributeValue> valori = new HashMap<>();
				valori.put("destinatario", AttributeValue.builder().s(dest).build());
				valori.put("id", AttributeValue.builder().s(System.currentTimeMillis()+"").build());
				valori.put("mittente", AttributeValue.builder().s(username).build());
				valori.put("messaggio", AttributeValue.builder().s(crittato).build());
				valori.put("letto", AttributeValue.builder().s("no").build());	
				
				PutItemRequest request = PutItemRequest.builder()
		    			.tableName("messaggi")
		    			.item(valori)
		    			.build();
				
		    	ddb.putItem(request);
		    	res += " Messaggio aggiunto alla bacheca per: " + dest;    	
		    	
			} catch (Exception e) {
				e.printStackTrace();
				res += " Fallimento messaggio per: " + dest;
			} 
    	}
    	
    	result.setResult(res.trim());
        return result;
    }
}