package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.EndpointListener;
import ru.yandex.qatools.camelot.config.PluginContext;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.System.identityHashCode;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class EndpointListenerImpl implements EndpointListener {
    final private static Logger LOGGER = LoggerFactory.getLogger(EndpointListenerImpl.class);
    final Queue<Pair<ListenerProcessor, Thread>> executors = new ConcurrentLinkedQueue<>();
    final PluginContext context;
    final String listenerName;

    public EndpointListenerImpl(PluginContext context) {
        this.context = context;
        this.listenerName = identityHashCode(this) + "(" + context.getClass().getSimpleName() + ")";
        LOGGER.info("Creating new output listener with queue " + listenerName + " for plugin " + context.getId());
    }

    @Override
    public void listen(final long timeout, final TimeUnit unit, Processor processor) throws InterruptedException {
        LOGGER.info(String.format("Adding new output listener %s to queue %d with timeout of %d%s for plugin %s",
                listenerName, identityHashCode(executors), timeout, unit, context.getId()));
        final ListenerProcessor task = new ListenerProcessor(processor, timeout, unit, context);
        Thread thread = new Thread(task);
        thread.start();
        executors.add(new ImmutablePair<>(task, thread));
        thread.join();
        LOGGER.info(String.format("Listener %s has been completed for plugin: %s", listenerName, context.getId()));
    }

    public Object notifyOnMessage(@Body final Object body, @Headers Map<String, Object> headers) {
        try {
            LOGGER.debug(String.format("Notifying listeners %s of plugin %s: received event of type %s",
                    listenerName, context.getId(), headers.get(BODY_CLASS)));
            Iterator<Pair<ListenerProcessor, Thread>> it = executors.iterator();
            while (it.hasNext()) {
                final Pair<ListenerProcessor, Thread> next = it.next();
                LOGGER.info(String.format("Endpoint notify live listener %s about message %s", next.getKey(), body));
                boolean remove = true;
                try {
                    remove = next.getKey().onMessage(body, headers);
                } catch (Exception e) {
                    LOGGER.error(String.format("Failed to notify the listener %s about message %s",
                            next.getKey().processor, body), e);
                }
                if (remove) {
                    it.remove();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to notify listeners", e);
        }
        return body;
    }

    private static class ListenerProcessor implements Runnable {
        final Processor processor;
        final long timeout;
        final TimeUnit unit;
        final PluginContext context;

        private ListenerProcessor(Processor processor, final long timeout, final TimeUnit unit, PluginContext context) {
            this.processor = processor;
            this.timeout = timeout;
            this.unit = unit;
            this.context = context;
        }

        @SuppressWarnings("unchecked")
        public boolean onMessage(@Body final Object body, @Headers Map<String, Object> headers) throws Exception {
            synchronized (this) {
                Object object = body;
                if (body instanceof byte[]) {
                    Class<? extends Serializable> bodyClass = (Class<? extends Serializable>)
                            context.getClassLoader().loadClass((String) headers.get(BODY_CLASS));
                    object = deserializeFromBytes((byte[]) object, context.getClassLoader(), bodyClass);
                }
                final boolean procOK = processor.onMessage(object, headers);
                if (procOK) {
                    this.notify();
                }
                return procOK;
            }
        }

        @Override
        public void run() {
            try {
                synchronized (this) {
                    this.wait(MILLISECONDS.convert(timeout, unit));
                }
            } catch (InterruptedException e) {
                LOGGER.error("Failed to wait for the incoming message...", e);
            }
        }
    }
}
