package ru.yandex.qatools.camelot.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author smecsia
 * @author innokenty
 */
public class Matchers {

    private static class NotNullMatcher
            extends TypeSafeMatcher<AggregatorStateStorage> {

        private final String key;

        private NotNullMatcher(String key) {
            this.key = key;
        }

        @Override
        protected boolean matchesSafely(AggregatorStateStorage stateStorage) {
            return stateStorage.getActual(key) != null;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.format(
                    "Storage containing not null object by key '%s'", key));
        }
    }

    private static class InstanceOfMatcher
            extends TypeSafeMatcher<AggregatorStateStorage> {

        private final String key;

        private final Class stateClass;

        private InstanceOfMatcher(String key, Class stateClass) {
            this.key = key;
            this.stateClass = stateClass;
        }

        @Override
        protected boolean matchesSafely(AggregatorStateStorage stateStorage) {
            Object actual = stateStorage.getActual(key);
            return actual != null && stateClass.isInstance(actual);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.format(
                    "Storage containing instance of %s by key '%s'",
                    stateClass.getSimpleName(), key));
        }
    }

    public static Matcher<AggregatorStateStorage> containStateFor(String key) {
        return new NotNullMatcher(key);
    }

    public static Matcher<AggregatorStateStorage> containStateFor(String key, Class stateClass) {
        return new InstanceOfMatcher(key, stateClass);
    }
}
