/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.NoSuchElementException;
import java.beans.*;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.openide.TopManager;
import org.openide.actions.ToggleBreakpointAction;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.text.*;
import org.openide.windows.CloneableOpenSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.Workspace;

import org.netbeans.modules.java.Util;

/*
/** Editor for servlet files generated from JSP files. Main features:
 * <ul>
 * <li>All text is read-only (guarded) </li>
 * <li>The editor can work on different files, reloads after recomplilation.</li>
 * </ul>
 *
 * @author Petr Jiricka, Yury Kamen
 */
public class ServletEditor extends CloneableEditorSupport 
    implements EditorCookie, CloseCookie, PrintCookie {

    /** Gets the lineset. Returns our lineset, which is a hack because the APIs don't handle dataobjects well. */
    public Line.Set getLineSet() {
	return new ServletLine.Set(super.getLineSet(), this);
    }

    /** Create a new Editor support for the given Java source.
     * @param entry the (primary) file entry representing the Java source file
     */
    public ServletEditor(JspDataObject jspdo) {
        super(new JspEnv(jspdo));
    }
    
    protected CloneableEditor createCloneableEditor () {
        return new ServletEditorComponent (this);
    }
    
    protected JspEnv jspEnv() {
        return (JspEnv)env;
    }
    
    /** Overriding the default loading from stream - need to look at encoding */
    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        FileObject fo = getServlet().getPrimaryFile();
        String encoding = JspDataObject.getFileEncoding(fo);
        InputStreamReader reader = new InputStreamReader(stream, encoding);
        kit.read(reader, doc, 0);
    }

    protected JspServletDataObject getServlet() {
        return jspEnv().getJspDataObject().getServletDataObject();
    }

    /** Constructs message that should be displayed when the data object
    * is modified and is being closed.
    *
    * @return text to show to the user
    */
    protected String messageSave () {
        return "";
    }

    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    protected String messageName () {
        DataObject dobj = getServlet();
        if (dobj != null) {
            return dobj.getName();
        }
        else {
            return "...";
        }
    }
    
    /** Text to use as tooltip for component.
    *
    * @return text to show to the user
    */
    protected String messageToolTip () {
        DataObject dobj = getServlet();
        if (dobj != null) {
            // update tooltip
            FileObject fo = dobj.getPrimaryFile ();
            try {
                return NbBundle.getMessage (ServletEditor.class, "LAB_EditorToolTip_Valid", new Object[] {
                    fo.getPackageName ('.'),
                    fo.getName (),
                    fo.getExt (),
                    fo.getFileSystem ().getDisplayName ()
                });
            } catch (FileStateInvalidException fsie) {
                return NbBundle.getMessage (ServletEditor.class, "LAB_EditorToolTip_Invalid", new Object[] {
                    fo.getPackageName ('.'),
                    fo.getName (),
                    fo.getExt ()
                });
            }
        }
        else
            return "...";
    }

    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpening () {
        DataObject obj = getServlet();
        if (obj == null)
            return "";

        return NbBundle.getMessage (EditorSupport.class , "CTL_ObjectOpen", // NOI18N
            obj.getName(),
            obj.getPrimaryFile().toString()
        );
    }
    

    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpened () {
        DataObject obj = getServlet();
        if (obj == null)
            return "";

        return NbBundle.getMessage (EditorSupport.class, "CTL_ObjectOpened", // NOI18N
            obj.getName (),
            obj.getPrimaryFile ().toString ()
        );
    }

    /** only accessibility method */
    MultiDataObject.Entry getJavaEntry() {
        return getServlet().getPrimaryEntry();
    }


    public static class ServletEditorComponent extends CloneableEditor {

        /** For externalization. */
        public ServletEditorComponent() {
            super();
        }

        public ServletEditorComponent(ServletEditor support) {
            super(support);
            init();
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            init();
        }

        protected JEditorPane getPane() {
            return pane;
        }
        
        /*protected CloneableEditorSupport cloneableEditorSupport() {
            return super.cloneableEditorSupport();
        }*/

        /** Called after creation of this object and after deexternalization. */
        private void init() {
            // set the pane read only
            if ( null != getPane()) {
                getPane().setEditable(false);
            }
            // set the activated nodes
            setCorrectActivatedNodes();
            // register a listener to set activated nodes after a change of the servlet
            ServletEditor se = (ServletEditor)cloneableEditorSupport();
            if (se != null) {
                se.jspEnv().addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (JspEnv.PROP_TIME.equals(evt.getPropertyName()))
                                setCorrectActivatedNodes();
                                ServletEditorComponent.this.updateName();
                                
                        }
                    }
                );
            }
        }
        
        void setCorrectActivatedNodes() {
            ServletEditor se = (ServletEditor)cloneableEditorSupport();
            if (se != null) {
                JspDataObject jspdo = se.jspEnv().getJspDataObject();
                DataObject servlet = jspdo.getServletDataObject();
                if (servlet != null)
                    setActivatedNodes(new Node[] { servlet.getNodeDelegate() });
                else
                    setActivatedNodes(new Node[] { jspdo.getNodeDelegate() });
            }
        }
        
    } // JspServletEditorComponent
    
    private static class JspEnv implements CloneableEditorSupport.Env,
        java.io.Serializable, PropertyChangeListener {
            
        private static final long serialVersionUID = -5748207023470614141L;
        
        /** JSP page for which we are displaying the servlets. */
        protected JspDataObject jspdo;
        
        /** support for firing of property changes
        */
        private transient PropertyChangeSupport propSupp;

        public JspEnv(JspDataObject jspdo) {
            this.jspdo = jspdo;
            init();
        }
        
        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            init();
        }
        
        private void init() {
            jspdo.addPropertyChangeListener(WeakListener.propertyChange (this, jspdo));
        }
        
        public JspDataObject getJspDataObject() {
            return jspdo;
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            if (JspDataObject.PROP_SERVLET_DATAOBJECT.equals (ev.getPropertyName())) {
                DataObject servlet = jspdo.getServletDataObject();
                if (servlet == null) {
                    firePropertyChange(PROP_VALID, new Boolean(true), new Boolean(false));
                }
                else {
                    firePropertyChange(PROP_TIME, null, null);
                }
            }
            
            firePropertyChange (
                ev.getPropertyName (),
                ev.getOldValue (),
                ev.getNewValue ()
            );
        }
        
        /** Fires property change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        protected void firePropertyChange (String name, Object oldValue, Object newValue) {
            prop ().firePropertyChange (name, oldValue, newValue);
        }
        
        /** Lazy getter for change support.
        */
        private PropertyChangeSupport prop () {
            if (propSupp == null) {
                synchronized (this) {
                    if (propSupp == null) {
                        propSupp = new PropertyChangeSupport (this);
                    }
                }
            }
            return propSupp;
        }
        
        /** Obtains the input stream.
        * @exception IOException if an I/O error occures
        */
        public InputStream inputStream () throws IOException {
            DataObject servlet = jspdo.getServletDataObject();
            if (servlet != null)
                return servlet.getPrimaryFile().getInputStream();
            else
                return null;
        }

        /** Obtains the output stream.
        * @exception IOException if an I/O error occures
        */
        public OutputStream outputStream () throws IOException {
            // this file is read only, don't need an input stream
            return null;
        }

        /** The time when the data has been modified
        */
        public Date getTime () {
            /*DataObject servlet = jspdo.getServletDataObject();
            if (servlet != null)
                return servlet.getPrimaryFile().lastModified();
            else*/
                return null;
        }

        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType () {
            return "text/x-java";
        }
        /** Adds property listener.
        */
        public void addPropertyChangeListener (PropertyChangeListener l) {
            prop ().addPropertyChangeListener (l);
        }
        /** Removes property listener.
        */
        public void removePropertyChangeListener (PropertyChangeListener l) {
            prop ().removePropertyChangeListener (l);
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
            DataObject servlet = jspdo.getServletDataObject();
            return (servlet != null);
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
        public void markModified () throws java.io.IOException {
            // do nothing
        }

        /** Reverse method that can be called to make the environment 
        * unmodified.
        */
        public void unmarkModified () {
        }

        /** Method that allows environment to find its 
        * cloneable open support.
        */
        public CloneableOpenSupport findCloneableOpenSupport () {
            return (CloneableOpenSupport)jspdo.getServletEditor();
        }
    }
}
