/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.rename;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.refactoring.WebXmlRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Handles renaming of classes specified in web.xml.
 *
 * @author Erno Mononen
 */
public class WebXmlRename extends WebXmlRefactoring{
    
    private final String oldFqn;
    private final RenameRefactoring rename;
    
    public WebXmlRename(String oldFqn, RenameRefactoring rename, WebApp webModel, FileObject webDD) {
        super(webDD, webModel);
        this.oldFqn = oldFqn;
        this.rename = rename;
    }
    
    public Problem preCheck() {
        return null;
    }
    
    
    public Problem checkParameters() {
        return null;
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    public void cancelRequest() {
        return;
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        
        String newName = renameClass(oldFqn, rename.getNewName());
        for (Servlet servlet : getServlets(oldFqn)){
            refactoringElements.add(rename, new ServletRenameElement(newName, oldFqn, webModel, webDD, servlet));
        }
        
        for (Listener listener : getListeners(oldFqn)){
            refactoringElements.add(rename, new ListenerRenameElement(newName, oldFqn,  webModel, webDD, listener));
        }
        
        for (Filter filter : getFilters(oldFqn)){
            refactoringElements.add(rename, new FilterRenameElement(newName, oldFqn, webModel, webDD, filter));
        }
        
        return null;
    }
    
    public static String renameClass(String originalFullyQualifiedName, String newName){
        int lastDot = originalFullyQualifiedName.lastIndexOf('.');
        return (lastDot <= 0) ? newName : originalFullyQualifiedName.substring(0, lastDot + 1) + newName;
    }
    
    private abstract static class WebRenameElement extends WebRefactoringElement{
        
        protected String oldName;
        protected String newName;
        protected RenameRefactoring rename;
        
        public WebRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD) {
            super(webApp, webDD);
            this.newName = newName;
            this.oldName = oldName;
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
            return MessageFormat.format(NbBundle.getMessage(WebXmlRename.class, "TXT_WebXmlServletRename"), args);
        }
        
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
        
        protected void doChange() {
            filter.setFilterClass(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(WebXmlRename.class, "TXT_WebXmlFilterRename"), args);
        }
        
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
        
        protected void doChange() {
            listener.setListenerClass(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(WebXmlRename.class, "TXT_WebXmlListenerRename"), args);
        }
        
        protected void undo() {
            listener.setListenerClass(oldName);
        }
        
    }
    
}
