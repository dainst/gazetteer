package org.dainst.gazetteer.helpers;

import java.util.ArrayList;
import java.util.List;

import org.dainst.gazetteer.domain.Shape;
import org.dainst.gazetteer.domain.ValidationResult;

public class PolygonValidator {
	
	private class LatLng {
		double lat;
		double lng;

		LatLng(double lat, double lng) {
			this.lat = lat;
			this.lng = lng;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LatLng other = (LatLng) obj;
			if (lat != other.lat)
				return false;
			if (lng != other.lng)
				return false;
			return true;
		}
	}
	
	public ValidationResult validate(Shape shape) {
		
		for (int i = 0; i < shape.getCoordinates().length; i++) {			
			for (int j = 0; j < shape.getCoordinates()[i].length; j++) {				
				double[][] path = shape.getCoordinates()[i][j];
				ValidationResult ValidationResult = validateCoordinates(path);
				if (!ValidationResult.isSuccess()) return ValidationResult;
				
				ValidationResult = checkForIntersections(shape, path);
				if (!ValidationResult.isSuccess()) return ValidationResult;
			}
		}
		
		return new ValidationResult(true);
	}
	
	private ValidationResult validateCoordinates(double[][] path) {
		
		LatLng firstPoint = new LatLng(path[0][1], path[0][0]);
		LatLng lastPoint = new LatLng(path[path.length - 1][1], path[path.length - 1][0]);
		
		if (!firstPoint.equals(lastPoint)) {
			return new ValidationResult(false, "Path not closed: First point of path does not equal last point", "pathNotClosed", "");
		}

        List<LatLng> points = new ArrayList<LatLng>();

        for (int i = 0; i < path.length; i++) {
            LatLng point = new LatLng(path[i][1], path[i][0]);
            if (points.contains(point) && !(i == path.length - 1 && point.equals(firstPoint))) {
            	return new ValidationResult(false, "Duplicate point (" + point.lng + ", " + point.lat + ")", "duplicatePoint",
            			"Lng: " + point.lng + ", Lat: " + point.lat);
            } else {
            	points.add(point);
            }
        }
		
		return new ValidationResult(true);
	}
	
	private ValidationResult checkForIntersections(Shape shape, double[][] path1) {
		
		for (int i = 0; i < shape.getCoordinates().length; i++) {			
			for (int j = 0; j < shape.getCoordinates()[i].length; j++) {

				double[][] path2 = null;
		
				if (j == shape.getCoordinates()[i].length - 1)
					path2 = shape.getCoordinates()[i][0];
				else
					path2 = shape.getCoordinates()[i][j + 1];
		
				ValidationResult ValidationResult = checkForPathIntersection(path1, path2);
				if (!ValidationResult.isSuccess()) return ValidationResult;
			}
		}
		
		return new ValidationResult(true);
	}
	
	private ValidationResult checkForPathIntersection(double[][] path1, double[][] path2) {
		
		for (int i = 0; i < path1.length; i++) {
			LatLng path1Point1 = new LatLng(path1[i][1], path1[i][0]);
			LatLng path1Point2 = null;
			
			if (i + 1 < path1.length)
				path1Point2 = new LatLng(path1[i + 1][1], path1[i + 1][0]);
			else
				path1Point2 = new LatLng(path1[0][1], path1[0][0]);

			for (int j = 0; j < path2.length; j++) {
				LatLng path2Point1 = new LatLng(path2[j][1], path2[j][0]);
				LatLng path2Point2 = null;
				
				if (j + 1 < path2.length)
					path2Point2 = new LatLng(path2[j + 1][1], path2[j + 1][0]);
				else
					path2Point2 = new LatLng(path2[0][1], path2[0][0]);

				if (!path1Point1.equals(path2Point1) && !path1Point2.equals(path2Point2) &&
						!path1Point1.equals(path2Point2) && !path1Point2.equals(path2Point1) &&
						checkForLineIntersection(path1Point1, path1Point2, path2Point1, path2Point2)) {
					return new ValidationResult(false, "Lines (" + path1Point1.lng + ", " + path1Point1.lat + " / " +
							path1Point2.lng + ", " + path1Point2.lat + ") and (" +
							path2Point1.lng + ", " + path2Point1.lat + " / " +
							path2Point2.lng + ", " + path2Point2.lat + " are intersecting!", "lineIntersection",
							"(Lng: " + path1Point1.lng + ", " + "Lat: " + path1Point1.lat + " / " +
							"Lng: " + path1Point2.lng + ", " + "Lat: " + path1Point2.lat + "), (" +
							"Lng: " + path2Point1.lng + ", " + "Lat: " + path2Point1.lat + " / " +
							"Lng: " + path2Point2.lng + ", " + "Lat: " + path2Point2.lat + ")");
				}
			}
		}

		return new ValidationResult(true);
	}

	private boolean checkForLineIntersection(LatLng latlng1, LatLng latlng2, LatLng latlng3, LatLng latlng4) {
		
		double a1 = latlng2.lat - latlng1.lat;
		double b1 = latlng1.lng - latlng2.lng;
		double c1 = a1 * latlng1.lng + b1 * latlng1.lat;

		double a2 = latlng4.lat - latlng3.lat;
		double b2 = latlng3.lng - latlng4.lng;
		double c2 = a2 * latlng3.lng + b2 * latlng3.lat;

		double determinate = a1 * b2 - a2 * b1;

		if (determinate != 0) {
			double x = (b2 * c1 - b1 * c2) / determinate;
			double y = (a1 * c2 - a2 * c1) / determinate;

			LatLng intersect = new LatLng(y, x);

			if (isInBoundedBox(latlng1, latlng2, intersect) && isInBoundedBox(latlng3, latlng4, intersect))
				return true;
		}

		return false;
	}

	private boolean isInBoundedBox (LatLng latlng1, LatLng latlng2, LatLng latlng3) {
		
		boolean betweenLats;
		boolean betweenLngs;

		if (latlng1.lat < latlng2.lat)
			betweenLats = (latlng1.lat <= latlng3.lat && latlng2.lat >= latlng3.lat);
		else
			betweenLats = (latlng1.lat >= latlng3.lat && latlng2.lat <= latlng3.lat);

		if (latlng1.lng < latlng2.lng)
			betweenLngs = (latlng1.lng <= latlng3.lng && latlng2.lng >= latlng3.lng);
		else
			betweenLngs = (latlng1.lng >= latlng3.lng && latlng2.lng <= latlng3.lng);

		return (betweenLats && betweenLngs);
	}
}
