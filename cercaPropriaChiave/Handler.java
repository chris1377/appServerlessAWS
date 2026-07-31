package dev.chris;



import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;



import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.*;



public class Handler implements RequestHandler<Map<String,String>, Risultato> {
	
    @Override
    public Risultato handleRequest(Map<String,String> map, Context context) {
        // Configura il client di Amazon Cognito
    	DynamoDbClient dynamoDb = DynamoDbClient.builder()
                .region(Region.EU_NORTH_1) 
                .build();

    	String username = (String) map.get("username");
        // Specifica i parametri per la query
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("chiavi")
                .keyConditionExpression("username = :val")
                .expressionAttributeValues(Collections.singletonMap(":val", AttributeValue.builder().s(username).build()))
                .build();

        // Esegui la query
        QueryResponse response = dynamoDb.query(queryRequest);
        dynamoDb.close();
        // Elabora i risultati
        Risultato ris = new Risultato();
        for (Map<String, AttributeValue> item : response.items()) {
            // Elabora ciascun elemento restituito
        	ris.setChiave(item.get("chiave").s());
        	ris.setRisult("Ok");
            return ris;
        }
        ris.setChiave("Error");
        ris.setRisult("Fail");
        return ris;  
    }

	
  }
