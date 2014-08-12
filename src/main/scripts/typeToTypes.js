connection = new Mongo();
db = connection.getDB("gazetteer");

db.place.find( { "type" : { $exists : true } } ).forEach(
	function(doc) {
		if (doc.types)
			doc.types.push(doc.type);
		else
			doc.types = [ doc.type ];

		delete doc.type;

		db.place.save(doc);
	});