# iDAI.gazetteer

In dem Projekt befinden sich sowohl Frontend als auch Backend-Code. Als IDE empfiehlt sich STS. Außerdem werden lokale Instanzen von MongoDB und Elasticsearch benötigt.

## Setup

### Import des Github Repos in IDE

#### STS
Das Projekt kann über _File -> Import -> Maven -> Existing Maven Project_ in STS importiert werden und kann nach Auflösen der Dependencies mit _Run As -> Run on Server_ gestartet werden. Danach ist der Gazetteer unter http://localhost:8080/gazetteer erreichbar.

#### IntelliJ
Das Projekt kann in IntelliJ über File -> New -> Project from Version Control -> Github importiert werden.

Einbinden von Java JDK:
Unter "File -> Project Structure... -> Plattform Settings -> SDK -> Add New SDK -> Java SDK"
muss das Verzeichnis der Java SDK Installation angegeben werden. Unter Mac ist dies z.B. "/Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home".

Einbinden von Tomcat:
Unter "Preferences -> Build, Execution, Deployment -> Application Servers -> Add Apllication Server -> Tomcat Server"
kann das Verzeichnis einer binären Distribution von Tomcat angegeben werden.  

### Konfiguration: MongoDB und Elasticsearch

MongoDB und Elasticsearch können mit Docker bzw. docker-compose automatisch erstellt werden.

Vorraussetzungen
* [Docker](https://www.docker.com/)
* [docker-compose](https://docs.docker.com/compose/)

Die Docker Images bauen mit:

```bash
docker-compose build
```

Um aus den gebauten Images dann Docker Container zu erstellen und zu starten:
```bash
docker-compose up
```

Um die Container zu stoppen:
CTRL + C oder
```bash
docker-compose stop
```

Wieder starten mit:
```bash
docker-compose start
```

Erstellte Container löschen (inklusive ElasticSearch Index und MongoDB Daten):
```bash
docker-compose down -v
```

Anschließend muss "127.0.0.1 gazetteer_mongo" noch als host auf dem System angelegt werden. Unter Linux/Mac OSX 
findet sich die Konfigurationsdatei unter `/etc/hosts`. Siehe auch: https://de.wikipedia.org/wiki/Hosts_(Datei).

### Konfiguration: Hauptanwendung (Java/Tomcat)

Vor dem Start muss sichergestellt werden, dass die nötigen Konfigurationsdateien vorhanden sind:

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

## Architektur

Backend (Java/SpringMVC) und Frontend (Javascript/AngularJS) werden im gleichen Projekt verwaltet. Der Quellcode des Backends liegt unter _src/main/java_, der des Frontends unter _src/main/webapp/resources/js_. Die AngularJS Views werden durch JSPs generiert und liegen unter _src/main/webapp/WEB-INF/views/html/partials_. Zusätzlich liegen unter _src/main/scripts_ einige in Ruby geschriebene Skripte für den Im- und Export für spezifische Projekte.

### i18n

Übersetzungen werden in den Dateien _src/main/resources/messages_<sprache>.properties_  und _src/main/webapps/resources/js/i18n/messages_<sprache>.js_ (Frontend) vorgenommen.

## Deployment

Das WAR-File kann mit folgendem Befehl erstellt werden:

`mvn clean package`

Das resultierende Web-Archiv kann dann in einem beliebigen Servlet-Container deployt werden.
