package org.dainst.gazetteer.harvest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dainst.gazetteer.dao.HarvesterDefinitionRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.ThesaurusRepository;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.Thesaurus;
import org.dainst.gazetteer.helpers.EntityIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestingHandler implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(HarvestingHandler.class);
	
	private HarvesterDefinition harvesterDefinition;
	
	private PlaceRepository placeDao;

	private ThesaurusRepository thesaurusDao;

	private HarvesterDefinitionRepository harvesterDefinitionDao;

	private EntityIdentifier entityIdentifier;
	
	public HarvestingHandler(HarvesterDefinition harvesterDefinition,
			PlaceRepository placeDao, ThesaurusRepository thesaurusDao, 
			HarvesterDefinitionRepository harvesterDefinitionDao,
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
			
			// get current harvesterDefinition from DB
			harvesterDefinition = harvesterDefinitionDao.getByName(harvesterDefinition.getName());
			
			// prevent multiple instances of the same harvester from being executed simultaneously
			if (!harvesterDefinition.isEnabled()) {
				logger.info("Harvester {} is disabled. Skipping execution ...",
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
			
			harvesterDefinition.setEnabled(false);
			harvesterDefinitionDao.save(harvesterDefinition);
			
			Thesaurus thesaurus = thesaurusDao.getThesaurusByKey(harvesterDefinition.getTargetThesaurus());
			if (thesaurus == null) {
				throw new IllegalStateException("Target thesaurus not found in database");
			}
			
			Harvester harvester = harvesterDefinition.getHarvesterType().newInstance();
			harvester.harvest(harvesterDefinition.getLastHarvestedDate());			
			
			while (true) {
				
				Place candidatePlace = harvester.getNextPlace();
				
				logger.debug("got place from harvester: {}", candidatePlace);
				
				if (candidatePlace == null) break;
				
				saveRecursive(candidatePlace, thesaurus);				
				
				/*Place identifiedPlace = entityIdentifier.identify(candidatePlace, thesaurus);
				if (identifiedPlace != null) {
					logger.info("identified place: {}", candidatePlace.getId());
					// TODO merge places
					for (Place child : candidatePlace.getChildren()) {
						identifiedPlace.addChild(child);
					}
					placeDao.delete(candidatePlace.getId());
					placeDao.save(identifiedPlace);
				}*/
				
				// TODO: beziehungen gerade biegen
				// bei CascadeType.MERGE werden die children und ihre identifier zu früh gespeichert
				// ohne gibt es eine TransientObjectException
				
			}
			
			harvester.close();
			
			harvesterDefinition.setLastHarvestedDate(new Date());
			harvesterDefinition.setEnabled(true);
			harvesterDefinitionDao.save(harvesterDefinition);
			
		} catch (Exception e) {
			//harvesterDefinition.setRunning(false);
			//harvesterDefinitionDao.save(harvesterDefinition);
			throw new RuntimeException("error while creating harvester", e);
		}
		
	}

	private void saveRecursive(Place candidatePlace, Thesaurus thesaurus) {
		
		candidatePlace.setThesaurus(thesaurus.getKey());				
		Place savedPlace = placeDao.save(candidatePlace);
		logger.info("saved place: {}", savedPlace.getId());
		
// TODO
//		if (candidatePlace.getChildren().size() > 0) {
//			for (Place child : candidatePlace.getChildren()) {
//				saveRecursive(child, thesaurus);
//			}
//		}
		
	}

}
