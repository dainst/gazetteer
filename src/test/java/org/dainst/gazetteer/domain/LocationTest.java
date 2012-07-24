package org.dainst.gazetteer.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

public class LocationTest {
	
	@Test
	public void testLongLat() {
		
		Location location = new Location(1.0, 2.0);
		
		assertEquals(1.0, location.getLat(), 0.1);
		assertEquals(2.0, location.getLng(), 0.1);
		
		double[] coordinates = location.getCoordinates();
		assertEquals(1.0, coordinates[0], 0.1);
		assertEquals(2.0, coordinates[1], 0.1);
		
		Point point = location.getPoint();
		assertEquals(1.0, point.getCoordinateSequence().getY(0), 0.1);
		assertEquals(2.0, point.getCoordinateSequence().getX(0), 0.1);
		
	}

}
