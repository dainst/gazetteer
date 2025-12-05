package org.dainst.gazetteer.harvest;

import org.dainst.gazetteer.dao.HarvesterDefinitionRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.Merger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
public class HarvestingManager implements InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(HarvestingManager.class);

	private final HarvesterDefinitionRepository harvesterDefinitionDao;
	private final PlaceRepository placeDao;
	private final TaskScheduler taskScheduler;
	private final IdGenerator idGenerator;
	private final Merger merger;

    public HarvestingManager(
            HarvesterDefinitionRepository harvesterDefinitionDao,
            PlaceRepository placeRepository,
            TaskScheduler taskScheduler,
            IdGenerator idGenerator,
            Merger merger
    ) {
        this.harvesterDefinitionDao = harvesterDefinitionDao;
        this.placeDao = placeRepository;
        this.taskScheduler = taskScheduler;
        this.idGenerator = idGenerator;
        this.merger = merger;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("initializing HarvestingManager");
        Iterable<HarvesterDefinition> defs = harvesterDefinitionDao.findAll();
        for (HarvesterDefinition def : defs) {
            logger.info("scheduling harvesting handler for definition: {}", def.getName());
            CronTrigger trigger = new CronTrigger(def.getCronExpression());
            HarvestingHandler handler = new HarvestingHandler(def, placeDao,
                    harvesterDefinitionDao, idGenerator, merger);
            taskScheduler.schedule(handler, trigger);
        }
    }
}
