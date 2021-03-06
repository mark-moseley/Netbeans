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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.whereused;

import java.text.MessageFormat;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.netbeans.modules.web.refactoring.TldRefactoring;
import org.netbeans.modules.web.taglib.model.FunctionType;
import org.netbeans.modules.web.taglib.model.ListenerType;
import org.netbeans.modules.web.taglib.model.TagType;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.web.taglib.model.ValidatorType;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * Finds usages of classes in tld files.
 *
 * @author Erno Mononen
 */
public class TldWhereUsed extends TldRefactoring{
    
    private final WhereUsedQuery whereUsedQuery;
    private final WebModule webModule;
    private final String clazz;
    
    public TldWhereUsed(String clazz, WebModule wm, WhereUsedQuery whereUsedQuery) {
        this.clazz = clazz;
        this.whereUsedQuery = whereUsedQuery;
        this.webModule = wm;
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        
        for(TaglibHandle taglibHandle : getTaglibs(webModule)){
            Taglib taglib = taglibHandle.getTaglib();
            for (TagType tagType : taglib.getTag()){
                if (clazz.equals(tagType.getTagClass())){
                    refactoringElements.add(whereUsedQuery, new TagClassWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
                if (clazz.equals(tagType.getTeiClass())){
                    refactoringElements.add(whereUsedQuery, new TeiClassWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
            for (FunctionType functionType : taglib.getFunction()){
                if (clazz.equals(functionType.getFunctionClass())){
                    refactoringElements.add(whereUsedQuery, new FunctionWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
            ValidatorType validatorType = taglib.getValidator();
            if (validatorType != null && clazz.equals(validatorType.getValidatorClass())){
                refactoringElements.add(whereUsedQuery, new ValidatorWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
            }
            for (ListenerType listenerType : taglib.getListener()){
                if (clazz.equals(listenerType.getListenerClass())){
                    refactoringElements.add(whereUsedQuery, new ListenerWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
        }
        
        return null;
    }
    
    private static class TagClassWhereUsedElement extends TldRefactoringElement {
        
        public TagClassWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibTagClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
    private static class TeiClassWhereUsedElement extends TldRefactoringElement {
        
        public TeiClassWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibTeiClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
    private static class FunctionWhereUsedElement extends TldRefactoringElement {
        
        public FunctionWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibFunctionClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
        
    }
    
    private static class ValidatorWhereUsedElement extends TldRefactoringElement {
        
        public ValidatorWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibValidatorClassWhereUsed"), clazz);
        }

        public void performChange() {
            // do nothing
        }
    }
    
    private static class ListenerWhereUsedElement extends TldRefactoringElement {
        
        public ListenerWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibListenerClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
}

