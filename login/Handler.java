package dev.chris;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Map<String,String>, Risultato> {
	
    @Override
    public Risultato handleRequest(Map<String,String> map, Context context) {
        // Configura il client di Amazon Cognito
    	 CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                 .region(Region.EU_NORTH_1)
                 .build();

         // Estrai i dati dalla richiesta HTTP
         String username = map.get("username");
         String password = map.get("password");

         // Recupera Pool ID e Client ID dalle variabili d'ambiente
         String userPoolId = System.getenv("USER_POOL_ID");
         String clientId = System.getenv("CLIENT_ID");

         Risultato ris = new Risultato();
         Map<String, String> authParameters = new HashMap<>();
         authParameters.put("USERNAME", username);
         authParameters.put("PASSWORD", password);
         
         // Crea una richiesta di autenticazione (Login)
         AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                 .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                 .userPoolId(userPoolId)
                 .clientId(clientId)
                 .authParameters(authParameters)
                 .build();

         try {
             // Esegui il Login
             AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
             String idToken = authResponse.authenticationResult().idToken();
             
             // Popola il risultato con il Token
             ris.setIdToken(idToken);
             ris.setRisult("Ok");
             ris.setMessaggio("Ok");
             return ris;
         } catch (Exception e) {
             // Gestisci errori di autenticazione (es. password errata)
             ris.setRisult("Fail");
             ris.setMessaggio(e.getMessage());
             ris.setIdToken("Error");
             return ris;
         }
     }
}