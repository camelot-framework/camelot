package ru.yandex.qatools.camelot.core.impl;

import org.apache.commons.collections.map.ListOrderedMap;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.builders.PluginTreeRouteBuilder;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginTreeTest {
    final Map<String, Plugin> treeModel = buildTreeModel();
    PluginTree tree;

    @Before
    public void init() {
        tree = PluginTree.buildTree(treeModel);
    }

    @Test
    public void testBuildTree() {
        assertEquals(tree.getChildren().size(), 2);
        assertEquals(tree.getChild("root1").getChildren().size(), 2);
        assertEquals(tree.getChild("root2").getChildren().size(), 2);
        assertEquals(tree.getChild("root1").getChild("root1.branch1").getChildren().size(), 2);
        assertEquals(tree.getChild("root1").getChild("root1.branch2").getChildren().size(), 1);
        assertEquals(tree.getChild("root2").getChild("root2.branch2").getChildren().size(), 3);
        assertEquals(tree.getChild("root2").getChild("root2.branch1").getChildren().size(), 1);
    }

    @Test
    public void testTreeRouteBuild() {
        PluginTreeRouteBuilder builder = new PluginTreeRouteBuilder(tree);
        List<List<PluginTree>> routes = builder.build();
        final String[][] routesCheck = new String[][]{
                {"root1", "root1.branch1", "root1.branch1.leaf1"},
                {"root1", "root1.branch1", "root1.branch1.leaf2"},
                {"root1", "root1.branch2", "root1.branch2.leaf1"},
                {"root2", "root2.branch1", "root2.branch1.leaf1"},
                {"root2", "root2.branch2", "root2.branch2.leaf1"},
                {"root2", "root2.branch2", "root2.branch2.leaf2"},
                {"root2", "root2.branch2", "root2.branch2.leaf3"},
        };
        int i = 0;
        for (List<PluginTree> route : routes) {
            int j = -1;
            for (PluginTree node : route) {
                if (node.getPlugin() != null) {
                    assertEquals(routesCheck[i][j], node.getPlugin().getId());
                }
                j++;
            }
            i++;
        }
    }

    @Test
    public void testIterator() {
        int index = 0;
        final String[] order = new String[]{
                "root1", "root1.branch1", "root1.branch1.leaf1", "root1.branch1.leaf2",
                "root1.branch2", "root1.branch2.leaf1",
                "root2", "root2.branch1", "root2.branch1.leaf1",
                "root2.branch2", "root2.branch2.leaf1", "root2.branch2.leaf2", "root2.branch2.leaf3"
        };
        for (Object node : tree) {
            assertTrue("node must be instance of PluginTree", node instanceof PluginTree);
            assertEquals(order[index++], ((PluginTree) node).getPlugin().getId());
        }
    }

    private Map<String, Plugin> buildTreeModel() {
        Map<String, Plugin> treeModel = new ListOrderedMap();
        addPlugin("root1", null, treeModel);
        addPlugin("root2", null, treeModel);
        addPlugin("root1.branch1", "root1", treeModel);
        addPlugin("root1.branch2", "root1", treeModel);
        addPlugin("root2.branch1", "root2", treeModel);
        addPlugin("root2.branch2", "root2", treeModel);
        addPlugin("root1.branch1.leaf1", "root1.branch1", treeModel);
        addPlugin("root1.branch1.leaf2", "root1.branch1", treeModel);
        addPlugin("root1.branch2.leaf1", "root1.branch2", treeModel);
        addPlugin("root2.branch1.leaf1", "root2.branch1", treeModel);
        addPlugin("root2.branch2.leaf1", "root2.branch2", treeModel);
        addPlugin("root2.branch2.leaf2", "root2.branch2", treeModel);
        addPlugin("root2.branch2.leaf3", "root2.branch2", treeModel);
        return treeModel;
    }

    private void addPlugin(String id, String parent, Map<String, Plugin> treeMap) {
        Plugin plugin = new Plugin();
        plugin.setId(id);
        plugin.setSource(parent);
        treeMap.put(id, plugin);
    }
}
