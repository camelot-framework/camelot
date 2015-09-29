package ru.yandex.qatools.camelot.config;

import java.util.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginTree implements Iterable {

    final private Plugin plugin;
    final private List<PluginTree> children = new ArrayList<>();
    private List<List<PluginTree>> routes;

    public PluginTree() {
        this.plugin = null;
    }

    public PluginTree(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public List<PluginTree> getChildren() {
        return children;
    }

    public PluginTree getChild(String id) {
        for (PluginTree child : children) {
            if (child.getPlugin() != null && child.getPlugin().getId().equals(id)) {
                return child;
            }
        }
        return null;
    }

    public List<List<PluginTree>> getRoutes() {
        if (routes == null) {
            routes = new PluginTreeRouteBuilder(this).build();
        }
        return routes;
    }

    public static PluginTree buildTree(Map<String, Plugin> pluginMap) {
        PluginTree mainRoot = new PluginTree();
        final Map<String, List<Plugin>> consumers = findConsumers(pluginMap);
        buildLeaf(mainRoot, consumers, null);
        return mainRoot;
    }

    private static void buildLeaf(PluginTree branch, Map<String, List<Plugin>> consumers, String pluginId) {
        if (!consumers.containsKey(pluginId)) {
            return; // leaf
        }
        for (Plugin plugin : consumers.get(pluginId)) {
            PluginTree child = new PluginTree(plugin);
            branch.children.add(child);
            buildLeaf(child, consumers, child.getPlugin().getId());
        }
    }

    private static Map<String, List<Plugin>> findConsumers(Map<String, Plugin> pluginMap) {
        Map<String, List<Plugin>> result = new HashMap<>();
        for (final Plugin plugin : pluginMap.values()) {
            final String from = plugin.getSource();
            if (!result.containsKey(from)) {
                result.put(from, new ArrayList<Plugin>());
            }
            result.get(from).add(plugin);
        }
        return result;
    }

    @Override
    public Iterator iterator() {
        return new PluginTreeIterator(this);
    }
}
