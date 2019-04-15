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
		StringBuffer sb = new StringBuffer();


		// See https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry#Geometric_objects
		sb.append("MULTIPOLYGON(");

		boolean firstGroup = true;

		for(int i = 0; i < coordinates.length; i++){
			if(!firstGroup)
				sb.append(", ");

			sb.append("(");

			boolean firstSingle = true;
			for(int j = 0; j < coordinates[i].length; j++){
				if(!firstSingle)
					sb.append(", ");
				sb.append("(");

				boolean firstVertex = true;
				for(int k = 0; k < coordinates[i][j].length; k++){
					if(!firstVertex)
						sb.append(", ");

					String vertex = coordinates[i][j][k][0] + " " + coordinates[i][j][k][1];
					sb.append(vertex);
					firstVertex = false;
				}

				sb.append(")");
				firstSingle = false;
			}

			sb.append(")");
			firstGroup = false;

		}

		sb.append(")");

		return sb.toString();
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
