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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.parser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.loaders.CppEditorSupport;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

public class CppMetaModel implements PropertyChangeListener {

    // TODO: need to get reparse time from settings
    private int reparseDelay = 1000;
    
    /** map of all files we're interested in */
    private Map<String,CppFile> map = Collections.synchronizedMap(new HashMap<String,CppFile>());

    private Set<ParsingListener> listeners = new HashSet<ParsingListener>();

    private static CppMetaModel instance;

    private static RequestProcessor cppParserRP;

    private static final Logger log = Logger.getLogger(CppMetaModel.class.getName());

    private CppMetaModel() {
	//log.log(Level.FINE, "CppMetaModel: Constructor");
    }

    public static CppMetaModel getDefault() {
	if (instance == null) {
	    instance = new CppMetaModel();
            TopComponent.getRegistry().addPropertyChangeListener(instance);

	}
	return instance;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())){
            checkClosed(evt.getNewValue());
        }
    }

    private void checkClosed(Object o){
        if (o instanceof Set) {
            Set<CppEditorSupport.CppEditorComponent> editors = new HashSet<CppEditorSupport.CppEditorComponent>();
            for(Object top : (Set)o){
                if (top instanceof CppEditorSupport.CppEditorComponent){
                    editors.add((CppEditorSupport.CppEditorComponent)top);
                }
            }
            checkClosed(editors);
        }
    }
    
    private void checkClosed(Set<CppEditorSupport.CppEditorComponent> editors){
        Set<String> opened = new HashSet<String>();
        for (CppEditorSupport.CppEditorComponent editor : editors) {
            CppEditorSupport support = editor.getSupport();
            if (support != null) {
                Document doc = support.getDocument();
                if (doc != null) {
                    String tittle = (String) doc.getProperty(Document.TitleProperty);
                    opened.add(tittle);
                }
            }
        }
        List<String> toDelete = new ArrayList<String>();
        for(String title : map.keySet()){
            if (!opened.contains(title)){
                toDelete.add(title);
            }
        }
        for(String title : toDelete){
            map.remove(title);
        }
        if (map.size() == 0){
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    // Helper methods for awhile...
    private static synchronized RequestProcessor getCppParserRP() {
	if (cppParserRP == null) {
	    cppParserRP = new RequestProcessor("CPP Parser", 1); // NOI18N
	}
	return cppParserRP;
    }

    // we need to provide mechanism for handling only most recent changes and 
    // reject the unnecessary ones, so cancel previous one and create new task 
    // using delay
    private RequestProcessor.Task task = null;
    public void scheduleParsing(final Document doc) {

	final String title = (String) doc.getProperty(Document.TitleProperty);
	log.log(Level.FINE, "CppMetaModel.scheduleParsing: Checking " + getShortName(doc) +
		" [" + Thread.currentThread().getName() + "]"); // NOI18N
	final CppFile file = map.get(title);
        // try to cancel task
        if (task != null) {
            task.cancel();
        }
	if (file == null) {
	    log.log(Level.FINE, "CppMetaModel.scheduleParsing: Starting initial parse for " +
			getShortName(doc));
	    task = getCppParserRP().post(new Runnable() {
		public void run() {
		    CppFile file = new CppFile(title);
		    map.put(title, file);
		    file.startParsing(doc);
                    fireObjectParsed(doc);
		}
	    }, reparseDelay);
	} else if (file.needsUpdate()) {
	    log.log(Level.FINE, "CppMetaModel.scheduleParsing: Starting update parse for " +
			getShortName(doc));
	    task = getCppParserRP().post(new Runnable() {
		public void run() {
		    file.startParsing(doc);
                    fireObjectParsed(doc);
		}
	    }, reparseDelay);
	} /*else {
	    DataObject dobj;
	    Object o = doc.getProperty(Document.StreamDescriptionProperty);
	    if (o instanceof DataObject) {
		dobj = (DataObject) o;
		log.log(Level.FINE, "CppMetaModel.scheduleParsing: Existing record for " + getShortName(doc));
	    }
	}*/
    }
    
    private void fireObjectParsed(Document doc)
    {
        synchronized (listeners)
        {
	    Object o = doc.getProperty(Document.StreamDescriptionProperty);
            DataObject dobj = (o instanceof DataObject) ? (DataObject) o : null;
//            for(Iterator it = listeners.iterator(); it.hasNext();)
//            {
//                ((ParsingListener)it.next()).objectParsed(new ParsingEvent(dobj));
//            }
            // vk++ had to change the code above to avoid concurrent modification exception
            ParsingListener[] alist = new ParsingListener[listeners.size()];
            listeners.toArray(alist);
            for (int i = 0; i < alist.length; i++) {
                alist[i].objectParsed(new ParsingEvent(dobj));
            }
            // vk--
            
        }        
    }

    private String getShortName(Document doc) {
	String longname = (String) doc.getProperty(Document.TitleProperty);
	int slash = longname.lastIndexOf(java.io.File.separatorChar);

	if (slash != -1) {
	    return longname.substring(slash + 1);
	} else {
	    return longname;
	}
    }

    public CppFile get(String key) {
	return map.get(key);
    }

    public void addParsingListener(ParsingListener listener) {
	//log.log(Level.FINE, "CppMetaModel: addParsingListener");
	synchronized (listeners) {
	    listeners.add(listener);
	}
    }

    public void removeParsingListener(ParsingListener listener) {
	//log.log(Level.FINE, "CppMetaModel: removeParsingListener");
	synchronized (listeners) {
	    listeners.remove(listener);
	}
    }

    private synchronized void fireParsingEvent(ParsingEvent evt) {
        List<ParsingListener> list = new ArrayList<ParsingListener>(listeners);
	for (ParsingListener listener : list) {
	    listener.objectParsed(evt);
	}
    }
}
