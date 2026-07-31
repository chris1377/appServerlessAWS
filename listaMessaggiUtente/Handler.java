package dev.chris;



import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;



import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.*;



public class Handler implements RequestHandler<Map<String,String>, Risultato> {
	
    @Override
    public Risultato handleRequest(Map<String,String> map, Context context) {
    	String username = (String) map.get("username");
        // Configura il client di Amazon Cognito
    	DynamoDbClient dynamoDb = DynamoDbClient.builder()
                .region(Region.EU_NORTH_1) 
                .build();

        // Specifica i parametri per la query
    	QueryRequest queryRequest = QueryRequest.builder()
                .tableName("messaggi") // Nome della tabella "ordini"
                .keyConditionExpression("destinatario = :val") // Specifica la chiave di partizione
                .expressionAttributeValues(Collections.singletonMap(":val", AttributeValue.builder().s(username).build()))
                .build();

        // Esegui la query
        QueryResponse response = dynamoDb.query(queryRequest);
        dynamoDb.close();
        // Elabora i risultati
        Risultato ris = new Risultato();
        ris.setRisult("Fail");
        List<List<String>> totale = new ArrayList<List<String>>();
        
        for (Map<String, AttributeValue> item : response.items()) {
        	List<String> tupla = new ArrayList<String>();
        	ris.setRisult("Ok");
        	tupla.add(item.get("id").s());
        	tupla.add(item.get("mittente").s());
        	tupla.add(item.get("letto").s());
        	totale.add(tupla);
            
        }
        ris.setMessaggi(totale);
        return ris;
       
    }
  }
