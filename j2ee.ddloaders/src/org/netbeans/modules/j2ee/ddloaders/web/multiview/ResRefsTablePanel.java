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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel;
import org.openide.util.NbBundle;
 
/** ResRefsTablePanel - panel containing table for resource references
 *
 * @author  mk115033
 * Created on April 11, 2005
 */
public class ResRefsTablePanel extends DefaultTablePanel {
    private ResRefTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    
    /** Creates new form ContextParamPanel */
    public ResRefsTablePanel(final DDDataObject dObj, final ResRefTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                int row = getTable().getSelectedRow();
                model.removeRow(row);
                dObj.setChangedFromUI(false);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }

    void setModel(WebApp webApp, ResourceRef[] params) {
        model.setData(webApp,params);
        this.webApp=webApp;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            final int row = (add?-1:getTable().getSelectedRow());
            final ResRefPanel dialogPanel = new ResRefPanel();
            if (!add) {
                ResourceRef resRef = model.getResourceRef(row);
                dialogPanel.setResRefName(resRef.getResRefName());
                dialogPanel.setResType(resRef.getResType());
                String auth = resRef.getResAuth();
                dialogPanel.setResAuth(auth==null?"Container":auth);
                String scope = resRef.getResSharingScope();
                dialogPanel.setResScope(scope==null?"Shareable":scope); //NOI18N
                dialogPanel.setDescription(resRef.getDefaultDescription());
            }
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(ResRefsTablePanel.class,"TTL_ResourceRef"),add) {
                protected String validate() {
                    String name = dialogPanel.getResRefName().trim();
                    String type = dialogPanel.getResType().trim();
                    if (name.length()==0) {
                        return NbBundle.getMessage(ResRefsTablePanel.class,"TXT_EmptyResRefName");
                    } else {
                        ResourceRef[] params = webApp.getResourceRef();
                        boolean exists=false;
                        for (int i=0;i<params.length;i++) {
                            if (row!=i && name.equals(params[i].getResRefName())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(ResRefsTablePanel.class,"TXT_ResRefNameExists",name);
                        }
                    }
                    if (type.length()==0) {
                        return NbBundle.getMessage(ResRefsTablePanel.class,"TXT_EmptyResTypeValue");
                    }
                    return null;
                }
            };
            
            if (add) dialog.setValid(false); // disable OK button
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getNameTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getTypeTF().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.show();
            dialogPanel.getNameTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getTypeTF().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                String name = dialogPanel.getResRefName().trim();
                String type = dialogPanel.getResType().trim();
                String auth = dialogPanel.getResAuth();
                String scope = dialogPanel.getResScope();
                String description = dialogPanel.getDescription();
                if (add) model.addRow(new String[]{name,type,auth,scope,description});
                else model.editRow(row,new String[]{name,type,auth,scope,description});
                dObj.setChangedFromUI(false);
            }
        }
    }
}
