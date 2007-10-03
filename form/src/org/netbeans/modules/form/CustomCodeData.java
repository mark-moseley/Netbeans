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

package org.netbeans.modules.form;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds code-related data of one component in a representation
 * suitable for editing in the customizer dialog.
 * 
 * @author Tomas Pavek
 */

class CustomCodeData {

    enum CodeCategory { CREATE_AND_INIT, DECLARATION }
    private CodeCategory defaultCategory = CodeCategory.CREATE_AND_INIT;

    // creation and initialization code
    private List<EditableBlock> initEditableBlocks = new ArrayList();
    private List<GuardedBlock> initGuardedBlocks = new ArrayList();

    // declaration code
    private VariableDeclaration declaration;
    private List<EditableBlock> declarationEditableBlocks = new ArrayList();
    private List<GuardedBlock> declarationGuardedBlocks = new ArrayList();

    private int highestPreference; // used temporarily when filling in editable blocks

    CustomCodeData() {
    }

    int getEditableBlockCount(CodeCategory category) {
        return getEditableList(category).size();
    }

    EditableBlock getEditableBlock(CodeCategory category, int index) {
        return getEditableList(category).get(index);
    }

    int getGuardedBlockCount(CodeCategory category) {
        return getGuardedList(category).size();
    }

    GuardedBlock getGuardedBlock(CodeCategory category, int index) {
        return getGuardedList(category).get(index);
    }

    VariableDeclaration getDeclarationData() {
        return declaration;
    }

    // -----

    void addEditableBlock(String code,
                          FormProperty targetProperty, int preferenceIndex, // String propName
                          String displayName, String hint)
    {
        addEditableBlock(null, code, targetProperty, preferenceIndex, displayName, hint, false, false);
    }

    void addEditableBlock(String code,
                          FormProperty targetProperty, int preferenceIndex, // String propName
                          String displayName, String hint,
                          boolean pre, boolean post)
    {
        addEditableBlock(null, code, targetProperty, preferenceIndex, displayName, hint, pre, post);
    }

    void addEditableBlock(CodeCategory category, String code,
                          FormProperty targetProperty, int preferenceIndex, // String propName
                          String displayName, String hint,
                          boolean pre, boolean post)
    {
        if (category == null)
            category = defaultCategory;

        List<EditableBlock> editList = getEditableList(category);
        if (editList.size() <= getGuardedList(category).size()) {
            highestPreference = 0;
            editList.add(new EditableBlock());
        }
        EditableBlock eBlock = editList.get(editList.size()-1);
        eBlock.addEntry(new CodeEntry(code, targetProperty, displayName, hint, pre, post),
                        preferenceIndex > highestPreference);
        if (preferenceIndex > highestPreference)
            highestPreference = preferenceIndex;
    }

    void addGuardedBlock(String code) {
        addGuardedBlock(null, code, null, null, false, null, null, null);
    }

    void addGuardedBlock(String defaultCode, String customCode, String customCodeMark, boolean customized,
                         FormProperty targetProperty, String displayName, String hint) // String propName
    {
        addGuardedBlock(null, defaultCode, customCode, customCodeMark, customized,
                        targetProperty, displayName, hint);
    }

    void addGuardedBlock(CodeCategory category,
                         String defaultCode, String customCode, String customCodeMark, boolean customized,
                         FormProperty targetProperty, String displayName, String hint) // String propName
    {
        if (category == null)
            category = defaultCategory;

        assert getGuardedList(category).size()+1 == getEditableList(category).size();

        CodeEntry customEntry = null;
        int guardHeading = 0;
        int guardEnding = 0;
        if (customCode != null) {
            int codeLen = customCode.length();
            int first = customCode.indexOf(customCodeMark);
            int last = customCode.lastIndexOf(customCodeMark);
            if (first > 0 && last < codeLen-1) { // not a first or last char
                int markLen = customCodeMark.length();
                String customizablePart = customCode.substring(first + markLen, last);
                customCode = customCode.substring(0, first)
                             + customizablePart
                             + customCode.substring(last + markLen);
                guardHeading = first;
                guardEnding = codeLen - markLen - last;
                customEntry = new CodeEntry(customized ? customizablePart : null,
                                            targetProperty, displayName, hint,
                                            false, false);
            }
            else customCode = null; // no customizable section
        }

        GuardedBlock gBlock = new GuardedBlock(defaultCode, customCode,
                                               guardHeading, guardEnding, customized,
                                               customEntry);
        getGuardedList(category).add(gBlock);
    }

    void setDefaultCategory(CodeCategory category) {
        defaultCategory = category;
    }

    void setDeclarationData(boolean local, int modifiers) {
        declaration = new VariableDeclaration(local, modifiers);
    }

    // -----

    private List<EditableBlock> getEditableList(CodeCategory category) {
        switch (category) {
            case CREATE_AND_INIT: return initEditableBlocks;
            case DECLARATION: return declarationEditableBlocks;
        }
        return null;
    }

    private List<GuardedBlock> getGuardedList(CodeCategory category) {
        switch (category) {
            case CREATE_AND_INIT: return initGuardedBlocks;
            case DECLARATION: return declarationGuardedBlocks;
        }
        return null;
    }

    // -----

    void check() {
        checkEditableGuardedPairs(initEditableBlocks, initGuardedBlocks);
        checkEditableGuardedPairs(declarationEditableBlocks, declarationGuardedBlocks);
    }

    private void checkEditableGuardedPairs(List<EditableBlock> eList, List<GuardedBlock> gList) {
        assert (eList.size() == 0 && gList.size() == 0)
               || eList.size() == gList.size() + 1;
    }

    // -----

    /** Holds custom code and its origin (property where it is stored).*/
    static class CodeEntry {
        private String code;

        private FormProperty targetProperty;
        private String displayName;
        private String hint;
        private boolean pre; // whether stored as pre-code of the property
        private boolean post; // whether stored as post-code of the property

        private CodeEntry(String code,
                          FormProperty prop, // String name
                          String displayName, String hint,
                          boolean pre, boolean post)
        {
            assert (!pre && !post) || pre != post;
            this.code = code;
            this.targetProperty = prop;
            this.displayName = displayName;
            this.hint = hint;
            this.pre = pre;
            this.post = post;
        }

        String getCode() {
            return code;
        }

        void setCode(String code) {
            this.code = code;
        }

        String getName() {
            return targetProperty.getName();
        }

        String getDisplayName() {
            return displayName;
        }

        String getToolTipText() {
            return hint;
        }

        FormProperty getTargetProperty() {
            return targetProperty;
        }

        boolean isPropertyPreInit() {
            return pre;
        }

        boolean isPropertyPostInit() {
            return post;
        }

        public String toString() {
            return displayName;
        }
    }

    static class EditableBlock {
        private List<CodeEntry> entries = new ArrayList();
        private int prefEntryIndex;

        private void addEntry(CodeEntry e, boolean preferred) {
            entries.add(e);
            if (preferred)
                prefEntryIndex = entries.size() - 1;
        }

        int getPreferredEntryIndex() {
            return prefEntryIndex;
        }

        CodeEntry[] getEntries() {
            return entries.toArray(new CodeEntry[entries.size()]);
        }
    }

    static class GuardedBlock {
        private String defaultCode;
        private String customCode;
        private int headerLength; // number of guarded chars before customizable area
        private int footerLength; // number of guarded chars following the customizable area
        private boolean customCodeSet;
        private CodeEntry customEntry; // describes where the custom code is stored

        private GuardedBlock(String defaultCode, String customCode,
                             int header, int footer, boolean customSet,
                             CodeEntry customEntry)
        {
            this.defaultCode = defaultCode;
            this.customCode = customCode;
            this.headerLength = header;
            this.footerLength = footer;
            this.customCodeSet = customSet;
            this.customEntry = customEntry;
        }

        String getDefaultCode() {
            return defaultCode;
        }

        String getCustomCode() {
            return customCode;
        }

        void setCustomizedCode(String code) {
            customEntry.setCode(code);
            if (code != null) {
                customCode = customCode.substring(0, headerLength)
                        + code
                        + customCode.substring(customCode.length() - footerLength);
                customCodeSet = true;
            }
            else {
                customCodeSet = false;
            }
        }

        boolean isCustomizable() {
            return customCode != null;
        }

        boolean isCustomized() {
            return customCodeSet;
        }

        CodeEntry getCustomEntry() {
            return customEntry;
        }

        int getHeaderLength() {
            return headerLength;
        }

        int getFooterLength() {
            return footerLength;
        }
    }

    static class VariableDeclaration {
        boolean local;
        int modifiers; // combination of java.lang.reflect.Modifier constants
        private VariableDeclaration(boolean local, int modifiers) {
            this.local = local;
            this.modifiers = modifiers;
        }
    }
}
