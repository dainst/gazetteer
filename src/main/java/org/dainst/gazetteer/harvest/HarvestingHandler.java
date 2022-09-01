package org.dainst.gazetteer.harvest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dainst.gazetteer.dao.HarvesterDefinitionRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.Merger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestingHandler implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(HarvestingHandler.class);
	
	private HarvesterDefinition harvesterDefinition;
	
	private PlaceRepository placeDao;

	private HarvesterDefinitionRepository harvesterDefinitionDao;

	private IdGenerator idGenerator;
	
	public HarvestingHandler(HarvesterDefinition harvesterDefinition,
			PlaceRepository placeDao, 
			HarvesterDefinitionRepository harvesterDefinitionDao,
			IdGenerator idGenerator,
			Merger merger) {
		
		this.harvesterDefinition = harvesterDefinition;
		this.placeDao = placeDao;
		this.harvesterDefinitionDao = harvesterDefinitionDao;
		this.idGenerator = idGenerator;
		
	}

	@Override
	public void run() {
		
		try {
			
			// get current harvesterDefinition from DB
			harvesterDefinition = harvesterDefinitionDao.getByName(harvesterDefinition.getName());
			
			// prevent multiple instances of the same harvester from being executed simultaneously
			if (!harvesterDefinition.isEnabled()) {
				logger.debug("Harvester {} is disabled. Skipping execution ...",
						harvesterDefinition.getName());
				return;
			}
			
			if (logger.isInfoEnabled()) {
				String formattedDate = "never";
				if (harvesterDefinition.getLastHarvestedDate() != null)
					formattedDate = new SimpleDateFormat().format(harvesterDefinition.getLastHarvestedDate());
				logger.info("Running {}. Last time run: {}", 
						harvesterDefinition.getName(), formattedDate);
			}
			
			harvesterDefinition.setEnabled(false);
			harvesterDefinitionDao.save(harvesterDefinition);
			
			@SuppressWarnings("unchecked")
			Class<Harvester> clazz = (Class<Harvester>) Class.forName(harvesterDefinition.getHarvesterType());
			Harvester harvester = clazz.newInstance();
			harvester.setIdGenerator(idGenerator);
			harvester.setPlaceRepository(placeDao);
			harvester.harvest(harvesterDefinition.getLastHarvestedDate());			
			harvester.close();
			
			harvesterDefinition.setLastHarvestedDate(new Date());
			//harvesterDefinition.setEnabled(true);
			harvesterDefinitionDao.save(harvesterDefinition);
			
		} catch (Exception e) {
			throw new RuntimeException("error while creating harvester", e);
		}
		
	}

}
