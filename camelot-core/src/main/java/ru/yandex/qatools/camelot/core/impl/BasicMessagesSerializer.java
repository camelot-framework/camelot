package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.core.MessagesSerializer;

import java.util.Map;

import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.util.SerializeUtil.unwrapBodyClassName;

/**
 * @author Ilya Sadykov
 */
public abstract class BasicMessagesSerializer implements MessagesSerializer {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Exchange preProcess(Exchange exchange, ClassLoader classLoader) {
        final Object body = exchange.getIn().getBody();
        if (body != null) {
            exchange.getIn().setBody(processBodyAndHeadersAfterReceive(body, exchange.getIn().getHeaders(), classLoader));
        }
        return exchange;
    }

    @Override
    public Exchange postProcess(Exchange exchange, ClassLoader classLoader) {
        final Object body = exchange.getIn().getBody();
        if (body != null) {
            exchange.getIn().setBody(processBodyAndHeadersBeforeSend(body, exchange.getIn().getHeaders(), classLoader));
        }
        return exchange;
    }

    @Override
    public Object processBodyAndHeadersAfterReceive(Object body, Map<String, Object> headers, ClassLoader classLoader) {
        try {
            Object res = deserialize(body, classLoader);
            headers.put(BODY_CLASS, res.getClass().getName());
            return res;
        } catch (Exception e) {
            logger.error("Failed to process body and headers after receiving body {}", body, e);
        }
        return body;
    }


    @Override
    public Object processBodyAndHeadersBeforeSend(Object body, Map<String, Object> headers, ClassLoader classLoader) {
        final String bodyClass = body.getClass().getName();
        final Object res = serialize(body, classLoader);
        headers.put(BODY_CLASS, bodyClass);
        return res;
    }

    @Override
    public String identifyBodyClassName(Object body) {
        if (body != null && body instanceof byte[]) {
            try {
                return unwrapBodyClassName((byte[]) body);
            } catch (Exception e) {
                logger.error("Failed to identify body class name of body {}", body, e);
            }
        }
        return null;
    }
}
