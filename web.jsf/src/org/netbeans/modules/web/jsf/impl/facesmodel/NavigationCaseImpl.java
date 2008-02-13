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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class NavigationCaseImpl extends DescriptionGroupImpl implements NavigationCase{
    
    protected static final List<String> NAVIGATION_CASE_SORTED_ELEMENTS = new ArrayList();
    static {
        NAVIGATION_CASE_SORTED_ELEMENTS.addAll(DescriptionGroupImpl.DESCRIPTION_GROUP_SORTED_ELEMENTS);
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.FROM_ACTION.getLocalName());
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.FROM_OUTCOME.getLocalName());
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.TO_VIEW_ID.getLocalName());
        NAVIGATION_CASE_SORTED_ELEMENTS.add(JSFConfigQNames.REDIRECT.getLocalName());
    }
    
    /** Creates a new instance of NavigationCaseImpl */
    public NavigationCaseImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public NavigationCaseImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.NAVIGATION_CASE));
    }
    
    public String getFromAction() {
        return getChildElementText(JSFConfigQNames.FROM_ACTION.getQName(getNamespaceURI()));
    }
    
    public void setFromAction(String fromAction) {
        setChildElementText(FROM_ACTION, fromAction, JSFConfigQNames.FROM_ACTION.getQName(getNamespaceURI()));
    }
    
    public String getFromOutcome() {
        return getChildElementText(JSFConfigQNames.FROM_OUTCOME.getQName(getNamespaceURI()));
    }
    
    public void setFromOutcome(String fromOutcome) {
        setChildElementText(FROM_OUTCOME, fromOutcome, JSFConfigQNames.FROM_OUTCOME.getQName(getNamespaceURI()));
    }

    protected List<String> getSortedListOfLocalNames() {
        return NAVIGATION_CASE_SORTED_ELEMENTS;
    }
    
    public void setRedirected(boolean redirect) {
        if (redirect)
            setChildElementText(REDIRECT, "", JSFConfigQNames.REDIRECT.getQName(getNamespaceURI()));
        else
            setChildElementText(REDIRECT, null, JSFConfigQNames.REDIRECT.getQName(getNamespaceURI()));
    }
    
    public boolean isRedirected() {
        return (null != getChildElementText(JSFConfigQNames.REDIRECT.getQName(getNamespaceURI())));
    }
    
    public String getToViewId() {
        return getChildElementText(JSFConfigQNames.TO_VIEW_ID.getQName(getNamespaceURI()));
    }
    
    public void setToViewId(String toViewId) {
        setChildElementText(TO_VIEW_ID, toViewId, JSFConfigQNames.TO_VIEW_ID.getQName(getNamespaceURI()));
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
