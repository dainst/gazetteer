package org.dainst.gazetteer.harvest;

import java.util.Date;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.helpers.IdGenerator;

public interface Harvester {
	
	public void setIdGenerator(IdGenerator idGenerator);
	
	public void setPlaceRepository(PlaceRepository placeRepository);
	
	public void harvest(Date date);
	
	public void close();

}
