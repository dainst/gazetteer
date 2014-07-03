package org.dainst.gazetteer.domain;

import java.util.Arrays;


public class Location {

	private double[] coordinates;
	
	// private Shape shape;
	
	private int confidence = 0;

	private boolean publicSite = false;
	
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

	public boolean isPublicSite() {
		return publicSite;
	}

	public void setPublicSite(boolean publicSite) {
		this.publicSite = publicSite;
	}

	@Override
	public String toString() {
		return "Location [coordinates=" + Arrays.toString(coordinates)
				//+ ", shape=" + shape.toString()
				+ ", confidence=" + confidence + ", publicSite=" + publicSite + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + confidence;
		result = prime * result + (publicSite ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(coordinates);
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
		if (publicSite != other.publicSite)
			return false;
		if (!Arrays.equals(coordinates, other.coordinates))
			return false;
		return true;
	}

	//public Shape getShape() {
	//	return shape;
	//}

	//public void setShape(Shape shape) {
	//	this.shape = shape;
	//}

}
