package ru.yandex.qatools.camelot.core;

import java.util.concurrent.Future;

/**
 * @author Ilya Sadykov
 */
public interface Broadcaster extends org.atmosphere.cpr.Broadcaster {
    public Future<Object> broadcast(Object msg);

    void send(Object message);
}
