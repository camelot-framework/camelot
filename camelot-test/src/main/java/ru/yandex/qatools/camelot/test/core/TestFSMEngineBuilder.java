package ru.yandex.qatools.camelot.test.core;

import ru.yandex.qatools.camelot.core.impl.CamelotFSMBuilder;
import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.Yatomata;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestFSMEngineBuilder<T> extends CamelotFSMBuilder<T> {
    private final Class<T> fsmClass;
    private final T fsmMock;
    private final CamelotFSMBuilder<T> origBuilder;

    TestFSMEngineBuilder(Class<T> fsmClass, T fsmMock, CamelotFSMBuilder<T> origBuilder) {
        super(fsmClass);
        this.fsmClass = fsmClass;
        this.fsmMock = fsmMock;
        this.origBuilder = origBuilder;
    }

    @Override
    public Yatomata<T> build(T fsm) {
        try {
            return new TestFSMEngine<>(fsmMock, fsmClass, null, origBuilder.build(fsm));
        } catch (FSMException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Yatomata<T> build(Object state, T fsmInstance) {
        try {
            return new TestFSMEngine<>(fsmMock, fsmClass, state, origBuilder.build(state, fsmInstance));
        } catch (FSMException e) {
            throw new RuntimeException(e);
        }
    }
}
