#! /bin/sh
curl -u admin:admin1 -XPOST "http://gazetteer.dainst.org/admin/generateLinks" > /var/log/gazetteer/generateLinks.log
