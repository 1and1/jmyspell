package org.dts.spell.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A proxy-based event multicaster for any listener-interface.
 * Uses automatically generated proxy-class to receive calls which
 * will be forwarded to all registered listeners.
 * 
 * @author Juha Komulainen
 */
public final class EventMulticaster<T> {

    /** Thread-safe list of registered listeners */
    private final List<T> listeners = new CopyOnWriteArrayList<T>();
    
    /** Proxy for multicasting the events */
    private final T multicaster;

    /**
     * Constructs a multicaster for given listener-interface.
     * 
     * @param type of the listener-interface
     */
    @SuppressWarnings("unchecked")
    public EventMulticaster(Class<T> type) {
        multicaster = (T) Proxy.newProxyInstance(type.getClassLoader(),
                                                 new Class[] { type },
                                                 new MyInvocationHandler());
    }

    /**
     * Registers new listener for receiving the events.
     */
    public void addListener(T listener) {
        if (listener == null) throw new NullPointerException("null listener");
        
        listeners.add(listener);
    }
    
    /**
     * Removes listener.
     */
    public void removeListener(T listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the multicaster. All methods called on the multicaster
     * will end up being called on all registered listeners (unless one
     * of them throws an exception, in which case the exception is thrown
     * and processing stops).
     */
    public T getMulticaster() {
        return multicaster;
    }

    /**
     * Return if the internal list had listeners.
     * @return
     */
    public boolean isEmpty() {
        return listeners.isEmpty() ;
    }

    /**
     * The invocation-handler which will receive the calls made to
     * proxy and will forward them to the listeners.
     */
    private class MyInvocationHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable 
        {
            try {
                for (T listener : listeners) {
                    method.invoke(listener, args);
                }
                return null;
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}