package org.dainst.gazetteer.domain;

public class Shape {
	
	private String type = "multipolygon";
	
	private double[][][][] coordinates;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double[][][][] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(double[][][][] coordinates) {
		this.coordinates = coordinates;
	}

}
