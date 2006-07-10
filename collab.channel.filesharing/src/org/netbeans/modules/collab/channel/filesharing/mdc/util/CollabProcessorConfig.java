/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.util;

import com.sun.collablet.CollabException;

import org.openide.execution.*;

import java.io.*;

import java.lang.reflect.Method;

import java.net.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.mdc.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.configbean.*;
import org.netbeans.modules.collab.core.Debug;


/**
 * CollabProcessorConfig
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public abstract class CollabProcessorConfig extends CollabConfigVerifier {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* eventHandler Map */
    protected HashMap eventHandlerFactoryMap = new HashMap();

    /**
     * constructor
     *
     */
    public CollabProcessorConfig() {
        super();
    }

    /**
     * constructor
     *
     */
    public CollabProcessorConfig(String currentVersion) {
        super(currentVersion);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * init
     *
     */
    public abstract void init() throws CollabException;

    /**
     * init
     *
     */
    protected void init(String configURL) throws CollabException {
        loadConfig(configURL);
    }

    /**
     * getEventHandler
     *
     * @return        eventhandler for given event
     */
    public HashMap getEventHandlerFactory() {
        return this.eventHandlerFactoryMap;
    }

    /**
     * getEventHandler
     *
     * @return        eventhandler for given event
     */
    public EventHandlerFactory getEventHandlerFactory(String eventID) {
        return getEventHandlerFactory(eventID, getVersion());
    }

    /**
     * getEventHandler
     *
     * @return        eventhandler for given event
     */
    protected EventHandlerFactory getEventHandlerFactory(String eventID, String version) {
        HashMap versionMap = (HashMap) this.eventHandlerFactoryMap.get(eventID);

        return (EventHandlerFactory) versionMap.get(version);
    }

    /**
     *
     *
     */
    protected void loadConfig(String configURL) throws CollabException {
        if (configURL == null) {
            throw new IllegalArgumentException("config URL null: ");
        }

        CCollab collab = null;

        try {
            URL url = new URL(configURL);
            InputStream in = url.openStream();

            //create DOM
            collab = CCollab.read(in);
        } catch (java.net.MalformedURLException murlex) {
            throw new CollabException(murlex);
        } catch (javax.xml.parsers.ParserConfigurationException parsex) {
            throw new CollabException(parsex);
        } catch (org.xml.sax.SAXException saxx) {
            throw new CollabException(saxx);
        } catch (java.io.IOException iox) {
            throw new CollabException(iox);
        }

        if (collab == null) {
            throw new IllegalArgumentException("config load failed for: " + configURL);
        }

        //get config
        Config[] config = collab.getMdcConfig();

        for (int i = 0; i < config.length; i++) {
            //getconfigVersion
            String configVersion = config[i].getVersion();

            //get ProcessorConfig
            EventProcessorConfig processorConfig = config[i].getMdcEventProcessorConfig();

            //get all registered events
            RegisterEventHandler[] registerEventHandlers = processorConfig.getRegisterEventHandler();

            for (int j = 0; j < registerEventHandlers.length; j++) {
                RegisterEventHandler eventHandler = registerEventHandlers[j];
                String eventName = eventHandler.getEventName();
                String normalizedEventID = createUniqueNormalizedEventID(configVersion, eventName);
                EventHandlerInfo eventHandlerInfo = eventHandler.getEventHandlerInfo();
                String eventHandlerClassName = eventHandlerInfo.getHandlerClass();

                try {
                    ClassLoader cl = new NbClassLoader();
                    Class myClass = Class.forName(eventHandlerClassName, true, cl);
                    Method getDefaultMethod = findGetDefault(myClass);
                    EventHandlerFactory eventHandlerClassFactory = (EventHandlerFactory) getDefaultMethod.invoke(
                            null, new Object[] {  }
                        );
                    Debug.log(this, "normalized eventID: " + normalizedEventID); //NoI18n
                    Debug.log(this, "eventHandlerClassName: " + eventHandlerClassName); //NoI18n
                    addEventHandlerFactory(normalizedEventID, getVersion(), 
                    //eventHandlerClassFactory.createEventHandler(getContext()));
                    eventHandlerClassFactory
                    );
                } catch (ClassNotFoundException classNotFound) {
                    Debug.log(this, "ClassNotFound for normalized eventID: " + //NoI18n
                        normalizedEventID
                    ); //NoI18n
                    Debug.logDebugException(
                        "ClassNotFound for normalized eventID: " + //NoI18n
                        normalizedEventID, classNotFound, true
                    ); //NoI18n
                    throw new CollabException(classNotFound);
                } catch (java.lang.InstantiationException iex) {
                    throw new CollabException(iex);
                } catch (java.lang.IllegalAccessException iax) {
                    throw new CollabException(iax);
                } catch (ExceptionInInitializerError eiie) {
                    Throwable t = eiie.getException();

                    if (t instanceof IllegalStateException) {
                        throw new CollabException(t.getMessage());
                    } else if (t instanceof Exception) {
                        throw (CollabException) t;
                    } else {
                        throw new CollabException(t.toString());
                    }
                } catch (Exception ex) {
                    throw new CollabException(ex);
                }
            }
        }
    }

    /**
     *
     *
     */
    private static Method findGetDefault(Class clazz) throws Exception {
        Method[] methods = clazz.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("getDefault")) //NoI18n
             {
                return methods[i];
            }
        }

        Debug.log("CollabProcessorConfig", clazz.getName() + "::getDefault() method not found"); //NoI18n

        return null;
    }

    /**
     *
     *
     */
    private void addEventHandlerFactory(String eventID, String version, EventHandlerFactory eventHandlerFactory) {
        HashMap versionMap = null;

        if (!eventHandlerFactoryMap.containsKey(eventID)) {
            versionMap = new HashMap();
            eventHandlerFactoryMap.put(eventID, versionMap);
        } else {
            versionMap = (HashMap) eventHandlerFactoryMap.get(eventID); //.add(eventHandler);
        }

        if (!versionMap.containsKey(version)) {
            versionMap.put(version, eventHandlerFactory);
        }
    }
}
