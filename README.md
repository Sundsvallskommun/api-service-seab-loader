# SeabLoader

## Leverantör

Sundsvalls kommun

## Beskrivning
SeabLoader är en tjänst som gör det möjligt att från InXchange ladda SEAB:s pdf-fakturor till InvoiceCache.

## Tekniska detaljer

### Integrationer
Tjänsten integrerar mot:

* Mikrotjänst InvoiceCache

### Starta tjänsten

| Konfigurationsnyckel                                                |Beskrivning|
|---------------------------------------------------------------------|---|
| **InvoiceCache**                                             ||
| `integration.invoicecache.url`                                          |URL för endpoint till InvoiceCache-tjänsten i WSO2|
| `spring.security.oauth2.client.registration.invoicecache.client-id`     |Klient-ID som ska användas för InvoiceCache-tjänsten|
| `spring.security.oauth2.client.registration.invoicecache.client-secret` |Klient-secret som ska användas för InvoiceCache-tjänsten|
| `spring.security.oauth2.client.provider.invoicecache.token-uri`         |URI till endpoint för att förnya token för InvoiceCache-tjänsten|

### Paketera och starta tjänsten
Applikationen kan paketeras genom:

```
./mvnw package
```
Kommandot skapar filen `api-service-seab-loader-<version>.jar` i katalogen `target`. Tjänsten kan nu köras genom kommandot `java -jar target/api-service-seab-loader-<version>.jar`.

### Bygga och starta med Docker
Exekvera följande kommando för att bygga en Docker-image:

```
docker build -f src/main/docker/Dockerfile -t api.sundsvall.se/ms-seab-loader:latest .
```

Exekvera följande kommando för att starta samma Docker-image i en container:

```
docker run -i --rm -p8080:8080 api.sundsvall.se/ms-seab-loader

```

#### Kör applikationen lokalt

Exekvera följande kommando för att bygga och starta en container i sandbox mode:  

```
docker-compose -f src/main/docker/docker-compose-sandbox.yaml build && docker-compose -f src/main/docker/docker-compose-sandbox.yaml up
```


## 
Copyright (c) 2022 Sundsvalls kommun