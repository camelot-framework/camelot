package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

/**
 * @author smecsia
 */
public class CamelotFSMBuilder<T> {
    final Class<T> fsmClass;

    public CamelotFSMBuilder(Class<T> fsmClass) {
        this.fsmClass = fsmClass;
    }

    public Yatomata<T> build(T fsmInstance) {
        return build(null, fsmInstance);
    }

    public Yatomata<T> build(Object state, T fsmInstance) {
        try {
            if (state == null) {
                return new YatomataImpl<>(fsmClass, fsmInstance);
            }
            return new YatomataImpl<>(fsmClass, fsmInstance, state);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize the FSM Engine for FSM " + fsmClass, e);
        }
    }
}
