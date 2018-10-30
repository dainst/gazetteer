FROM elasticsearch:1

RUN ./bin/plugin install elasticsearch/elasticsearch-analysis-icu/2.0.0

COPY elasticsearch.yml /etc/elasticsearch/elasticsearch.yml

ENTRYPOINT  ./bin/elasticsearch -Des.config=/etc/elasticsearch/elasticsearch.yml