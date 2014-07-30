package org.dainst.gazetteer.helpers;

import org.dainst.gazetteer.domain.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.transaction.annotation.Transactional;

public class MongoBasedIncrementingIdGenerator implements IdGenerator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoBasedIncrementingIdGenerator.class);
	
	private MongoTemplate mongoTemplate;
	
	private int blockSize = 100;
	
	private long nextId = 0;

	private Counter counter;
	
	public MongoBasedIncrementingIdGenerator(MongoTemplate mongoTemplate, String counterId, long start) {
		this.mongoTemplate = mongoTemplate;
		counter = mongoTemplate.findById(counterId, Counter.class);
		if (counter == null) {
			counter = new Counter();
			counter.setId(counterId);
			counter.setValue(start);
			mongoTemplate.save(counter);
		}
		nextId = counter.getValue();
		allocateBlock();
	}

	@Override
	public String generate(Place place) {
		if (nextId >= counter.getValue()) {
			allocateBlock();
		}
		LOGGER.debug(Thread.currentThread().getName() + " - generated ID " + (nextId));
		return String.valueOf(nextId++);
	}
	
	@Transactional
	private synchronized void allocateBlock() {
		LOGGER.debug(Thread.currentThread().getName() + " - start allocateBlock()");
		counter = mongoTemplate.findById(counter.getId(), Counter.class);
		counter.inc(blockSize);
		mongoTemplate.save(counter);
		LOGGER.debug(Thread.currentThread().getName() + " - allocated IDs up to " + counter.getValue());
		LOGGER.debug(Thread.currentThread().getName() + " - end allocateBlock()");
	}

	public void setBlockSize(int step) {
		this.blockSize = step;
	}
	
	@Document
	public static class Counter {
		
		@Id
		private String id;
		
		private long value = 1;
		
		public Counter() {
			
		}
		
		// copy constructor
		public Counter(Counter counter) {
			this.id = counter.getId();
			this.value = counter.getValue();
			LOGGER.debug(Thread.currentThread().getName() + " - created counter with " + getValue());
		}

		public void inc(int step) {
			this.value += step;
			LOGGER.debug(Thread.currentThread().getName() + " - incremented counter to " + getValue());
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public long getValue() {
			return value;
		}

		public void setValue(long value) {
			this.value = value;
		}
		
	}

}
