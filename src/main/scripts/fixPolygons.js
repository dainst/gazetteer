connection = new Mongo();
db = connection.getDB("gazetteer");

db.place.find( { "prefLocation.shape" : { $exists : true } } ).forEach(
	function(doc) {
		if (doc.prefLocation.shape.coordinates && doc.prefLocation.shape.coordinates.length > 0) {
			var coordinates = doc.prefLocation.shape.coordinates;
			
			// Polygone durchlaufen
			for (var i = 0; i < coordinates.length; i++) {
				
				// Pfade durchlaufen
				for (var j = 0; j < coordinates[i].length; j++) {
					var path = coordinates[i][j];
					var firstPoint = path[0];
					var lastPoint = path[path.length - 1];

					if (firstPoint[0] != lastPoint[0] || firstPoint[1] != lastPoint[1])
						path.push(firstPoint);
				}
			}
		}

		db.place.save(doc);
	});