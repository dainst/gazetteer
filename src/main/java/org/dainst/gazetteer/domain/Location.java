package org.dainst.gazetteer.domain;


public class Location {

	private double[] coordinates;

	private double[][] polygon;
	
	private int confidence = 0;
	
	public Location() {
		
	}
	
	public Location(double lng, double lat) {
		coordinates = new double[]{ lng, lat };
	}

	public double[] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(double[] point) {
		this.coordinates = point;
	}

	public double[][] getPolygon() {
		return polygon;
	}

	public void setPolygon(double[][] polygon) {
		this.polygon = polygon;
	}
	
	public double getLat() {
		if (coordinates.length >= 2)
			return coordinates[1];
		else
			return 0;
	}
	
	public double getLng() {
		if (coordinates.length >= 1)
			return coordinates[0];
		else
			return 0;
	}

	public int getConfidence() {
		return confidence;
	}

	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}

}
