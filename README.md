# iDAI.gazetteer

In dem Projekt befinden sich sowohl Frontend als auch Backend-Code. Als IDE empfiehlt sich STS. Außerdem werden lokale Instanzen von MongoDB und Elasticsearch benötigt.

## Setup

### Test VM

Mongodb und Elasticsearch können mit Hilfe von [Test Kitchen](http://kitchen.ci/) in einer lokalen VM angelegt werden.

Folgende Tools müssen dafür installiert sein:
* [VirtualBox](https://www.virtualbox.org/)
* [Vagrant](http://www.vagrantup.com/)
* Bundler, installieren mit: `gem install bundler`

Gazetteer-Repository klonen, anschließend im Wurzelordner VM erstellen und starten:
```bash
bundle install
kitchen create <sup>1</sup>
kitchen converge
```

Login auf dem virtuellen Server mit `kitchen login`.

Auf dem Server ein Replica Set für die Synchronisierung von MongoDB und Elasticsearch in der MongoDB anlegen:
```bash
mongo gazetteer --eval 'rs.initiate({ _id : "rs0", members : [ { _id : 0, host : "localhost:27017" } ] })'
```

Auf dem Server ggf. Testdaten laden:
```bash
mongoimport --db gazetteer --collection place --file synced_folders/src/test/resources/test_places.jsonl
mongoimport --db gazetteer --collection place --file synced_folders/src/test/resources/test_users.jsonl
```

### Konfiguration

Vor dem Start muss sichergestellt werden, dass die nötigen Konfigurationsdateien vorhanden sind:

* src/main/resources/log4j.xml
* src/main/webapp/WEB-INF/web.xml
* src/main/webapp/WEB-INF/config.properties
* src/main/webapp/WEB-INF/elasticsearch.properties
* src/main/webapp/WEB-INF/mail.properties (optional, bei fehlender Mail-Konfiguration werden keine Mails versandt)

In den entsprechenden Ordnern befinden sich Template-Files, die eine Grundkonfiguration bereitstellen. Vor dem Start sollten die Templates umbenannt bzw. kopiert und evtl. angepasst werden.

### STS

Das Projekt kann über _File -> Import -> Maven -> Existing Maven Project_ in STS importiert werden und kann nach Auflösen der Dependencies mit _Run As -> Run on Server_ gestartet werden. Danach ist der Gazetteer unter http://localhost:8080/gazetteer erreichbar.

Anschließend kann die erste Indizierung (und Einrichtung des MongoDB-Rivers) angestoßen werden:
```bash
curl -XPOST http://admin:adminpassword@localhost:8080/gazetteer/admin/reindex
```


## Architektur

Backend (Java/SpringMVC) und Frontend (Javascript/AngularJS) werden im gleichen Projekt verwaltet. Der Quellcode des Backends liegt unter _src/main/java_, der des Frontends unter _src/main/webapp/resources/js_. Die AngularJS Views werden durch JSPs generiert und liegen unter _src/main/webapp/WEB-INF/views/html/partials_. Zusätzlich liegen unter _src/main/scripts_ einige in Ruby geschriebene Skripte für den Im- und Export für spezifische Projekte.

### i18n

Übersetzungen werden in den Dateien _src/main/resources/messages_<sprache>.properties_  und _src/main/webapps/resources/js/i18n/messages_<sprache>.js_ (Frontend) vorgenommen.

## Deployment

Das WAR-File kann mit folgendem Befehl erstellt werden:

`mvn clean package`

Das resultierende Web-Archiv kann dann in einem beliebigen Servlet-Container deployt werden.

## Troubleshooting

##### <sup>1</sup> `kitchen create` Error

Fehlermeldung:

```
>>>>>> ------Exception-------
>>>>>> Class: Kitchen::ActionFailed
>>>>>> Message: 1 actions failed.
>>>>>>     Failed to complete #create action: [Expected process to exit with [0], but received '1'
---- Begin output of vagrant up --no-provision --provider virtualbox ----
STDOUT: Bringing machine 'default' up with 'virtualbox' provider...
==> default: Box 'bento/ubuntu-14.04' could not be found. Attempting to find and install...
    default: Box Provider: virtualbox
    default: Box Version: >= 0
STDERR: The box 'bento/ubuntu-14.04' could not be found or
could not be accessed in the remote catalog. If this is a private
box on HashiCorp's Atlas, please verify you're logged in via
'vagrant login'. Also, please double-check the name. The expanded
URL and error message are shown below:

URL: ["https://atlas.hashicorp.com/bento/ubuntu-14.04"]
Error:
---- End output of vagrant up --no-provision --provider virtualbox ----
Ran vagrant up --no-provision --provider virtualbox returned 1] on default-ubuntu-1404
>>>>>> ----------------------
>>>>>> Please see .kitchen/logs/kitchen.log for more details
>>>>>> Also try running 'kitchen diagnose --all' for configuration
```

Lösung: Die von Vagrant mitgelieferte curl Version löschen (bzw. umbenennen): `sudo mv /opt/vagrant/embedded/bin/curl /opt/vagrant/embedded/bin/curl_backup`
