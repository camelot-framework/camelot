package ru.yandex.qatools.camelot.common;

import org.junit.Test;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.common.LocalMemoryStorage;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class LocalMemoryStorageTest {

    public static final String KEY = "KEY";

    @Test
    public void testLock() throws InterruptedException {
        final Storage storage = new LocalMemoryStorage<>();

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
