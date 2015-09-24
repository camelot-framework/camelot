package ru.yandex.qatools.camelot.test.core;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.NewState;

import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.yandex.qatools.camelot.util.ReflectUtil.getAnnotation;
import static ru.yandex.qatools.fsm.utils.ReflectUtils.getMethodsInClassHierarchy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestUtil {

    public static Object pluginMock(Plugin plugin) throws Exception { //NOSONAR
        final Class<?> classToMock = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
        final Object mock = mock(classToMock);
        preparePluginMock(plugin, mock);
        return mock;
    }

    @SuppressWarnings("unchecked")
    public static void preparePluginMock(Plugin plugin, final Object mock) throws Exception { //NOSONAR
        final Class<?> classToMock = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
        final Object realFsmObject =
                plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass()).newInstance();
        plugin.getContext().getInjector().inject(realFsmObject, plugin.getContext());
        if (getAnnotation(classToMock, FSM.class) != null) {
            for (final Method m : getMethodsInClassHierarchy(classToMock)) {
                if (getAnnotation(m, NewState.class) != null) {
                    // We're invoking the REAL method here instead of mocked
                    final Answer<Object> realAnswer = new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                            return m.invoke(realFsmObject, invocationOnMock.getArguments());
                        }
                    };
                    if (m.getParameterTypes().length == 2) {
                        when(m.invoke(mock, any(Class.class), any())).thenAnswer(realAnswer);
                    } else {
                        when(m.invoke(mock, any(Class.class))).thenAnswer(realAnswer);
                    }
                }
            }
        }
    }
}
