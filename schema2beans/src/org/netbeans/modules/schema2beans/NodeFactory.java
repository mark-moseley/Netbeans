/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;


/**
 *  Where schema2beans asks to create DOM Node elements
 */
public class NodeFactory {
    Document 	factory;
    
    NodeFactory(Document doc) {
	this.factory = doc;
    }
    
    Node createElement(BeanProp prop) {
	return this.factory.createElement(prop.getDtdName());
    }
    
    Node createText() {
	return this.factory.createTextNode("");	// NOI18N
    }
    
}



