package ru.yandex.qatools.camelot.common;

import org.apache.camel.Exchange;

import java.util.Map;

/**
 * @author Ilya Sadykov
 */
public interface MessagesSerializer {

    /**
     * Deserialize body from bytes with the provided classloader
     */
    Object deserialize(Object body, ClassLoader classLoader);

    /**
     * Serialize body to bytes with the provided classloader
     */
    Object serialize(Object body, ClassLoader classLoader);

    /**
     * Process exchange after input to processor
     */
    Exchange preProcess(Exchange exchange, ClassLoader classLoader);

    /**
     * Process exchange before output from processor
     */
    Exchange postProcess(Exchange exchange, ClassLoader classLoader);

    /**
     * Process body and headers before sending
     */
    Object processBodyAndHeadersBeforeSend(Object body, Map<String, Object> headers, ClassLoader classLoader);

    /**
     * Process body and headers after we've received them
     */
    Object processBodyAndHeadersAfterReceive(Object body, Map<String, Object> headers, ClassLoader classLoader);

    /**
     * Returns the class name of the serialized body if it is contained within exchange
     */
    String identifyBodyClassName(Object body);
}
