/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import java.lang.reflect.Method;

/**
 * Manages information about used component events and their handlers (in
 * one form). Also maps handled events to methods of a "common event
 * dispatching listener" (CEDL), which may be generated by the code generator
 * (in certain modes).
 *
 * @author Tomas Pavek
 */

public class FormEvents {

    static final Event[] NO_EVENTS = {};

    // CEDL: mapping listener type name to ListenerInfo
    private Map usedListeners = new HashMap();

    // CEDL: mapping listener method signature to list of Event
    private Map usedMethods = new HashMap();

    // event handlers: mapping event handler name to list of Event
    private Map eventHandlers = new HashMap();

    private FormModel formModel;

    FormEvents(FormModel formModel) {
        this.formModel = formModel;
    }

    // --------
    // public interface - getters

    public boolean hasEventsInCEDL() {
        return !usedListeners.isEmpty();
    }

    public Class[] getCEDLTypes() {
        Collection infoValues = usedListeners.values();
        Class[] listenerTypes = new Class[infoValues.size()];
        int i = 0;
        Iterator it = infoValues.iterator();
        while (it.hasNext())
            listenerTypes[i++] = ((ListenerInfo)it.next()).listenerType;
        
        return listenerTypes;
    }

    public Event[] getEventsForCEDLMethod(Method listenerMethod) {
        List eventList = (List) usedMethods.get(fullMethodName(listenerMethod));
        if (eventList == null)
            return NO_EVENTS;

        Event[] eventArray = new Event[eventList.size()];
        eventList.toArray(eventArray);
        return eventArray;
    }

    public Event[] getEventsForHandler(String handlerName) {
        List handlerEventList = (List) eventHandlers.get(handlerName);
        Event[] events = new Event[handlerEventList.size()];
        handlerEventList.toArray(events);
        return events;
    }

    public Method getOriginalListenerMethod(String handlerName) {
        List handlerEventList = (List) eventHandlers.get(handlerName);
        return handlerEventList != null ?
               ((Event)handlerEventList.get(0)).getListenerMethod() : null;
    }

    public String[] getAllEventHandlers() {
        Set nameSet = eventHandlers.keySet();
        String[] names = new String[nameSet.size()];
        nameSet.toArray(names);
        return names;
    }

    // --------
    // public interface - adding/removing events

    public void attachEvent(Event event, String handlerName, String handlerText)
    {
        boolean newHandler = false; // if new event handler has been created

        if (handlerName == null && event.hasEventHandlers()) { // nothing to do
            handlerName = (String) event.getEventHandlerList().get(0);
            event = null;
        }
        else { // find/create handler, attach event to it
            List handlerEventList;
            if (handlerName != null) {
                handlerEventList = (List) eventHandlers.get(handlerName);
                if (handlerEventList != null) // handler already exists
                    checkCompatibility(event, (Event) handlerEventList.get(0));
            }
            else { // no name provided, find a free one
                handlerEventList = null;
                handlerName = findFreeHandlerName(
                                    event.getListenerMethod().getName(),
                                    event.getComponent().getName());
            }

            if (handlerEventList == null) { // create new handler
                handlerEventList = new ArrayList(3);
                eventHandlers.put(handlerName, handlerEventList);
                newHandler = true;
            }

            if (!event.isInCEDL())
                registerEventInCEDL(event);

            if (event.addEventHandler(handlerName))
                handlerEventList.add(event);
            else // handler not added (event is already attached to it)
                event = null;
        }

        formModel.fireEventHandlerAdded(event,
                                        handlerName, handlerText, newHandler);
    }

    public void detachEvent(Event event, String handlerName) {
        if (event.removeEventHandler(handlerName)) {
            if (!event.hasEventHandlers())
                unregisterEventFromCEDL(event);

            detachEventHandler(event, handlerName);
        }
    }

    public void detachEvent(Event event) {
        unregisterEventFromCEDL(event);
        String[] handlerNames = event.getEventHandlers();
        for (int i=0; i < handlerNames.length; i++) {
            event.removeEventHandler(handlerNames[i]);
            detachEventHandler(event, handlerNames[i]);
        }
    }

    public void renameEventHandler(String oldHandlerName, String newHandlerName)
    {
        if (oldHandlerName == null || newHandlerName == null
                || oldHandlerName.equals(newHandlerName))
            return;

        List handlerEventList = (List) eventHandlers.get(oldHandlerName);
        if (handlerEventList == null)
            return; // oldHandlerName handler not found

        if (eventHandlers.get(newHandlerName) == null) {
            for (int j=0; j < handlerEventList.size(); j++) {
                Event event = (Event) handlerEventList.get(j);
                event.renameEventHandler(oldHandlerName, newHandlerName);
            }
            eventHandlers.remove(oldHandlerName);
            eventHandlers.put(newHandlerName, handlerEventList);
            formModel.fireEventHandlerRenamed(oldHandlerName, newHandlerName);
        }
        else {
            IllegalArgumentException iae =
                new IllegalArgumentException("Cannot rename handler"); // NOI18N
            org.openide.ErrorManager.getDefault().annotate(
                iae, FormUtils.getBundleString("MSG_CannotRename")); // NOI18N
            throw iae;
        }
    }

    public String findFreeHandlerName(String methodName, String componentName) {
        return findFreeHandlerName(componentName
                                   + methodName.substring(0, 1).toUpperCase()
                                   + methodName.substring(1));
    }

    public String findFreeHandlerName(String baseName) {
        String name = baseName;
        int n = 0;
        while (eventHandlers.get(name) != null)
            name = baseName + (++n);
        return name;
    }

    // --------
    // package private interface

    static String getEventIdName(Method eventMethod) {
        StringBuffer buf = new StringBuffer(64);

        buf.append("$"); // NOI18N
        buf.append(eventMethod.getDeclaringClass().getName());
        buf.append("."); // NOI18N
        buf.append(eventMethod.getName());
        buf.append("("); // NOI18N

        Class[] parameterTypes = eventMethod.getParameterTypes();
        for (int i=0; i < parameterTypes.length; i++) {
            buf.append(parameterTypes[i].getName());
            if (i+1 < parameterTypes.length)
                buf.append(", "); // NOI18N
        }

        buf.append(")"); // NOI18N
        return buf.toString();
    }

    // --------
    // private methods

    private void registerEventInCEDL(Event event) {
        // listener class must be an interface (required for CEDL)
        Class listenerType = event.getEventSetDescriptor().getListenerType();
        if (!listenerType.isInterface())
            return;

        // event method must have EventObject as parameter (required for CEDL)
        Class[] parameters = event.getListenerMethod().getParameterTypes();
        if (parameters.length == 0
                || !java.util.EventObject.class.isAssignableFrom(parameters[0]))
            return;

        if (!addEventToMethod(event))
            return; // method signature already used

        String listenerTypeName = listenerType.getName();
        ListenerInfo lInfo = (ListenerInfo)
                             usedListeners.get(listenerTypeName);
        if (lInfo == null) {
            lInfo = new ListenerInfo(listenerType);
            usedListeners.put(listenerTypeName, lInfo);
        }
        else lInfo.listenerType = listenerType;
        lInfo.useCount++;

        event.setInCEDL(true);
    }

    private void unregisterEventFromCEDL(Event event) {
        if (removeEventFromMethod(event)) {
            String listenerTypeName = event.getEventSetDescriptor()
                                                .getListenerType().getName();
            ListenerInfo lInfo = (ListenerInfo)
                                 usedListeners.get(listenerTypeName);
            if (lInfo != null && --lInfo.useCount == 0)
                usedListeners.remove(listenerTypeName);

            event.setInCEDL(false);
        }
    }

    private boolean addEventToMethod(Event event) {
        String methodName = fullMethodName(event.getListenerMethod());
        List eventList = (List) usedMethods.get(methodName);

        if (eventList == null) {
            eventList = new ArrayList();
            eventList.add(event);
            usedMethods.put(methodName, eventList);
        }
        else {
            for (Iterator it=eventList.iterator(); it.hasNext(); ) {
                Event e = (Event) it.next();
                if (e.getComponent() == event.getComponent())
                    return false; // same event, or another event of the same
                                  // component with the same method signature
            }
            eventList.add(event);
        }

        return true;
    }

    private boolean removeEventFromMethod(Event event) {
        boolean removed;
        String methodName = fullMethodName(event.getListenerMethod());
        List eventList = (List) usedMethods.get(methodName);

        if (eventList != null) {
            removed = eventList.remove(event);
            if (eventList.size() == 0)
                usedMethods.remove(methodName);
        }
        else removed = false;

        return removed;
    }

    private void detachEventHandler(Event event, String handlerName) {
        List handlerEventList = (List) eventHandlers.get(handlerName);
        handlerEventList.remove(event);
        if (handlerEventList.size() == 0)
            eventHandlers.remove(handlerName); // handler is not used anymore

        formModel.fireEventHandlerRemoved(event,
                                          handlerName,
                                          handlerEventList.size() == 0);
    }

    private void checkCompatibility(Event event1, Event event2) {
        Method m1 = event1.getListenerMethod();
        Method m2 = event2.getListenerMethod();
        Class[] params1 = m1.getParameterTypes();
        Class[] params2 = m2.getParameterTypes();
        boolean ok;

        if (params1.length == params2.length) {
            ok = true;
            for (int i=0; i < params1.length; i++)
                if (!params1[i].getName().equals(params2[i].getName())) {
                    ok = false;
                    break;
                }
            if (ok)
                ok = m1.getReturnType().equals(m2.getReturnType());
        }
        else ok = false;

        if (!ok) {
            IllegalArgumentException iae =
                new IllegalArgumentException("Incompatible event"); // NOI18N
            org.openide.ErrorManager.getDefault().annotate(
                iae, FormUtils.getBundleString("MSG_CannotAttach")); // NOI18N
            throw iae;
        }
    }

    private static String fullMethodName(Method m) {
        StringBuffer name = new StringBuffer();
        name.append(m.getName());
        name.append("("); // NOI18N

        Class[] params = m.getParameterTypes();
        for (int i=0; i < params.length; i++) {
            name.append(params[i].getName());
            if (i+1 < params.length)
                name.append(", "); // NOI18N
        }

        name.append(")"); // NOI18N
        return name.toString();
    }

    // --------

    private static class ListenerInfo {
        Class listenerType;
        int useCount;
        ListenerInfo(Class listenerType) {
            this.listenerType = listenerType;
        }
    }
}
