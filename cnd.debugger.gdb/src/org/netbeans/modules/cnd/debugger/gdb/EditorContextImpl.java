/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import javax.swing.JEditorPane;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;

import org.netbeans.modules.cnd.debugger.gdb.breakpoints.DebuggerAnnotation;

/**
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class EditorContextImpl extends EditorContext {
    
    private static String fronting = System.getProperty("netbeans.debugger.fronting"); // NOI18N
    
    private PropertyChangeSupport   pcs;
    private Map                     annotationToURL = new HashMap();
    private ChangeListener          changedFilesListener;
    private Map                     timeStampToRegistry = new HashMap();
    private Set                     modifiedDataObjects;
    private PropertyChangeListener  editorObservableListener;

    private Lookup.Result resDataObject;
    private Lookup.Result resEditorCookie;
    private Lookup.Result resNode;

    private Object currentLock = new Object();
    private String currentURL = null;
    private EditorCookie currentEditorCookie = null;
    
    
    {
        pcs = new PropertyChangeSupport(this);

        resDataObject = Utilities.actionsGlobalContext().lookup(new Lookup.Template(DataObject.class));
        resDataObject.addLookupListener(new EditorLookupListener(DataObject.class));

        resEditorCookie = Utilities.actionsGlobalContext().lookup(new Lookup.Template(EditorCookie.class));
        resEditorCookie.addLookupListener(new EditorLookupListener(EditorCookie.class));

        resNode = Utilities.actionsGlobalContext().lookup(new Lookup.Template(Node.class));
        resNode.addLookupListener(new EditorLookupListener(Node.class));

    }
    
    
    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    public boolean showSource(String url, int lineNumber, Object timeStamp) {
        Line l = getLine(url, lineNumber, timeStamp); // false = use original ln
        if (l == null) {
            return false;
        }
        if (fronting != null) {
            if (fronting.equals("true"))
                l.show(Line.SHOW_TOFRONT); //FIX 47825
            else
                l.show(Line.SHOW_GOTO);
            return true;
        }
        if (Utilities.isWindows()) {
            l.show(Line.SHOW_TOFRONT); //FIX 47825
        } else  {
            l.show(Line.SHOW_GOTO);
        }
        return true;
    }
    
    
    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    public void createTimeStamp(Object timeStamp) {
        modifiedDataObjects = new HashSet(DataObject.getRegistry().getModifiedSet());
        Registry r = new Registry();
        timeStampToRegistry.put(timeStamp, r);
        Iterator i = modifiedDataObjects.iterator();
        while (i.hasNext()) {
            r.register ((DataObject) i.next ());
        }
        if (changedFilesListener == null) {
            changedFilesListener = new ChangedFilesListener();
            DataObject.getRegistry().addChangeListener(changedFilesListener);
        }
    }

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    public void disposeTimeStamp(Object timeStamp) {
        timeStampToRegistry.remove(timeStamp);
        if (timeStampToRegistry.isEmpty()) {
            DataObject.getRegistry().removeChangeListener(changedFilesListener);
            changedFilesListener = null;
        }
    }
    
    public Object annotate(String url, int lineNumber, String annotationType, Object timeStamp) {
        Line l =  getLine(url, lineNumber, timeStamp);
        if (l == null) {
            return null;
        }
        DebuggerAnnotation annotation = new DebuggerAnnotation(annotationType, l);
        annotationToURL.put(annotation, url);
        
        return annotation;
    }

    /**
     * Removes given annotation.
     *
     * @return true if annotation has been successfully removed
     */
    public void removeAnnotation(Object a) {
        DebuggerAnnotation annotation = (DebuggerAnnotation) a;
        annotation.detach();
        
        if (annotationToURL.remove(annotation) == null) {
            return;
        }
    }

    /**
     * Returns line number given annotation is associated with.
     *
     * @param annotation a annotation
     * @param timeStamp a time stamp to be used
     *
     * @return line number given annotation is associated with
     */
    public int getLineNumber(Object annotation, Object timeStamp) {
        DebuggerAnnotation a = (DebuggerAnnotation) annotation;
        if (timeStamp == null) {
            return a.getLine().getLineNumber() + 1;
        }
        String url = (String) annotationToURL.get(a);
        Line.Set lineSet = getLineSet(url, timeStamp);
        return lineSet.getOriginalLineNumber(a.getLine()) + 1;
    }
    
    /**
     * Updates timeStamp for gived url.
     *
     * @param timeStamp time stamp to be updated
     * @param url an url
     */
    public void updateTimeStamp(Object timeStamp, String url) {
        Registry registry = (Registry) timeStampToRegistry.get(timeStamp);
        registry.register(getDataObject(url));
    }

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public int getCurrentLineNumber() {
        EditorCookie e = getCurrentEditorCookie();
        if (e == null) {
            return -1;
        }
        JEditorPane ep = getCurrentEditor();
        if (ep == null) {
            return -1;
        }
        StyledDocument d = e.getDocument();
        if (d == null) {
            return -1;
        }
        Caret caret = ep.getCaret();
        if (caret == null) {
            return -1;
        }
        int ln = NbDocument.findLineNumber(d, caret.getDot());
        return ln + 1;
    }

    /**
     * Returns number of line most recently selected in editor or <code>-1</code>.
     *
     * @return number of line most recently selected in editor or <code>-1</code>
     */
    public int getMostRecentLineNumber() {
        EditorCookie e = getMostRecentEditorCookie();
        if (e == null) {
            return -1;
        }
        JEditorPane ep = getMostRecentEditor();
        if (ep == null) {
            return -1;
        }
        StyledDocument d = e.getDocument();
        if (d == null) {
            return -1;
        }
        Caret caret = ep.getCaret();
        if (caret == null) {
            return -1;
        }
        int ln = NbDocument.findLineNumber(d, caret.getDot());
        return ln + 1;
    }
    
    /**
     * Returns URL of source currently selected in editor or empty string.
     *
     * @return URL of source currently selected in editor or empty string
     */
    public String getCurrentURL() {
        synchronized (currentLock) {
            if (currentURL == null) {
                DataObject[] nodes = (DataObject[]) resDataObject.allInstances().toArray(new DataObject[0]);

                currentURL = ""; // NOI18N
                if (nodes.length != 1) {
                    return currentURL;
                }
                
                DataObject dobj = nodes[0];
                if (dobj instanceof DataShadow) {
                    dobj = ((DataShadow) dobj).getOriginal();
                }

                try {
                    currentURL = dobj.getPrimaryFile().getURL().toString();
                } catch (FileStateInvalidException ex) {
                    //noop
                }
            }

            return currentURL;
        }
    }
    
    /**
     *  Return the most recent URL or empty string. The difference between this and getCurrentURL()
     *  is that this one will return a URL when the editor has lost focus.
     *
     *  @return url in string form
     */
    public String getMostRecentURL() {
	String url = getCurrentURL();
	
	if (url.length() == 0) {
	    Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
	    if (nodes != null) {
		for (int i = 0; i < nodes.length; i++) {
		    DataObject dobj = (DataObject) nodes[i].getCookie(DataObject.class);
		    if (dobj != null) {
			try {
			    url = dobj.getPrimaryFile().getURL().toExternalForm();
			    break;
			} catch (FileStateInvalidException ex) {
			}
		    }
		}
	    }
	}
	return url;
    }

    /**
     * Returns name of method currently selected in editor or empty string.
     *
     * @return name of method currently selected in editor or empty string
     */
    public String getCurrentFunctionName() {
        return "";  // FIXUP
    }

    /**
     * Returns function name currently selected in editor or empty string.
     *
     * @return function name currently selected in editor or empty string
     */
    public String getSelectedFunctionName() {
        EditorCookie e = getCurrentEditorCookie();
        if (e == null) {
            return "";  // NOI18N
        }
        JEditorPane ep = getCurrentEditor();
        if (ep == null) {
            return "";  // NOI18N
        }
        StyledDocument doc = e.getDocument();
        if (doc == null) {
            return "";  // NOI18N
        }
        int offset = ep.getCaret().getDot();
        String t = null;
//        if ( (ep.getSelectionStart () <= offset) &&
//             (offset <= ep.getSelectionEnd ())
//        )   t = ep.getSelectedText ();
//        if (t != null) return t;
        
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        // XXX - Fixup
        /*
        try {
            javax.swing.text.Element lineElem = 
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) return "";
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset () - lineStartOffset;
            // t contains current line in editor
            t = doc.getText (lineStartOffset, lineLen);
            
            int identStart = col;
            while ( identStart > 0 && 
                    Character.isJavaIdentifierPart (
                        t.charAt (identStart - 1)
                    )
            )   identStart--;

            int identEnd = col;
            while (identEnd < lineLen && 
                   Character.isJavaIdentifierPart (t.charAt (identEnd))
            ) {
                identEnd++;
            }
            int i = t.indexOf ('(', identEnd);
            if (i < 0) return "";
            if (t.substring (identEnd, i).trim ().length () > 0) return "";

            if (identStart == identEnd) return "";
            return t.substring (identStart, identEnd);
        } catch (BadLocationException ex) {
            return ""; // NOI18N
        }
            */
        return ""; // XXX - Fixup
    }
    
    /**
     * Returns line number of given field in given class.
     *
     * @param url the url of file the class is deined in
     * @param className the name of class (or innerclass) the field is 
     *                  defined in
     * @param fieldName the name of field
     *
     * @return line number or -1
     */
    public int getFieldLineNumber(String url, String className, String fieldName) {
        DataObject dataObject = getDataObject(url);
        if (dataObject == null) {
            return -1;
        }
        /*
        SourceCookie.Editor sc = (SourceCookie.Editor) dataObject.getCookie(SourceCookie.Editor.class);
        if (sc == null) {
            return -1;
        }
        sc.open();
        StyledDocument sd = sc.getDocument();
        if (sd == null) {
            return -1;
        }
        ClassElement[] classes = sc.getSource().getAllClasses();
        FieldElement fe = null;
        int i, k = classes.length;
        for (i = 0; i < k; i++) {
            if (classes [i].getName().getFullName().equals(className)) {
                fe = classes [i].getField(Identifier.create(fieldName));
                break;
            }
        }
        if (fe == null) {
            return -1;
        }
        int position = sc.sourceToText(fe).getStartOffset();
        return NbDocument.findLineNumber(sd, position) + 1;
         */
        return -1;
    }
    
    /**
     * Get the MIME type of the current file.
     *
     * @return The MIME type of the current file
     */
    public String getCurrentMIMEType() {
        FileObject fo;
        
        synchronized (currentLock) {
            DataObject[] nodes = (DataObject[]) resDataObject.allInstances().toArray(new DataObject[0]);

            if (nodes.length != 1) {
                return ""; // NOI18N
            }

            DataObject dobj = nodes[0];
            if (dobj instanceof DataShadow) {
                dobj = ((DataShadow) dobj).getOriginal();
            }

            try {
                fo = URLMapper.findFileObject(dobj.getPrimaryFile().getURL());
            } catch (FileStateInvalidException ex) {
                fo = null;
            }

            return fo != null ? fo.getMIMEType() : ""; // NOI18N
        }
    }
    
    /**
     * Get the MIME type of the most recently selected file.
     *
     * @return The MIME type of the most recent selected file
     */
    public String getMostRecentMIMEType() {
	String mime = getCurrentMIMEType();
        
	if (mime.length() == 0) {
	    Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
	    if (nodes != null) {
		for (int i = 0; i < nodes.length; i++) {
		    DataObject dobj = (DataObject) nodes[i].getCookie(DataObject.class);
		    if (dobj != null) {
			if (dobj instanceof DataShadow) {
			    dobj = ((DataShadow) dobj).getOriginal();
			}
			try {
			    FileObject fo = URLMapper.findFileObject(dobj.getPrimaryFile().getURL());
			    mime = fo.getMIMEType();
			    break;
			} catch (FileStateInvalidException ex) {
			}
		    }
		}
	    }
	}
	return mime;
    }
    
    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    /**
     * Adds a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to add
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener(propertyName, l);
    }
    
    /**
     * Removes a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to remove
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener(propertyName, l);
    }
    
    private JEditorPane getCurrentEditor() {
        EditorCookie e = getCurrentEditorCookie();
        if (e == null){
            return null;
        }
        JEditorPane[] op = e.getOpenedPanes();
        // We listen on open panes if e implements EditorCookie.Observable
        if ((op == null) || (op.length < 1)) {
            return null;
        }
        return op [0];
    }
    
    private JEditorPane getMostRecentEditor() {
        EditorCookie e = getMostRecentEditorCookie();
        if (e == null){
            return null;
        }
        JEditorPane[] op = e.getOpenedPanes();
        // We listen on open panes if e implements EditorCookie.Observable
        if ((op == null) || (op.length < 1)) {
            return null;
        }
        return op [0];
    }
    
    private EditorCookie getCurrentEditorCookie() {
        synchronized (currentLock) {
            if (currentEditorCookie == null) {
                TopComponent tc = TopComponent.getRegistry().getActivated();
                if (tc != null) {
                    currentEditorCookie = (EditorCookie) tc.getLookup().lookup(EditorCookie.class);
                }
                // Listen on open panes if currentEditorCookie implements EditorCookie.Observable
                if (currentEditorCookie instanceof EditorCookie.Observable) {
                    if (editorObservableListener == null) {
                        editorObservableListener = new EditorLookupListener(EditorCookie.Observable.class);
                    }
                    ((EditorCookie.Observable) currentEditorCookie).addPropertyChangeListener(editorObservableListener);
                }
            }
            return currentEditorCookie;
        }
    }
    
    private EditorCookie getMostRecentEditorCookie() {
	EditorCookie ec = getCurrentEditorCookie();
	
	if (ec == null) {
	    Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
	    if (nodes != null) {
		for (int i = 0; i < nodes.length; i++) {
		    ec = (EditorCookie) nodes[i].getCookie(EditorCookie.class);
		    if (ec != null) {
			System.err.println("Got it!");
		    }
		}
	    }
//	    // Listen on open panes if currentEditorCookie implements EditorCookie.Observable
//	    if (currentEditorCookie instanceof EditorCookie.Observable) {
//		if (editorObservableListener == null) {
//		    editorObservableListener = new EditorLookupListener(EditorCookie.Observable.class);
//		}
//		((EditorCookie.Observable) currentEditorCookie).addPropertyChangeListener(editorObservableListener);
//	    }
	}
	return ec;
    }

    private Line.Set getLineSet(String url, Object timeStamp) {
        DataObject dataObject = getDataObject(url);
        if (dataObject == null) {
            return null;
        }
        
        if (timeStamp != null) {
            // get original
            Registry registry = (Registry) timeStampToRegistry.get(timeStamp);
            Line.Set ls = null;
            
            // Workaround for #54754 
            try {
                ls = registry.getLineSet(dataObject);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            
            if (ls != null) {
                return ls;
            }
        }
        
        // get current
        LineCookie lineCookie = (LineCookie) dataObject.getCookie(LineCookie.class);
        if (lineCookie == null) {
            return null;
        }
        return lineCookie.getLineSet();
    }

    private Line getLine(String url, int lineNumber, Object timeStamp) {
        Line.Set ls = getLineSet(url, timeStamp);
        if (ls == null) {
            return null;
        }
        try {
            if (timeStamp == null)
                return ls.getCurrent(lineNumber - 1);
            else
                return ls.getOriginal(lineNumber - 1);
        } catch (IndexOutOfBoundsException e) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }

    private static DataObject getDataObject(String url) {
        FileObject file;
        assert (!(((url == null || !url.startsWith("file:"))) && Boolean.getBoolean("gdb.assertions.enabled"))); // NOI18N
        
        try {
            file = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            assert !Boolean.getBoolean("gdb.assertions.enabled"); // NOI18N
            return null;
        } catch (Exception ex) {
            assert !Boolean.getBoolean("gdb.assertions.enabled"); // NOI18N
            return null; // DEBUG code (remove before checkin)
        }

        if (file == null) {
            return null;
        }
        try {
            return DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }
    
    private static class Registry {
        
        private Map dataObjectToLineSet = new HashMap();
        
        void register(DataObject dataObject) {
            LineCookie lc = (LineCookie) dataObject.getCookie(LineCookie.class);
            if (lc == null) {
                return;
            }
            dataObjectToLineSet.put(dataObject, lc.getLineSet());
        }
        
        Line.Set getLineSet (DataObject dataObject) {
            return (Line.Set) dataObjectToLineSet.get(dataObject);
        }
    }
    
    private class ChangedFilesListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Set newDOs = new HashSet(DataObject.getRegistry().getModifiedSet());
            newDOs.removeAll(modifiedDataObjects);
            Iterator i1 = timeStampToRegistry.values().iterator();
            while (i1.hasNext()) {
                Registry r = (Registry) i1.next();
                Iterator i2 = newDOs.iterator();
                while (i2.hasNext()) {
                    r.register ((DataObject) i2.next());
                }
            }
            modifiedDataObjects = new HashSet(DataObject.getRegistry().getModifiedSet());
        }
    }
    
    private class EditorLookupListener extends Object implements LookupListener, PropertyChangeListener {
        
        private Class type;
        
        public EditorLookupListener(Class type) {
            this.type = type;
        }
        
        public void resultChanged(LookupEvent ev) {
            if (type == DataObject.class) {
                synchronized (currentLock) {
                    currentURL = null;
                    if (currentEditorCookie instanceof EditorCookie.Observable) {
                        ((EditorCookie.Observable) currentEditorCookie).
                                removePropertyChangeListener(editorObservableListener);
                    }
                    currentEditorCookie = null;
                }
                pcs.firePropertyChange(TopComponent.Registry.PROP_CURRENT_NODES, null, null);
            } else if (type == EditorCookie.class) {
                synchronized (currentLock) {
                    currentURL = null;
                    if (currentEditorCookie instanceof EditorCookie.Observable) {
                        ((EditorCookie.Observable) currentEditorCookie).
                                removePropertyChangeListener(editorObservableListener);
                    }
                    currentEditorCookie = null;
                }
                pcs.firePropertyChange(TopComponent.Registry.PROP_CURRENT_NODES, null, null);
            } else if (type == Node.class) {
                pcs.firePropertyChange (TopComponent.Registry.PROP_CURRENT_NODES, null, null);
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_OPENED_PANES)) {
                pcs.firePropertyChange(EditorCookie.Observable.PROP_OPENED_PANES, null, null);
            }
        }
        
    }
}
