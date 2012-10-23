package org.dainst.gazetteer.domain;

import org.springframework.data.mongodb.core.geo.Point;
import org.springframework.data.mongodb.core.geo.Polygon;

public class Location {

	private Point point;

	private Polygon polygon;
	
	private int confidence = 0;
	
	public Location() {
		
	}
	
	public Location(double lat, double lng) {
		point = new Point(lng, lat);
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}
	
	public double[] getCoordinates() {
		return point.asArray();
	}
	
	public void setCoordinates(double[] coordinates) {
		point = new Point(coordinates[1], coordinates[0]);
	}
	
	public double getLat() {
		return point.getY();
	}
	
	public double getLng() {
		return point.getX();
	}

	public int getConfidence() {
		return confidence;
	}

	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}

}
