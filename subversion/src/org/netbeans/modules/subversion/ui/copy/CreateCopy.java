/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.copy;

import java.awt.Dialog;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.CreateFolderAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * // XXX could be a descriptor subclass
 * @author Tomas Stupka
 */
public class CreateCopy implements DocumentListener, FocusListener {

    private RepositoryFile repositoryRoot;
    private String context;
    private Object value;
    private CreateCopyPanel panel;
    private DialogDescriptor dialogDescriptor;
    private Dialog dialog;
    
    /** Creates a new instance of CreateCopy */
    public CreateCopy(RepositoryFile repositoryRoot, String context) {
        this.repositoryRoot = repositoryRoot;
        this.context = context;
        panel = new CreateCopyPanel();
        ((JTextField) panel.urlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        panel.messageTextArea.getDocument().addDocumentListener(this);
        
        dialogDescriptor = new DialogDescriptor(panel, "Copy " + context + " to"); // XXX
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(CreateCopyAction.class));
        dialogDescriptor.setValid(false);                                
        
        RepositoryPaths repositoryPaths = 
            new RepositoryPaths(
                repositoryRoot, 
                (JTextField) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton
            );
        repositoryPaths.setupBrowserBehavior(true, false, new BrowserAction[] { new CreateFolderAction(context)} );
        
        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
    }
    
    public void show() {                        
        dialog.setVisible(true);
        value = dialogDescriptor.getValue();       
    }

    public Object getValue() {
        return value;
    }
    
    public SVNUrl getRepositoryFileUrl() {
        return repositoryRoot.getRepositoryUrl().appendPath(((JTextField)panel.urlComboBox.getEditor().getEditorComponent()).getText());
    }

    public void insertUpdate(DocumentEvent e) {
        validateUserInput();        
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput();        
    }

    public void changedUpdate(DocumentEvent e) {        
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        validateUserInput();
    }

    private boolean validateUserInput() {
        // XXX error message
        
        String text = ((JTextField)panel.urlComboBox.getEditor().getEditorComponent()).getText();
        if (text == null || text.length() == 0) {            
            dialogDescriptor.setValid(false);
            return false;
        }
        
        try {
            new SVNUrl(repositoryRoot.getRepositoryUrl().toString() + "/" + text);
        } catch (MalformedURLException ex) {
            dialogDescriptor.setValid(false);
            return false;
        }
        
        text = panel.messageTextArea.getText();
        if (text == null || text.length() == 0) {            
            dialogDescriptor.setValid(false);
            return false;
        }
        
        dialogDescriptor.setValid(true);
        return true;
    }    

    String getMessage() {
        return panel.messageTextArea.getText();
    }
}
