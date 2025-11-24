package org.dainst.gazetteer.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.dainst.gazetteer.helpers.MongoBasedIncrementingIdGenerator.Counter;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.mongodb.core.MongoTemplate;

public class MongoBasedIncrementingIdGeneratorTests {

    @Test
    public void testSingleThreaded() {
        final Counter counter = new Counter();
        counter.setId("test");
        counter.setValue(1);

        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.findById("test", Counter.class)).thenReturn(counter);

        MongoBasedIncrementingIdGenerator idGenerator =
            new MongoBasedIncrementingIdGenerator(mongoTemplate, "test", 1);

        for (int i = 1; i < 500; i++) {
            String id = idGenerator.generate(null);
            assertEquals(String.valueOf(i), id);
        }
    }

    @Test
    public void testMultithreaded() throws InterruptedException {
        final Counter counter = new Counter();
        counter.setId("test");
        counter.setValue(1);

        final List<String> generatedIds1 = new ArrayList<String>();
        final List<String> generatedIds2 = new ArrayList<String>();

        final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.findById("test", Counter.class)).then(
            new Answer<Counter>() {
                public Counter answer(InvocationOnMock invocation) {
                    System.out.println(
                        Thread.currentThread().getName() +
                            " - call of mocked findById()"
                    );
                    return new Counter(counter);
                }
            }
        );
        doAnswer(
            new Answer<Counter>() {
                public Counter answer(InvocationOnMock invocation) {
                    counter.setValue(
                        ((Counter) invocation.getArguments()[0]).getValue()
                    );
                    System.out.println(
                        Thread.currentThread().getName() +
                            " - call of mocked save(). Set local counter to " +
                            counter.getValue()
                    );
                    return null;
                }
            }
        )
            .when(mongoTemplate)
            .save(any(Counter.class));

        final MongoBasedIncrementingIdGenerator idGenerator =
            new MongoBasedIncrementingIdGenerator(mongoTemplate, "test", 1);

        Thread t1 = new Thread() {
            public void run() {
                for (int i = 1; i < 100; i++) {
                    String id = idGenerator.generate(null);
                    generatedIds1.add(id);
                }
            }
        };
        t1.start();

        Thread t2 = new Thread() {
            public void run() {
                for (int i = 1; i < 100; i++) {
                    String id = idGenerator.generate(null);
                    generatedIds2.add(id);
                }
            }
        };
        t2.start();

        // wait for threads to finish
        while (t1.isAlive() && t2.isAlive()) {
            Thread.sleep(1000);
        }

        for (String id : generatedIds2) {
            if (generatedIds1.contains(id)) System.out.println(
                "Duplicate ID: " + id
            );
            assertFalse(generatedIds1.contains(id));
        }
    }

    @Test
    public void testMultipleInstances() {
        final Counter counter = new Counter();
        counter.setId("test");
        counter.setValue(1);

        final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.findById("test", Counter.class)).then(
            new Answer<Counter>() {
                public Counter answer(InvocationOnMock invocation) {
                    System.out.println(
                        Thread.currentThread().getName() +
                            " - call of mocked findById()"
                    );
                    return new Counter(counter);
                }
            }
        );
        doAnswer(
            new Answer<Counter>() {
                public Counter answer(InvocationOnMock invocation) {
                    counter.setValue(
                        ((Counter) invocation.getArguments()[0]).getValue()
                    );
                    System.out.println(
                        Thread.currentThread().getName() +
                            " - call of mocked save(). Set local counter to " +
                            counter.getValue()
                    );
                    return null;
                }
            }
        )
            .when(mongoTemplate)
            .save(any(Counter.class));

        final MongoBasedIncrementingIdGenerator idGenerator1 =
            new MongoBasedIncrementingIdGenerator(mongoTemplate, "test", 1);
        final MongoBasedIncrementingIdGenerator idGenerator2 =
            new MongoBasedIncrementingIdGenerator(mongoTemplate, "test", 1);

        assertEquals("1", idGenerator1.generate(null));
        assertEquals("101", idGenerator2.generate(null));

        for (int i = 0; i < 100; i++) {
            idGenerator2.generate(null);
        }

        assertEquals("2", idGenerator1.generate(null));
        assertEquals("202", idGenerator2.generate(null));

        for (int i = 0; i < 100; i++) {
            idGenerator1.generate(null);
        }

        assertEquals("303", idGenerator1.generate(null));
        assertEquals("203", idGenerator2.generate(null));
    }
}
