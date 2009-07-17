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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ACCESS;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author ak119685
 */
public class DwarfEntry {
    private CompilationUnit compilationUnit;
    private DwarfAbbriviationTableEntry abbriviationTableEntry;
    private ArrayList<Object> values = new ArrayList<Object>();
    private ArrayList<DwarfEntry> children = new ArrayList<DwarfEntry>();
    private long refference;
    private int hierarchyLevel;
    private String qualifiedName = null;
    private String name = null;
    private DwarfEntry parent;
    
    /** Creates a new instance of DwarfEntry */
    
    public DwarfEntry(CompilationUnit compilationUnit, DwarfAbbriviationTableEntry abbrEntry, long refference, int hierarchyLevel) {
        this.abbriviationTableEntry = abbrEntry;
        this.compilationUnit = compilationUnit;
        this.refference = refference;
        this.hierarchyLevel = hierarchyLevel;
    }
    
    public TAG getKind() {
        return abbriviationTableEntry.getKind();
    }
    
    public String getName() throws IOException {
        if (name == null) {
            Object nameAttr = getAttributeValue(ATTR.DW_AT_name);
            name = (nameAttr == null) ? "" : stripComments((String)nameAttr); // NOI18N
        }
        
        return name;
    }
    
    public String getQualifiedName() throws IOException {
        if (qualifiedName == null) {
            DwarfEntry specification = getSpecification();
            
            if (specification != null) {
                return specification.getQualifiedName();
            }

            qualifiedName = constructQualifiedName();
        }
        
        return qualifiedName;
    }
    
    private String constructQualifiedName() throws IOException {
        if (parent == null) {
            return getName();
        }
        
        TAG kind = parent.getKind();
        switch (kind) {
            case DW_TAG_compile_unit:
                return getName();
            case DW_TAG_lexical_block:
                return null;
        }
        
        String aName = getName();
        String pname = parent.getQualifiedName();
        String qname = (pname != null && aName != null && !pname.equals("") && !aName.equals("")) ? pname + "::" + aName : null;  // NOI18N

        return qname;
    }

    private String stripComments(String str) {
        if (str == null) {
            return null;
        }

        int idx = str.indexOf('#');

        if (idx != -1) {
            str = str.substring(0, idx);
        }
        
        return str.trim();
    }
    
    public void setQualifiedName(String qualifiedName) throws IOException {
        this.qualifiedName = qualifiedName;
        
        DwarfEntry origin = getAbstractOrigin();
        if (origin != null) {
            origin.setQualifiedName(qualifiedName);
        }
        
        DwarfEntry specification = getSpecification();
        if (specification != null) {
            specification.setQualifiedName(qualifiedName);
        }
    }
    
    public String getType() throws IOException {
        return compilationUnit.getType(this);
    }
    
    public int getUintAttributeValue(ATTR attr) throws IOException {
        Object value = getAttributeValue(attr);
        
        if (value == null) {
            return -1;
        }
        int result = ((Number)value).intValue();
        
        if (result < 0) {
            result &= 0xFF;
        }
        
        return result;
    }
    
    public Object getAttributeValue(ATTR attr) throws IOException {
        return getAttributeValue(attr, true);
    }
    
    public Object getAttributeValue(ATTR attr, boolean recursive) throws IOException {
        Object attrValue = null;
        
        // Get the index of this attribute from the abbriviation table entry
        // associated with this one.
        
        int attrIdx = abbriviationTableEntry.getAttribute(attr);
        
        // If there is no such attribute in this entry - try to get this
        // attribute from "abstract origin" or "specification" entry (if any)
        
        if (attrIdx == -1) {
            if (recursive) {
                Integer offset = -1;
                
                if (abbriviationTableEntry.getAttribute(ATTR.DW_AT_abstract_origin) >= 0) {
                    offset = (Integer)getAttributeValue(ATTR.DW_AT_abstract_origin);
                } else if (abbriviationTableEntry.getAttribute(ATTR.DW_AT_specification) >= 0) {
                    offset = (Integer)getAttributeValue(ATTR.DW_AT_specification);
                }
                
                
                if (offset >= 0) {
                    DwarfEntry attrEntry = compilationUnit.getEntry(offset);
                    if (attrEntry != null) {
                        attrValue = attrEntry.getAttributeValue(attr);
                    }
                }
            }
        } else {
            // Attribute has been found
            attrValue = values.get(attrIdx);
        }
        
        return attrValue;
    }
    
    public void addValue(Object value) {
        values.add(value);
    }
    
    public ArrayList<DwarfEntry> getChildren() {
        return children;
    }
    
    /**
     * Gets an entry, for which this entry is referred as specification
     * (via DW_AT_specification).
     * Note that this works only after all entries have been read.
     */
    public DwarfEntry getDefinition() throws IOException {
        return compilationUnit.getDefinition(this);
    }
    
    /**
     * Gets an entry that is referred by this is entry as specification
     * (via DW_AT_specification).
     * Note that this works only after all entries have been read.
     */
    public DwarfEntry getSpecification() throws IOException {
        Object o = getAttributeValue(ATTR.DW_AT_specification);
        if( o instanceof Integer ) {
            return compilationUnit.getEntry(((Integer) o).intValue());
        }
        return null;
    }
    
    public DwarfEntry getAbstractOrigin() throws IOException {
        Object o = getAttributeValue(ATTR.DW_AT_abstract_origin);
        if (o instanceof Integer) {
            return compilationUnit.getEntry(((Integer)o).intValue());
        }
        return null;
    }
    
    public boolean hasChildren() {
        return abbriviationTableEntry.hasChildren();
    }
    
    public void addChild(DwarfEntry child) {
        children.add(child);
        child.setParent(this);
    }
    
    public DwarfEntry getParent() {
        return parent;
    }
    
    private void setParent(DwarfEntry parent) {
        this.parent = parent;
    }
    
    public long getRefference() {
        return refference;
    }
    
    public String getParametersString() throws IOException {
        return getParametersString(true);
    }
    
    public String getParametersString(boolean withNames) throws IOException {
        ArrayList<DwarfEntry> params = getParameters();
        StringBuilder paramStr = new StringBuilder(); // NOI18N
        DwarfEntry param = null;
                
        paramStr.append('(');
        
        for (Iterator<DwarfEntry> it = params.iterator(); it.hasNext();) {
            param = it.next();
            
            if (param.getKind().equals(TAG.DW_TAG_unspecified_parameters)) {
                paramStr.append("..."); // NOI18N
            } else {
                paramStr.append(param.getType());
                if (withNames) {
                    paramStr.append(" "); // NOI18N
                    paramStr.append(param.getName());
                }
            }
            
            if (it.hasNext()) {
                paramStr.append(", "); // NOI18N
            }
        }
        
        paramStr.append(')'); // NOI18N

        return paramStr.toString();
    }
    
    public DwarfDeclaration getDeclaration() throws IOException {
        TAG kind = getKind();
        String aName = getQualifiedName();
        String type = getType();
        String paramStr = ""; // NOI18N
        
        if (kind.equals(TAG.DW_TAG_subprogram)) {
            paramStr += getParametersString();
        }
        
        String declarationString = type + " " + (aName == null ? getName() : aName) + paramStr; // NOI18N
        
        int declarationLine = getLine();
        int declarationColumn = getColumn();
        
        String declarationPosition = ((declarationLine == -1) ? "" : declarationLine) +  // NOI18N
                ((declarationColumn == -1) ? "" : ":" + declarationColumn); // NOI18N
        
        declarationPosition += " <" + refference + " (0x" + Long.toHexString(refference) + ")>"; // NOI18N
        
        return new DwarfDeclaration(kind.toString(), declarationString, getDeclarationFilePath(), declarationPosition);
    }
    
    public int getLine() throws IOException {
        return getUintAttributeValue(ATTR.DW_AT_decl_line);
    }
    
    public int getColumn() throws IOException {
        return getUintAttributeValue(ATTR.DW_AT_decl_column);
    }
    
    public ArrayList<DwarfEntry> getParameters() throws IOException {
        ArrayList<DwarfEntry> result = new ArrayList<DwarfEntry>();
        ArrayList<DwarfEntry> aChildren = getChildren();
        
        for (DwarfEntry child: aChildren) {
            if (child.isParameter() && !child.isArtifitial()) {
                result.add(child);
            }
        }
        
        return result;
    }
    
    public ArrayList<DwarfEntry> getMembers() throws IOException {
        ArrayList<DwarfEntry> result = new ArrayList<DwarfEntry>();
        ArrayList<DwarfEntry> aChildren = getChildren();
        
        for (DwarfEntry child: aChildren) {
            if (child.isMember() && !child.isArtifitial()) {
                result.add(child);
            }
        }
        
        return result;
    }
    
    public TAG getTag() {
        return abbriviationTableEntry.getKind();
    }
    
    public void dump(PrintStream out) {
        out.print("<" + hierarchyLevel + "><" + Long.toHexString(refference) + ">: "); // NOI18N
        abbriviationTableEntry.dump(out, this);
        
        for (int i = 0; i < children.size(); i++) {
            children.get(i).dump(out);
        }
    }

    @Override
    public String toString() {
        ByteArrayOutputStream st = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(st);
        dump(out);
        return st.toString();
    }

    public boolean isArtifitial() throws IOException {
        Object isArt = getAttributeValue(ATTR.DW_AT_artificial);
        return ((isArt != null) && ((Boolean)isArt).booleanValue());
    }
    
    public boolean hasAbastractOrigin() throws IOException {
        Object abastractOrigin = getAttributeValue(ATTR.DW_AT_abstract_origin);
        return (abastractOrigin != null);
    }
    
    public boolean isExternal() throws IOException {
        Object result = getAttributeValue(ATTR.DW_AT_external);
        return ((result != null) && ((Boolean)result).booleanValue());
    }
    
    public boolean isNamespace() {
        return getKind().equals(TAG.DW_TAG_namespace);
    }
    
    
    public ACCESS getAccessibility() throws IOException {
        Object result = getAttributeValue(ATTR.DW_AT_accessibility);
        return (result == null) ? null : ACCESS.get(((Number)result).intValue());
    }
    
    public boolean isParameter() {
        TAG kind = getKind();
        return kind.equals(TAG.DW_TAG_formal_parameter) || kind.equals(TAG.DW_TAG_unspecified_parameters);
    }
    
    public boolean isMember() {
        TAG kind = getKind();
        //return kind.equals(TAG.DW_TAG_member);
        return !kind.equals(TAG.DW_TAG_inheritance);
    }
    
    public boolean isEntryDefinedInFile(int fileEntryIdx) throws IOException {
        int fileIdx = getUintAttributeValue(ATTR.DW_AT_decl_file);
        return (fileIdx == fileEntryIdx);
    }
    
    public String getDeclarationFilePath() throws IOException {
        int fileIdx = (Integer)getUintAttributeValue(ATTR.DW_AT_decl_file);
        return (fileIdx <= 0) ? null : compilationUnit.getStatementList().getFilePath(fileIdx);
    }
    
    public String getTypeDef() throws IOException {
        if (getKind().equals(TAG.DW_TAG_typedef)) {
            return getType();
        }
        
        Integer typeRefIdx = (Integer)getAttributeValue(ATTR.DW_AT_type);
        DwarfEntry typeRef = compilationUnit.getTypedefFor(typeRefIdx);
        
        return (typeRef == null) ? getType() : typeRef.getType();
    }
    
    ArrayList<Object> getValues() {
        return values;
    }
    
}
