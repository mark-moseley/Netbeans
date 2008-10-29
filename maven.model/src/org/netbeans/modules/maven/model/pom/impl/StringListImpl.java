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

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;

/**
 *
 * @author mkleint
 */
public class StringListImpl extends POMComponentImpl implements StringList {
    private POMQName childname;

    protected StringListImpl(POMModel model, Element element, POMQName childs) {
        super(model, element);
        this.childname = childs;
    }

    public StringListImpl(POMModel model, POMQName listName, POMQName childs) {
        this(model, createElementNS(model, listName), childs);
    }


    // child elements
    public List<String> getListChildren() {
        List<POMExtensibilityElement> el = getChildren(POMExtensibilityElement.class);
        List<String> toRet = new ArrayList<String>();
        for (POMExtensibilityElement elem : el) {
            if (elem.getQName().getLocalPart().equals(childname.getQName().getLocalPart())) {
                toRet.add(elem.getElementText());
            }
        }
        return toRet.size() > 0 ? toRet : null;
    }

    public void addListChild(String child) {
        assert child != null;
        POMExtensibilityElement el = getModel().getFactory().createPOMExtensibilityElement(childname.getQName());
        el.setElementText(child);
        appendChild(childname.getName(), el);

    }

    public void removeListChild(String child) {
        assert child != null;
        List<POMExtensibilityElement> el = getChildren(POMExtensibilityElement.class);
        for (POMExtensibilityElement elem : el) {
            if (elem.getQName().getLocalPart().equals(childname.getQName().getLocalPart()) && child.equals(elem.getElementText())) {
                removeChild(childname.getName(), elem);
                return;
            }
        }
    }



    // child elements
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }


}