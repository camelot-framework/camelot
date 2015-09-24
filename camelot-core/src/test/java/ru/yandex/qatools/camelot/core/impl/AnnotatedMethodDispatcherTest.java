package ru.yandex.qatools.camelot.core.impl;

import org.junit.Test;
import org.mockito.InOrder;
import ru.yandex.qatools.camelot.common.AnnotatedMethodDispatcher;
import ru.yandex.qatools.fsm.annotations.OnException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static ru.yandex.qatools.camelot.common.Metadata.getMeta;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AnnotatedMethodDispatcherTest {

    class BaseState {

    }

    class DerivedState extends BaseState {
    }

    class ConcreteState extends BaseState {
    }

    interface Event {

    }

    class BaseEvent implements Event {

    }

    class DerivedEvent extends BaseEvent {

    }

    class ConcreteEvent extends DerivedEvent {

    }

    public static class SomeClass {
        @OnException
        public void mBaseStateDerivedEventObject(BaseState param1, DerivedEvent param2, Object param3) {

        }

        @OnException
        public BaseState mBaseStateBaseEventObject(BaseState param1, BaseEvent param2, Object param3) {
            return param1;
        }

        @OnException
        public void mBaseStateConcreteEvent(BaseState param1, ConcreteEvent param2) {

        }

        @OnException
        public void mConcreteStateConcreteEvent(ConcreteState param1, ConcreteEvent param2) {

        }

        @OnException
        public void mConcreteEventObject(ConcreteEvent param2, Object param3) {

        }

        @OnException
        public void mBaseState(BaseState state) {

        }

        @OnException
        public void mBaseStateEvent(BaseState state, Event param2) {

        }
    }

    @Test
    public void testBoth() throws Throwable {
        SomeClass obj = mock(SomeClass.class);
        AnnotatedMethodDispatcher caller = new AnnotatedMethodDispatcher(obj, getMeta(SomeClass.class));
        BaseState state = mock(BaseState.class);
        ConcreteEvent event = mock(ConcreteEvent.class);
        Object param = mock(Object.class);

        caller.dispatch(OnException.class, false, state, event, param);
        InOrder inOrder = inOrder(obj);
        inOrder.verify(obj).mBaseStateDerivedEventObject(state, event, param);
        inOrder.verify(obj).mBaseStateBaseEventObject(state, event, param);
        inOrder.verify(obj).mBaseStateConcreteEvent(state, event);
        inOrder.verify(obj).mBaseStateEvent(state, event);
        inOrder.verify(obj).mBaseState(state);
        inOrder.verify(obj).mConcreteEventObject(event, param);
        verifyNoMoreInteractions(obj);
    }

    @Test
    public void testBase() throws Throwable {
        SomeClass obj = mock(SomeClass.class);
        AnnotatedMethodDispatcher caller = new AnnotatedMethodDispatcher(obj, getMeta(SomeClass.class));
        DerivedState state = mock(DerivedState.class);
        ConcreteEvent event = mock(ConcreteEvent.class);
        Object param = mock(Object.class);

        caller.dispatch(OnException.class, true, state, event, param);
        verify(obj).mBaseStateDerivedEventObject(state, event, param);
        verifyNoMoreInteractions(obj);
    }

    @Test
    public void testDerived() throws Throwable {
        SomeClass obj = mock(SomeClass.class);
        AnnotatedMethodDispatcher caller = new AnnotatedMethodDispatcher(obj, getMeta(SomeClass.class));
        when(obj.mBaseStateBaseEventObject(any(BaseState.class), any(BaseEvent.class), any())).thenCallRealMethod();
        DerivedState state = mock(DerivedState.class);
        BaseEvent event = mock(BaseEvent.class);
        Object param = mock(Object.class);

        Map<Method, Object> res = caller.dispatch(OnException.class, true, state, event, param);
        verify(obj).mBaseStateBaseEventObject(state, event, param);
        verifyNoMoreInteractions(obj);

        assertEquals(1, res.size());
        assertEquals(state, res.values().iterator().next());
    }

    @Test
    public void testConcrete() throws Throwable {
        SomeClass obj = mock(SomeClass.class);
        AnnotatedMethodDispatcher caller = new AnnotatedMethodDispatcher(obj, getMeta(SomeClass.class));
        ConcreteState state = mock(ConcreteState.class);
        ConcreteEvent event = mock(ConcreteEvent.class);

        caller.dispatch(OnException.class, true, state, event);
        verify(obj).mConcreteStateConcreteEvent(state, event);
        verifyNoMoreInteractions(obj);
    }

}
