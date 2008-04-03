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

package org.netbeans.core.startup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;

/**
 * A special DocumentBuilderFactory that delegates to other factories till
 * it finds one that can satisfy configured requirements.
 *
 * @author Petr Nejedly
 */
public class DOMFactoryImpl extends DocumentBuilderFactory {

    private static Class getFirst() {
        try {
            String name = System.getProperty("nb.backup." + Factory_PROP); // NOI18N
            return name == null ? null : Class.forName(name);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private Map<String, Object> attributes = new HashMap<String, Object>();
    
    /** The default property name according to the JAXP spec */
    private static final String Factory_PROP =
        "javax.xml.parsers.DocumentBuilderFactory"; // NOI18N

    public static void install() {
        System.getProperties().put(Factory_PROP,
                                   DOMFactoryImpl.class.getName());
    }

    static {
        if (getFirst() == null) {
            ClassLoader orig = Thread.currentThread().getContextClassLoader();
            // Not app class loader. only ext and bootstrap
            try {
               Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader().getParent());
               System.setProperty("nb.backup." + Factory_PROP, DocumentBuilderFactory.newInstance().getClass().getName()); // NOI18N
            } finally {
               Thread.currentThread().setContextClassLoader(orig);            
            }
        }
        
        DOMFactoryImpl.install();
        SAXFactoryImpl.install();
    }
    
    public java.lang.Object getAttribute(java.lang.String name) throws java.lang.IllegalArgumentException {
        return attributes.get(name);
    }
    
    public boolean getFeature (String name) {
        return Boolean.TRUE.equals (getAttribute (name));
    }
    
    public void setFeature(String name, boolean value) throws ParserConfigurationException {
        try {
            setAttribute (name, Boolean.valueOf (value));
        } catch (IllegalArgumentException ex) {
            ParserConfigurationException p = new ParserConfigurationException ();
            p.initCause (ex);
            throw p;
        }
    }
    
    

    public DocumentBuilder newDocumentBuilder() throws javax.xml.parsers.ParserConfigurationException {
        try {
            return tryCreate();
        } catch (IllegalArgumentException e) {
            throw (ParserConfigurationException) new ParserConfigurationException(e.toString()).initCause(e);
        }
    }


    public void setAttribute(java.lang.String name, java.lang.Object value) throws java.lang.IllegalArgumentException {
        attributes.put(name, value);
        try {
            tryCreate();
        } catch (ParserConfigurationException e) {
            throw (IllegalArgumentException) new IllegalArgumentException(e.toString()).initCause(e);
        }
    }
    
    private DocumentBuilder tryCreate() throws ParserConfigurationException, IllegalArgumentException {
        for (Iterator it = new LazyIterator(getFirst(), DocumentBuilderFactory.class, DOMFactoryImpl.class); it.hasNext(); ) {
            try {
                DocumentBuilder builder = tryCreate((Class)it.next());
                return builder;
            } catch (ParserConfigurationException e) {
                if (!it.hasNext()) throw e;
            } catch (IllegalArgumentException e) {
                if (!it.hasNext()) throw e;
            }
        }
        throw new IllegalStateException("Can't get here!"); // NOI18N
    }

    private DocumentBuilder tryCreate(Class delClass) throws ParserConfigurationException, IllegalArgumentException {
        Exception ex = null;
        try {
            DocumentBuilderFactory delegate = (DocumentBuilderFactory)delClass.newInstance();
            delegate.setNamespaceAware(isNamespaceAware());
            delegate.setValidating(isValidating());
            delegate.setIgnoringElementContentWhitespace(isIgnoringElementContentWhitespace());
            delegate.setExpandEntityReferences(isExpandEntityReferences());
            delegate.setIgnoringComments(isIgnoringComments());
            delegate.setCoalescing(isCoalescing());

            for (Iterator it = attributes.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                delegate.setAttribute((String)entry.getKey(), entry.getValue());
            }
            return delegate.newDocumentBuilder();
        } catch (InstantiationException e) {
            ex = e;
        } catch (IllegalAccessException e) {
            ex = e;
        }
        throw (ParserConfigurationException) new ParserConfigurationException("Broken factory").initCause(ex); // NOI18N
    }    
}
