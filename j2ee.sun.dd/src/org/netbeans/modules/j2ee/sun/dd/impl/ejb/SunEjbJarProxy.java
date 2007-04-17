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
package org.netbeans.modules.j2ee.sun.dd.impl.ejb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.impl.DDTreeWalker;
import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;


/**
 *
 * @author Nitya Doraisamy
 * @author Peter Williams
 */
public class SunEjbJarProxy implements SunEjbJar, RootInterfaceImpl {

    private SunEjbJar ejbJarRoot;
    private String version;
    private OutputProvider outputProvider;
    private int ddStatus;
    private SAXParseException error;    
    private List<PropertyChangeListener> listeners; 
        

    public SunEjbJarProxy(SunEjbJar ejbJarRoot, String version) {
        this.ejbJarRoot = ejbJarRoot;
        this.version = version;
        this.listeners = new ArrayList<PropertyChangeListener>();
    }

    public int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        return ejbJarRoot==null?-1:ejbJarRoot.addSecurityRoleMapping(securityRoleMapping);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans getEnterpriseBeans() {
        return ejbJarRoot==null?null:ejbJarRoot.getEnterpriseBeans();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping() {
        return ejbJarRoot==null?null:ejbJarRoot.getSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int param) {
        return ejbJarRoot==null?null:ejbJarRoot.getSecurityRoleMapping(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans newEnterpriseBeans() {
        if(ejbJarRoot == null)
            return null;
        else
            return ejbJarRoot.newEnterpriseBeans();
    }

    public int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        return ejbJarRoot==null?-1:ejbJarRoot.removeSecurityRoleMapping(securityRoleMapping);
    }

    public void setEnterpriseBeans(org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans enterpriseBeans) {
        if (ejbJarRoot!=null) ejbJarRoot.setEnterpriseBeans(enterpriseBeans);
    }

    public void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] securityRoleMapping) {
        if (ejbJarRoot!=null) ejbJarRoot.setSecurityRoleMapping(securityRoleMapping);
    }

    public void setSecurityRoleMapping(int param, org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        if (ejbJarRoot!=null) ejbJarRoot.setSecurityRoleMapping(param, securityRoleMapping);
    }

    public int sizeSecurityRoleMapping() {
         return ejbJarRoot==null?-1:ejbJarRoot.sizeSecurityRoleMapping();
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        if (ejbJarRoot != null) 
            ejbJarRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
         if (ejbJarRoot != null) 
            ejbJarRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void setVersion(java.math.BigDecimal version) {
        String newVersion = version.toString();
        String currentVersion = null;
        if (this.version.equals(newVersion))
            return;
        if (ejbJarRoot != null) {
            Document document = null;
            if (ejbJarRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar)ejbJarRoot).graphManager().getXmlDocument();
                currentVersion = SunEjbJar.VERSION_2_0_0;
            }else if (ejbJarRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar)ejbJarRoot).graphManager().getXmlDocument();
                currentVersion = SunEjbJar.VERSION_2_1_0;
            }else if (ejbJarRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar)ejbJarRoot).graphManager().getXmlDocument();
                currentVersion = SunEjbJar.VERSION_2_1_1;
            }else if (ejbJarRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar)ejbJarRoot).graphManager().getXmlDocument();
                currentVersion = SunEjbJar.VERSION_3_0_0;
            }
            
            //remove the doctype
            document = removeDocType(document);
            
            if(newVersion.equals(SunEjbJar.VERSION_3_0_0)){
                //This will always be an upgrade
                generate3_00Graph(document);
            }
            if(newVersion.equals(SunEjbJar.VERSION_2_1_1)){
                if(currentVersion.equals(SunEjbJar.VERSION_2_1_0) || currentVersion.equals(SunEjbJar.VERSION_2_0_0))
                    generate2_11Graph(document);
                else
                    downgradeEjbJarGraph(document, newVersion, currentVersion);
            }
            if(newVersion.equals(SunEjbJar.VERSION_2_1_0)){
                if(currentVersion.equals(SunEjbJar.VERSION_2_0_0))
                    generate2_10Graph(document);
                else
                    downgradeEjbJarGraph(document, newVersion, currentVersion);
            }
            if(newVersion.equals(SunEjbJar.VERSION_2_0_0)){
                //This will always be a downgrade
                downgradeEjbJarGraph(document, newVersion, currentVersion);
            }
        }
    }

    private Document removeDocType(Document document){
        if (document != null) {
            org.w3c.dom.Element docElement = document.getDocumentElement();
            if (docElement != null) {
                org.w3c.dom.DocumentType docType = document.getDoctype();
                if (docType != null) {
                    document.removeChild(docType); //NOI18N
                }
            }
        }
        return document;
    } 
    
    private void downgradeEjbJarGraph(Document document, String downgradeVersion, String currentVersion){
            DDTreeWalker downgradeScanner = new DDTreeWalker(document, downgradeVersion, currentVersion);
            downgradeScanner.downgradeSunEjbJarDocument();
            if(downgradeVersion.equals(SunEjbJar.VERSION_2_1_1)){
                generate2_11Graph(document);
            }else if(downgradeVersion.equals(SunEjbJar.VERSION_2_1_0)){
                generate2_10Graph(document);
            }else if(downgradeVersion.equals(SunEjbJar.VERSION_2_0_0)){
                generate2_00Graph(document);
            }
    }
    
    private void generate3_00Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar ejbGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar.createGraph(document);
        ejbGraph.changeDocType(DTDRegistry.SUN_EJBJAR_300_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_300_DTD_SYSTEM_ID);
        this.ejbJarRoot = ejbGraph;
    }
    
    private void generate2_11Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar ejbGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar.createGraph(document);
        ejbGraph.changeDocType(DTDRegistry.SUN_EJBJAR_211_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_211_DTD_SYSTEM_ID);
        this.ejbJarRoot = ejbGraph;
    }
    
    private void generate2_10Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar ejbGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar.createGraph(document);
        ejbGraph.changeDocType(DTDRegistry.SUN_EJBJAR_210_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_210_DTD_SYSTEM_ID);
        this.ejbJarRoot = ejbGraph;
    }
    
    private void generate2_00Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar ejbGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar.createGraph(document);
        ejbGraph.changeDocType(DTDRegistry.SUN_EJBJAR_200_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_200_DTD_SYSTEM_ID);
        this.ejbJarRoot = ejbGraph;
    }
    
    public java.math.BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }
    
    public void setOriginal(SunEjbJar ejbJarRoot) {
        if (this.ejbJarRoot != ejbJarRoot) {
            for (int i=0;i<listeners.size();i++) {
                PropertyChangeListener pcl = listeners.get(i);
                if (this.ejbJarRoot != null) {
                    this.ejbJarRoot.removePropertyChangeListener(pcl);
                }
                if (ejbJarRoot != null) {
                    ejbJarRoot.addPropertyChangeListener(pcl);
                }
            }
            this.ejbJarRoot = ejbJarRoot;
            if (ejbJarRoot != null) {
                setProxyVersion(ejbJarRoot.getVersion().toString());
            }
        }
    }
    
    public SunEjbJar getOriginal() {
        return ejbJarRoot;
    }
    
    public SAXParseException getError() {
        return error;
    }
    
    public void setError(SAXParseException error) {
        this.error=error;
    }    
    
    public void setProxyVersion(java.lang.String value) {
        if ((version==null && value!=null) || !version.equals(value)) {
            PropertyChangeEvent evt = new PropertyChangeEvent(
                    this, PROPERTY_VERSION, version, value); 
            version=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }
    
    public Object getValue(String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getValue(name);
    }
    
    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (ejbJarRoot!=null) {
            ejbJarRoot.write(os);
        }
    }

    public String dumpBeanNode() {
        if (ejbJarRoot!=null) 
            return ejbJarRoot.dumpBeanNode();
        else
            return null;
    }

    public void setValue(String name, Object[] value) {
        if (ejbJarRoot!=null) ejbJarRoot.setValue(name, value);
    }

    public Object[] getValues(String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getValues(name);
    }

    public void setValue(String name, int index, Object value) {
        if (ejbJarRoot!=null) ejbJarRoot.setValue(name, index, value);
    }

    public void setValue(String name, Object value) {
        if (ejbJarRoot!=null) ejbJarRoot.setValue(name, value);
    }

    public Object getValue(String name, int index) {
        return ejbJarRoot==null?null:ejbJarRoot.getValue(name, index);
    }

    public String getAttributeValue(String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getAttributeValue(name);
    }

    public int size(String name) {
        return ejbJarRoot==null?-1:ejbJarRoot.size(name);
    }

    public int addValue(String name, Object value) {
        return ejbJarRoot==null?-1:ejbJarRoot.addValue(name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return ejbJarRoot==null?null:ejbJarRoot.findPropertyValue(propName, value);
    }

    public int removeValue(String name, Object value) {
        return ejbJarRoot==null?-1:ejbJarRoot.removeValue(name, value);
    }

    public void write(java.io.Writer w) throws java.io.IOException, DDException {
        if (ejbJarRoot!=null) ejbJarRoot.write(w);
    }

    public void removeValue(String name, int index) {
        if (ejbJarRoot!=null) ejbJarRoot.removeValue(name, index);
    }

   public Object clone() {
        SunEjbJarProxy proxy = null;
        if (ejbJarRoot==null)
            proxy = new SunEjbJarProxy(null, version);
        else {
            SunEjbJar clonedSunEjb=(SunEjbJar)ejbJarRoot.clone();
            proxy = new SunEjbJarProxy(clonedSunEjb, version);
        }
        proxy.setError(error);
        return proxy;
    }

    public String getAttributeValue(String propName, String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getAttributeValue(propName, index, name);
    }

    public void setAttributeValue(String name, String value) {
         if (ejbJarRoot!=null) ejbJarRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (ejbJarRoot!=null) ejbJarRoot.setAttributeValue(propName, name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (ejbJarRoot!=null) ejbJarRoot.setAttributeValue(propName, index, name, value);
    }

    public CommonDDBean getPropertyParent(String name) {
        return ejbJarRoot.getPropertyParent(name);
    }

    public void merge(CommonDDBean root, int mode) {
        if (root != null) {
            if (root instanceof SunEjbJarProxy)
                ejbJarRoot.merge(((SunEjbJarProxy)root).getOriginal(), mode);
            else ejbJarRoot.merge(root, mode);
        }
    }
        
    public CommonDDBean cloneVersion(String version) {
        return ejbJarRoot == null ? null : ejbJarRoot.cloneVersion(version);
    }
   
    public int getStatus() {
        return ddStatus;
    }
    
    public void setStatus(int value) {
        if (ddStatus!=value) {
            PropertyChangeEvent evt = new PropertyChangeEvent(
                    this, PROPERTY_STATUS, new Integer(ddStatus), new Integer(value));
            ddStatus=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }
    
    public RootInterface getRootInterface() {
        return this;
    }
    
    public boolean hasOriginal() {
        return getOriginal() != null;
    }
    
    
    /** Contract between friend modules that enables 
    * a specific handling of write(FileObject) method for targeted FileObject
    */
    public static interface OutputProvider {
        public void write(SunEjbJar ejbJarRoot) throws java.io.IOException;
    }
}
