package org.dainst.gazetteer.harvest;

import java.util.Date;
import java.util.Map;

import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.helpers.IdGenerator;

public interface Harvester {
	
	public void setIdGenerator(IdGenerator idGenerator);
	
	public void harvest(Date date);
	
	public Map<String,Place> getNextPlaces();
	
	public void close();

}
