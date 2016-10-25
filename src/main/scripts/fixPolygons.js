connection = new Mongo();
db = connection.getDB("gazetteer");

db.place.find( { "prefLocation.shape" : { $exists : true } } ).forEach(
        function(doc) {

                var invalidPolygon = false;

                if (doc.prefLocation.shape.coordinates && doc.prefLocation.shape.coordinates.length > 0) {
                        var coordinates = doc.prefLocation.shape.coordinates;

                        // Iterate polygons
                        for (var i = 0; i < coordinates.length; i++) {

                                // Iterate paths
                                for (var j = 0; j < coordinates[i].length; j++) {
                                        var path = coordinates[i][j];
                                        var firstPoint = path[0];
                                        var lastPoint = path[path.length - 1];

                                        if (firstPoint[0] != lastPoint[0] || firstPoint[1] != lastPoint[1]) {
                                                path.push(firstPoint);
                                                print("First point does not equal last point");
                                                invalidPolygon = true;
                                        }

                                        // Iterate points
                                        var points = {};
                                        var pointsToRemove = [];

                                        for (var k = 0; k < path.length; k++) {
                                                var point = path[k];
                                                if (points[point[0]] && points[point[0]] == point[1] && !(k == path.length - 1 && point[0] == firstPoint[0] && point[1] == firstPoint[1])) {
                                                        print("Duplicate point " + point[0] + "," + point[1]);
                                                        invalidPolygon = true;
                                                        pointsToRemove.push(point);
                                                } else {
                                                        points[point[0]] = point[1];
                                                }
                                        }

                                        for (var k = 0; k < pointsToRemove.length; k++) {
                                                var index = path.indexOf(pointsToRemove[k]);
                                                path.splice(index, 1);
                                        }
                                }
                        }
                }

                db.place.save(doc);

                if (invalidPolygon) {
                        print("Fixed invalid polygon of place " + doc._id + "\n");
                }
        });
