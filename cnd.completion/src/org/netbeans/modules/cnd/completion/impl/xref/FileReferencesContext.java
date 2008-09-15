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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmContextUtilities;

/**
 *
 * @author Alexander Simon
 */
public final class FileReferencesContext {
    private CsmFile csmFile;
    private int lastOffset;
    private boolean isClened = false;
    private Map<String,List<CsmUID<CsmVariable>>> fileLocalVars;
    private List<Offsets> fileObjectOffsets;
    private List<Offsets> fileDeclarationsOffsets;
    private Map<String,CsmUID<CsmMacro>> projectMacros;
    
    FileReferencesContext(CsmScope csmScope){
        if (CsmKindUtilities.isFile(csmScope)) {
            csmFile = (CsmFile) csmScope;
        } else if (CsmKindUtilities.isFunction(csmScope)) {
            csmFile = ((CsmFunction)csmScope).getContainingFile();
        }
        lastOffset = 0;
    }

    public void clean(){
        isClened = true;
        _clean();
    }

    private void _clean(){
        if (fileLocalVars != null) {
            fileLocalVars = null;
            fileDeclarationsOffsets = null;
            fileObjectOffsets = null;
            projectMacros = null;
        }
    }
    
    public boolean isCleaned(){
        return isClened;
    }

    public void advance(int offset){
        if (csmFile == null) {
            return;
        }
        if (lastOffset > offset) {
            _clean();
        }
        lastOffset = offset;
        if (fileLocalVars == null) {
            // no increment for fileLocalVars
            fileLocalVars = new HashMap<String,List<CsmUID<CsmVariable>>>();
            fillFileLocalIncludeVariables();
            // no increment for fileOffsets
            fileDeclarationsOffsets = new ArrayList<Offsets>();
            fileObjectOffsets = new ArrayList<Offsets>();
            fillFileOffsets();
            projectMacros = new HashMap<String,CsmUID<CsmMacro>>();
            fillProjectMacros();
        }
    }

    public List<CsmVariable> getFileLocalIncludeVariables(String name){
        if (fileLocalVars == null) {
            return null;
        }
        List<CsmUID<CsmVariable>> vars = fileLocalVars.get(name);
        if (vars == null || vars.isEmpty()){
            return Collections.<CsmVariable>emptyList();
        }
        List<CsmVariable> res = new ArrayList<CsmVariable>(vars.size());
        for(CsmUID<CsmVariable> uid : vars){
            CsmVariable v = uid.getObject();
            if (v != null){
                res.add(v);
            }
        }
        return res;
    }
    
    public CsmObject findInnerFileDeclaration(int offset){
        if (fileDeclarationsOffsets == null) {
            return null;
        }
        Offsets key = new Offsets(offset);
        int res = Collections.binarySearch(fileDeclarationsOffsets, key);
        if (res >= 0) {
            if (res < fileDeclarationsOffsets.size()-1) {
                Offsets next = fileDeclarationsOffsets.get(res+1);
                if (next.compareTo(key) == 0) {
                    return next.object;
                }
            }
            return fileDeclarationsOffsets.get(res).object;
        }
        return null;
    }

    public CsmObject findInnerFileObject(int offset){
        if (fileObjectOffsets == null) {
            return null;
        }
        Offsets key = new Offsets(offset);
        int res = Collections.binarySearch(fileObjectOffsets, key);
        if (res >=0) {
            if (res < fileObjectOffsets.size()-1) {
                Offsets next = fileObjectOffsets.get(res+1);
                if (next.compareTo(key) == 0) {
                    return next.object;
                }
            }
            return fileObjectOffsets.get(res).object;
        }
        return null;
    }

    public CsmMacro findIncludedMacro(String name){
        if (projectMacros == null) {
            return null;
        }
        CsmUID<CsmMacro> uid = projectMacros.get(name);
        if (uid != null) {
            return uid.getObject();
        }
        return null;
    }

    private void fillFileLocalIncludeVariables() {
        CsmDeclaration.Kind[] kinds = new CsmDeclaration.Kind[] {
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION};
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                           null, true, true, false);
        fillFileLocalIncludeVariables(filter, csmFile, new HashSet<CsmFile>(), true);
    }
    
    private void fillFileLocalIncludeVariables(CsmFilter filter, CsmFile file, Set<CsmFile> antiLoop, boolean first) {
        if (antiLoop.contains(file)) {
            return;
        }
        antiLoop.add(file);
        for(CsmInclude incl : file.getIncludes()){
            CsmFile f = incl.getIncludeFile();
            if (f != null) {
                fillFileLocalIncludeVariables(filter, f, antiLoop, false);
            }
        }
        if (!first) {
            Iterator<CsmVariable> it = CsmSelect.getDefault().getStaticVariables(file, filter);
            while(it.hasNext()) {
                CsmOffsetableDeclaration decl = it.next();
                 if (CsmKindUtilities.isFileLocalVariable(decl)) {
                     CsmVariable var = (CsmVariable) decl;
                     String name = var.getName().toString();
                     List<CsmUID<CsmVariable>> list = fileLocalVars.get(name);
                     if (list == null) {
                         list = new ArrayList<CsmUID<CsmVariable>>();
                     }
                     list.add(var.getUID());
                }
            }
        }
    }

    private void fillFileOffsets(){
        for(CsmOffsetableDeclaration declaration : csmFile.getDeclarations()){
            fileDeclarationsOffsets.add(new Offsets(declaration));
        }
        for(CsmInclude declaration : csmFile.getIncludes()){
            fileObjectOffsets.add(new Offsets(declaration));
        }
        for(CsmMacro declaration : csmFile.getMacros()){
            fileObjectOffsets.add(new Offsets(declaration));
        }
        Collections.sort(fileObjectOffsets);
    }
    
    private static class Offsets implements Comparable<Offsets> {
        private int startOffset;
        private int endOffset;
        private CsmObject object;
        Offsets(CsmOffsetableDeclaration declaration){
            startOffset = declaration.getStartOffset();
            endOffset = declaration.getEndOffset();
            object = declaration;
        }
        Offsets(CsmMacro macros){
            startOffset = macros.getStartOffset();
            endOffset = macros.getEndOffset();
            object = macros;
        }
        Offsets(CsmInclude include){
            startOffset = include.getStartOffset();
            endOffset = include.getEndOffset();
            object = include;
        }
        Offsets(int offset){
            startOffset = offset;
            endOffset = offset;
        }

        public int compareTo(Offsets o) {
            if (object != null && o.object == null) {
                if (startOffset <= o.startOffset && o.startOffset < endOffset) {
                    return 0;
                }
            } else if (object == null && o.object != null){
                if (o.startOffset <= startOffset && startOffset < o.endOffset) {
                    return 0;
                }
            }
            return startOffset - o.startOffset;
        }
    }

    private void fillProjectMacros() {
        gatherIncludeMacros(csmFile, new HashSet<CsmFile>());
    }
    
    private void gatherIncludeMacros(CsmFile file, Set<CsmFile> visitedFiles) {
        if( visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        for (Iterator<CsmInclude> iter = file.getIncludes().iterator(); iter.hasNext();) {
            CsmInclude inc = iter.next();
            CsmFile incFile = inc.getIncludeFile();
            if( incFile != null ) {
                getFileLocalMacros(incFile);
                gatherIncludeMacros(incFile, visitedFiles);
            }
        }
    }

    private void getFileLocalMacros(CsmFile file){
        for (CsmMacro macro : file.getMacros()) {
            String name = macro.getName().toString();
            CsmUID<CsmMacro> uid = projectMacros.get(name);
            if (uid == null) {
                projectMacros.put(name, macro.getUID());
            }
        }
    }
}
