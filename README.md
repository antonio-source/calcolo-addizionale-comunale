# Calcolo Addizionale Comunale

Questo progetto Spring Boot fornisce un servizio REST per il calcolo dell'addizionale comunale.

## Requisiti

*   Java 11
*   Apache Maven

## Avvio dell'applicazione

1.  Clona il repository.
2.  Esegui il seguente comando dalla directory principale del progetto:

    ```bash
    mvn spring-boot:run
    ```

L'applicazione sarà disponibile all'indirizzo `http://localhost:8080`.

## Endpoints

### Documentazione API (Swagger)

Una volta avviata l'applicazione, la documentazione interattiva dell'API è disponibile all'indirizzo:

*   `http://localhost:8080/`

### H2 Console

Il database in-memory H2 è accessibile dalla console web all'indirizzo:

*   `http://localhost:8080/h2-console`

**Impostazioni di connessione:**

*   **Driver Class:** `org.h2.Driver`
*   **JDBC URL:** `jdbc:h2:mem:pensioni-tipologiche`
*   **User Name:** `sa`
*   **Password:** (lasciare vuoto)
