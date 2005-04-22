/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.text;


import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.CloneableOpenSupport;


/**
 * Empty implementstion of <code>CloneableEditorSupport</code>.
 * Helper test class. It's used by regression test.
 * deadlock from bug #12557.
 *
 * @author  Peter Zavadsky
 */
public class SimpleCESHidden extends CloneableEditorSupport {
    
    private FileObject fo;

    public SimpleCESHidden(Env env) {
        super(env);
        
        this.fo = env.fo;
    }
        
    /** Message to display when an object is being opened.
     * @return the message or null if nothing should be displayed
    */
    protected String messageOpening() {
        return "Opening " + fo.getName();
    }
    
    /** Message to display when an object has been opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpened() {
        return "Opened " + fo.getName();
    }
    
    /** Constructs message that should be used to name the editor component.
     *
     * @return name of the editor
     */
    protected String messageName() {
        return fo.getName();
    }
    
    /** Constructs message that should be displayed when the data object
     * is modified and is being closed.
     *
     * @return text to show to the user
     */
    protected String messageSave() {
        return fo.getName() + "*";
    }
    
    /** Text to use as tooltip for component.
     *
     * @return text to show to the user
     */
    protected String messageToolTip() {
        return fo.getNameExt();
    }

    
    /** Helper Env implementation. */
    public static class Env extends Object implements CloneableEditorSupport.Env {
            
        /** object to serialize and be connected to*/
        private FileObject fo;

        /** Reference to support instance. */
        private CloneableOpenSupport support;
        

        /** Constructor. Attaches itself as listener to 
        * the data object so, all property changes of the data object
        * are also rethrown to own listeners.
        *
        * @param obj data object to be attached to
        */
        public Env (FileObject fo) {
            this.fo = fo;
        }


        public void setSupport(CloneableOpenSupport support) {
            this.support = support;
        }
        
        /** Method that allows environment to find its 
         * cloneable open support.
         * @return the support or null if the environemnt is not in valid 
         * state and the CloneableOpenSupport cannot be found for associated
         * data object
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return support;
        }
        
        /** Obtains the input stream.
         * @exception IOException if an I/O error occures
         */
        public InputStream inputStream () throws IOException {
            return fo.getInputStream();
        }

        /** Obtains the output stream.
         * @exception IOException if an I/O error occures
         */
        public OutputStream outputStream () throws IOException {
            return fo.getOutputStream(fo.lock());
        }

        /** The time when the data has been modified */
        public Date getTime () {
            return null;
        }

        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType () {
            return fo.getMIMEType();
        }
        
        /** Adds property listener.
        */
        public void addPropertyChangeListener (PropertyChangeListener l) {
        }
        
        /** Removes property listener.
        */
        public void removePropertyChangeListener (PropertyChangeListener l) {
        }

        /** Adds veto listener.
        */
        public void addVetoableChangeListener (VetoableChangeListener l) {
        }
        /** Removes veto listener.
        */
        public void removeVetoableChangeListener (VetoableChangeListener l) {
        }

        /** Test whether the support is in valid state or not.
        * It could be invalid after deserialization when the object it
        * referenced to does not exist anymore.
        *
        * @return true or false depending on its state
        */
        public boolean isValid () {
            return fo.isValid();
        }
        
        /** Test whether the object is modified or not.
        * @return true if the object is modified
        */
        public boolean isModified () {
            return false;
        }

        /** Support for marking the environement modified.
        * @exception IOException if the environment cannot be marked modified
        *    (for example when the file is readonly), when such exception
        *    is the support should discard all previous changes
        */
        public void markModified () throws java.io.IOException {}

        /** Reverse method that can be called to make the environment 
        * unmodified.
        */
        public void unmarkModified () {}

    } // End of Env class.
}
