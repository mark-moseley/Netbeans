/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.monitor.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

public class SessionData extends BaseBean {

    static Vector comparators = new Vector();

    static public final String SESSIONIN = "SessionIn"; //NOI18N
    static public final String SESSIONOUT = "SessionOut"; //NOI18N

    public SessionData() {
	this(Common.USE_DEFAULT_VALUES);
    }

    public SessionData(int options) {
	super(SessionData.comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 6));
	// Properties (see root bean comments for the bean graph)
	this.createProperty("SessionIn", SESSIONIN, //NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    SessionIn.class);
	this.createAttribute(SESSIONIN, 
			     "lastAccessed", //NOI18N
			     "LastAccessed", //NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SESSIONIN, 
			     "inactiveInterval", //NOI18N
			     "InactiveInterval",  //NOI18N
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("SessionOut", SESSIONOUT, //NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    SessionOut.class);
	this.createAttribute(SESSIONOUT, 
			     "lastAccessed", //NOI18N
			     "LastAccessed",  //NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SESSIONOUT, 
			     "inactiveInterval", //NOI18N
			     "InactiveInterval", //NOI18N
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {

    }

    // This attribute is mandatory
    public void setSessionIn(SessionIn value) {
	this.setValue(SESSIONIN, value);
    }

    //
    public SessionIn getSessionIn() {
	return (SessionIn)this.getValue(SESSIONIN);
    }

    // This attribute is mandatory
    public void setSessionOut(SessionOut value) {
	this.setValue(SESSIONOUT, value);
    }

    //
    public SessionOut getSessionOut() {
	return (SessionOut)this.getValue(SESSIONOUT);
    }

    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    //
    static public void addComparator(BeanComparator c) {
	SessionData.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c) {
	SessionData.comparators.remove(c);
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
    public void addPropertyChangeListener(String n, PropertyChangeListener l) {
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
	str.append("SessionIn");//NOI18N
	n = this.getSessionIn();
	if (n != null)
	    n.dump(str, indent + "\t"); //NOI18N
	else
	    str.append(indent+"\tnull"); //NOI18N
	this.dumpAttributes(SESSIONIN, 0, str, indent);

	str.append(indent);
	str.append("SessionOut"); //NOI18N
	n = this.getSessionOut();
	if (n != null)
	    n.dump(str, indent + "\t"); //NOI18N
	else
	    str.append(indent+"\tnull"); //NOI18N
	this.dumpAttributes(SESSIONOUT, 0, str, indent);

    }

    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("SessionData\n"); //NOI18N
	this.dump(str, "\n  "); //NOI18N
	return str.toString();
    }
}
