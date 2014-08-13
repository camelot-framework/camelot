package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.core.beans.TestEvent;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestEventUtil {

    public static String suiteFullName(TestEvent e) {
        return String.format("%s.%s.%s", e.getProfile(), e.getPackagename(), e.getClassname());
    }

    public static String methodFullName(TestEvent e) {
        return String.format("%s.%s.%s.%s", e.getProfile(), e.getPackagename(), e.getClassname(), e.getMethodname());
    }
}
