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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.options;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.util.Mutex;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sergey Grinev
 */
public class CodeAssistanceOptions {
    
    private AuxiliaryConfiguration aux;
    
    private static final String CodeAssistanceData = "code-assistance-data"; //NOI18N
    private static final String CodeModelEnabled = "code-model-enabled"; //NOI18N
    private static final String ParseOrphanEnabled = "parse-orphan-enabled"; //NOI18N
    
    private final String namespace;
    private final boolean shared;
    
    // constructors
    
    public CodeAssistanceOptions(Project project, boolean shared) {
        this.shared = shared;
        aux = ((AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class));
        
        AntBasedProjectType antPrj = ((AntBasedProjectType) project.getLookup().lookup(AntBasedProjectType.class));
        namespace = antPrj.getPrimaryConfigurationDataElementNamespace(shared);
    }
    
    public CodeAssistanceOptions(Project project) {
        this(project, false);
    }
    
    // options
    
    public Boolean getCodeAssistanceEnabled() {
        String value = doLoad(CodeModelEnabled);
        return !value.equals("") && value != null ? new Boolean(value) : Boolean.TRUE;
    }
    
    public void setCodeAssistanceEnabled(Boolean enabled) {
        doSave(CodeModelEnabled, enabled.toString());
    }

    public Boolean getParseOrphanEnabled() {
        String value = doLoad(ParseOrphanEnabled);
        return !value.equals("") && value != null ? new Boolean(value) : Boolean.TRUE;
    }
    
    public void setParseOrphanEnabled(Boolean enabled) {
        doSave(ParseOrphanEnabled, enabled.toString());
    }
    
    // private methods
    
    private Element getConfigurationFragment() {
        Element data = aux.getConfigurationFragment(CodeAssistanceData, namespace, shared);
        if (data == null) {
            data = createDocument(namespace, shared ? "project" : "project-private").createElementNS(namespace, CodeAssistanceData); //NOI18N
        }
        if (data == null) {
            System.err.println("CodeAssistanceOptions: Failed to load and create configuration fragment (" +
                    CodeAssistanceData + " : " + namespace + ")"); //NOI18N
        }
        return data;
    }
    
    private Element getNode(Element configurationFragment, String name) {
        NodeList nodes = configurationFragment.getElementsByTagNameNS(namespace, name);
        Element node;
        if (nodes.getLength() == 0) {
            node = configurationFragment.getOwnerDocument().createElementNS(namespace, name);
            configurationFragment.appendChild(node);
        } else {
            node = (Element)nodes.item(0);
        }
        return node;
    }
    
    private String doLoad(final String name) {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element configurationFragment = getConfigurationFragment();
                if (configurationFragment == null) {
                    return null;
                }
                return getNode(configurationFragment, name).getTextContent();
            }
        });
    }
    
    private void doSave(final String name, final String value) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                Element configurationFragment = getConfigurationFragment();
                if (configurationFragment != null) {
                    Element el = getNode(configurationFragment, name);
                    el.setTextContent(value);
                    aux.putConfigurationFragment(configurationFragment, shared);
                }
            }
        });
    }
    
    // utility
    
    private static Document createDocument(String ns, String root) throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder().getDOMImplementation().createDocument(ns, root, null);
        } catch (ParserConfigurationException ex) {
            throw (DOMException)new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }
}

