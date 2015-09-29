package ru.yandex.qatools.camelot.config;

import java.util.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginTreeRouteBuilder {

    private final Deque<State> states = new LinkedList<>();

    public PluginTreeRouteBuilder(PluginTree pluginTree) {
        states.addLast(new State(pluginTree));
    }

    public List<List<PluginTree>> build() {
        final List<List<PluginTree>> routes = new ArrayList<>();
        while (!states.isEmpty()) {
            if (hasNextChild()) {
                final State state = state();
                final PluginTree nextChild = nextChild(state);
                states.addLast(new State(nextChild));
            } else {
                if (state().node.getChildren().isEmpty()) {
                    final List<PluginTree> route = new ArrayList<>();
                    routes.add(route);
                    for (State state : states) {
                        route.add(state.node);
                    }
                }
                states.pollLast();
            }
        }
        return routes;
    }

    private PluginTree nextChild(State state) {
        return state.node.getChildren().get(state.childIndex++);
    }

    private State state() {
        return states.getFirst();
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
