package dev.chris;

import org.slf4j.LoggerFactory;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;


import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpResponse;




public class Handler implements RequestHandler<Map<String,String>, String> {
	
    @Override
    public String handleRequest(Map<String,String> map, Context context) {
        // Configura il client di Amazon Cognito
    	 CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                 .region(Region.EU_NORTH_1) // Sostituisci con la tua regione
                 .build();

         // Estrai i dati dalla richiesta (es. username, password, attributi)
         String username = (String) map.get("username");
         String code = (String) map.get("code");
        
         
         // Crea una richiesta di registrazione
         AdminConfirmSignUpRequest confirmRequest = AdminConfirmSignUpRequest.builder()
                 .username(username)
                 .userPoolId("eu-north-1_IDeesQeuJ")
                 .confirmationCode(code)
                 .build();

         try {
             // Esegui la registrazione
        	 AdminConfirmSignUpResponse confirmResponse = cognitoClient.adminConfirmSignUp(confirmRequest);

             // You can access the response data if needed
            
             
             
             return "Email confirmation successful" + confirmResponse.toString();
         } catch (Exception e) {
             // Gestisci eventuali errori o eccezioni qui
        	 e.printStackTrace();
             return "Email confirmation failed";
         }
     }

	
  }
