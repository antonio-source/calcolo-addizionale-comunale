# Calcolo Addizionale Comunale

Questo progetto Spring Boot fornisce un servizio REST per il calcolo dell'addizionale comunale, con caricamento automatico dei dati da file CSV.

## Requisiti

*   Java 11
*   Apache Maven
*   PostgreSQL (richiesto solo per il profilo di produzione)

## Profili di Configurazione

Il progetto è configurato per funzionare con due profili Spring distinti:

### 1. Profilo `default` (per Sviluppo)

*   **Attivazione**: Automatico, se non viene specificato nessun altro profilo.
*   **Database**: Utilizza un database in-memory H2 che viene creato e distrutto a ogni avvio.
*   **Gestione Schema**: Lo schema del database è gestito da Hibernate (`ddl-auto`).
*   **Dati**: I dati vengono caricati dai file CSV a ogni avvio, ma vengono persi allo spegnimento.

### 2. Profilo `prod` (per Produzione)

*   **Attivazione**: Deve essere attivato esplicitamente.
*   **Database**: Si connette a un database PostgreSQL esterno. Le credenziali devono essere configurate nel file `src/main/resources/application-prod.properties`.
*   **Gestione Schema**: Lo schema del database è gestito tramite **Flyway**. Le modifiche allo schema devono essere fatte tramite nuovi script di migrazione.
*   **Dati**: I dati vengono caricati all'avvio se le tabelle sono vuote.

## Avvio dell'applicazione

All'avvio, l'applicazione esegue un `DataLoaderRunner` che si occupa di svuotare il database e ripopolarlo con i dati presenti nei file CSV nella cartella `resources/csv/aliquote-addizionali-comunali`.

### Avvio in modalità Sviluppo (default)

Esegui il seguente comando dalla directory principale del progetto:

```bash
# Usa il database H2 in-memory
mvn spring-boot:run
```

### Avvio in modalità Produzione

1.  Assicurati di aver configurato le credenziali del tuo database PostgreSQL nel file `application-prod.properties`.
2.  Compila il progetto creando il file JAR:
    ```bash
    mvn clean package
    ```
3.  Esegui l'applicazione attivando il profilo `prod`:
    ```bash
    # Usa PostgreSQL e Flyway
    java -jar -Dspring.profiles.active=prod target/calcolo-addizionale-comunale-1.0-SNAPSHOT.jar
    ```

## Gestione Schema Database con Flyway (Profilo `prod`)

Quando il profilo `prod` è attivo, lo schema del database è gestito da Flyway. Per apportare modifiche allo schema:

1.  Crea un nuovo file SQL nella directory `src/main/resources/db/migration`.
2.  Il nome del file deve seguire la convenzione `V<NUMERO>__<DESCRIZIONE>.sql` (es. `V2__aggiungi_colonna_note.sql`).

Flyway applicherà automaticamente le nuove migrazioni all'avvio successivo dell'applicazione.

## Endpoints

### Documentazione API (Swagger)

Una volta avviata l'applicazione, la documentazione interattiva dell'API è disponibile all'indirizzo:

*   `http://localhost:8080/`

### H2 Console (solo profilo `default`)

Il database in-memory H2 è accessibile dalla console web all'indirizzo:

*   `http://localhost:8080/h2-console`

**Impostazioni di connessione:**

*   **Driver Class:** `org.h2.Driver`
*   **JDBC URL:** `jdbc:h2:mem:pensioni-tipologiche`
*   **User Name:** `sa`
*   **Password:** (lasciare vuoto)
