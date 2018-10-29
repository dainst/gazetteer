#!/bin/sh

mongo-connector -m gazetteer_mongodb:27017 \
                -t gazetteer_elasticsearch:9200 \
                -d elastic2_doc_manager \
                -a $MONGO_USER \
                -p $MONGO_PASSWORD

cat /mongo-connector.log