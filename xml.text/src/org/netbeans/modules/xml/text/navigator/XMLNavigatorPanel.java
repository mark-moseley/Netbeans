/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.navigator;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.UserQuestionException;

/** An implementation of NavigatorPanel for XML navigator.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class XMLNavigatorPanel implements NavigatorPanel {
    
    private NavigatorContent navigator;
    
    private Lookup.Result selection;
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            navigate(selection.allInstances());
        }
    };
    
    private DocumentModel model;
    
    /** public no arg constructor needed for system to instantiate the provider. */
    public XMLNavigatorPanel() {
        navigator = new NavigatorContent();
    }
    
    public String getDisplayHint() {
        return "XML files navigator";
    }
    
    public String getDisplayName() {
        return "XML View";
    }
    
    public JComponent getComponent() {
        return navigator;
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    public void panelActivated(Lookup context) {
        selection = context.lookup(new Lookup.Template(DataObject.class));
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        selection.removeLookupListener(selectionListener);
        selection = null;
    }
    
    public void navigate(Collection/*<DataObject>*/ selectedFiles) {
        if(selectedFiles.size() == 1) {
            //create document model
            final DataObject d = (DataObject) selectedFiles.iterator().next();
            EditorCookie ec = (EditorCookie)d.getCookie(EditorCookie.class);
            if(ec == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "The DataObject " + d.getName() + "(class=" + d.getClass().getName() + ") has no EditorCookie!?");
            } else {
                try {
                    //test if the document is opened in editor
                    BaseDocument bdoc = (BaseDocument)ec.openDocument();
                    //create UI
                    if(bdoc != null) navigator.navigate(bdoc);
                    
                }catch(UserQuestionException uqe) {
                    //do not open a question dialog when the document is just loaded into the navigator
                    navigator.showDocumentTooLarge();
                }catch(IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }
    
}
