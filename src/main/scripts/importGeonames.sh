#! /bin/sh
curl -u admin:admin1 -XPOST "http://gazetteer.dainst.org/admin/importGeonames" > /var/log/gazetteer/importGeonames.log
