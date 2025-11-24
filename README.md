# iDAI.gazetteer

Das Projekt ist eine [spring boot](https://spring.io/projects/spring-boot) application und in ihm befinden sich sowohl
Frontend als auch Backend-Code. Es werden lokale Instanzen von MongoDB und Elasticsearch benötigt.

## Setup

### Konfiguration: Hauptanwendung

Vor dem Start muss sichergestellt werden, dass die nötigen Konfigurationsdateien vorhanden sind:

TODO CHECK LIST AND LOCATION
* src/main/resources/log4j.xml
* src/main/webapp/WEB-INF/web.xml
* src/main/webapp/WEB-INF/config.properties
* src/main/webapp/WEB-INF/elasticsearch.properties
* src/main/webapp/WEB-INF/mail.properties (optional, bei fehlender Mail-Konfiguration werden keine Mails versandt)

In den entsprechenden Ordnern befinden sich Template-Files, die eine Grundkonfiguration bereitstellen. Vor dem Start sollten die Templates umbenannt bzw. kopiert und evtl. angepasst werden. 
z.B. Anpassen des Speicherpfades der Logdatei, da ggfs. in template Pfad keine Schreibrechte vorliegen. In src/main/resources/log4j.xml:
```bash
<appender name="file" class="org.apache.log4j.DailyRollingFileAppender">
   <param name="file" value="/Users/[USERNAME]/gazetteer.log" />
```

#### docker compose

Es existiert eine `compose.yml` um eine Entwicklungsumgebung zu starten.

`docker compose up --build` startet:
  * mongodb: eine MongoDB mit Testdaten
  * elasticsearch: ElasticSearch
  * gazetteer: iDAI.gazetteer
  * elasticsearch-init: Ein Container der das ElasticSearch Place Template pushed, einen Index erstellt und eine Reindexierung veranlasst

### Testdaten

Die Datei [test_places.jsonl](src/test/resources/test_places.jsonl) und
[test_users.jsonl](src/test/resources/test_users.jsonl) enthalten Testdaten.  
Diese können mit folgendem Befehl importiert werden: 
```shell
  mongoimport --db gazetteer --collection place --file test_places.jsonl
  mongoimport --db gazetteer --collection user --file test_users.jsonl
```

Nicht alle Beispieldaten haben GeoLocations und werden auf der Karte angezeigt.
Eine Auswahl an Daten mit Geolocations (und Link auf die Entwicklungsinstanz):
* [Algerien](http://localhost:8080/app/#!/show/2042612)
* [Constantine](http://localhost:8080/app/#!/show/2042624)
* [Hippo Regius](http://localhost:8080/app/#!/show/2042614)

### Indizierung

Eine Elasticsearch-Reindizierung kann bei laufendem Gazetteer über die Admin-API angestoßen werden:

```
curl -XPOST admin:password@localhost:8080/admin/reindex
```

Dazu muss ein Useraccount verwendet werden, der über Adminberechtigungen verfügt.

## Architektur

Backend (Spring Boot) und Frontend (Javascript/AngularJS) werden im gleichen Projekt verwaltet. Der Quellcode des Backends liegt unter _src/main/java_, der des Frontends unter _src/main/webapp/resources/js_. Die AngularJS Views werden durch JSPs generiert und liegen unter _src/main/webapp/WEB-INF/views/html/partials_. Zusätzlich liegen unter _src/main/scripts_ einige in Ruby geschriebene Skripte für den Im- und Export für spezifische Projekte.

### i18n

Übersetzungen werden in den Dateien _src/main/resources/messages_<sprache>.properties_  und _src/main/webapps/resources/js/i18n/messages_<sprache>.js_ (Frontend) vorgenommen.

## Deployment

For creating the production and test `.war` packages see the comments in [build.sh](./build.sh).

# Migration from 2.9.9 to 3.0.0

## ElasticSearch Migration from 6.x to 8.x
TODO

## MongoDB Migration from 4 to 8

### In Development (if you want to keep data)

```bash
# Temporarily set version tag in docker-compose to '4.0'
docker compose exec mongodb mongodump --out /data/dump
docker compose rm mongodb
# Set version tag back to '8'
docker compose exec mongodb mongorestore /data/dump
rm -rf .mongo-data/dump
```

### In Development (if you dont care about your data)

```bash
docker compose rm mongodb
rm -rf .mongo-data
```

### In Production on-metal MongoDB

```bash
mongodump --out ./dump
mongorestore ./dump
# Verify that data is migrated and make a backup of dump
 ```
