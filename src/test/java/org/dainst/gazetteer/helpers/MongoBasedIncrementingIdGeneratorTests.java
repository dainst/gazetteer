package org.dainst.gazetteer.helpers;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.dainst.gazetteer.helpers.MongoBasedIncrementingIdGenerator.Counter;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.mockito.Mockito.*;

public class MongoBasedIncrementingIdGeneratorTests {

	@Test
	public void testNewCounter() {
		
		MongoTemplate mongoTemplate = mock(MongoTemplate.class);
		when(mongoTemplate.findById("test",Counter.class)).thenReturn(null);
		
		MongoBasedIncrementingIdGenerator idGenerator = new MongoBasedIncrementingIdGenerator(mongoTemplate, "test", 1);
		
		for(int i=1; i < 100000; i++) {
			String id = idGenerator.generate(null);
			assertEquals(String.valueOf(i), id);
		}
		
	}

	@Test
	public void testMultithreaded() throws InterruptedException {
		
		Counter counter = new MongoBasedIncrementingIdGenerator.Counter();
		counter.setId("test");
		counter.setValue(42);
		
		final Set<String> generatedIds1 = new HashSet<String>();
		final Set<String> generatedIds2 = new HashSet<String>();
		
		final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
		when(mongoTemplate.findById("test",Counter.class)).thenReturn(counter);
		
		Thread t1 = new Thread() {
			public void run() {
				MongoBasedIncrementingIdGenerator idGenerator = new MongoBasedIncrementingIdGenerator(mongoTemplate, "test", 1);
				for(int i=42; i < 100000; i++) {
					String id = idGenerator.generate(null);
					generatedIds1.add(id);
				}
			}
		};
		t1.start();
		
		Thread t2 = new Thread() {
			public void run() {
				MongoBasedIncrementingIdGenerator idGenerator = new MongoBasedIncrementingIdGenerator(mongoTemplate, "test", 1);
				for(int i=42; i < 100000; i++) {
					String id = idGenerator.generate(null);
					generatedIds2.add(id);
				}
			}
		};
		t2.start();
		
		// wait for threads to finish
		while(t1.isAlive() && t2.isAlive()) {
			Thread.sleep(1000);
		}
		
		for (String id : generatedIds2) {
			assertFalse(generatedIds1.contains(id));
		}
			
	}

}
