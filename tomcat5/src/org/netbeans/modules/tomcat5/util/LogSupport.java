/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.windows.*;

/**
 * <code>LogSupport</code> class for creating links in the output window.
 *
 * @author  Stepan Herold
 */
public class LogSupport {
    private Map/*<Link, Link>*/ links = Collections.synchronizedMap(new HashMap());
    private Annotation errAnnot;
    
    /**
     * Return a link which implements <code>OutputListener</code> interface. Link
     * is then used to represent a link in the output window. This class also 
     * handles error annotations which are shown after a line is clicked.
     * 
     * @return link which implements <code>OutputListener</code> interface. Link
     *         is then used to represent a link in the output window.
     */
    public Link getLink(String errorMsg, String path, int line) {
        Link newLink = new Link(errorMsg, path, line);
        Link cachedLink = (Link)links.get(newLink);
        if (cachedLink != null) {
            return cachedLink;
        }
        links.put(newLink, newLink);
        return newLink;
    }

    /**
     * Detach error annotation.
     */
    public void detachAnnotation() {
        if (errAnnot != null) {
            errAnnot.detach();
        }
    }
    
    /**
     * <code>LineInfo</code> is used to store info about the parsed line.
     */
    public static class LineInfo {
        private String path;
        private int line;
        private String message;
        private boolean error;
        private boolean accessible;
        
        /**
         * <code>LineInfo</code> is used to store info about the parsed line.
         *
         * @param path path to file
         * @param line line number where the error occurred
         * @param message error message
         * @param error represents the line an error?
         * @param accessible is the file accessible?
         */
        public LineInfo(String path, int line, String message, boolean error, boolean accessible) {
            this.path = path;
            this.line = line;
            this.message = message;
            this.error = error;
            this.accessible = accessible;
        }
        
        public String path() {
            return path;
        }
        
        public int line() {
            return line;
        }
        
        public String message() {
            return message;
        }
        
        public boolean isError() {
            return error;
        }
        
        public boolean isAccessible() {
            return accessible;
        }
    }    
    
    /**
     * Error annotation.
     */
    static class ErrorAnnotation extends Annotation {
        private String shortDesc = null;
        
        public ErrorAnnotation(String desc) {
            shortDesc = desc;
        }
        
        public String getAnnotationType() {
            return "org-netbeans-modules-tomcat5-error"; // NOI18N
        }
        
        public String getShortDescription() {
            return shortDesc;
        }
        
    }
    
    /**
     * <code>Link</code> is used to create a link in the output window. To create
     * a link use the <code>getLink</code> method of the <code>LogSupport</code>
     * class. This prevents from memory vast by returning already existing instance,
     * if one with such values exists.
     */
    public class Link implements OutputListener {
        private String msg;
        private String path;
        private int line;
        
        private int hashCode = 0;
        
        Link(String msg, String path, int line) {
            this.msg = msg;
            this.path = path;
            this.line = line;
        }
        
        public int hashCode() {
            if (hashCode == 0) {
                int result = 17;
                result = 37 * result + line;
                result = 37 * result + (path != null ? path.hashCode() : 0);
                result = 37 * result + (msg != null ? msg.hashCode() : 0);
                hashCode = result;
            }
            return hashCode;
        } 
        
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Link) {
                Link anotherLink = (Link)obj;
                if ((((msg != null) && msg.equals(anotherLink.msg)) || (msg == anotherLink.msg))
                    && (((path != null) && path.equals(anotherLink.path)) || (path == anotherLink.path))
                    && line == anotherLink.line) {
                        return true;
                }
            }
            return false;
        }
        
        /**
         * If the link is clicked, required file is opened in the editor and an 
         * <code>ErrorAnnotation</code> is attached.
         */
        public void outputLineAction(OutputEvent ev) {
            FileObject sourceFile = GlobalPathRegistry.getDefault().findResource(path);
            if (sourceFile == null) sourceFile = FileUtil.toFileObject(new File(path));
            DataObject dataObject = null;
            if (sourceFile != null) {
                try {
                    dataObject = DataObject.find(sourceFile);
                } catch(DataObjectNotFoundException ex) {// ignore it
                }
            }
            if (dataObject != null) {
                EditorCookie editorCookie = (EditorCookie)dataObject.getCookie(EditorCookie.class);
                if (editorCookie == null) return;
                editorCookie.open();
                int errLineNum = 0;
                Line errorLine = null;
                try {
                    errorLine = editorCookie.getLineSet().getCurrent(line - 1);
                } catch (IndexOutOfBoundsException iobe) {
                    return;
                }
                if (errAnnot != null) {
                    errAnnot.detach();
                }
                String errorMsg = msg;
                if (errorMsg == null || errorMsg.equals("")) { //NOI18N
                    errorMsg = NbBundle.getMessage(Link.class, "MSG_ExceptionOccurred");
                }
                errAnnot = new ErrorAnnotation(errorMsg);
                errAnnot.attach(errorLine);
                errAnnot.moveToFront();
                errorLine.show(Line.SHOW_TRY_SHOW);
            }
        }
        
        /**
         * If a link is cleared, error annotation is detached and link cache is 
         * clared.
         */
        public void outputLineCleared(OutputEvent ev) {
            if (errAnnot != null) {
                errAnnot.detach();
            }
            if (!links.isEmpty()) {
                links.clear();
            }
        }
        
        public void outputLineSelected(OutputEvent ev) {           
        }
    }    
}
