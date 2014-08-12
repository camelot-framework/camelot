package ru.yandex.qatools.camelot.test.core;

import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestFSMEngine<T> implements Yatomata<T> {
    final Yatomata<T> watcher;
    final Yatomata<T> original;

    public TestFSMEngine(T fsmMock, Class<T> fsmClass, Object state, Yatomata origEngine) throws FSMException {
        if (state == null) {
            watcher = new YatomataImpl<>(fsmClass, fsmMock);
        } else {
            watcher = new YatomataImpl<>(fsmClass, fsmMock, state);
        }
        this.original = origEngine;
    }

    @Override
    public Object fire(Object event) {
        Object res = original.fire(event);
        watcher.fire(event);
        return res;
    }

    @Override
    public T getFSM() {
        return original.getFSM();
    }

    @Override
    public Object getCurrentState() {
        return original.getCurrentState();
    }

    @Override
    public boolean isCompleted() {
        return original.isCompleted();
    }

    @Override
    public Class<T> getFSMClass() {
        return original.getFSMClass();
    }

}
