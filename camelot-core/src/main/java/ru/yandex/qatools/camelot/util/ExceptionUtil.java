package ru.yandex.qatools.camelot.util;

/**
 * @author Ilya Sadykov
 */
public class ExceptionUtil {

    public static String formatStackTrace(Throwable exc) {
        Throwable cause = exc;
        StringBuilder builder = new StringBuilder();
        while (cause != null) {
            builder.append("Caused by ").append(cause.toString()).append(": ").append(cause.getMessage()).
                    append("\n");
            builder.append(formatStackTrace(cause.getStackTrace()));
            cause = cause.getCause();
        }
        return builder.toString();
    }


    public static String formatStackTrace(StackTraceElement[] stackTraceElements) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            builder.append("\tat ").append(element.getClassName()).
                    append("<").append(element.getMethodName()).append(">");
            builder.append("(").append(element.getFileName());
            if (element.getLineNumber() > 0) {
                builder.append(":").append(element.getLineNumber());
            }
            builder.append(")");
            builder.append("\n");
        }
        return builder.toString();
    }
}
