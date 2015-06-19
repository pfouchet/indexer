mongoexport --db datastore --collection recipe --query {} --fields _id,fid | sed 's/{ "_id" : ".*", "fid" : //g' | sed 's/ { "$ref" : "sourceSystem", "$id" ://g' | sed 's/} } }/}/g' > /tmp/ids.json
