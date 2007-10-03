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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.TextItemEditorModel;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * 
 * @author Peter Williams
 */
public class SunWebDetailsPanel extends BaseSectionNodeInnerPanel {
	
    private SunWebApp sunWebApp;
    
    public SunWebDetailsPanel(SectionNodeView sectionNodeView, final SunWebApp sunWebApp, final ASDDVersion version) {
        super(sectionNodeView, version);
        this.sunWebApp = sunWebApp;
        
        initComponents();
        initUserComponents(sectionNodeView);
    }

    private void initUserComponents(SectionNodeView sectionNodeView) {
        showAS80Fields(as80FeaturesVisible);
        showAS81Fields(as81FeaturesVisible);
        showAS90Fields(as90FeaturesVisible);
        
        SunDescriptorDataObject dataObject = (SunDescriptorDataObject) sectionNodeView.getDataObject();
        XmlMultiViewDataSynchronizer synchronizer = dataObject.getModelSynchronizer();
        if(as80FeaturesVisible) {
            addRefreshable(new ItemEditorHelper(jTxtContextRoot, new ContextRootEditorModel(synchronizer)));
        }
        if(as81FeaturesVisible) {
            addRefreshable(new ItemEditorHelper(jTxtErrorUrl, new ErrorUrlEditorModel(synchronizer)));
        }
        if(as90FeaturesVisible) {
            addRefreshable(new ItemEditorHelper(jTxtHttpservletSecurityProvider, new HttpServletSecurityEditorModel(synchronizer)));
        }
    }
	
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLblContextRoot = new javax.swing.JLabel();
        jTxtContextRoot = new javax.swing.JTextField();
        jLblErrorUrl = new javax.swing.JLabel();
        jTxtErrorUrl = new javax.swing.JTextField();
        jLblHttpservletSecurityProvider = new javax.swing.JLabel();
        jTxtHttpservletSecurityProvider = new javax.swing.JTextField();

        setAlignmentX(LEFT_ALIGNMENT);
        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        jLblContextRoot.setLabelFor(jTxtContextRoot);
        jLblContextRoot.setText(NbBundle.getMessage(SunWebDetailsPanel.class, "LBL_ContextRoot_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblContextRoot, gridBagConstraints);
        jLblContextRoot.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SunWebDetailsPanel.class, "ACSN_ContextRoot")); // NOI18N

        jTxtContextRoot.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtContextRootKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtContextRoot, gridBagConstraints);
        jTxtContextRoot.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SunWebDetailsPanel.class, "ACSN_ContextRoot")); // NOI18N
        jTxtContextRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SunWebDetailsPanel.class, "ACSD_ContextRoot")); // NOI18N

        jLblErrorUrl.setLabelFor(jTxtErrorUrl);
        jLblErrorUrl.setText(NbBundle.getMessage(SunWebDetailsPanel.class, "LBL_ErrorUrl_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblErrorUrl, gridBagConstraints);
        jLblErrorUrl.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SunWebDetailsPanel.class, "ACSN_ErrorUrl")); // NOI18N

        jTxtErrorUrl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtErrorUrlKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtErrorUrl, gridBagConstraints);

        jLblHttpservletSecurityProvider.setLabelFor(jTxtHttpservletSecurityProvider);
        jLblHttpservletSecurityProvider.setText(NbBundle.getMessage(SunWebDetailsPanel.class, "LBL_HttpservletSecurityProvider_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblHttpservletSecurityProvider, gridBagConstraints);
        jLblHttpservletSecurityProvider.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SunWebDetailsPanel.class, "ACSN_HttpservletSecurityProvider")); // NOI18N

        jTxtHttpservletSecurityProvider.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtHttpservletSecurityProviderKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jTxtHttpservletSecurityProvider, gridBagConstraints);

        getAccessibleContext().setAccessibleName(null);
        getAccessibleContext().setAccessibleDescription(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jTxtHttpservletSecurityProviderKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtHttpservletSecurityProviderKeyReleased
//        WebAppRoot bean = masterPanel.getBean();
//		if(bean != null) {
//			try {
//				bean.setHttpservletSecurityProvider(jTxtHttpservletSecurityProvider.getText());
////				masterPanel.validateField(WebAppRoot.FIELD_HTTP_SERVLET_SECURITY_PROVIDER);
//			} catch(java.beans.PropertyVetoException exception) {
//				jTxtHttpservletSecurityProvider.setText(bean.getHttpservletSecurityProvider());
//			}
//		}
    }//GEN-LAST:event_jTxtHttpservletSecurityProviderKeyReleased

    private void jTxtErrorUrlKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtErrorUrlKeyReleased
//        WebAppRoot bean = masterPanel.getBean();
//		if(bean != null) {
//			try {
//				bean.setErrorUrl(jTxtErrorUrl.getText());
////				masterPanel.validateField(WebAppRoot.FIELD_ERROR_URL);
//			} catch(java.beans.PropertyVetoException exception) {
//				jTxtErrorUrl.setText(bean.getErrorUrl());
//			}
//		}
    }//GEN-LAST:event_jTxtErrorUrlKeyReleased

	private void jTxtContextRootKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtContextRootKeyReleased
//        WebAppRoot bean = masterPanel.getBean();
//		if(bean != null) {
//			try {
//				bean.setContextRoot(jTxtContextRoot.getText());
////				masterPanel.validateField(WebAppRoot.FIELD_CONTEXT_ROOT);
//			} catch(java.beans.PropertyVetoException exception) {
//				jTxtContextRoot.setText(bean.getContextRoot());
//			}
//		}
	}//GEN-LAST:event_jTxtContextRootKeyReleased
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblContextRoot;
    private javax.swing.JLabel jLblErrorUrl;
    private javax.swing.JLabel jLblHttpservletSecurityProvider;
    private javax.swing.JTextField jTxtContextRoot;
    private javax.swing.JTextField jTxtErrorUrl;
    private javax.swing.JTextField jTxtHttpservletSecurityProvider;
    // End of variables declaration//GEN-END:variables
	
    // TODO after 5.0, generalize version based field display for multiple (> 2)
    // appserver versions.  (!PW note: this is the first panel to have changes across
    // 3 revisions of the relevant DTD.)
    //
    // Generalization idea: Tag each control a "MinVersion" property whose value is the instance
    // of AsDDVersion that corresponds with the needed appserver version.  Could later tag
    // with "MaxVersion", or even "ExcludeVersion", if necessary.
    //
    private void showAS80Fields(boolean visible) {
        jLblContextRoot.setVisible(visible);
        jTxtContextRoot.setVisible(visible);
    }
    
    private void showAS81Fields(boolean visible) {
        jLblErrorUrl.setVisible(visible);
        jTxtErrorUrl.setVisible(visible);
    }
    
    private void showAS90Fields(boolean visible) {
        jLblHttpservletSecurityProvider.setVisible(visible);
        jTxtHttpservletSecurityProvider.setVisible(visible);
    }
    
    // Model classes for handling updates to the fields (is there a better or
    // more generic way to do this?)
    private class ContextRootEditorModel extends TextItemEditorModel {

        public ContextRootEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            return sunWebApp.getContextRoot();
        }

        protected void setValue(String value) {
            sunWebApp.setContextRoot(value);
        }
    }
    
    private class ErrorUrlEditorModel extends TextItemEditorModel {

        public ErrorUrlEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            try {
                return sunWebApp.getErrorUrl();
            } catch(VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return null;
        }

        protected void setValue(String value) {
            try {
                sunWebApp.setErrorUrl(value);
            } catch(VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    private class HttpServletSecurityEditorModel extends TextItemEditorModel {

        public HttpServletSecurityEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            try {
                return sunWebApp.getHttpservletSecurityProvider();
            } catch(VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return null;
        }

        protected void setValue(String value) {
            try {
                sunWebApp.setHttpservletSecurityProvider(value);
            } catch(VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

}
