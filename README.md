# Applicazione Web Serverless su AWS - Progetto di Tesi

## Introduzione
Questo repository contiene il codice back-end e i dettagli implementativi sviluppati per il mio progetto di tesi triennale, incentrato sulla realizzazione di applicazioni web serverless sfruttando l'infrastruttura di **Amazon Web Services (AWS)**. 

L'obiettivo dell'applicazione è fornire una **bacheca online sicura** in cui gli utenti possono registrarsi, autenticarsi e scambiarsi messaggi protetti tramite crittografia asimmetrica (algoritmo RSA a 2048 bit). 

Tutta la logica applicativa è delegata alle funzioni **AWS Lambda** (scritte in Java), garantendo scalabilità automatica senza la necessità di gestire server fisici o virtuali. In preparazione alla pubblicazione, il codice originale è stato fortemente ottimizzato:
* **Riduzione del Cold Start**: Sono state rimosse tutte le librerie e gli import non necessari per velocizzare i tempi di inizializzazione dei container Lambda.
* **Sicurezza**: Le credenziali e gli ID dell'infrastruttura (come il `Pool ID` di Cognito) sono stati disaccoppiati dal codice sorgente e gestiti in modo sicuro tramite Variabili di Ambiente.

---

## Front-end e Hosting (AWS Amplify)

Il front-end dell'applicazione è stato sviluppato interamente in **HTML, CSS e JavaScript (vanilla)**. Per la pubblicazione e l'hosting delle pagine web statiche è stato utilizzato il servizio **AWS Amplify**.

L'interfaccia utente è composta da diverse pagine, tra cui `index.html` (home page della bacheca), `login.html`, `registrazione.html`, `crittaMessaggio.html` e `decrittaMessaggio.html`, tutte corredate da un foglio di stile `style.css` per una presentazione grafica curata. 

Il front-end è completamente disaccoppiato dall'infrastruttura di calcolo: le pagine HTML comunicano con le funzioni Lambda del back-end inviando **richieste asincrone (AJAX)** agli endpoint HTTP esposti tramite **Amazon API Gateway**. L'intero pacchetto front-end è stato archiviato in un file `.zip` e distribuito direttamente tramite la console di Amplify Hosting.

---

## Architettura del Back-end (Funzioni Lambda)

Le funzioni Lambda sono suddivise in quattro aree logiche principali, richiamate dal front-end tramite API Gateway.

### 1. Autenticazione e Gestione Identità (Amazon Cognito)
Queste funzioni delegano la gestione degli utenti al servizio Amazon Cognito, garantendo standard di sicurezza elevati per l'accesso all'area personale.

| Funzione | Descrizione | Input Principali | Output |
| :--- | :--- | :--- | :--- |
| **Registrazione** | Interfaccia la richiesta dell'utente con Cognito (`SignUp`) per creare un nuovo profilo. | `username`, `password`, `email` | Stringa di conferma invio email. |
| **Conferma** | Valida l'account appena creato tramite il codice univoco recapitato via email (`adminConfirmSignUp`). | `username`, `code` | Esito della validazione. |
| **Login** | Verifica le credenziali ed emette un JSON Web Token (JWT) di sessione (`AdminInitiateAuth`). | `username`, `password` | JWT (`idToken`) o Errore. |

### 2. Sicurezza e Generazione Chiavi
Queste funzioni gestiscono la creazione e il recupero del materiale crittografico, interagendo con **Amazon DynamoDB**. Per garantire l'assoluta privacy, le chiavi private non vengono mai memorizzate nel database.

| Funzione | Descrizione | Input | Output |
| :--- | :--- | :--- | :--- |
| **Cerca Propria Chiave** | Esegue una *Query* per verificare se l'utente ha già una chiave attiva. | `username` | Chiave pubblica Base64 o `Fail`. |
| **Genera Chiavi** | Genera una coppia RSA (2048 bit). Salva la pubblica sul DB e restituisce la privata all'utente. | `username` | Chiavi pubblica e privata. |

### 3. Core Messaggistica Sicura
Questa è l'area più complessa del sistema, dove si integrano chiamate tra diverse funzioni Lambda e operazioni di lettura/scrittura su DB.

| Funzione | Descrizione | Input Principali | Output |
| :--- | :--- | :--- | :--- |
| **Critta Messaggio** | Invoca in modo sincrono una Lambda per ottenere la chiave del destinatario, cifra il testo (RSA/PKCS1) e lo salva su DB. | `mittente`, `messaggio`, `destinatari` | Esito dell'invio. |
| **Decritta Messaggio** | Recupera il payload cifrato, lo decifra localmente con la chiave privata dell'utente e imposta lo stato su "letto". | `destinatario`, `id`, `chiave privata` | Testo in chiaro. |

### 4. Lettura Dati e Visualizzazione
Funzioni ottimizzate per il recupero delle informazioni che popolano il front-end (home page e sezioni personali). Sfruttano operazioni di *Scan* e *Query* su DynamoDB.

| Funzione | Descrizione | Operazione DB |
| :--- | :--- | :--- |
| **Lista Chiavi Pubbliche** | Recupera l'elenco globale degli utenti e delle rispettive chiavi pubbliche per la Home. | *Scan* su tabella `chiavi` |
| **Lista Utenti** | Genera l'elenco dei destinatari disponibili per il form di invio messaggi. | *Scan* su tabella `chiavi` |
| **Lista Tutti Messaggi** | Carica i metadati e i testi cifrati dell'intera bacheca pubblica globale. | *Scan* su tabella `messaggi` |
| **Lista Messaggi Utente** | Recupera solo i messaggi diretti a uno specifico utente (area privata). | *Query* su tabella `messaggi` |

---

## Gestione dei Permessi (AWS IAM)

In conformità con il **Principio del Minimo Privilegio (Least Privilege)**, essenziale in architetture cloud per garantire la sicurezza del sistema, a ogni singola funzione Lambda è stato associato un Ruolo IAM personalizzato. Ogni ruolo concede l'accesso strettamente necessario alle risorse coinvolte:

* Le funzioni di **Autenticazione** hanno accesso esclusivo al proprio `UserPool` di Cognito.
* Le funzioni di **Lettura/Scrittura** hanno regole di *Allow* mirate su specifiche API DynamoDB (`dynamodb:PutItem`, `dynamodb:Query`, `dynamodb:Scan`, `dynamodb:GetItem`, `dynamodb:UpdateItem`) limitate al *Resource ARN* della rispettiva tabella.
* La funzione di **Cifratura** possiede l'autorizzazione speciale `lambda:InvokeFunction` per poter orchestrare la chiamata in modo sincrono verso l'estrattore di chiavi pubbliche.
