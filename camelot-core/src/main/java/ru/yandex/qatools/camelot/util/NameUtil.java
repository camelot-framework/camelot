package ru.yandex.qatools.camelot.util;

import org.apache.commons.lang3.StringUtils;
import ru.yandex.qatools.camelot.config.Parameter;
import ru.yandex.qatools.camelot.config.Plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ru.yandex.qatools.camelot.util.MapUtil.mapParams;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class NameUtil {

    /**
     * Generate the routeId for the from -> to
     */
    public static String routeId(String fromUri, String... toUri) {
        return fromUri + "->" + StringUtils.join(toUri, ",");
    }

    /**
     * Generate the default plugin id (if there was no id specified)
     */
    public static String defaultPluginId(Plugin plugin) {
        return plugin.getContext().getPluginClass();
    }

    /**
     * Generates the plugin widget bean name
     */
    public static String pluginStorageKey(String pluginId) {
        return pluginId + "Storage";
    }

    /**
     * Generates the plugin resource bean name
     */
    public static String pluginResourceBeanName(String pluginId) {
        return pluginId + "Resource";
    }

    /**
     * Generates broadcast plugin uri
     */
    public static String broadcastUri(String pluginId, String suffix) {
        return "activemq:topic:" + pluginId + suffix;
    }

    /**
     * Generates local plugin uri with id and suffix
     */
    public static String localUri(String pluginId, String suffix) {
        return "direct:plugin." + pluginId + suffix;
    }

    /**
     * Generates the plugin input uri
     */
    public static String pluginInputUri(Plugin plugin) {
        return plugin.getBaseInputUri() + "." + plugin.getId();
    }

    /**
     * Generates the plugin output uri
     */
    public static String pluginOutputUri(Plugin plugin) {
        return plugin.getBaseOutputUri() + "." + plugin.getId();
    }

    /**
     * Generates broadcast plugin route id
     */
    public static String broadcastRouteId(String broadcastUri, String engineName) {
        return broadcastUri + engineName;
    }

    /**
     * Generates local plugin uri with id and suffix
     */
    public static String localRouteId(String localUri, String engineName) {
        return localUri + engineName;
    }

    /**
     * Generates the plugin input RouteId
     */
    public static String pluginInputRouteId(String inputUri, String engineName) {
        return inputUri + engineName;
    }

    /**
     * Generates the plugin output RouteId
     */
    public static String pluginOutputRouteId(String outputUri, String engineName) {
        return outputUri + engineName;
    }

    /**
     * Generates the broker configuration
     */
    public static String pluginBrokerConfig(Plugin plugin) {
        if (!isEmpty(plugin.getBrokerConfig())) {
            return plugin.getBrokerConfig();
        }

        Map<String, String> params = new HashMap<>();
        if (plugin.getContext() !=null && plugin.getContext().getSource() != null &&
                plugin.getContext().getSource().getBroker() != null) {
            List<Parameter> defaults = plugin.getContext().getSource().getBroker().getParams();
            params.putAll(mapParams(defaults));
        }
        if (plugin.getBroker() != null) {
            params.putAll(mapParams(plugin.getBroker().getParams()));
        }

        return createBrokerConfigFromParamsMap(params);
    }

    public static String createBrokerConfigFromParamsMap(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        builder.append("?");

        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> parameter = iterator.next();
            builder.append(parameter.getKey()).append("=").append(parameter.getValue());
            if (iterator.hasNext()) {
                builder.append("&");
            }
        }

        return builder.toString();
    }
}
