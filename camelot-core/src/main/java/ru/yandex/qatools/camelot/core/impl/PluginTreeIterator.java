package ru.yandex.qatools.camelot.core.impl;

import org.apache.commons.lang.NotImplementedException;

import java.util.Iterator;
import java.util.Stack;

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

    private Stack<State> states = new Stack<State>();

    public PluginTreeIterator(PluginTree pluginTree) {
        states.push(new State(pluginTree));
    }

    @Override
    public boolean hasNext() {
        while (!hasNextChild() && states.size() > 0) {
            states.pop();
        }
        return states.size() > 0;
    }

    @Override
    public Object next() {
        if (hasNextChild()) {
            final State state = state();
            final PluginTree nextChild = nextChild(state);
            states.push(new State(nextChild));
            return nextChild;
        }
        return states.pop().node;
    }

    private PluginTree nextChild(State state) {
        return state.node.getChildren().get(state.childIndex++);
    }

    private State state() {
        return states.get(states.size() - 1);
    }

    private boolean hasNextChild() {
        return states.size() > 0 && state().childIndex < state().node.getChildren().size();
    }

    @Override
    public void remove() {
        throw new NotImplementedException("Not implemented!");
    }
}
