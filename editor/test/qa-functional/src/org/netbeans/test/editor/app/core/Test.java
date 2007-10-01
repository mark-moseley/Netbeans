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
package org.netbeans.test.editor.app.core;

import java.beans.*;
import java.util.ArrayList;
import javax.swing.*;

import java.util.Vector;
import java.util.Collection;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.actions.ActionRegistry;
import org.netbeans.test.editor.app.core.cookies.PerformCookie;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.MultiLineStringProperty;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.core.properties.StringProperty;
import org.netbeans.test.editor.app.gui.QuestionDialog;
import org.netbeans.test.editor.app.gui.actions.TreeNewType;
import org.netbeans.test.editor.app.util.ParsingUtils;
//import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;


/**
 *
 * @author  ehucka
 * @version
 */
public class Test extends TestGroup {
    
    public static final String AUTHOR = "Author";
    public static final String VERSION = "Version";
    public static final String COMMENT = "Comment";
    
    private String author,version,comment;
    
    public JEditorPane editor;
    public Logger logger;
    
    private static boolean testing = false; /*Are we inside of test suite?*/
    
    /** Creates new Test */
    public Test(String name) {
	super(name);
	if ((author=System.getProperty("user.name")) == null) {
	    author = "NoAuthor";
	}
	version = "1.0";
	comment = "";
    }
    
    public Test(Element node) {
	super(node);
	author = node.getAttribute(AUTHOR);
	version = node.getAttribute(VERSION);
	comment = ParsingUtils.loadString(node, COMMENT);
	logger = new Logger(Main.frame.getEditor());
    }
    
    public Element toXML(Element node) {
	node = super.toXML(node);
	node.setAttribute(AUTHOR, author);
	node.setAttribute(VERSION, version);
	return ParsingUtils.saveString(node, COMMENT, comment);
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
	super.fromXML(node);
	author = node.getAttribute(AUTHOR);
	version = node.getAttribute(VERSION);
	comment = ParsingUtils.loadString(node, COMMENT);
    }
    
    public Properties getProperties() {
	Properties ret=super.getProperties();
	ret.put(AUTHOR, new StringProperty(author));
	ret.put(VERSION, new StringProperty(version));
	ret.put(COMMENT, new MultiLineStringProperty(comment));
	return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
	if (name.compareTo(AUTHOR) == 0) {
	    return new StringProperty(author);
	} else if (name.compareTo(VERSION) == 0) {
	    return new StringProperty(version);
	} else if (name.compareTo(COMMENT) == 0) {
	    return new MultiLineStringProperty(comment);
	} else {
	    return super.getProperty(name);
	}
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
	if (name.compareTo(AUTHOR) == 0) {
	    setAuthor(((StringProperty)(value)).getProperty());
	} else if (name.compareTo(VERSION) == 0) {
	    setVersion(((StringProperty)(value)).getProperty());
	} else if (name.compareTo(COMMENT) == 0) {
	    setComment(((MultiLineStringProperty)(value)).getProperty());
	} else {
	    super.setProperty(name,value);
	}
    }
    
    public JEditorPane getEditor() {
	return editor;
    }
    
    public static void setTesting() {
	testing = true;
    }
    
    public static boolean isTesting() {
	return testing;
    }
    
    public Logger getLogger() {
	return logger;
    }
    
    public String getAuthor() {
	return author;
    }
    
    public void setAuthor(String value) {
	String oldValue = author;
	author = value;
	firePropertyChange(AUTHOR, oldValue, author);
    }
    
    public String getVersion() {
	return version;
    }
    
    public void setVersion(String value) {
	String oldValue = version;
	version = value;
	firePropertyChange(VERSION, oldValue, version);
    }
    
    public String getComment() {
	return comment;
    }
    
    public void setComment(String value) {
	String oldValue = comment;
	comment = value;
	firePropertyChange(COMMENT, oldValue, comment);
    }
    
    public void rebuidlLoggers() {
	Test t;
	for(int i=0;i < getChildCount();i++) {
	    if (get(i) instanceof TestSubTest) {
		t=(Test)(get(i));
		if (t.logger == null)
		    t.logger=logger;
	    }
	}
    }
    
    public void perform() {
	TestNode n;
	
	System.err.println("\nTest "+getName()+" starts performing.");
	isPerforming=true;
	for(int i=0;i < getChildCount();i++) {
	    if (!isPerforming) break;
	    n=get(i);
	    if (n instanceof TestAction ) {
		n.perform();
	    } else {
		if (n instanceof TestSubTest) {
		    n.perform();
		}
	    }
	}
	isPerforming=false;
    }
    
    public void stop() {
	getLogger().stopPerforming();
	isPerforming=false;
    }
    
    //******************************************************************************
    
    public TestCallAction[] getCallActions() {
	ArrayList ret=new ArrayList();
	TestNode n,n2;
	TestGroup g;
	for (int i=0;i < getChildCount();i++) {
	    n=(TestNode)(get(i));
	    if (n instanceof TestCallAction) {
		ret.add(n);
	    } else if (n instanceof TestSubTest) {
		g=(TestGroup)(n);
		for (int j=0;j < g.getChildCount();j++) {
		    n2=(TestNode)(g.get(j));
		    if (n2 instanceof TestCallAction) {
			ret.add(n2);
		    }
		}
	    }
	}
	return (TestCallAction[])(ret.toArray(new TestCallAction[] {}));
    }
    
    protected void registerNewTypes() {
	ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSubTest.class);
	ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestStep.class);
	ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestCallAction.class);
    }
    
    protected void registerCookies() {
	getCookieSet().put(PerformCookie.class,new PerformCookie() {
	    public void perform() {
		Test.this.perform();
	    }
	    
	    public boolean isPerforming() {
		return Test.this.isPerforming;
	    }
	});
    }
}