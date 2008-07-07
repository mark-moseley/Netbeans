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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.subversion.ui.repository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.diff.options.AccessibleJFileChooser;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class Repository implements ActionListener, DocumentListener, ItemListener {
    
    public final static int FLAG_URL_EDITABLE           = 2;
    public final static int FLAG_URL_ENABLED            = 4;
    public final static int FLAG_ACCEPT_REVISION        = 8;
    public final static int FLAG_SHOW_REMOVE            = 16;
    public final static int FLAG_SHOW_HINTS             = 32;    
    public final static int FLAG_SHOW_PROXY             = 64;    
               
    private ConnectionType currentPanel;
    private RepositoryPanel repositoryPanel;
    private boolean valid = true;
    private List<PropertyChangeListener> listeners;
    
    private RepositoryConnection editedRC;
    
    public static final String PROP_VALID = "valid";                                                    // NOI18N

    private String message;            
    private int modeMask;
    private Dimension maxNeededSize;
    private ConnectionType http;
    private ConnectionType file;
    private ConnectionType svnSSHCli;
    private ConnectionType invalidUrlPanel;
    
    public Repository(String titleLabel) {
        this(0, titleLabel);
    }
            
    public Repository(int modeMask, String titleLabel) {
        
        this.modeMask = modeMask;
        
        initPanel();
        
        repositoryPanel.titleLabel.setText(titleLabel);
                                        
        repositoryPanel.urlComboBox.setEditable(isSet(FLAG_URL_EDITABLE));
        repositoryPanel.urlComboBox.setEnabled(isSet(FLAG_URL_ENABLED));        
        
        repositoryPanel.tipLabel.setVisible(isSet(FLAG_SHOW_HINTS));
        repositoryPanel.removeButton.setVisible(isSet(FLAG_SHOW_REMOVE));        
        repositoryPanel.removeButton.addActionListener(this);

        // retrieve the dialog size for the largest configuration
        maxNeededSize = repositoryPanel.getPreferredSize();
        
        refreshUrlHistory();
    }
    
    public void selectUrl(SVNUrl url, boolean force) {
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();
        int idx = dcbm.getIndexOf(url.toString());
        if(idx > -1) {
            dcbm.setSelectedItem(url.toString());    
        } else if(force) {
            RepositoryConnection rc = new RepositoryConnection(url.toString());
            dcbm.addElement(rc);
            dcbm.setSelectedItem(rc);
        }                        
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == repositoryPanel.removeButton) {
            onRemoveClick();
        }
    }

    private void onRemoveClick() {
        RepositoryConnection rc = getSelectedRCIntern();
        if (rc != null) {
            remove(rc);
        }
    }

    private void initPanel() {        
        repositoryPanel = new RepositoryPanel();
        http = new ConnectionType.Http(this);
        file = new ConnectionType.FileUrl(this);
        if(SvnClientFactory.isCLI()) {
            svnSSHCli = new ConnectionType.SvnSSHCli(this);
        } else {
            svnSSHCli = new ConnectionType.SvnSSHJhl(this);
        }
        invalidUrlPanel = new ConnectionType.InvalidUrl(this);

        svnSSHCli.showHints(isSet(FLAG_SHOW_HINTS));

        currentPanel = file;
        repositoryPanel.connPanel.add(file.getPanel(), BorderLayout.CENTER);
        
        repositoryPanel.urlComboBox.addActionListener(this);
        getUrlComboEditor().getDocument().addDocumentListener(this);
        repositoryPanel.urlComboBox.addItemListener(this);
        
        onSelectedRepositoryChange();
    }
    
    public void refreshUrlHistory() {
        
        List<RepositoryConnection> recentUrls = SvnModuleConfig.getDefault().getRecentUrls();
                
        Set<RepositoryConnection> recentRoots = new LinkedHashSet<RepositoryConnection>();
        recentRoots.addAll(recentUrls);                               
        
        if(repositoryPanel.urlComboBox.isEditable()) {
            // templates for supported connection methods        
            recentRoots.add(new RepositoryConnection("file:///"));      // NOI18N
            recentRoots.add(new RepositoryConnection("http://"));       // NOI18N
            recentRoots.add(new RepositoryConnection("https://"));      // NOI18N
            recentRoots.add(new RepositoryConnection("svn://"));        // NOI18N
            recentRoots.add(new RepositoryConnection("svn+ssh://"));    // NOI18N
        };
        
        ComboBoxModel rootsModel = new RepositoryModel(new Vector<RepositoryConnection>(recentRoots));                        
        repositoryPanel.urlComboBox.setModel(rootsModel);
        
        if (recentRoots.size() > 0 ) {         
            repositoryPanel.urlComboBox.setSelectedIndex(0);
            currentPanel.refresh(getSelectedRCIntern());
        }         
        
        if(repositoryPanel.urlComboBox.isEditable()) {
            JTextComponent textEditor = getUrlComboEditor();
            textEditor.selectAll();            
        }         
        updateVisibility();
    }

    public void storeRecentUrls() {
        SvnModuleConfig.getDefault().setRecentUrls(getRecentUrls());
    }
    
    public boolean isChanged() {
        List<RepositoryConnection> connections = getRecentUrls();
        List<RepositoryConnection> storedConnections = SvnModuleConfig.getDefault().getRecentUrls();        
        return !SvnUtils.equals(connections, storedConnections);
    }
    
    private List<RepositoryConnection> getRecentUrls() {
        ComboBoxModel model = repositoryPanel.urlComboBox.getModel();
        List<RepositoryConnection> ret = new ArrayList<RepositoryConnection>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            ret.add((RepositoryConnection)model.getElementAt(i));
        }
        return ret;
    }
    
    private JTextComponent getUrlComboEditor() {
        Component editor = repositoryPanel.urlComboBox.getEditor().getEditorComponent();
        JTextComponent textEditor = (JTextComponent) editor;
        return textEditor;
    }     
    
    public void setEditable(boolean editable) {
        repositoryPanel.urlComboBox.setEditable(editable);
        currentPanel.setEditable(editable);
    }
    
    public void storeConfigValues() {
        currentPanel.storeConfigValues();
    }
    
    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void changedUpdate(DocumentEvent e) { 
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {     
        Runnable awt = new Runnable() {
            public void run() {
                if (e.getDocument() == ((JTextComponent) repositoryPanel.urlComboBox.getEditor().getEditorComponent()).getDocument()) {
                    onSelectedRepositoryChange();
                } 
                validateSvnUrl();
            }
        };
        SwingUtilities.invokeLater(awt);
    }
            
    /**
     * Fast url syntax check. It can invalidate the whole step
     */
    void validateSvnUrl() {
        boolean valid = true;

        RepositoryConnection rc = null; 
        try {
            rc = getSelectedRCIntern();
            // check for a valid svnurl
            rc.getSvnUrl();                             
            if(!isSet(FLAG_ACCEPT_REVISION) && !rc.getSvnRevision().equals(SVNRevision.HEAD)) 
            {
                message = NbBundle.getMessage(Repository.class, "MSG_Repository_OnlyHEADRevision");
                valid = false;
            } else {
                // check for a valid svnrevision
                rc.getSvnRevision();
            }
        } catch (Exception ex) {             
            message = ex.getLocalizedMessage();
            valid = false;
        }        
        
        if(valid) {            
            valid = rc != null && !rc.getUrl().equals("");
            if(!currentPanel.isValid(rc)) {
                valid = false;
            }
        }
        
        setValid(valid, message);
        currentPanel.setEnabled(valid);
        repositoryPanel.removeButton.setEnabled(rc != null && rc.getUrl().length() > 0);
    }
    
    /**    
     * Always updates UI fields visibility.
     */
    private void onSelectedRepositoryChange() {
        setValid(true, "");                                                                            // NOI18N     
        String urlString = "";                                                                         // NOI18N         
        try {
            urlString = getUrlString();
        } catch (InterruptedException ex) {
            return; // should not happen
            }
                
        if(urlString != null) {
                       
            RepositoryConnection editedrc = getEditedRC();
            editedrc.setUrl(urlString);
            
            DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();                
            int idx = dcbm.getIndexOf(editedrc);       
            if(idx > -1) {
                //dcbm.setSelectedItem(urlString);                                                
                currentPanel.refresh((RepositoryConnection)dcbm.getElementAt(idx));
            } 
            currentPanel.onSelectedRepositoryChange(urlString);
            currentPanel.fillRC(editedrc);
            
        }
        message = "";                                                                                   // NOI18N
        updateVisibility();
    }            

    private RepositoryConnection getEditedRC() {
        if(editedRC == null) {
            editedRC = new RepositoryConnection("");
        }
        return editedRC;
    }

    private void updateVisibility() {
        try {
            String selectedUrlString = getUrlString();
            repositoryPanel.connPanel.remove(currentPanel.getPanel());
            if(selectedUrlString.startsWith("http:")) {                             // NOI18N
                currentPanel = http;
            } else if(selectedUrlString.startsWith("https:")) {                     // NOI18N
                currentPanel = http;
            } else if(selectedUrlString.startsWith("svn:")) {                       // NOI18N
                currentPanel = http;
            } else if(selectedUrlString.startsWith("svn+")) {                       // NOI18N
                currentPanel = svnSSHCli;
            } else if(selectedUrlString.startsWith("file:")) {                      // NOI18N
                currentPanel = file;
            } else {
                currentPanel = invalidUrlPanel;
            }
            repositoryPanel.tipLabel.setText(currentPanel.getTip(selectedUrlString));
            currentPanel.updateVisibility(selectedUrlString);
            repositoryPanel.connPanel.add(currentPanel.getPanel(), BorderLayout.CENTER);
            repositoryPanel.connPanel.repaint();
        } catch (InterruptedException ex) {
            return;
        }        
    }           
            
    /**
     * Load selected root from Swing structures (from arbitrary thread).
     * @return null on failure
     */
    private String getUrlString() throws InterruptedException {        
        if(!repositoryPanel.urlComboBox.isEditable()) {
            Object selection = repositoryPanel.urlComboBox.getSelectedItem();
            if(selection != null) {
                return selection.toString().trim();    
            }
            return "";    
        } else {
            final String[] svnUrl = new String[1];
            try {
                Runnable awt = new Runnable() {
                    public void run() {
                        svnUrl[0] = (String) repositoryPanel.urlComboBox.getEditor().getItem().toString().trim();
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    awt.run();
                } else {
                    SwingUtilities.invokeAndWait(awt);
                }
                return svnUrl[0].trim();
            } catch (InvocationTargetException e) {
                Subversion.LOG.log(Level.SEVERE, null, e);
            }
            return null;            
        }
    }

    public RepositoryConnection getSelectedRC() {
        RepositoryConnection rc = getSelectedRCIntern();
        return rc;
    }

    RepositoryConnection getSelectedRCIntern() {
        String urlString;
        try {
            urlString = getUrlString();            
        }
        catch (InterruptedException ex) {
            // should not happen
            Subversion.LOG.log(Level.SEVERE, null, ex);
            return null;
        };
        
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();                
        int idx = dcbm.getIndexOf(urlString);        
        
        if(idx > -1) {
            return (RepositoryConnection) dcbm.getElementAt(idx);
        }        
        return getEditedRC();        
    }
    
    public RepositoryPanel getPanel() {
        return repositoryPanel;
    }
    
    public boolean isValid() {
        return valid;
    }

    void setValid(boolean valid, String message) {
        boolean oldValue = this.valid;
        this.message = message;
        this.valid = valid;
        fireValidPropertyChanged(oldValue, valid);
    }

    private void fireValidPropertyChanged(boolean oldValue, boolean valid) {
        if(listeners==null) {
            return;
        }
        for (Iterator it = listeners.iterator();  it.hasNext();) {
            PropertyChangeListener l = (PropertyChangeListener) it.next();
            l.propertyChange(new PropertyChangeEvent(this, PROP_VALID, new Boolean(oldValue), new Boolean(valid)));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if(listeners==null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        listeners.add(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if(listeners==null) {
            return;
        }
        listeners.remove(l);
    }

    public String getMessage() {
        return message;
    }
    
    public void remove(RepositoryConnection toRemove) {
        RepositoryModel model = (RepositoryModel) repositoryPanel.urlComboBox.getModel();        
        model.removeElement(toRemove);        
    }

    public void itemStateChanged(ItemEvent evt) {
        if(evt.getStateChange() == ItemEvent.SELECTED) {
            RepositoryConnection rc = (RepositoryConnection) evt.getItem();
            currentPanel.refresh(rc);
            updateVisibility();  
            editedRC = new RepositoryConnection(rc);           
        } else if(evt.getStateChange() == ItemEvent.DESELECTED) {
            updateVisibility();  
        }       
    }

    public boolean show(String title, HelpCtx helpCtx, boolean setMaxNeddedSize) {
        RepositoryDialogPanel rdp = new RepositoryDialogPanel();
        rdp.panel.setLayout(new BorderLayout());
        JPanel p = getPanel();
        if(setMaxNeddedSize) {
            p.setPreferredSize(maxNeededSize);
        }        
        rdp.panel.add(p, BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(rdp, title); // NOI18N        
        showDialog(dialogDescriptor, helpCtx);
        return dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Object show(String title, HelpCtx helpCtx, Object[] options) {
        RepositoryDialogPanel rdp = new RepositoryDialogPanel();
        rdp.panel.setLayout(new BorderLayout());
        rdp.panel.add(getPanel(), BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(rdp, title); // NOI18N        
        if(options!= null) {
            dialogDescriptor.setOptions(options); // NOI18N
        }        
        showDialog(dialogDescriptor, helpCtx);
        return dialogDescriptor.getValue();
    }
    
    private void showDialog(DialogDescriptor dialogDescriptor, HelpCtx helpCtx) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(helpCtx);        

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Repository.class, "ACSD_RepositoryPanel"));
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(Repository.class, "ACSN_RepositoryPanel"));
        dialog.setVisible(true);
    }

    private boolean isSet(int flag) {
        return (modeMask & flag) != 0;
    }
    
    public class RepositoryModel  extends DefaultComboBoxModel {

        public RepositoryModel(Vector v) {
            super(v);
        }

        public void setSelectedItem(Object obj) {
            if(obj instanceof String) {
                int idx = getIndexOf(obj);
                if(idx > -1) {
                    obj = getElementAt(idx);
                } else {
                    obj = createNewRepositoryConnection((String) obj);                   
                }                
            }            
            super.setSelectedItem(obj);
        }

        public int getIndexOf(Object obj) {
            if(obj instanceof String) {
                obj = createNewRepositoryConnection((String)obj);                
            }
            return super.getIndexOf(obj);
        }

        public void addElement(Object obj) {
            if(obj instanceof String) {
                obj = createNewRepositoryConnection((String)obj);                
            }
            super.addElement(obj);
        }

        public void insertElementAt(Object obj,int index) {
            if(obj instanceof String) {
                String str = (String) obj;
                RepositoryConnection rc = null;
                try {
                    rc = (RepositoryConnection) getElementAt(index);                    
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if(rc != null) {
                    rc.setUrl(str);
                    obj = rc;
                }                
                obj = createNewRepositoryConnection(str);
            } 
            super.insertElementAt(obj, index);
        }         

        public void removeElement(Object obj) {
            int index = getIndexOf(obj);
            if ( index != -1 ) {
                removeElementAt(index);
            }
        }
        
        private RepositoryConnection createNewRepositoryConnection(String url) {
            editedRC.setUrl(url);
            return new RepositoryConnection(editedRC);
        }
    }
}
