#!/usr/bin/env bash

if [ ! -f /data/db/.metadata/.replicaset ]; then
  mongod --fork --dbpath /data/db --port 27017 --logpath /var/log/mongod.log
  RET=1
  while [ $RET -ne 0 ]
  do
    echo "=> Waiting for confirmation of MongoDB service startup"
    sleep 5
    mongo gazetteer --eval "help" >/dev/null 2>&1
    RET=$?
  done

  echo "Restarting service..."
  echo "Shutdown."
  mongod --shutdown
  echo "Start"
  mongod --logpath /var/log/mongod.log --fork --replSet rs0 --dbpath /data/db

  echo "Initializing replica..."
  mongo gazetteer --eval 'rs.initiate({ _id : "rs0", members : [ { _id : 0, host : "gazetteer_mongo:27017" } ] });'

  mkdir -p /data/db/.metadata
  touch /data/db/.metadata/.replicaset

  echo "Importing dummy data..."
  mongoimport --db gazetteer --collection place --file /docker-entrypoint-initdb.d/data/test_places.jsonl
  mongoimport --db gazetteer --collection user --file /docker-entrypoint-initdb.d/data/test_users.jsonl

  echo "Restarting service..."
  echo "Shutdown."
  mongod --shutdown
  echo "Start"
  mongod --replSet rs0 --dbpath /data/db --bind_ip_all
else
  mongod --replSet rs0 --dbpath /data/db --bind_ip_all
fi