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
/*
 * WebAppPropertiesPanel.java
 *
 * Created on October 13, 2005, 3:00 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.ResourceBundle;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;

import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;

/**
 *
 * @author Peter Williams
 */
public class WebAppPropertiesPanel extends javax.swing.JPanel implements TableModelListener {
	
	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
	
	private WebAppRootCustomizer masterPanel;
	
    // Table for editing Property idempotent url patterns
    private GenericTableModel idempotentUrlPatternModel;
    private GenericTablePanel idempotentUrlPatternPanel;

    // Listens for changes to the default list of charsets
    private ParameterEncodingPanel parameterEncodingPanel;
    private PropertyChangeListener parameterEncodingChangeListener;
	
	/** Creates new form WebAppPropertiesPanel */
	public WebAppPropertiesPanel(WebAppRootCustomizer src) {
		masterPanel = src;
		
		initComponents();
		initUserComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_WebAppPropertiesTab"));
        getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_WebAppPropertiesTab"));
    }
    // </editor-fold>//GEN-END:initComponents
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

	private void initUserComponents() {
        /** Add parameter encoding panel.
         */
        parameterEncodingPanel = new ParameterEncodingPanel();

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(parameterEncodingPanel, gridBagConstraints);		

        parameterEncodingChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                String newValue = (String) pce.getNewValue();
                updateParameterEncoding(pce.getPropertyName(), newValue);
            }
        };
        
        // add idempotentUrlPattern table
        ArrayList tableColumns = new ArrayList(2);
        tableColumns.add(new GenericTableModel.AttributeEntry(SunWebApp.IDEMPOTENT_URL_PATTERN, "UrlPattern", // NOI18N 
            webappBundle, "UrlPattern", true, false)); // NOI18N
        tableColumns.add(new GenericTableModel.AttributeEntry(SunWebApp.IDEMPOTENT_URL_PATTERN, "NumOfRetries", // NOI18N 
            webappBundle, "NumOfRetries", true, false)); // NOI18N
        
        idempotentUrlPatternModel = new GenericTableModel(SunWebApp.IDEMPOTENT_URL_PATTERN, tableColumns);
        idempotentUrlPatternPanel = new GenericTablePanel(idempotentUrlPatternModel, 
            webappBundle, "IdempotentUrlPatterns", // NOI18N - property name
            HelpContext.HELP_WEBAPP_IDEMPOTENTURLPATTERN_POPUP);
        idempotentUrlPatternPanel.setHeadingMnemonic(webappBundle.getString("MNE_IdempotentUrlPatterns").charAt(0)); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
        add(idempotentUrlPatternPanel, gridBagConstraints);		
	}
	
	public void addListeners() {
		idempotentUrlPatternModel.addTableModelListener(this);
		parameterEncodingPanel.addPropertyChangeListener(ParameterEncodingPanel.PROP_DEFAULT_CHARSET, parameterEncodingChangeListener);
		parameterEncodingPanel.addPropertyChangeListener(ParameterEncodingPanel.PROP_FORM_HINT_FIELD, parameterEncodingChangeListener);
		parameterEncodingPanel.addListeners();
	}
	
	public void removeListeners() {
		parameterEncodingPanel.removeListeners();
		parameterEncodingPanel.removePropertyChangeListener(ParameterEncodingPanel.PROP_DEFAULT_CHARSET, parameterEncodingChangeListener);
		parameterEncodingPanel.removePropertyChangeListener(ParameterEncodingPanel.PROP_FORM_HINT_FIELD, parameterEncodingChangeListener);
		idempotentUrlPatternModel.removeTableModelListener(this);
	}
	
	/** Initialization of all the fields in this panel from the bean that
	 *  was passed in.
	 */
	public void initFields(WebAppRoot bean) {
        parameterEncodingPanel.initFields(bean.getAppServerVersion(), bean.getDefaultCharset(), bean.getFormHintField(), true);
        idempotentUrlPatternPanel.setModelBaseBean(bean.getIdempotentUrlPattern(), bean.getAppServerVersion());
	}
	
    private void updateParameterEncoding(String propName, String newValue) {
        WebAppRoot bean = masterPanel.getBean();
        if(bean != null) {
            try {
                if(ParameterEncodingPanel.PROP_DEFAULT_CHARSET.equals(propName)) {
                    bean.setDefaultCharset(newValue);
                } else if(ParameterEncodingPanel.PROP_FORM_HINT_FIELD.equals(propName)) {
                    bean.setFormHintField(newValue);
                }
            } catch(PropertyVetoException ex) {
            }
        }
    }
    
	/** ----------------------------------------------------------------------- 
	 *  Implementation of javax.swing.event.TableModelListener
	 */
	public void tableChanged(TableModelEvent e) {
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
            Object eventSource = e.getSource();
            if(eventSource == idempotentUrlPatternModel) {
                // Nothing to do, same deal as JspConfig
            }

            // Force property change to be issued by the bean
            bean.setDirty();
        }
	}
}
