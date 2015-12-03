package ru.yandex.qatools.camelot.mongodb;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.mongodb.test.BroadcastProcessor;
import ru.yandex.qatools.camelot.test.CamelotTestRunner;
import ru.yandex.qatools.camelot.test.Helper;
import ru.yandex.qatools.camelot.test.PluginMock;
import ru.yandex.qatools.camelot.test.TestHelper;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * @author Ilya Sadykov
 */
@RunWith(CamelotTestRunner.class)
public class MongodbDirectRoutesInitializerTest {
    @Helper
    TestHelper helper;

    @PluginMock
    BroadcastProcessor proc;

    @Test
    public void testBroadcastProc() throws Exception {
        helper.sendTo(BroadcastProcessor.class, "hello");
        verify(proc, timeout(2000)).onString("hello");
    }
}