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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import com.sun.collablet.CollabException;

import org.openide.execution.*;
import org.w3c.dom.*;

import java.lang.reflect.Method;

import java.net.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.util.XMLParser;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author Ayub Khan ayub.khan@sun.com
 */
public class FileHandlerMapParser extends XMLParser {
    static int indent = 0;

    /**
     * fills map with element entries from the xml file
     * returns the doctype public ID and URI namespaces.
     */
    public static CollabFileHandlerFactory[] getFileHandler(URL url)
    throws CollabException {
        CollabFileHandlerFactory[] filehanderFactories = null;

        /* layer.xml
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE filesystem PUBLIC "-//NetBeans//DTD Filesystem 1.0//EN" "http://www.netbeans.org/dtds/filesystem-1_0.dtd">
        <filesystem>
                <folder name="Services">
                        <folder name="Collaboration">
                                <folder name="Channels">
                                        <file name="com-sun-tools-ide-collab-channel-filesharing-FilesharingCollabletFactory.instance"/>
                                        <folder name="FileHandlers">
                                                <file name="com-sun-tools-ide-collab-channel-filesharing-filehandler-CollabTextFileHandlerFactory.instance"/>
                                                <file name="com-sun-tools-ide-collab-channel-filesharing-filehandler-CollabJavaFileHandlerFactory.instance"/>
                                                <file name="com-sun-tools-ide-collab-channel-filesharing-filehandler-CollabXMLFileHandlerFactory.instance"/>
                                                <file name="com-sun-tools-ide-collab-channel-filesharing-filehandler-CollabFormFileHandlerFactory.instance"/>
                                                <file name="com-sun-tools-ide-collab-channel-filesharing-filehandler-CollabDefaultFileHandlerFactory.instance"/>
                                        </folder>
                                        <folder name="Filesystem">
                                                <file name="com-sun-tools-ide-collab-channel-filesharing-filesystem-CollabFilesystemManager.instance"/>
                                        </folder>
                                </folder>
                        </folder>
                </folder>
        </filesystem>
         */

        // Check for the basic protocol wrapper
        Element root = parse(url);

        String sDoctype = root.getAttribute("publicID"); // NOI18N
        String namespace = root.getAttribute("nsURI"); // NOI18N

        if ((sDoctype == null) && (namespace == null)) {
            throw new IllegalArgumentException("missing doctype attribute!"); // NOI18N
        }

        if (!root.getTagName().equals("filesystem")) {
            throw new IllegalArgumentException("Illegal metadata format");
        }

        // Process the actual message
        NodeList servicesList = root.getElementsByTagName("folder");

        for (int i = 0; i < servicesList.getLength(); i++) {
            Element services = (Element) servicesList.item(i);
            String elementNamei = services.getAttribute("name");

            if ((elementNamei != null) && elementNamei.equals("Services")) {
                NodeList collaborationList = services.getElementsByTagName("folder");

                for (int j = 0; j < collaborationList.getLength(); j++) {
                    Element collaboration = (Element) collaborationList.item(j);
                    String elementNamej = collaboration.getAttribute("name");

                    if ((elementNamej != null) && elementNamej.equals("Collaboration")) {
                        NodeList channelsList = collaboration.getElementsByTagName("folder");

                        for (int k = 0; k < channelsList.getLength(); k++) {
                            Element channels = (Element) channelsList.item(k);
                            String elementNamek = channels.getAttribute("name");

                            if ((elementNamek != null) && elementNamek.equals("Channels")) {
                                NodeList fileHandlersList = channels.getElementsByTagName("folder");

                                for (int l = 0; l < fileHandlersList.getLength(); l++) {
                                    Element fileHandlers = (Element) fileHandlersList.item(l);
                                    String elementNamel = fileHandlers.getAttribute("name");

                                    if ((elementNamel != null) && elementNamel.equals("FileHandlers")) {
                                        NodeList fileList = fileHandlers.getElementsByTagName("file");
                                        List filehanderFactoryList = new ArrayList();

                                        for (int m = 0; m < fileList.getLength(); m++) {
                                            Element fileHandlerFactory = (Element) fileList.item(m);
                                            String eventHandlerClassName = fileHandlerFactory.getAttribute("name");

                                            if (
                                                (eventHandlerClassName == null) ||
                                                    (eventHandlerClassName.trim().length() == 0)
                                            ) {
                                                continue;
                                            }

                                            eventHandlerClassName = eventHandlerClassName.substring(
                                                    0, eventHandlerClassName.indexOf('.')
                                                );
                                            eventHandlerClassName = eventHandlerClassName.replace('-', '.');

                                            try {
                                                ClassLoader cl = new NbClassLoader();
                                                Class myClass = Class.forName(eventHandlerClassName, true, cl);
                                                Method getDefaultMethod = findGetDefault(myClass);
                                                CollabFileHandlerFactory eventHandlerClassFactory = (CollabFileHandlerFactory) getDefaultMethod.invoke(
                                                        null, new Object[] {  }
                                                    );
                                                Debug.log(
                                                    "FileHandlerMapParser",
                                                    "FileHandlerMapParser, adding Class: " + //NoI18n	
                                                    eventHandlerClassFactory.getID()
                                                );
                                                filehanderFactoryList.add(eventHandlerClassFactory);
                                            } catch (ClassNotFoundException classNotFound) {
                                                Debug.log(
                                                    "FileHandlerMapParser", //NoI18n
                                                    "ClassNotFound for filehandler Factory: " + //NoI18n
                                                    eventHandlerClassName
                                                );
                                                Debug.logDebugException(
                                                    "FileHandlerMapParser, ClassNotFound for filehandler Factory: " + //NoI18n	
                                                    eventHandlerClassName, classNotFound, true
                                                );
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

                                        filehanderFactories = (CollabFileHandlerFactory[]) filehanderFactoryList.toArray(
                                                new CollabFileHandlerFactory[0]
                                            );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return filehanderFactories;
    }

    /**
     *
     *
     */
    private static Method findGetDefault(Class clazz) throws Exception {
        Method[] methods = clazz.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("getDefaultNoLookup")) //NoI18n
             {
                return methods[i];
            }
        }

        return null;
    }
}
