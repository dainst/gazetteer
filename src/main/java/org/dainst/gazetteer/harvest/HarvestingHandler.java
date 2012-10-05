package org.dainst.gazetteer.harvest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dainst.gazetteer.dao.HarvesterDefinitionDao;
import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.dao.ThesaurusDao;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.Thesaurus;
import org.dainst.gazetteer.helpers.EntityIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestingHandler implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(HarvestingHandler.class);
	
	private HarvesterDefinition harvesterDefinition;
	
	private PlaceDao placeDao;

	private ThesaurusDao thesaurusDao;

	private HarvesterDefinitionDao harvesterDefinitionDao;

	private EntityIdentifier entityIdentifier;
	
	public HarvestingHandler(HarvesterDefinition harvesterDefinition,
			PlaceDao placeDao, ThesaurusDao thesaurusDao, 
			HarvesterDefinitionDao harvesterDefinitionDao,
			EntityIdentifier entityIdentifier) {
		
		this.harvesterDefinition = harvesterDefinition;
		this.placeDao = placeDao;	
		this.thesaurusDao = thesaurusDao;
		this.harvesterDefinitionDao = harvesterDefinitionDao;
		this.entityIdentifier = entityIdentifier;
		
	}

	@Override
	public void run() {
		
		try {
			
			// prevent multiple instances of the same harvester from being executed simultaneously
			if (harvesterDefinition.isRunning()) {
				logger.info("An instance of {} is still running. Skipping execution ...",
						harvesterDefinition.getHarvesterType().getSimpleName());
				return;
			}
			
			if (logger.isInfoEnabled()) {
				String formattedDate = "never";
				if (harvesterDefinition.getLastHarvestedDate() != null)
					formattedDate = new SimpleDateFormat().format(harvesterDefinition.getLastHarvestedDate());
				logger.info("Running {}. Last time run: {}", 
						harvesterDefinition.getHarvesterType().getSimpleName(), formattedDate);
			}
			
			harvesterDefinition.setRunning(true);
			harvesterDefinitionDao.save(harvesterDefinition);
			
			Thesaurus thesaurus = thesaurusDao.getThesaurusByKey(harvesterDefinition.getTargetThesaurus());
			if (thesaurus == null) {
				throw new IllegalStateException("Target thesaurus not found in database");
			}
			
			Harvester harvester = harvesterDefinition.getHarvesterType().newInstance();
			harvester.harvest(harvesterDefinition.getLastHarvestedDate());			
			
			while (true) {
				
				Place place = harvester.getNextPlace();
				if (place == null) break;
				
				Place identifiedPlace = entityIdentifier.identify(place);
				if (identifiedPlace != null) {
					// TODO merge places
					place = identifiedPlace;
					logger.info("identified place: {}", place.getId());
				}
				
				place.setThesaurus(thesaurus);
				placeDao.save(place);
				logger.info("saved place: {}", place.getId());
				
			}
			
			harvester.close();
			
			harvesterDefinition.setLastHarvestedDate(new Date());
			harvesterDefinition.setRunning(false);
			harvesterDefinitionDao.save(harvesterDefinition);
			
		} catch (Exception e) {
			harvesterDefinition.setRunning(false);
			harvesterDefinitionDao.save(harvesterDefinition);
			throw new RuntimeException("error while creating harvester", e);
		}
		
	}

}
