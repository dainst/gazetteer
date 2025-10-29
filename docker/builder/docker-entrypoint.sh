#! /bin/bash
mkdir -p /src/.tomcat/
rm -r /src/.tomcat/*

cp src/main/resources/log4j2.xml.template src/main/resources/log4j2.xml
cp src/main/webapp/WEB-INF/web.xml.template src/main/webapp/WEB-INF/web.xml
cp src/main/webapp/WEB-INF/config.properties.template src/main/webapp/WEB-INF/config.properties
cp src/main/webapp/WEB-INF/mail.properties.template src/main/webapp/WEB-INF/mail.properties
sed -i 's|/var/log/gazetteer/gazetteer.log|logs/gazetteer.log|g' src/main/resources/log4j2.xml

mvn clean package -Dmaven.test.skip=true
cp /src/target/gazetteer.war /src/.tomcat/ROOT.war

inotifywait -r -m -e modify /src/src | while read file_path file_event file_name; do
    echo ${file_path}${file_name} event: ${file_event}
    echo "Skipping $(timeout 3 cat | wc -l) further changes"
    mvn clean package -Dmaven.test.skip=true
    cp /src/target/gazetteer.war /src/.tomcat/ROOT.war
done
