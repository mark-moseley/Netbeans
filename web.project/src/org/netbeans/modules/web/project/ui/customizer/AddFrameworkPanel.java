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

package org.netbeans.modules.web.project.ui.customizer;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.netbeans.modules.web.api.webmodule.WebFrameworkSupport;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;

/**
 *
 * @author  Radko Najman
 */
public class AddFrameworkPanel extends javax.swing.JPanel {
    
    /** Creates new form AddFrameworkPanel */
    public AddFrameworkPanel(List usedFrameworks) {
	initComponents();
        jListFrameworks.setCellRenderer(new FrameworksListCellRenderer());
	createFrameworksList(usedFrameworks);
        jListFrameworks.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
    
    private void createFrameworksList(List usedFrameworks) {
        List frameworks = WebFrameworkSupport.getFrameworkProviders();
	DefaultListModel model = new DefaultListModel();
	jListFrameworks.setModel(model);
        
	for (int i = 0; i < frameworks.size(); i++) {
	    WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
	    if (usedFrameworks.size() == 0)
		model.addElement(framework);
	    else
		for (int j = 0; j < usedFrameworks.size(); j++)
		    if (!((WebFrameworkProvider) usedFrameworks.get(j)).getName().equals(framework.getName())) {
			model.addElement(framework);
			break;
		    }
	}

	
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jListFrameworks = new javax.swing.JList();

        jScrollPane1.setViewportView(jListFrameworks);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JList jListFrameworks;
    public javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    public List getSelectedFrameworks() {
	List selectedFrameworks = new LinkedList();
	DefaultListModel model = (DefaultListModel) jListFrameworks.getModel();
        int[] indexes = jListFrameworks.getSelectedIndices();
        for (int i = 0; i < indexes.length; i++)
	    selectedFrameworks.add(model.get(indexes[i]));
        
        return selectedFrameworks;
    }

    public static class FrameworksListCellRenderer extends DefaultListCellRenderer {
	
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof WebFrameworkProvider) {
                WebFrameworkProvider item = (WebFrameworkProvider) value;
                return super.getListCellRendererComponent(list, item.getName(), index, isSelected, cellHasFocus);
            } else 
		return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

}
