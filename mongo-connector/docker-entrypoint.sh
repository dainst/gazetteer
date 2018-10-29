#!/bin/sh

mongo-connector -m $MONGO_CONTAINER_NAME:27017 \
                -t $ELASTICSEARCH_CONTAINER_NAME:9200 \
                -d elastic2_doc_manager \
                -a $MONGO_USER \
                -p $MONGO_PASSWORD

cat /mongo-connector.log