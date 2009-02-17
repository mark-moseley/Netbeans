/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.pom.impl;

import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;	

/**
 *
 * @author mkleint
 */
public class DeveloperImpl extends IdPOMComponentImpl implements Developer {

    public DeveloperImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public DeveloperImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().DEVELOPER));
    }
    public String getUrl() {
        return getChildElementText(getModel().getPOMQNames().URL.getQName());
    }

    public void setUrl(String url) {
        setChildElementText(getModel().getPOMQNames().URL.getName(), url,
                getModel().getPOMQNames().URL.getQName());
    }

    public String getName() {
        return getChildElementText(getModel().getPOMQNames().NAME.getQName());
    }

    public void setName(String name) {
        setChildElementText(getModel().getPOMQNames().NAME.getName(), name,
                getModel().getPOMQNames().NAME.getQName());
    }

    public String getEmail() {
        return getChildElementText(getModel().getPOMQNames().EMAIL.getQName());
    }

    public void setEmail(String email) {
        setChildElementText(getModel().getPOMQNames().EMAIL.getName(), email,
                getModel().getPOMQNames().EMAIL.getQName());
    }

    public String getOrganization() {
        return getChildElementText(getModel().getPOMQNames().ORGANIZATION.getQName());
    }

    public void setOrganization(String organization) {
        setChildElementText(getModel().getPOMQNames().ORGANIZATION.getName(), organization,
                getModel().getPOMQNames().ORGANIZATION.getQName());
    }

    public String getOrganizationUrl() {
        return getChildElementText(getModel().getPOMQNames().ORGANIZATIONURL.getQName());
    }

    public void setOrganizationUrl(String url) {
        setChildElementText(getModel().getPOMQNames().ORGANIZATIONURL.getName(), url,
                getModel().getPOMQNames().ORGANIZATIONURL.getQName());
    }

    public String getTimezone() {
        return getChildElementText(getModel().getPOMQNames().TIMEZONE.getQName());
    }

    public void setTimezone(String zone) {
        setChildElementText(getModel().getPOMQNames().TIMEZONE.getName(), zone,
                getModel().getPOMQNames().TIMEZONE.getQName());
    }

    // attributes

    // child elements
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public static class List extends ListImpl<Developer> {
        public List(POMModel model, Element element) {
            super(model, element, model.getPOMQNames().DEVELOPER, Developer.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, model.getPOMQNames().DEVELOPERS));
        }
    }


}