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
package org.netbeans.modules.web.refactoring.rename;

import java.text.MessageFormat;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.refactoring.WebXmlRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Base class for refactorings that handle moving and renaming
 * packages / classes.
 *
 * @author Erno Mononen
 */
abstract class BaseWebXmlRename extends WebXmlRefactoring{
    
    public BaseWebXmlRename(FileObject webDD, WebApp webModel) {
        super(webDD, webModel);
    }
    
    protected abstract AbstractRefactoring getRefactoring();

    /**
     * @return a list of <code>RenameItem</code>s representing the new and old 
     * names of the classes that are affected by this refactoring.
     */ 
    protected abstract List<RenameItem> getRenameItems();
    
    
    public final Problem prepare(RefactoringElementsBag refactoringElements) {
        
        for (RenameItem item : getRenameItems()){
            
            String newName = item.getNewFqn();
            String oldFqn = item.getOldFqn();
            
            for (Servlet servlet : getServlets(oldFqn)){
                refactoringElements.add(getRefactoring(), new ServletRenameElement(newName, oldFqn, webModel, webDD, servlet));
            }
            
            for (Listener listener : getListeners(oldFqn)){
                refactoringElements.add(getRefactoring(), new ListenerRenameElement(newName, oldFqn,  webModel, webDD, listener));
            }
            
            for (Filter filter : getFilters(oldFqn)){
                refactoringElements.add(getRefactoring(), new FilterRenameElement(newName, oldFqn, webModel, webDD, filter));
            }
            
            for (EjbRef ejbRef : getEjbRefs(oldFqn, true)){
                refactoringElements.add(getRefactoring(), new EjbRemoteRefRenameElement(newName, oldFqn, webModel, webDD, ejbRef));
            }
            
            for (EjbRef ejbRef : getEjbRefs(oldFqn, false)){
                refactoringElements.add(getRefactoring(), new EjbHomeRefRenameElement(newName, oldFqn, webModel, webDD, ejbRef));
            }
            
            for (EjbLocalRef ejbLocalRef : getEjbLocalRefs(oldFqn, false)){
                refactoringElements.add(getRefactoring(), new EjbLocalRefRenameElement(newName, oldFqn, webModel, webDD, ejbLocalRef));
            }
            
            for (EjbLocalRef ejbLocalRef : getEjbLocalRefs(oldFqn, true)){
                refactoringElements.add(getRefactoring(), new EjbLocalHomeRefRenameElement(newName, oldFqn, webModel, webDD, ejbLocalRef));
            }
        }
        
        return null;
    }
    
    private abstract static class WebRenameElement extends WebRefactoringElement{
        
        protected String oldName;
        protected String newName;
        //        protected RenameRefactoring rename;
        
        public WebRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD) {
            super(webApp, webDD);
            this.newName = newName;
            this.oldName = oldName;
        }

        protected String getName() {
            return oldName;
        }
        
    }
    
    private static class ServletRenameElement extends WebRenameElement{
        
        private Servlet servlet;
        public ServletRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, Servlet servlet) {
            super(newName, oldName, webApp, webDD);
            this.servlet = servlet;
        }
        
        @Override
        protected void doChange() {
            servlet.setServletClass(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(BaseWebXmlRename.class, "TXT_WebXmlServletRename"), args);
        }
        
        @Override
        protected void undo() {
            servlet.setServletClass(oldName);
        }
        
    }
    
    private static class FilterRenameElement extends WebRenameElement{
        
        private Filter filter;
        public FilterRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, Filter filter) {
            super(newName, oldName, webApp, webDD);
            this.filter = filter;
        }
        
        @Override
        protected void doChange() {
            filter.setFilterClass(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(BaseWebXmlRename.class, "TXT_WebXmlFilterRename"), args);
        }
        
        @Override
        protected void undo() {
            filter.setFilterClass(oldName);
        }
        
    }
    
    private static class ListenerRenameElement extends WebRenameElement{
        
        private Listener listener;
        
        public ListenerRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, Listener listener) {
            super(newName, oldName, webApp, webDD);
            this.listener = listener;
        }
        
        @Override
        protected void doChange() {
            listener.setListenerClass(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(BaseWebXmlRename.class, "TXT_WebXmlListenerRename"), args);
        }
        
        @Override
        protected void undo() {
            listener.setListenerClass(oldName);
        }
        
    }
    
    private static class EjbHomeRefRenameElement extends WebRenameElement{
        
        private EjbRef ejbRef;
        
        public EjbHomeRefRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, EjbRef ejbRef) {
            super(newName, oldName, webApp, webDD);
            this.ejbRef = ejbRef;
        }
        
        @Override
        protected void doChange() {
            ejbRef.setHome(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(BaseWebXmlRename.class, "TXT_WebXmlRefHomeRename"), args);
        }
        
        @Override
        protected void undo() {
            ejbRef.setHome(oldName);
        }
    }
    
    private static class EjbRemoteRefRenameElement extends WebRenameElement{
        
        private EjbRef ejbRef;
        
        public EjbRemoteRefRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, EjbRef ejbRef) {
            super(newName, oldName, webApp, webDD);
            this.ejbRef = ejbRef;
        }
        
        @Override
        protected void doChange() {
            ejbRef.setRemote(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(BaseWebXmlRename.class, "TXT_WebXmlRefRemoteRename"), args);
        }
        
        @Override
        protected void undo() {
            ejbRef.setRemote(oldName);
        }
    }
    
    private static class EjbLocalRefRenameElement extends WebRenameElement{
        
        private EjbLocalRef ejbLocalRef;
        
        public EjbLocalRefRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, EjbLocalRef ejbLocalRef) {
            super(newName, oldName, webApp, webDD);
            this.ejbLocalRef = ejbLocalRef;
        }
        
        @Override
        protected void doChange() {
            ejbLocalRef.setLocal(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(BaseWebXmlRename.class, "TXT_WebXmlRefLocalRename"), args);
        }
        
        @Override
        protected void undo() {
            ejbLocalRef.setLocal(oldName);
        }
    }
    private static class EjbLocalHomeRefRenameElement extends WebRenameElement{
        
        private EjbLocalRef ejbLocalRef;
        
        public EjbLocalHomeRefRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, EjbLocalRef ejbLocalRef) {
            super(newName, oldName, webApp, webDD);
            this.ejbLocalRef = ejbLocalRef;
        }
        
        @Override
        protected void doChange() {
            ejbLocalRef.setLocalHome(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(BaseWebXmlRename.class, "TXT_WebXmlRefLocalHomeRename"), args);
        }
        
        @Override
        protected void undo() {
            ejbLocalRef.setLocalHome(oldName);
        }
    }
    
}
