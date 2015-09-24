package ru.yandex.qatools.camelot.config;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginTreeIterator implements Iterator {
    private static class State {
        PluginTree node;
        int childIndex = 0;

        private State(PluginTree node) {
            this.node = node;
        }
    }

    private Deque<State> states = new LinkedList<>();

    public PluginTreeIterator(PluginTree pluginTree) {
        states.addLast(new State(pluginTree));
    }

    @Override
    public boolean hasNext() {
        while (!hasNextChild() && !states.isEmpty()) {
            states.pollLast();
        }
        return !states.isEmpty();
    }

    @Override
    public Object next() { //NOSONAR
        if (hasNextChild()) {
            final State state = state();
            final PluginTree nextChild = nextChild(state);
            states.addLast(new State(nextChild));
            return nextChild;
        }
        return states.pollLast().node;
    }

    private PluginTree nextChild(State state) {
        return state.node.getChildren().get(state.childIndex++);
    }

    private State state() {
        return states.getLast();
    }

    private boolean hasNextChild() {
        return !states.isEmpty() && state().childIndex < state().node.getChildren().size();
    }

    @Override
    public void remove() {
        throw new NotImplementedException("Not implemented!");
    }
}
