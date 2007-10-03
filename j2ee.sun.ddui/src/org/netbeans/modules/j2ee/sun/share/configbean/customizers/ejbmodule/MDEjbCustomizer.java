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
 * MDEjbCustomizer.java        October 27, 2003, 1:05 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.MDEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.ValidationError;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class MDEjbCustomizer extends EjbCustomizer {

    private MDEjb theMDBean;
    private MDEjbPanel mdEjbPanel;
    private BeanPoolPanel beanPoolPanel;
    private ActivationCfgPropertyPanel actvnCfgPrptyPanel;
    private MdbConnectionFactoryPanel mdbConnectionFactoryPanel;

    private ActivationCfgPropertyModel activnCfgPrptyModel;

    
    /** Creates a new instance of MDEjbCustomizer */
	public MDEjbCustomizer() {
	}

    public MDEjb getMDBean() {
        return theMDBean;
    }

    // Get the bean specific panel
    protected JPanel getBeanPanel() {
        mdEjbPanel = new MDEjbPanel(this);
        return mdEjbPanel;
    }

    // Initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean) {
        mdEjbPanel.initFields(theMDBean);
    }

    protected void addTabbedBeanPanels() {
        beanPoolPanel = new BeanPoolPanel(this);
        tabbedPanel.insertTab(bundle.getString("LBL_BeanPool"), null, beanPoolPanel, null, 0); // NOI18N

        mdbConnectionFactoryPanel = new MdbConnectionFactoryPanel(this);
        mdbConnectionFactoryPanel.getAccessibleContext().setAccessibleName(bundle.getString("Mdb_Connection_Factory_Acsbl_Name"));       //NOI18N
        mdbConnectionFactoryPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("Mdb_Connection_Factory_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_Mdb_Connection_Factory"), // NOI18N
            mdbConnectionFactoryPanel);

        activnCfgPrptyModel = new ActivationCfgPropertyModel();
        actvnCfgPrptyPanel = new ActivationCfgPropertyPanel(activnCfgPrptyModel);
        actvnCfgPrptyPanel.putClientProperty(PARTITION_KEY, ValidationError.PARTITION_EJB_MDBACTIVATION);
        actvnCfgPrptyPanel.getAccessibleContext().setAccessibleName(bundle.getString("Activation_Config_Property_Acsbl_Name"));             //NOI18N
        actvnCfgPrptyPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("Activation_Config_Property_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_Activation_Config_Property"), // NOI18N
            actvnCfgPrptyPanel);

        // Select Bean Pool Panel
        tabbedPanel.setSelectedIndex(tabbedPanel.indexOfTab(bundle.getString("LBL_BeanPool")));  //NOI18N        
    }
    
    protected void addListeners() {
        super.addListeners();
        
        activnCfgPrptyModel.addTableModelListener(this);
    }
    
    protected void removeListeners() {
        super.removeListeners();

        activnCfgPrptyModel.removeTableModelListener(this);
    }     

    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        beanPoolPanel.initFields(theBean.getBeanPool());

        mdbConnectionFactoryPanel.initFields(theMDBean);

        MdbResourceAdapter mdbResourceAdapter = theMDBean.getMdbResourceAdapter();
        if(mdbResourceAdapter == null) {
            actvnCfgPrptyPanel.setModel(theMDBean, null);
        } else {
            ActivationConfig activationCfg = mdbResourceAdapter.getActivationConfig();
            if(activationCfg == null) {
                actvnCfgPrptyPanel.setModel(theMDBean, null);
            } else {
                actvnCfgPrptyPanel.setModel(theMDBean, activationCfg.getActivationConfigProperty());
            }
        }
    }

    public String getHelpId() {
        return "AS_CFG_MDEjb";                                          //NOI18N
    }
    
    protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);
		
		if(bean instanceof MDEjb) {
            theMDBean = (MDEjb) bean;
			result = true;
		} else {
			// if bean is not a MDEjb, then it shouldn't have passed BaseEjb either.
			assert (result == false) : 
				"MDEjbCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N
				
            theMDBean = null;
			result = false;
		}
		
		return result;
    }
    
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);

        MDEjb bean = getMDBean();
        if(bean != null) {
            Object eventSource = e.getSource();
            
            // TODO send event on what row actually changed.
            if(eventSource == activnCfgPrptyModel) {
                bean.firePropertyChange("activationConfig", null, new Object());
                validateField(MDEjb.FIELD_MD_ADAPTER);
            }
        }
    }
}
