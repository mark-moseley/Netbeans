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
package org.netbeans.modules.cnd.refactoring.ui;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * WhereUsedQueryUI for C/C++
 * 
 * @author Vladimir Voskresensky
 */
public class WhereUsedQueryUI implements RefactoringUI {
    private WhereUsedQuery query;
    private WhereUsedPanel panel;
    private final CsmObject origObject;
    private final String name;
    public WhereUsedQueryUI(CsmObject csmObject) {
        this.query = new WhereUsedQuery(Lookups.singleton(csmObject));
        this.origObject = csmObject;
        name = getSearchElementName(this.origObject);
    }
    
    public boolean isQuery() {
        return true;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        // this method returns panel used for displaying config options
        // of refactoring/find usages
        // i.e. panel with checkboxes
        // called from AWT
        if (panel == null) {
            panel = new WhereUsedPanel(name, origObject, parent);
        }
        return panel;
    }

    public Problem setParameters() {
        // handle parameters defined in panel
        assert panel != null;
        query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS,panel.isSearchInComments());
        if (panel.getReferencedObject() == null) {
            query.setRefactoringSource(Lookup.EMPTY);
        } else {
            query.setRefactoringSource(Lookups.singleton(panel.getReferencedObject()));
        }
        if (panel.isVirtualMethod()) {
            setForMethod();
            return query.checkParameters();
        } else if (panel.isClass()) {
            setForClass();
            return query.checkParameters();
        } else {
            return null;
        }
    }
    
    private void setForMethod() {
        assert panel != null;
        if (panel.isMethodFromBaseClass()) {
            if (panel.getBaseMethod() == null) {
                query.setRefactoringSource(Lookup.EMPTY);
            } else {
                query.setRefactoringSource(Lookups.singleton(panel.getBaseMethod()));
            }            
        } else {
            if (panel.getReferencedObject() == null) {
                query.setRefactoringSource(Lookup.EMPTY);
            } else {
                query.setRefactoringSource(Lookups.singleton(panel.getReferencedObject()));
            }
        }
        query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS,panel.isMethodOverriders());
        query.putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS,panel.isMethodFromBaseClass());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isMethodFindUsages());
    }
    
    private void setForClass() {
        assert panel != null;
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES,panel.isClassSubTypes());
        query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES,panel.isClassSubTypesDirectOnly());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isClassFindUsages());
    }
    
    public Problem checkParameters() {
        assert panel != null;
        if (panel.isVirtualMethod()) {
            setForMethod();
            return query.fastCheckParameters();
        } else if (panel.isClass()) {
            setForClass();
            return query.fastCheckParameters();
        } else {
            return null;
        }
    }

    public AbstractRefactoring getRefactoring() {
        return query;
    }

    public String getDescription() {
        // this method returns description displayed in Find Usages tab
        // i.e. "Usages of "name" (2 occurrences]"
        if (panel!=null) {
            String description = panel.getDescription();
            String key = "DSC_WhereUsed"; // NOI18N
            if (panel.isClass()) {
                if (!panel.isClassFindUsages()) {
                    if (panel.isClassSubTypesDirectOnly()) {
                        key = "DSC_WhereUsedFindDirectSubTypes"; // NOI18N
                    } else {
                        key = "DSC_WhereUsedFindAllSubTypes"; // NOI18N
                    }
                }
            } else if (panel.isVirtualMethod()) {
                if (panel.isMethodFromBaseClass()) {
                    description = panel.getBaseMethodDescription();
                }
                if (panel.isMethodOverriders()) {
                    key = panel.isMethodFindUsages() ? 
                        "DSC_WhereUsedUsagesAndMethodOverriders" : // NOI18N
                        "DSC_WhereUsedMethodOverriders"; // NOI18N
                }
            }
            description = description.replace("<html>", "").replace("</html>", ""); // NOI18N
            return getString(key, description);
//            if (CsmKindUtilities.isClass(origCsmObject)/*kind == ElementKind.MODULE || kind == ElementKind.CLASS*/) {
//                if (!panel.isClassFindUsages())
//                    if (!panel.isClassSubTypesDirectOnly()) {
//                        return getFormattedString("DSC_WhereUsedFindAllSubTypes", name);
//                    } else {
//                        return getFormattedString("DSC_WhereUsedFindDirectSubTypes", name);
//                    }
//            } else {
//                if (CsmKindUtilities.isFunction(origCsmObject)/*kind == ElementKind.METHOD*/) {
//                    String description = null;
//                    if (panel.isMethodFindUsages()) {
//                        description = getString("DSC_FindUsages");
//                    }
//                    
//                    if (panel.isMethodOverriders()) {
//                        if (description != null) {
//                            description += " " + getString("DSC_And") + " ";
//                        } else {
//                            description = "";
//                        }
//                        description += getString("DSC_WhereUsedMethodOverriders");
//                    }
//                    
//                    description += " " + getFormattedString("DSC_WhereUsedOf", panel.getMethodDeclaringClass() + '.' + name); //NOI18N
//                    return description;
//                }
//            }
        }
        return getString("DSC_WhereUsed", name); // NOI18N
    }
    
    private String getString(String key, String value) {
        return NbBundle.getMessage(WhereUsedQueryUI.class, key, value);
    }

    public String getName() {
        return getString("LBL_WhereUsed", name); // NOI18N
    }
    
    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WhereUsedQueryUI.class);
    }
    
    private String getSearchElementName(CsmObject csmObj) {
        assert csmObj != null;
        String objName;
        if (csmObj instanceof CsmReference) {
            objName = ((CsmReference)csmObj).getText();
        } else if (CsmKindUtilities.isNamedElement(csmObj)) {
            objName = ((CsmNamedElement)csmObj).getName();
        } else if (csmObj != null) {
            objName = "<UNNAMED ELEMENT>"; // NOI18N
        } else {
            objName = "<UNRESOLVED ELEMENT>"; // NOI18N
        }
        return objName;
    }     
}
