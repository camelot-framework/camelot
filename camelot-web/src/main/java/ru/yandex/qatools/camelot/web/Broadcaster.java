package ru.yandex.qatools.camelot.web;

import java.util.concurrent.Future;

/**
 * @author Ilya Sadykov
 */
public interface Broadcaster extends org.atmosphere.cpr.Broadcaster { //NOSONAR
    @Override
    Future<Object> broadcast(Object msg);

    void send(Object message);
}
