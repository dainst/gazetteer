#!/bin/bash
echo "Pushing place template to ElasticSearch..."
curl http://$ELASTICSEARCH_CONTAINER_NAME:9200/_template/gazetteer_template -X PUT -H "Content-Type: application/json" -d "@/mappings/place_template.json"

echo "Create the index..."
curl -X PUT "http://$ELASTICSEARCH_CONTAINER_NAME:9200/gazetteer"

echo "Reindexing…"
curl -XPOST admin:password@$TOMCAT_CONTAINER_NAME:8080/admin/reindex