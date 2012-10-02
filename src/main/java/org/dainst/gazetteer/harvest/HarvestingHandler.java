package org.dainst.gazetteer.harvest;

import java.util.Date;

import org.dainst.gazetteer.dao.HarvesterDefinitionDao;
import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.dao.ThesaurusDao;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.Thesaurus;

public class HarvestingHandler implements Runnable {
	
	private HarvesterDefinition harvesterDefinition;
	
	private PlaceDao placeDao;

	private ThesaurusDao thesaurusDao;

	private HarvesterDefinitionDao harvesterDefinitionDao;
	
	public HarvestingHandler(HarvesterDefinition harvesterDefinition,
			PlaceDao placeDao, ThesaurusDao thesaurusDao, 
			HarvesterDefinitionDao harvesterDefinitionDao) {		
		this.harvesterDefinition = harvesterDefinition;
		this.placeDao = placeDao;	
		this.thesaurusDao = thesaurusDao;
		this.harvesterDefinitionDao = harvesterDefinitionDao;
	}

	@Override
	public void run() {
		
		try {
			
			harvesterDefinition.setLastHarvestedDate(new Date());
			harvesterDefinitionDao.save(harvesterDefinition);
			
			Thesaurus thesaurus = thesaurusDao.getThesaurusByKey(harvesterDefinition.getTargetThesaurus());
			
			Harvester harvester = harvesterDefinition.getHarvesterType().newInstance();
			harvester.harvest(harvesterDefinition.getLastHarvestedDate());
			
			while (true) {
				Place place = harvester.getNextPlace();
				if (place == null) break;
				place.setThesaurus(thesaurus);
				placeDao.save(place);
			}
			
		} catch (Exception e) {
			throw new RuntimeException("error while creating harvester", e);
		}
		
	}

}
