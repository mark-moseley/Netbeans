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

/**
 * WebServicePersistenceManager.java
 * @author  Winston Prakash
 */

package org.netbeans.modules.websvc.registry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Set;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;

import org.openide.ErrorManager;

import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.model.WebServiceDataPersistenceDelegate;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;

public class WebServicePersistenceManager implements ExceptionListener, org.netbeans.modules.websvc.registry.netbeans.PersistenceManagerInterface {

	private static final String SAXParserFactory_PROP = "javax.xml.parsers.SAXParserFactory"; // NOI18N
        private static final String SAX_PARSER = "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"; // NOI18N
	
        private File websvcDir = new File(System.getProperty("netbeans.user"), "websvc"); // NOI18N
	private File websvcRefFile = new File(websvcDir, "websvc_ref.xml"); // NOI18N	

	public WebServicePersistenceManager() {
	}
	
	public void load(ClassLoader cl) {
            //Thread.dumpStack();
            // System.out.println("WebServicePersistenceManager load called");
                
             WebServiceListModel wsListModel = WebServiceListModel.getInstance();
		if(websvcRefFile.exists()) {
                        String originalParserFactory = System.getProperty(SAXParserFactory_PROP);
			ClassLoader origClassLoader = null;
			XMLDecoder decoder = null;

			try {
				System.getProperties().put(SAXParserFactory_PROP, SAX_PARSER);

				origClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
				
				decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(websvcRefFile)));
                                Object o = decoder.readObject();
                                int wsDataNums = 0;
                                
                                if (o instanceof Integer) {
                                    wsDataNums = ((Integer)o).intValue();
                                } else {
                                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while loading in WS registry: " + o);
                                    return;
                                }

				for(int i = 0; i< wsDataNums; i++) {
					try {
						WebServiceData wsData = (WebServiceData) decoder.readObject();
						wsListModel.addWebService(wsData);
					} catch(Exception exc) {
						ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exc);
					}
				}

                                o = decoder.readObject();
                                int wsGroups = 0;
                                if (o instanceof Integer) {
                                    wsGroups = ((Integer)o).intValue();
                                } else {
                                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while loading in WS registry: " + o);
                                    return;
                                }
				
                                for(int i = 0; i< wsGroups; i++) {
					try {
						WebServiceGroup group = (WebServiceGroup) decoder.readObject();
						wsListModel.addWebServiceGroup(group);
					} catch(Exception ex) {
						ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
					}
				}
			} catch(Throwable thrown) {
				ErrorManager.getDefault().notify(ErrorManager.ERROR, thrown);
			} finally {
				// Restore the SAXParserFactor property that was changed, restore
				// this threads context classloader and close the decoder stream 
				// if it was opened.
                                System.getProperties().put(SAXParserFactory_PROP,originalParserFactory);
                            
				if(origClassLoader != null) {
					Thread.currentThread().setContextClassLoader(origClassLoader);
				}

				if(decoder != null) {
					decoder.close();
				}
			}
		}
	}

	public void save(ClassLoader cl) {
		//System.out.println("WebServicePersistenceManager save called");
		//System.out.println("No webservices " + WSListModel.getInstance().getWSList().size());
             WebServiceListModel wsListModel = WebServiceListModel.getInstance();
		ClassLoader origClassLoader = null;
		XMLEncoder encoder = null;
		
		try {
			if(!websvcDir.exists()) {
				websvcDir.mkdirs();
			}

			if(websvcRefFile.exists()) {
				websvcRefFile.delete();
			}

			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(websvcRefFile)));
			encoder.setExceptionListener(this);
			WebServiceDataPersistenceDelegate wsDelegate = new WebServiceDataPersistenceDelegate();
			encoder.setPersistenceDelegate(Class.forName("javax.xml.namespace.QName", false, cl), 
				new WebServiceDataPersistenceDelegate()); // NOI18N
			encoder.setPersistenceDelegate(Class.forName("com.sun.xml.rpc.wsdl.document.soap.SOAPStyle", false, cl), 
				new WebServiceDataPersistenceDelegate()); // NOI18N
			encoder.setPersistenceDelegate(Class.forName("com.sun.xml.rpc.wsdl.document.soap.SOAPUse", false, cl), 
				new WebServiceDataPersistenceDelegate()); // NOI18N
			Set wsDataSet = wsListModel.getWebServiceSet();
			encoder.writeObject(new Integer(wsDataSet.size()));
			Iterator iter = wsDataSet.iterator();
			
			origClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(cl);
			
			while(iter.hasNext()) {
				WebServiceData wsData = (WebServiceData) iter.next();
				encoder.writeObject(wsData);
			}
			
			Set wsGroupSet =  wsListModel.getWebServiceGroupSet();
			encoder.writeObject(new Integer(wsGroupSet.size()));
			iter = wsGroupSet.iterator();

			while(iter.hasNext()) {
				WebServiceGroup group = (WebServiceGroup) iter.next();
				encoder.writeObject(group);
			}
		} catch(Throwable thrown) {
			ErrorManager.getDefault().notify(ErrorManager.ERROR, thrown);
		} finally {
			// Restore this threads context classloader and close the encoder
			// stream if it was opened.
			if(origClassLoader != null) {
				Thread.currentThread().setContextClassLoader(origClassLoader);
			}
			
			if(encoder != null) {
				encoder.close();//was encoder
				encoder.flush();
			}
		}
	}

	public void exceptionThrown(Exception e) {
		e.printStackTrace();
	}
}
