/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.monitor.data;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import org.netbeans.modules.web.monitor.client.TransactionNode;

public class DispatchData extends BaseBean implements DataRecord {

    private final static boolean debug = false;

    static Vector comparators = new Vector();

    static public final String CLIENTDATA = "ClientData";   // NOI18N
    static public final String SESSIONDATA = "SessionData"; // NOI18N
    static public final String COOKIESDATA = "CookiesData"; // NOI18N
    static public final String REQUESTDATA = "RequestData"; // NOI18N
    static public final String SERVLETDATA = "ServletData"; // NOI18N
    static public final String CONTEXTDATA = "ContextData"; // NOI18N
    static public final String ENGINEDATA = "EngineData"; // NOI18N
    static public final String DISPATCHES = "Dispatches"; // NOI18N

    public DispatchData() {
	this(Common.USE_DEFAULT_VALUES);
    }
    
    public DispatchData(int options) {
	super(comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 6));
	this.createProperty("ClientData", CLIENTDATA,  // NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    ClientData.class);
	this.createAttribute(CLIENTDATA, "protocol", "Protocol", // NOI18N 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(CLIENTDATA, "remoteAddress", "RemoteAddress", // NOI18N 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(CLIENTDATA, "software", "Software",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "locale", "Locale",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "formatsAccepted", "FormatsAccepted",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "encodingsAccepted", "EncodingsAccepted",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "charsetsAccepted", "CharsetsAccepted",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("SessionData", SESSIONDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    SessionData.class);
	this.createAttribute(SESSIONDATA, "before", "Before",  // NOI18N
			     AttrProp.ENUM | AttrProp.REQUIRED,
			     new String[] {
				 "false", // NOI18N
				 "true" // NOI18N
			     }, "false"); // NOI18N
	this.createAttribute(SESSIONDATA, "after", "After",  // NOI18N
			     AttrProp.ENUM | AttrProp.REQUIRED,
			     new String[] {
				 "false", // NOI18N
				 "true" // NOI18N
			     }, "false"); // NOI18N
	this.createAttribute(SESSIONDATA, "id", "Id",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SESSIONDATA, "created", "Created",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("CookiesData", COOKIESDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    CookiesData.class);

	// PENDING - I think I need to move this to the request data
	// class to make it an independent class that can be handled
	// on its own (if I want to pass less data about). 
	this.createProperty("RequestData", REQUESTDATA,  // NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    RequestData.class);
	this.createAttribute(REQUESTDATA, "uri", "Uri",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(REQUESTDATA, "method", "Method",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(REQUESTDATA, "urlencoded", "Urlencoded",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "queryString", "QueryString",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "replace", "Replace",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "protocol", "Protocol",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "ipaddress", "Ipaddress",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "scheme", "Scheme",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "status", "Status",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createProperty("ServletData", SERVLETDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    ServletData.class);
	this.createAttribute(SERVLETDATA, "name", "Name",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "className", "ClassName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "packageName", "PackageName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "servletInfo", "ServletInfo",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "relPath", "RelPath",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "transPath", "TransPath",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "contextName", "ContextName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "absPath", "AbsPath",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "jre", "Jre",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "platform", "Platform",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "serverPort", "ServerPort",  // NOI18N
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "serverName", "ServerName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createAttribute(SERVLETDATA, "collected", "Collected",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createProperty("ContextData", CONTEXTDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    ContextData.class);
	this.createAttribute(CONTEXTDATA, "contextName", "ContextName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CONTEXTDATA, "absPath", "AbsPath",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createProperty("EngineData", ENGINEDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    EngineData.class);

	this.createAttribute(ENGINEDATA, "jre", "Jre",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(ENGINEDATA, "platform", "Platform",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(ENGINEDATA, "serverPort", "ServerPort",  // NOI18N
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(ENGINEDATA, "serverName", "ServerName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("Dispatches", DISPATCHES,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    Dispatches.class);


	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {
    }

    // This attribute is mandatory
    public void setClientData(ClientData value) {
	this.setValue(CLIENTDATA, value);
    }

    //
    public ClientData getClientData() {
	return (ClientData)this.getValue(CLIENTDATA);
    }

    // This attribute is optional
    public void setSessionData(SessionData value) {
	this.setValue(SESSIONDATA, value);
    }

    //
    public SessionData getSessionData() {
	return (SessionData)this.getValue(SESSIONDATA);
    }

    // This attribute is optional
    public void setCookiesData(CookiesData value) {
	this.setValue(COOKIESDATA, value);
    }

    //
    public CookiesData getCookiesData() {
	return (CookiesData)this.getValue(COOKIESDATA);
    }

    // This attribute is optional
    public void setDispatches(Dispatches value) {
	this.setValue(DISPATCHES, value);
    }

    //
    public Dispatches getDispatches() {
	if(debug) System.out.println("Running getDispatches"); //NOI18N
	return (Dispatches)this.getValue(DISPATCHES);
    }

    // This attribute is mandatory
    public void setRequestData(RequestData value) {
	this.setValue(REQUESTDATA, value);
    }

    //
    public RequestData getRequestData() {
	return (RequestData)this.getValue(REQUESTDATA);
    }

    // This attribute is optional
    public void setServletData(ServletData value) {
	this.setValue(SERVLETDATA, value);
    }

    //
    public ServletData getServletData() {
	return (ServletData)this.getValue(SERVLETDATA);
    }

    // This attribute is optional
    public void setEngineData(EngineData value) {
	this.setValue(ENGINEDATA, value);
    }

    //
    public EngineData getEngineData() {
	return (EngineData)this.getValue(ENGINEDATA);
    }

    // This attribute is optional
    public void setContextData(ContextData value) {
	this.setValue(CONTEXTDATA, value);
    }

    //
    public ContextData getContextData() {
	return (ContextData)this.getValue(CONTEXTDATA);
    }

    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    public String getServerAndPort() 
    {
	String server = null;
	String port = null;
		
	try {
	    server = getEngineData().getAttributeValue("serverName"); // NOI18N
	    port = getEngineData().getAttributeValue("serverPort"); // NOI18N
	    return server.concat(":").concat(port); //NOI18N
	}
	catch(NullPointerException npe) {
	}
	    
	// Backwards compatibility
	server = getServletData().getAttributeValue("serverName"); // NOI18N
	port = getServletData().getAttributeValue("serverPort"); // NOI18N
	return server.concat(":").concat(port); //NOI18N
    }


    public String getServerName() {
	
	try {
	    return getEngineData().getAttributeValue("serverName"); // NOI18N
	}
	catch(NullPointerException npe) {
	}
	    
	// Backwards compatibility
	return getServletData().getAttributeValue("serverName"); // NOI18N
    }

    public int getServerPort() {
	
	String portS = null;
	try {
	    portS = getEngineData().getAttributeValue("serverPort"); // NOI18N
	}
	catch(NullPointerException npe) {
	}
	if(portS == null) 
	    portS = getServletData().getAttributeValue("serverPort"); // NOI18N 
	return Integer.parseInt(portS);
    }


    public String getServerPortAsString() {
	
	String portS = null;
	try {
	    portS = getEngineData().getAttributeValue("serverPort"); // NOI18N
	}
	catch(NullPointerException npe) {
	}
	if(portS == null) 
	    portS = getServletData().getAttributeValue("serverPort"); // NOI18N 
	return portS;
	
	
    }


    public void setServerName(String server) {
	
	try {
	    getEngineData().setAttributeValue("serverName", server); // NOI18N
	    return;
	}
	catch(NullPointerException npe) {
	}
	    
	try {
	    getServletData().getAttributeValue("serverName, server"); // NOI18N
	    return;
	}
	catch(NullPointerException npe) {
	}
    }

    public void setServerPort(int port) {
	
	try {
	    getEngineData().setAttributeValue("serverPort",  // NOI18N
					      String.valueOf(port));
	    return;
	}
	catch(NullPointerException npe) {
	}
	

	try {
	    getServletData().setAttributeValue("serverPort",  // NOI18N
					      String.valueOf(port));
	    return;
	}
	catch(NullPointerException npe) {
	}
    }

    public void setServerPort(String port) {
	
	try {
	    getEngineData().setAttributeValue("serverPort",  // NOI18N
					      port); 
	    return;
	}
	catch(NullPointerException npe) {
	}
	

	try {
	    getServletData().setAttributeValue("serverPort",  // NOI18N
					       port);
	    return;
	}
	catch(NullPointerException npe) {
	}
    }
    

    //
    static public void addComparator(BeanComparator c) {
	MonitorData.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c) {
	MonitorData.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l) {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l) {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l){
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n,
					     PropertyChangeListener l) {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
	String s;
	BaseBean n;
	str.append(indent);
	str.append("ClientData"); // NOI18N
	n = this.getClientData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(CLIENTDATA, 0, str, indent);

	str.append(indent);
	str.append("SessionData"); // NOI18N
	n = this.getSessionData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(SESSIONDATA, 0, str, indent);

	str.append(indent);
	str.append("CookiesData"); // NOI18N
	n = this.getCookiesData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(COOKIESDATA, 0, str, indent);

	str.append(indent);
	str.append("RequestData"); // NOI18N
	n = this.getRequestData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(REQUESTDATA, 0, str, indent);

	str.append(indent);
	str.append("ServletData"); // NOI18N
	n = this.getServletData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(SERVLETDATA, 0, str, indent);

	str.append("ContextData"); // NOI18N
	n = this.getContextData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(CONTEXTDATA, 0, str, indent);

	str.append(indent);
	str.append("Dispatches");	// NOI18N
	n = this.getDispatches();
	if (n != null)
	    n.dump(str, indent + "\t");	// NOI18N
	else
	    str.append(indent+"\tnull");	// NOI18N
	this.dumpAttributes(DISPATCHES, 0, str, indent);
    }

    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("MonitorData\n"); // NOI18N
	this.dump(str, "\n  "); // NOI18N
	return str.toString();
    }
}
