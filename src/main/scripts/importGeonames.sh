#! /bin/sh
curl -u admin:admin1 -XPOST "https://gazetteer.dainst.org/admin/importGeonames" > /var/log/gazetteer/importGeonames.log
