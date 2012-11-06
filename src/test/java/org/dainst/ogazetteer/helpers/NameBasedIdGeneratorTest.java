package org.dainst.ogazetteer.helpers;

import static org.junit.Assert.*;

import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.helpers.NameBasedIdGenerator;
import org.junit.Test;

public class NameBasedIdGeneratorTest {

	@Test
	public void testIfNameSet() {
		
		NameBasedIdGenerator generator = new NameBasedIdGenerator();
		generator.setLength(8);
		
		Place place = new Place();
		place.addName(new PlaceName("Köln-Deutz"));		
		String id = generator.generate(place);
		System.out.println(id);		
		assertEquals("koln-deu", id);
		
		Place place2 = new Place();
		place2.addName(new PlaceName("Arbeitsstelle für Digitale Archäologie"));		
		id = generator.generate(place2);
		System.out.println(id);		
		assertEquals("arbeitss", id);
		
	}
	
	@Test
	public void testIfNameNotSet() {
		
		NameBasedIdGenerator generator = new NameBasedIdGenerator();
		generator.setLength(8);
		
		Place place = new Place();
		String id = generator.generate(place);
		System.out.println(id);		
		
	}
	
}
