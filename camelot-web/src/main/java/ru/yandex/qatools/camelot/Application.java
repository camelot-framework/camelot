package ru.yandex.qatools.camelot;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import ru.yandex.qatools.camelot.features.LoadPluginResourceFeature;

import static java.lang.String.format;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 03.07.14
 */
public class Application extends ResourceConfig {

    public Application() {
        register(RequestContextFilter.class);
        register(JacksonFeature.class);
        register(LoadPluginResourceFeature.class);
        registerFinder(packageScanner(".web"));
        registerFinder(packageScanner(".core.web"));
    }

    private PackageNamesScanner packageScanner(String path) {
        return new PackageNamesScanner(new String[]{format("%s%s", getClass().getPackage().getName(), path)}, true);
    }
}
