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
package org.netbeans.modules.maven.model.profile.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.profile.ProfilesComponent;
import org.netbeans.modules.maven.model.profile.ProfilesComponentVisitor;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesQName;
import org.netbeans.modules.maven.model.profile.Properties;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class PropertiesImpl extends ProfilesComponentImpl implements Properties {

    public PropertiesImpl(ProfilesModel model, Element element) {
        super(model, element);
    }
    
    public PropertiesImpl(ProfilesModel model) {
        this(model, createElementNS(model, model.getProfilesQNames().PROPERTIES));
    }

    // attributes

    // child elements


    public void setProperty(String key, String value) {
        QName qname = ProfilesQName.createQName(key, getModel().getProfilesQNames().isNSAware());
        setChildElementText(qname.getLocalPart(), value,
                qname);
    }

    public String getProperty(String key) {
        return getChildElementText(ProfilesQName.createQName(key, getModel().getProfilesQNames().isNSAware()));
    }

    public Map<String, String> getProperties() {
        Map<String, String> toRet = new HashMap<String, String>();
        List<ProfilesComponent> chlds = getChildren();
        for (ProfilesComponent pc : chlds) {
            Element el = pc.getPeer();
            String key = el.getLocalName();
            String val = getChildElementText(ProfilesQName.createQName(key, getModel().getProfilesQNames().isNSAware()));
            toRet.put(key, val);
        }
        return toRet;
    }

    public void accept(ProfilesComponentVisitor visitor) {
        visitor.visit(this);
    }

}