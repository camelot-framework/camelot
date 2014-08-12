package ru.yandex.qatools.camelot.core.builders;

import ru.yandex.qatools.camelot.core.impl.PluginTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginTreeRouteBuilder {

    private final Stack<State> states = new Stack<>();

    public PluginTreeRouteBuilder(PluginTree pluginTree) {
        states.push(new State(pluginTree));
    }

    public List<List<PluginTree>> build() {
        final List<List<PluginTree>> routes = new ArrayList<>();
        while (!states.isEmpty()) {
            if (hasNextChild()) {
                final State state = state();
                final PluginTree nextChild = nextChild(state);
                states.push(new State(nextChild));
            } else {
                if (state().node.getChildren().size() == 0) {
                    final List<PluginTree> route = new ArrayList<PluginTree>();
                    routes.add(route);
                    for (State state : states) {
                        route.add(state.node);
                    }
                }
                states.pop();
            }
        }
        return routes;
    }

    private PluginTree nextChild(State state) {
        return state.node.getChildren().get(state.childIndex++);
    }

    private PluginTree currChild(State state) {
        return state.node.getChildren().get(state.childIndex);
    }

    private State state() {
        return states.get(states.size() - 1);
    }

    private boolean hasNextChild() {
        return !states.isEmpty() && state().childIndex < state().node.getChildren().size();
    }

    private static class State {
        private PluginTree node;
        private int childIndex = 0;

        private State(PluginTree node) {
            this.node = node;
        }
    }
}
