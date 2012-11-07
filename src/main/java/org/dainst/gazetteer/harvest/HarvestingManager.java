package org.dainst.gazetteer.harvest;

import org.dainst.gazetteer.dao.HarvesterDefinitionRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.ThesaurusRepository;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.helpers.EntityIdentifier;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.Merger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class HarvestingManager {
	
	private static Logger logger = LoggerFactory.getLogger(HarvestingManager.class);
	
	@Autowired
	private HarvesterDefinitionRepository harvesterDefinitionDao;
	
	@Autowired
	private PlaceRepository placeDao;

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private ThesaurusRepository thesaurusDao;
	
	@Autowired
	private EntityIdentifier entityIdentifier;
	
	@Autowired
	private IdGenerator idGenerator;
	
	@Autowired
	private Merger merger;

	public void initialize() {
		logger.info("initializing HarvestingManager");
		Iterable<HarvesterDefinition> defs = harvesterDefinitionDao.findAll();
		for (HarvesterDefinition def : defs) {
			logger.info("scheduling harvesting handler for definition: " + def.getName());
			CronTrigger trigger = new CronTrigger(def.getCronExpression());
			HarvestingHandler handler = new HarvestingHandler(def, placeDao,
					thesaurusDao, harvesterDefinitionDao, idGenerator, entityIdentifier, merger);
			taskScheduler.schedule(handler, trigger);
		}
	}

}
