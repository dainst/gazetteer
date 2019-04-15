#!/bin/bash

until $(curl -o /dev/null -s --head --fail $ELASTICSEARCH_CONTAINER_NAME:9200); do
    echo "Waiting for ES to start..."
    sleep 5
done

echo "Pushing place template to ElasticSearch..."
curl http://$ELASTICSEARCH_CONTAINER_NAME:9200/places/ -X PUT -H "Content-Type: application/json" -d "@/mappings/place_template.json"