package org.dainst.gazetteer.domain;

import java.util.Arrays;


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

	@Override
	public String toString() {
		return "Location [coordinates=" + Arrays.toString(coordinates)
				+ ", polygon=" + Arrays.toString(polygon) + ", confidence="
				+ confidence + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + confidence;
		result = prime * result + Arrays.hashCode(coordinates);
		result = prime * result + Arrays.hashCode(polygon);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (confidence != other.confidence)
			return false;
		if (!Arrays.equals(coordinates, other.coordinates))
			return false;
		if (!Arrays.deepEquals(polygon, other.polygon))
			return false;
		return true;
	}

}
