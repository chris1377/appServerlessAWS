package dev.chris;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import java.util.Map;

public class Handler implements RequestHandler<Map<String,String>, String> {
	
    @Override
    public String handleRequest(Map<String,String> map, Context context) {
        // Configura il client di Amazon Cognito
    	CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                 .region(Region.EU_NORTH_1)
                 .build();

         // Estrai i dati dalla richiesta HTTP
         String username = map.get("username");
         String password = map.get("password");
         String email = map.get("email");

         // Recupera il Client ID in modo sicuro dalle variabili d'ambiente
         String clientId = System.getenv("CLIENT_ID");

         // Crea la richiesta di registrazione per Cognito
         SignUpRequest signUpRequest = SignUpRequest.builder()
                 .clientId(clientId)
                 .username(username)
                 .password(password)
                 .userAttributes(AttributeType.builder()
                         .name("email")
                         .value(email)
                         .build())
                 .build();

         try {
             // Esegui la registrazione
             SignUpResponse response = cognitoClient.signUp(signUpRequest);
             return "Registrazione riuscita, controlla la tua mail per attivare l'account";
         } catch (Exception e) {
             // Gestione errore
             return "Errore durante la registrazione: " + e.getMessage();
         }
     }
}