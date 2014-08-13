package ru.yandex.qatools.camelot.core.beans;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class EventUtils {
    public static String printEvent(TestEventData message) {
        return (message != null) ? (message.getSystem() + "[" + message.getProfile() + "] " + message.getPackagename() + "." +
                message.getClassname() + "." + message.getMethodname() + " ... " + message.getTimestamp()) : null;
    }
}
