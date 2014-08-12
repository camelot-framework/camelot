package ru.yandex.qatools.camelot.test;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelotTestRunner.class)
@DisableTimers
public class CamelotTestWithTestComponentTest {

    @TestComponent
    Steps steps;

    @Test
    public void testWithSteps() throws Exception {
        steps.testRoute();
    }

}
