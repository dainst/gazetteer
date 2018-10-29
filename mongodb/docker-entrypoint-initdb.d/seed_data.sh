#!/usr/bin/env bash
mongoimport --db gazetteer --collection place --file /docker-entrypoint-initdb.d/data/test_places.jsonl
mongoimport --db gazetteer --collection user --file /docker-entrypoint-initdb.d/data/test_users.jsonl
