<br />
<div align="center">
  <h1 align="center"><strong>iDAI.gazetteer</strong></h1>
  <p align="center">https://gazetteer.dainst.org/</p>
</div>
<br/>

Gazetteer ist eine [Spring Boot](https://spring.io/projects/spring-boot)-Anwendung. In diesem Repostiory befinden sich sowohl
Frontend als auch Backend-Code. Das Frontend wird vom Backend geservt. Es werden lokale Instanzen von MongoDB und Elasticsearch benötigt.

## Inhaltsverzeichnis

<ol>
  <li>
    <a href="#Setup">Setup</a>
    <ul>
      <li><a href="#Anforderungen">Anforderungen</a></li>
      <li><a href="#Konfiguration">Konfiguration</a></li>
      <li><a href="#Entwicklungsumgebung">Entwicklungsumgebung</a></li>
      <li><a href="#Testdaten">Testdaten</a></li>
      <li><a href="#Indizierung">Indizierung</a></li>
    </ul>
  </li>
  <li>
    <a href="#Tests">Tests</a>
    <ul>
      <li><a href="#Unit-Tests">Unit Tests</a></li>
      <li><a href="#API-Tests">API Tests</a></li>
    </ul>
  </li>
  <li>
    <a href="#Struktur">Struktur</a>
    <ul>
      <li><a href="#i18n">i18n</a></li>
    </ul>
  </li>
  <li>
    <a href="#Deployment">Deployment</a>
  </li>
</ol>

## Setup

### Anforderungen

- Ein JDK der Version 21 oder höher (zum Beispiel [Oracle OpenJDK](https://jdk.java.net/))
- Für die Entwicklung, eine Implementierung von [Compose](https://compose-spec.io/) (z.B. [Docker Compose](https://docs.docker.com/compose/install/))

### Konfiguration

Gazetteer enthält Defaults für Konfigurationswerte in der `src/main/resources/application.properties`. Bei Bedarf können diese als Umgebungsvariablen überschrieben werden.

Die Benennung der Umgebungsvariblen sind durch das [Konfigurationsframework von Spring](https://docs.spring.io/spring-boot/reference/features/external-config.html) aus den Property-Namen generiert, siehe [Relaxed Binding](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding).

Für den Prodokutionsbetrieb müssen in der Regel diese Variablen gesetzt werden:

- `SMTPHOST`
- `SMTPPORT`
- `SENDERMAIL`
- `MAILUSERNAME`
- `MAILPASSWORD`
- `NOTIFICATIONMAIL`
- `CLUSTER_NAME`
- `BASEURI`
- `GEONAMESSOLRURI`
- `TEMPDIRECTORYPATH`
- `MONGOREPLICASET`

In der Docker-Entwicklungsumgebung müssen keine Umgebungsvariablen gesetzt werden.

### Entwicklungsumgebung

Es existiert eine `compose.yml` um eine Entwicklungsumgebung zu starten.

`docker compose up --build` startet:

- mongodb: Eine MongoDB mit Testdaten
- elasticsearch: ElasticSearch
- gazetteer: iDAI.gazetteer
- elasticsearch-init: Pusht die ElasticSearch Index Template, erstellt den Index und veranlasst eine Reindizierung.

### Testdaten

Beim ersten Start des mongodb-Containers werden diese Testdaten importiert:

- [Algerien](http://localhost:8080/app/#!/show/2042612)
- [Constantine](http://localhost:8080/app/#!/show/2042624)
- [Hippo Regius](http://localhost:8080/app/#!/show/2042614)

Siehe `docker/mongo/setup.sh`, falls die Daten manuell importiert werden müssen.

### Indizierung

Eine Elasticsearch-Reindizierung kann bei laufendem Gazetteer über die Admin-API angestoßen werden:

```bash
curl -XPOST admin:password@localhost:8080/admin/reindex
```

<p align="right">(<a href="#Inhaltsverzeichnis">Zum Inhaltsverzeichnis</a>)</p>

## Tests

### Unit Tests

```bash
./mvnw test
```

### API Tests

API Tests können erweitert werden, indem mit [Bruno](https://docs.usebruno.com/) `gazetteer_api_test` als Workspace geöffnet wird.

Um die Tests nur auszuführen, kann die [Bruno CLI installiert](https://docs.usebruno.com/bru-cli/installation) werden.

```bash
docker compose up
cd gazetteer_api_test
bru run
```

<p align="right">(<a href="#Inhaltsverzeichnis">Zum Inhaltsverzeichnis</a>)</p>

## Struktur

Backend (Spring Boot) und Frontend (Javascript/AngularJS) werden im gleichen Projekt verwaltet. Der Quellcode des Backends liegt unter _src/main/java_, der des Frontends unter _src/main/webapp/resources/js_. Die AngularJS Views werden durch JSPs generiert und liegen unter _src/main/webapp/WEB-INF/views/html/partials_. Zusätzlich liegen unter _src/main/scripts_ einige in Ruby geschriebene Skripte für den Im- und Export für spezifische Projekte.

### i18n

UI-Texte werden in zwei Dateisets eingepflegt.

- `src/main/resources/messages_<sprache>.properties`
- `src/main/webapps/resources/js/i18n/messages*<sprache>.js`

<p align="right">(<a href="#Inhaltsverzeichnis">Zum Inhaltsverzeichnis</a>)</p>

## Deployment

The gazetteer uses [Maven Wrapper](https://maven.apache.org/tools/wrapper/) to build the docker images.

To build a new image run locally:

```shell
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=gazetteer:latest
```

Alternatively, you may want to tag a new release version:
```shell
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=gazetteer:<MAJOR>.<MINOR>.<PATCH>
```

Finally you have to push the new or updated image to the registry:
```
docker push ghcr.io/dainst/gazetteer:<version>
```

In order to push images, you have to authenticate your local machine with the registry, see: [Github Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry).


<p align="right">(<a href="#Inhaltsverzeichnis">Zum Inhaltsverzeichnis</a>)</p>
