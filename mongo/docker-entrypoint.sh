#!/usr/bin/env bash

if [ ! -f /data/db/.metadata/.replicaset ]; then
  mongod --fork --dbpath /data/db --port 27017 --logpath /var/log/mongod.log
  RET=1
  while [ $RET -ne 0 ]
  do
    echo "=> Waiting for confirmation of MongoDB service startup"
    sleep 5
    mongo admin --eval "help" >/dev/null 2>&1
    RET=$?
  done
  mongo admin --eval "db.createUser({user:'$MONGO_INITDB_ROOT_USERNAME',pwd:'$MONGO_INITDB_ROOT_PASSWORD',roles:[{role:'root',db:'admin'}]})"
  echo "Restarting service..."
  mongod --shutdown && mongod --logpath /var/log/mongod.log --fork --replSet rs0 --dbpath /data/db

  echo "Initializing replica..."
  mongo --authenticationDatabase admin -u $MONGO_INITDB_ROOT_USERNAME -p $MONGO_INITDB_ROOT_PASSWORD --eval 'rs.initiate({ _id : "rs0", members : [ { _id : 0, host : "gazetteer_mongo:27017" } ] });'

  mkdir -p /data/db/.metadata
  touch /data/db/.metadata/.replicaset

  echo "Importing dummy data..."
  mongoimport --db $MONGO_INITDB_DATABASE --collection place --file /docker-entrypoint-initdb.d/data/test_places.jsonl
  mongoimport --db $MONGO_INITDB_DATABASE --collection user --file /docker-entrypoint-initdb.d/data/test_users.jsonl

  mongod --shutdown && mongod --replSet rs0 --dbpath /data/db --bind_ip_all
else
  mongod --replSet rs0 --dbpath /data/db --bind_ip_all
fi