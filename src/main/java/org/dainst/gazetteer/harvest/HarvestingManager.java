package org.dainst.gazetteer.harvest;

import java.util.List;

import org.dainst.gazetteer.dao.HarvesterDefinitionDao;
import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.dao.ThesaurusDao;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.helpers.EntityIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class HarvestingManager {
	
	private static Logger logger = LoggerFactory.getLogger(HarvestingManager.class);
	
	@Autowired
	private HarvesterDefinitionDao harvesterDefinitionDao;
	
	@Autowired
	private PlaceDao placeDao;

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private ThesaurusDao thesaurusDao;
	
	@Autowired
	private EntityIdentifier entityIdentifier;

	public void initialize() {
		logger.info("initializing HarvestingManager");
		List<HarvesterDefinition> defs = harvesterDefinitionDao.list();
		for (HarvesterDefinition def : defs) {
			logger.info("scheduling harvesting handler for definition: " + def.getName());
			CronTrigger trigger = new CronTrigger(def.getCronExpression());
			HarvestingHandler handler = new HarvestingHandler(def, placeDao,
					thesaurusDao, harvesterDefinitionDao, entityIdentifier);
			taskScheduler.schedule(handler, trigger);
		}
	}

}
