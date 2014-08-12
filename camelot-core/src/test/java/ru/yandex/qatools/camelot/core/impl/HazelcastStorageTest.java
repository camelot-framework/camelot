package ru.yandex.qatools.camelot.core.impl;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.api.Storage;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:camelot-core-context.xml", "classpath*:test-camelot-core-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
@SuppressWarnings("unchecked")
public class HazelcastStorageTest {
    public static final String KEY = "KEY";

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Test
    public void testLock() throws InterruptedException {
        final Storage storage = new HazelcastStorage(hazelcastInstance, "MAP");

        new Thread() {
            @Override
            public void run() {
                storage.lock(KEY, 0, SECONDS);
                try {
                    sleep(SECONDS.toMillis(3));
                    storage.unlock(KEY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        sleep(SECONDS.toMillis(1));

        assertFalse("Should not allow lock of the key", storage.lock(KEY, 1, SECONDS));
        sleep(SECONDS.toMillis(2));
        assertTrue("Should allow lock of the key", storage.lock(KEY, 1, SECONDS));

    }
}
