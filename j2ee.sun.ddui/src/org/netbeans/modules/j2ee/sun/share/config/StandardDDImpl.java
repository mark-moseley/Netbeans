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


package org.netbeans.modules.j2ee.sun.share.config;

import javax.enterprise.deploy.model.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

public class StandardDDImpl implements DDBean {

    DDCommon proxy;

    void setProxy(DDCommon proxy) {
        this.proxy = proxy;
        proxy.container = this;
    }

    public StandardDDImpl(DDCommon proxy) {
        this.proxy = proxy;
        proxy.container = this;
    }

    public String getXpath() {
        return proxy.getXpath();
    }

    public DDBeanRoot getRoot() {
        return proxy.getRoot();
    }
    
    public DDBean[] getChildBean(String xpath) {
        return proxy.getChildBean(xpath);
    }
    
    public String[] getText(String xpath) {
        return proxy.getText(xpath);
    }
    
    public void addXpathListener(String xpath,XpathListener listener) {
        proxy.addXpathListener(xpath,listener);
    }
    
    public void removeXpathListener(String xpath,XpathListener listener) {
        proxy.removeXpathListener(xpath,listener);
    }
    
    public String getId() {
        return proxy.getId();
    }
    
    public String getText() {
        return proxy.getText();
    }
    
    public String[] getAttributeNames() {
        return null;
    }
    
    public String getAttributeValue(String name) {
        return null;
    }
    
    public ConfigBeanStorage[] getConfigBeans() {
        return proxy.getConfigBeans();
    }
    
    public J2eeModuleProvider getModuleProvider() {
        return proxy.getModuleProvider();
    }
}
