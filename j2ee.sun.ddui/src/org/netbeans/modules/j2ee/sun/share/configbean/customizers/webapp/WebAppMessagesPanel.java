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
 * WebAppGeneralPanel.java
 *
 * Created on November 5, 2003, 4:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.ResourceBundle;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyVetoException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;

import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 */
public class WebAppMessagesPanel extends javax.swing.JPanel implements TableModelListener {
	
	private final ResourceBundle webappBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
	
	private WebAppRootCustomizer masterPanel;
	
	// Table for editing MessageDestination entries
	private GenericTableModel messageDestinationModel;
	private GenericTablePanel messageDestinationPanel;

	// Table for editing MessageDestinationRef entries
//	private GenericTableModel messageDestinationRefModel;
//	private GenericTablePanel messageDestinationRefPanel;
	
    // true if AS 9.0+ fields are visible.
    private boolean as90FeaturesVisible;
    
	/** Creates new form WebAppMessagesPanel */
	public WebAppMessagesPanel(WebAppRootCustomizer src) {
		masterPanel = src;
		
		initComponents();
		initUserComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_WebAppMessagesTab"));
        getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_WebAppMessagesTab"));
    }//GEN-END:initComponents
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

	private void initUserComponents() {
        
		as90FeaturesVisible = true;
        
		/* Add message destination table panel :
		 * TableEntry list has two properties: destination name, jndi name
		 */
		ArrayList tableColumns = new ArrayList(2);
		tableColumns.add(new GenericTableModel.ValueEntry(null, MessageDestination.MESSAGE_DESTINATION_NAME, 
			webappBundle, "MessageDestinationName", true, true));	// NOI18N - property name
		tableColumns.add(new GenericTableModel.ValueEntry(null, MessageDestination.JNDI_NAME,
			webappBundle, "JNDIName", true, false));	// NOI18N - property name
		
		messageDestinationModel = new GenericTableModel(messageDestinationFactory, tableColumns);
		messageDestinationPanel = new GenericTablePanel(messageDestinationModel, 
			webappBundle, "MessageDestination",	// NOI18N - property name
			HelpContext.HELP_WEBAPP_MESSAGE_DESTINATION_POPUP);
		
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(6, 6, 0, 5);
		add(messageDestinationPanel, gridBagConstraints);		
        
		/* Add message destination ref table panel :
		 * TableEntry list has two properties: destination ref name, jndi name
		 */
//		tableColumns = new ArrayList(2);
//		tableColumns.add(new GenericTableModel.ValueEntry(null, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME, 
//			webappBundle, "MessageDestinationRefName", true, true));	// NOI18N - property name
//		tableColumns.add(new GenericTableModel.ValueEntry(null, MessageDestinationRef.JNDI_NAME,
//			webappBundle, "JNDIName", true, false));	// NOI18N - property name
//		
//		messageDestinationRefModel = new GenericTableModel(messageDestinationRefFactory, tableColumns);
//		messageDestinationRefPanel = new GenericTablePanel(messageDestinationRefModel, 
//			webappBundle, "MessageDestinationRef",	// NOI18N - property name
//			HelpContext.HELP_WEBAPP_MESSAGE_DESTINATION_REF_POPUP);
//		
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
//		add(messageDestinationRefPanel, gridBagConstraints);		
	}
	
	public void addListeners() {
		messageDestinationModel.addTableModelListener(this);
//		messageDestinationRefModel.addTableModelListener(this);
	}
	
	public void removeListeners() {
//		messageDestinationRefModel.removeTableModelListener(this);
		messageDestinationModel.removeTableModelListener(this);
	}
	
	/** Initialization of all the fields in this panel from the bean that
	 *  was passed in.
	 */
	public void initFields(WebAppRoot bean) {
		messageDestinationPanel.setModel(bean.getMessageDestinations(), bean.getAppServerVersion());		
        
        if(ASDDVersion.SUN_APPSERVER_9_0.compareTo(bean.getAppServerVersion()) <= 0) {
            showAS90Fields();
//    		messageDestinationRefPanel.setModel(bean.getMessageDestinationRefs(), bean.getAppServerVersion());		
        }
	}
	
    private void showAS90Fields() {
        if(!as90FeaturesVisible) {
            as90FeaturesVisible = true;
//            messageDestinationRefPanel.setVisible(true);
        }
    }
    
    private void hideAS90Fields() {
        if(as90FeaturesVisible) {
            as90FeaturesVisible = false;
//            messageDestinationRefPanel.setVisible(false);
        }
    }
    
	/** ----------------------------------------------------------------------- 
	 *  Implementation of javax.swing.event.TableModelListener
	 */
	public void tableChanged(TableModelEvent e) {
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			try {
				Object eventSource = e.getSource();
				if(eventSource == messageDestinationModel) {
					bean.setMessageDestinations(messageDestinationModel.getData());
					bean.firePropertyChange("messageDestination", null, messageDestinationModel.getData());
//				} else if(eventSource == messageDestinationRefModel) {
//					bean.setMessageDestinationRefs(messageDestinationRefModel.getData());
//					bean.firePropertyChange("messageDestinationRef", null, messageDestinationRefModel.getData());
				}
			} catch(PropertyVetoException ex) {
				// FIXME undo whatever changed... how?
			}
		}
	}
    
    // New for migration to sun DD API model.  Factory instance to pass to generic table model
    // to allow it to create messageDestination beans.
	static GenericTableModel.ParentPropertyFactory messageDestinationFactory =
        new GenericTableModel.ParentPropertyFactory() {
            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
                return StorageBeanFactory.getStorageBeanFactory(asVersion).createMessageDestination();
            }
        };
        
//	static GenericTableModel.ParentPropertyFactory messageDestinationRefFactory =
//        new GenericTableModel.ParentPropertyFactory() {
//            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
//                return StorageBeanFactory.getStorageBeanFactory(asVersion).createMessageDestinationRef();
//            }
//        };
}
