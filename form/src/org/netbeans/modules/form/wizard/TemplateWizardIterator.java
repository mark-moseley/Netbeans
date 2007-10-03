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

package org.netbeans.modules.form.wizard;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Special template wizard iterator for BeanForm template - requires to
 * specify superclass additionally.
 *
 * @author Tomas Pavek, Jan Stola
 */

class TemplateWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private transient WizardDescriptor.Panel superclassPanel;
    private transient boolean superclassPanelCurrent;
    private transient WizardDescriptor.InstantiatingIterator delegateIterator;

    private boolean specifySuperclass;

    public static TemplateWizardIterator createForSuperclass() {
        return new TemplateWizardIterator(true);
    }

    public static TemplateWizardIterator create() {
        return new TemplateWizardIterator(false);
    }

    public TemplateWizardIterator(boolean specifySuperclass) {
        delegateIterator = JavaTemplates.createJavaTemplateIterator();
        this.specifySuperclass = specifySuperclass;
    }

    public void initialize(WizardDescriptor wizard) {
        delegateIterator.initialize(wizard);
        superclassPanelCurrent = false;
        if (superclassPanel == null && specifySuperclass) {
            superclassPanel = new SuperclassWizardPanel();
            
            ResourceBundle bundle = NbBundle.getBundle(TemplateWizardIterator.class);
            JComponent comp = (JComponent)delegateIterator.current().getComponent();
            String[] contentData = (String[])comp.getClientProperty("WizardPanel_contentData"); // NOI18N
            String[] newContentData = new String[contentData.length+1];
            System.arraycopy(contentData, 0, newContentData, 0, contentData.length);
            newContentData[contentData.length] = bundle.getString("CTL_SuperclassTitle"); // NOI18N
            comp.putClientProperty("WizardPanel_contentData", newContentData); // NOI18N
        }    
    }

    public void uninitialize(WizardDescriptor wizard) {
        delegateIterator.uninitialize(wizard);
        superclassPanel = null;
    }

    public Set instantiate() throws IOException, IllegalArgumentException {
        Set set = delegateIterator.instantiate();
        FileObject template = (FileObject) set.iterator().next();
        
        if (specifySuperclass) {
            final String className = template.getName();
            final String superclassName = 
                    ((SuperclassWizardPanel) superclassPanel).getSuperclassName();
            JavaSource js = JavaSource.forFileObject(template);
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {
                }
                public void run(WorkingCopy wcopy) throws Exception {
                    wcopy.toPhase(JavaSource.Phase.RESOLVED);

                    for (Tree t: wcopy.getCompilationUnit().getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS && className.equals(((ClassTree) t).getSimpleName().toString())) {
                            ClassTree orig = (ClassTree) t;
                            TreeMaker maker = wcopy.getTreeMaker();
                            TypeElement superclassElm = wcopy.getElements().getTypeElement(superclassName);
                            ExpressionTree extendsTree = superclassElm != null
                                    ? maker.QualIdent(superclassElm)
                                    : maker.Identifier(superclassName);
                            ClassTree copy = maker.Class(
                                    orig.getModifiers(),
                                    orig.getSimpleName(),
                                    orig.getTypeParameters(),
                                    extendsTree,
                                    (List<? extends ExpressionTree>) orig.getImplementsClause(),
                                    orig.getMembers()
                                    );
                            wcopy.rewrite(orig, copy);
                            break;
                        }
                    }
                }
            }).commit();
        }
        
        template.setAttribute("justCreatedByNewWizard", Boolean.TRUE); // NOI18N
        
        return set;
    }

    public WizardDescriptor.Panel current() {
        return superclassPanelCurrent ? superclassPanel : delegateIterator.current();
    }

    public boolean hasNext() {
        return !superclassPanelCurrent && superclassPanel != null;
    }
    
    public boolean hasPrevious() {
        return superclassPanelCurrent ? true : delegateIterator.hasPrevious();
    }
    
    public void nextPanel() {
        if (delegateIterator.hasNext()) {
            delegateIterator.nextPanel();
        } else {
            if (superclassPanelCurrent || superclassPanel == null) {
                throw new NoSuchElementException();
            } else {
                superclassPanelCurrent = true;
            }
        }
    }
    
    public void previousPanel() {
        if (superclassPanelCurrent) {
            superclassPanelCurrent = false;
        } else {
            delegateIterator.previousPanel();
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        delegateIterator.addChangeListener(l);
    }
    
    public String name() {
        return superclassPanelCurrent ? "" : delegateIterator.name(); // NOI18N
    }
    
    public void removeChangeListener(ChangeListener l) {
        delegateIterator.removeChangeListener(l);
    }

    // ---------

    static class SuperclassWizardPanel implements WizardDescriptor.FinishablePanel {

        private SuperclassPanel panelUI;

        String getSuperclassName() {
            String name = panelUI != null ?
                          panelUI.superclassTextField.getText() : null;
            return name != null && !"".equals(name) ? name : "java.lang.Object"; // NOI18N
        }

        public Component getComponent() {
            if (panelUI == null)
                panelUI = new SuperclassPanel();
            return panelUI;
        }

        public boolean isValid() {
            return true;
        }

        public void readSettings(Object settings) {
        }

        public void storeSettings(Object settings) {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public org.openide.util.HelpCtx getHelp () {
            return new org.openide.util.HelpCtx("gui.creatingforms"); // NOI18N
        }
        
        public boolean isFinishPanel() {
            return true;
        }
        
    }

    // -------

    static class SuperclassPanel extends javax.swing.JPanel {

        SuperclassPanel() {
            ResourceBundle bundle = NbBundle.getBundle(TemplateWizardIterator.class);
            setName(bundle.getString("CTL_SuperclassTitle")); // NOI18N
            putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1)); //NOI18N
            getAccessibleContext()
                .setAccessibleDescription(bundle.getString("ACSD_SuperclassPanel")); // NOI18N

            setLayout(new GridBagLayout());
            setBorder(new javax.swing.border.EmptyBorder(8, 8, 8, 8));

            label1 = new JLabel();
            superclassTextField = new JTextField();

            label1.setLabelFor(superclassTextField);
            label1.setText(bundle.getString("CTL_SuperclassName")); // NOI18N
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 0, 0, 12);
            add(label1, gridBagConstraints);

            superclassTextField.setText("java.lang.Object"); // NOI18N
            superclassTextField.setToolTipText(bundle.getString("CTL_SuperclassName_Hint")); // NOI18N
            superclassTextField.getAccessibleContext()
                .setAccessibleDescription(bundle.getString("ACSD_SuperclassTextField"));  // NOI18N
            superclassTextField.addFocusListener(new FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    superclassTextField.selectAll();
                }
            });

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(superclassTextField, gridBagConstraints);
        }

        public void addNotify() {
            super.addNotify();
            superclassTextField.requestFocus();
        }

        private JLabel label1;
        private JTextField superclassTextField;
    }
}
