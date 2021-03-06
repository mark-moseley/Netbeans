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

/*
 * DocumentationCustomizer.java
 *
 * Created on May 10, 2006, 10:04 AM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Ajit Bhate
 */
public class DocumentationCustomizer extends AbstractSchemaComponentCustomizer<Documentation> {
    static final long serialVersionUID = 1L;
    
    private transient DocumentListener contentListener;
    
    /**
     * Creates new form DocumentationCustomizer
     */
    public DocumentationCustomizer(SchemaComponentReference<Documentation> reference) {
        super(reference);
        initComponents();
        reset();
    }
    protected void applyChanges() throws IOException {
        getReference().get().setContentFragment(getContent());
    }
    
    public void reset() {
        initUI();
        setSaveEnabled(false);
        setResetEnabled(false);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(DocumentationCustomizer.class);
    }
    
    private void initUI() {
        if(contentListener == null) {
            contentListener = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    determineValidity();
                }
                public void insertUpdate(DocumentEvent e) {
                    determineValidity();
                }
                public void removeUpdate(DocumentEvent e) {
                    determineValidity();
                }
            };
        } else {
            contentEditorPane.getDocument().
                    removeDocumentListener(contentListener);
        }
        contentEditorPane.setText(getReference().get().getContentFragment());
        contentEditorPane.getDocument().
                addDocumentListener(contentListener);
    }
    
    private boolean isModified() {
        return !getContent().equals(getReference().get().getContentFragment());
    }
    
    private String getContent() {
        return contentEditorPane.getText();
    }
    
    private void determineValidity() {
        boolean flag = isModified();
        setSaveEnabled(flag);
        setResetEnabled(flag);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        innerPanel = new javax.swing.JPanel();
        contentScrollPane = new javax.swing.JScrollPane();
        contentEditorPane = new javax.swing.JEditorPane()
        {
            static final long serialVersionUID = 1L;
            protected void processMouseEvent(java.awt.event.MouseEvent e)
            {
                if(e.getButton()==java.awt.event.MouseEvent.BUTTON3)
                {
                    e.consume();
                    return;
                }
                super.processMouseEvent(e);
            }
        };

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        innerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        contentScrollPane.setBorder(null);
        contentEditorPane.setContentType("text/xml");
        contentScrollPane.setViewportView(contentEditorPane);

        org.jdesktop.layout.GroupLayout innerPanelLayout = new org.jdesktop.layout.GroupLayout(innerPanel);
        innerPanel.setLayout(innerPanelLayout);
        innerPanelLayout.setHorizontalGroup(
            innerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(innerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(contentScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap())
        );
        innerPanelLayout.setVerticalGroup(
            innerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, innerPanelLayout.createSequentialGroup()
                .add(contentScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(innerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(23, 23, 23)
                .add(innerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane contentEditorPane;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JPanel innerPanel;
    // End of variables declaration//GEN-END:variables
    
}
