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



public class Handler implements RequestHandler<Void, Risultato> {
	
    @Override
    public Risultato handleRequest(Void map, Context context) {
        // Configura il client di Amazon Cognito
    	DynamoDbClient dynamoDb = DynamoDbClient.builder()
                .region(Region.EU_NORTH_1) 
                .build();

        // Specifica i parametri per la query
        ScanRequest queryRequest = ScanRequest.builder()
                .tableName("chiavi") // Nome della tabella "ordini"
                .build();

        // Esegui la query
        ScanResponse response = dynamoDb.scan(queryRequest);
        dynamoDb.close();
        // Elabora i risultati
        Risultato ris = new Risultato();
        ris.setRisult("Fail");
        List<String> utenti = new ArrayList<String>();
        for (Map<String, AttributeValue> item : response.items()) {
        	ris.setRisult("Ok");
           utenti.add(item.get("username").s());
            
        }
        ris.setUtenti(utenti);
        return ris; 
    }

	
  }
