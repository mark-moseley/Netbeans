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
package org.netbeans.modules.maven.model.settings.impl;

import org.netbeans.modules.maven.model.settings.Activation;
import org.netbeans.modules.maven.model.settings.Mirror;
import org.netbeans.modules.maven.model.settings.ModelList;
import org.netbeans.modules.maven.model.settings.Properties;
import org.netbeans.modules.maven.model.settings.Repository;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class MirrorImpl extends SettingsComponentImpl implements Mirror {

    private static final Class<? extends SettingsComponent>[] ORDER = new Class[] {
    };

    public MirrorImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public MirrorImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().MIRROR));
    }

    // attributes

    // child elements


    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

    public String getName() {
        return getChildElementText(getModel().getSettingsQNames().NAME.getQName());
    }

    public void setName(String name) {
        setChildElementText(getModel().getSettingsQNames().NAME.getName(), name,
                getModel().getSettingsQNames().NAME.getQName());
    }



    public String getUrl() {
        return getChildElementText(getModel().getSettingsQNames().URL.getQName());
    }

    public void setUrl(String url) {
        setChildElementText(getModel().getSettingsQNames().URL.getName(), url,
                getModel().getSettingsQNames().URL.getQName());
    }

    public String getMirrorOf() {
        return getChildElementText(getModel().getSettingsQNames().MIRROROF.getQName());
    }

    public void setMirrorOf(String mirrorof) {
        setChildElementText(getModel().getSettingsQNames().MIRROROF.getName(), mirrorof,
                getModel().getSettingsQNames().MIRROROF.getQName());
    }

    public String getId() {
        return getChildElementText(getModel().getSettingsQNames().ID.getQName());
    }

    public void setId(String id) {
        setChildElementText(getModel().getSettingsQNames().ID.getName(), id,
                getModel().getSettingsQNames().ID.getQName());
    }


    public static class List extends ListImpl<Mirror> {
        public List(SettingsModel model, Element element) {
            super(model, element, model.getSettingsQNames().MIRROR, Mirror.class);
        }

        public List(SettingsModel model) {
            this(model, createElementNS(model, model.getSettingsQNames().MIRRORS));
        }
    }


}