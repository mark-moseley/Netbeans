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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.Dialog;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;

import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/**
 *
 * @author  ads
 */
public class JavaMELibsVisualPanel extends JPanel {

    private static final String CONTENT_NUMBERED  = "WizardPanel_contentNumbered";  // NOI18N
    private static final String CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"; // NOI18N
    private static final String AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle";  // NOI18N
    
    /** Creates new form JavaMELibsVisualPanel */
    public JavaMELibsVisualPanel() {
        initComponents();
        
        myLibDescList.setModel( new LibraryListModel() );
    }

    void readData( WizardDescriptor settings ) {
        myWizardDescriptor = settings;
        List<String> libNames = (List<String>)myWizardDescriptor.getProperty( 
                CustomComponentWizardIterator.LIB_NAMES);
        List<String> libDisplayNames = (List<String>)myWizardDescriptor.getProperty( 
                CustomComponentWizardIterator.LIB_DISPLAY_NAMES);
        
        LibraryListModel model = (LibraryListModel)myLibDescList.getModel();
        model.updateModel(libNames, libDisplayNames);
    }

    
    private class LibraryListModel extends AbstractListModel{

        private Vector delegate = new Vector();

        public void updateModel(List<String> libNames,  List<String> libDisplayNames){
            // clean
            removeAllElements();
            // fil with new elements
            if (libNames == null || libDisplayNames == null ){
                return;
            }
            assert libNames.size() == libDisplayNames.size() 
                    : "libraries data is not consistent";
            
            Iterator<String> itN = libNames.iterator();
            Iterator<String> itDN = libDisplayNames.iterator();
            while (itN.hasNext()){
                String name = itN.next();
                String displayName = itDN.next();
                addElement(displayName + " [ " + name + " ]"); // NOI18N 
            }
            
        }
                
        /**
         * Adds the specified component to the end of this list. 
         *
         * @param   obj   the component to be added
         * @see Vector#addElement(Object)
         */
        public void addElement(Object obj) {
            int index = delegate.size();
            delegate.addElement(obj);
            fireIntervalAdded(this, index, index);
        }

        /**
         * Removes all components from this list and sets its size to zero.
         * <blockquote>
         * <b>Note:</b> Although this method is not deprecated, the preferred
         *    method to use is <code>clear</code>, which implements the 
         *    <code>List</code> interface defined in the 1.2 Collections framework.
         * </blockquote>
         *
         * @see #clear()
         * @see Vector#removeAllElements()
         */
        public void removeAllElements() {
            int index1 = delegate.size() - 1;
            delegate.removeAllElements();
            if (index1 >= 0) {
                fireIntervalRemoved(this, 0, index1);
            }
        }

        /**
         * Removes the element at the specified position in this list.
         * Returns the element that was removed from the list.
         * <p>
         * Throws an <code>ArrayIndexOutOfBoundsException</code>
         * if the index is out of range
         * (<code>index &lt; 0 || index &gt;= size()</code>).
         *
         * @param index the index of the element to removed
         */
        public Object remove(int index) {
            Object rv = delegate.elementAt(index);
            delegate.removeElementAt(index);
            fireIntervalRemoved(this, index, index);
            return rv;
        }

        public int getSize() {
            return delegate.size();
        }

        public Object getElementAt(int index) {
            return delegate.elementAt(index);
        }


    }
            
    void storeData( WizardDescriptor settings ) {
        /*
         * nothing to save. 
         * UI on this step just shows list of libraries stored as the following 
         * properties by NewLibraryDescriptor.instantiate():
         * (List<Library>)settings.getProperty( 
         *        CustomComponentWizardIterator.LIBRARIES);
         * (List<String>)settings.getProperty( 
         *        CustomComponentWizardIterator.LIB_NAMES);
         * (List<String>)settings.getProperty( 
         *        CustomComponentWizardIterator.LIB_DISPLAY_NAMES);
         * 
         * Libraries are configured if all of the folowing is true
         * - all three lists are not null
         * - all three lists are not empty
         * - all thre lists have the same size
         */
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myDescLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        myLibDescList = new javax.swing.JList();
        myAddButton = new javax.swing.JButton();
        myRemoveButton = new javax.swing.JButton();

        myDescLabel.setLabelFor(myLibDescList);
        org.openide.awt.Mnemonics.setLocalizedText(myDescLabel, org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "LBL_AddedLibDescriptors")); // NOI18N

        jScrollPane1.setViewportView(myLibDescList);
        myLibDescList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "ACSN_DescriptorsList")); // NOI18N
        myLibDescList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "ACSD_DescriptorsList")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myAddButton, org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "BTN_AddLibDesc")); // NOI18N
        myAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myRemoveButton, org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "BTN_LibRemove")); // NOI18N
        myRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePressed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(myAddButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(myRemoveButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(myDescLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(myDescLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(myAddButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myRemoveButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
                .addContainerGap())
        );

        myDescLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "ACSN_DescLabel")); // NOI18N
        myDescLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "ASCD_DescLabel")); // NOI18N
        myAddButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "ACSN_LibAdd")); // NOI18N
        myAddButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "ACSN_LibAdd")); // NOI18N
        myRemoveButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "ACSN_LibRemove")); // NOI18N
        myRemoveButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaMELibsVisualPanel.class, "ACSN_LibRemove")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void addPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPressed
    WizardDescriptor.Iterator iterator = new NewLibraryDescriptor( myWizardDescriptor );
    myInnerDescriptor = new WizardDescriptor( iterator );
    myInnerDescriptor.putProperty( AUTO_WIZARD_STYLE, true );
    myInnerDescriptor.putProperty( CONTENT_DISPLAYED, true );
    myInnerDescriptor.putProperty( CONTENT_NUMBERED, true );
    Dialog dialog = DialogDisplayer.getDefault().createDialog( myInnerDescriptor );
    dialog.setVisible( true );
    readData(myWizardDescriptor);
}//GEN-LAST:event_addPressed

private void removePressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePressed
    int index = myLibDescList.getSelectedIndex();
    // remove in UI
    ((LibraryListModel)myLibDescList.getModel()).remove(index);
    // remove from WizardDescriptor
    List<String> libNames = (List<String>)myWizardDescriptor.getProperty( 
                CustomComponentWizardIterator.LIB_NAMES);
    List<String> libDisplayNames = (List<String>)myWizardDescriptor.getProperty( 
                CustomComponentWizardIterator.LIB_DISPLAY_NAMES);
    libNames.remove(index);
    libDisplayNames.remove(index);
    
}//GEN-LAST:event_removePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton myAddButton;
    private javax.swing.JLabel myDescLabel;
    private javax.swing.JList myLibDescList;
    private javax.swing.JButton myRemoveButton;
    // End of variables declaration//GEN-END:variables

    private WizardDescriptor myWizardDescriptor;
    private WizardDescriptor myInnerDescriptor;

}
