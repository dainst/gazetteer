package org.dainst.gazetteer.domain;

import java.util.Arrays;

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

	public String toWKT() {
		String wkt = "Polygon((" + Double.toString(coordinates[0][0][0][0]) + " " + Double.toString(coordinates[0][0][0][1]);
		for (int i = 1; i < coordinates[0][0].length; i++) {
			wkt = wkt + ", " + Double.toString(coordinates[0][0][i][0]) + " " + Double.toString(coordinates[0][0][i][1]);
		}
		wkt = wkt + "))";
		return wkt;
	}

	public void setCoordinates(double[][][][] coordinates) {
		this.coordinates = coordinates;
	}
	
	@Override
	public int hashCode() {
		if (coordinates != null)
			return Arrays.hashCode(coordinates);
		else
			return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shape other = (Shape) obj;
		if (!Arrays.equals(coordinates, other.coordinates))
			return false;
		return true;
	}
}
