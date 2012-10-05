package org.dainst.gazetteer.harvest;

import java.util.Date;

import org.dainst.gazetteer.domain.Place;

public interface Harvester {
	
	public void harvest(Date date);
	
	public Place getNextPlace();
	
	public void close();

}
