/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;


/**
 *
 * @author  vkraemer
 */
public class MDEjb extends BaseEjb {
    
    /** Holds value of property subscriptionName. */
    private String subscriptionName;
    
    /** Holds value of property maxMessageLoad. */
    private String maxMessageLoad;
    
	/** Holds value of property mdbConnectionFactory. */
	private MdbConnectionFactory mdbConnectionFactory;

	/** Holds value of property mdbResourceAdapter. */
	private MdbResourceAdapter mdbResourceAdapter;

        public static final String __Enabled = "enabled"; //NOI18N
        public static final String __JMSResource = "jms"; //NOI18N
        public static final String __JMSConnectionFactory = "jms_CF"; //NOI18N
        public static final String __JndiName = "jndi-name"; //NOI18N
        public static final String __ResType = "res-type"; //NOI18N

        private static final String MESSAGE_DSTN_NAME = "msg_dstn_name"; //NOI18N
        private static final String MESSAGE_DSTN_TYPE = "msg_dstn_type"; //NOI18N

        private static final String QUEUE = "javax.jms.Queue"; //NOI18N
        private static final String QUEUE_CNTN_FACTORY = "javax.jms.QueueConnectionFactory"; //NOI18N
        private static final String TOPIC = "javax.jms.Topic"; //NOI18N
        private static final String TOPIC_CNTN_FACTORY = "javax.jms.TopicConnectionFactory"; //NOI18N
       


    /** Creates a new instance of SunONEStatelessEjbDConfigBean */
	public MDEjb() {
	}

    /** Getter for property subscriptionName.
     * @return Value of property subscriptionName.
     *
     */
    public String getSubscriptionName() {
        return this.subscriptionName;
    }
   
    /** Setter for property subscriptionName.
     * @param subscriptionName New value of property subscriptionName.
     *
     * @throws PropertyVetoException
     *
     */
    public void setSubscriptionName(String subscriptionName) throws java.beans.PropertyVetoException {
        String oldSubscriptionName = this.subscriptionName;
        getVCS().fireVetoableChange("subscriptionName", oldSubscriptionName, subscriptionName);
        this.subscriptionName = subscriptionName;
        getPCS().firePropertyChange("subscriptionName", oldSubscriptionName, subscriptionName);
    }
    
    /** Getter for property maxMessageLoad.
     * @return Value of property maxMessageLoad.
     *
     */
    public String getMaxMessageLoad() {
        return this.maxMessageLoad;
    }
    
    /** Setter for property maxMessageLoad.
     * @param maxMessageLoad New value of property maxMessageLoad.
     *
     * @throws PropertyVetoException
     *
     */
    public void setMaxMessageLoad(String maxMessageLoad) throws java.beans.PropertyVetoException {
        String oldMaxMessageLoad = this.maxMessageLoad;
        getVCS().fireVetoableChange("maxMessageLoad", oldMaxMessageLoad, maxMessageLoad);
        this.maxMessageLoad = maxMessageLoad;
        getPCS().firePropertyChange("maxMessageLoad", oldMaxMessageLoad, maxMessageLoad);
    }
    
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	protected class MDEjbSnippet extends BaseEjb.BaseEjbSnippet {
        public CommonDDBean getDDSnippet() {
            Ejb ejb = (Ejb) super.getDDSnippet();
            String version = getAppServerVersion().getEjbJarVersionAsString();

            if(subscriptionName != null){
                ejb.setJmsDurableSubscriptionName(subscriptionName);
            }

            if(maxMessageLoad != null){
                ejb.setJmsMaxMessagesLoad(maxMessageLoad);
            }

            if(null != mdbConnectionFactory){
                ejb.setMdbConnectionFactory((MdbConnectionFactory)mdbConnectionFactory.cloneVersion(version));
            }

            if(null != mdbResourceAdapter){
                ejb.setMdbResourceAdapter((MdbResourceAdapter)mdbResourceAdapter.cloneVersion(version));
            }

            return ejb;
        }
        
		public boolean hasDDSnippet() {
            boolean result = super.hasDDSnippet();
            
            if(!result) {
                if(Utils.notEmpty(subscriptionName)) {
                    return true;
                }

                if(Utils.notEmpty(maxMessageLoad)) {
                    return true;
                }

                if(mdbConnectionFactory != null) {
                    return true;
                }

                if(mdbResourceAdapter != null) {
                    return true;
                }
            }
            
            return result;
		}
    }


	java.util.Collection getSnippets() {
            Collection snippets = new ArrayList();
            snippets.add(new MDEjbSnippet());	
            return snippets;
        }


	protected void loadEjbProperties(Ejb savedEjb) {
		super.loadEjbProperties(savedEjb);
		
        subscriptionName = savedEjb.getJmsDurableSubscriptionName();
        maxMessageLoad = savedEjb.getJmsMaxMessagesLoad();
                
		MdbConnectionFactory mdbConnectionFactory = savedEjb.getMdbConnectionFactory();
		if(null != mdbConnectionFactory){
			this.mdbConnectionFactory = mdbConnectionFactory;
		}

		MdbResourceAdapter mdbResourceAdapter = savedEjb.getMdbResourceAdapter();
		if(null != mdbResourceAdapter){
			this.mdbResourceAdapter = mdbResourceAdapter;
		}
	}
    
    protected void clearProperties() {
        super.clearProperties();
        
        subscriptionName = null;
        maxMessageLoad = null;
        mdbConnectionFactory = null;
        mdbResourceAdapter = null;
    }
    
//    protected void setDefaultProperties() {
//        super.setDefaultProperties();
//
//        // Message driven beans always have a default JNDI name, which could be
//        // set here.  However, this is instead implemented in BaseEjb.setDefaultProperties()
//        // with help from getDefaultJndiName() and requiresJndiName() which are overridden
//        // in this class to provide the correct defaults (jms/[ejbname] and true).
//    }
    
    // Not really necessary to override this, but do it anyway so the proper name
    // is always available.
    protected String getDefaultJndiName() {
        return "jms/" + getEjbName(); // NOI18N // J2EE recommended jndiName for jms resources.
    }

    protected boolean requiresJndiName() {
        // For JavaEE5 and later spec bean, jndi name is optional, otherwise required.
        return J2EEVersion.J2EE_1_4.compareSpecification(getJ2EEModuleVersion()) >= 0;
    }

    
	/* ------------------------------------------------------------------------
	 * XPath to Factory mapping support
	 */
/*
	private HashMap mdEjbFactoryMap;

	protected Map getXPathToFactoryMap() {
		if(mdEjbFactoryMap == null) {
			mdEjbFactoryMap = (HashMap) super.getXPathToFactoryMap();
			
			// add child DCB's specific to Entity Beans
		}
		
		return mdEjbFactoryMap;
	}
*/

    //Methods called by Customizer
    public void addActivationConfigProperty(ActivationConfigProperty property){
        ActivationConfig activationCfg = getActivationConfig();
        activationCfg.addActivationConfigProperty(property);
    }


    public void removeActivationConfigProperty(ActivationConfigProperty property){
        if(null != mdbResourceAdapter){
            ActivationConfig activationCfg = mdbResourceAdapter.getActivationConfig();
            if(null != activationCfg){
                activationCfg.removeActivationConfigProperty(property);
                if(activationCfg.sizeActivationConfigProperty() < 1){
                    activationCfg.setActivationConfigProperty(null);
                }
                if(null == activationCfg.getDescription()){
                    mdbResourceAdapter.setActivationConfig(null);
                    if(null == mdbResourceAdapter.getResourceAdapterMid()){
                        try{
                            setMdbResourceAdapter(null);
                        }catch(java.beans.PropertyVetoException exception){
                        }
                    }
                }
            }
        }
    }


    private ActivationConfig getActivationConfig(){
        MdbResourceAdapter mdbResourceAdapter = getMdbResourceAdapter();
        if(null == mdbResourceAdapter){
            mdbResourceAdapter = getConfig().getStorageFactory().createMdbResourceAdapter();
            try{
                setMdbResourceAdapter(mdbResourceAdapter);
            }catch(java.beans.PropertyVetoException exception){
            }
        }

        ActivationConfig activationCfg = 
            mdbResourceAdapter.getActivationConfig();
        if(null == activationCfg){
            activationCfg = getConfig().getStorageFactory().createActivationConfig();
            mdbResourceAdapter.setActivationConfig(activationCfg);
        }
        return activationCfg;
    }


    /** Getter for property mdbConnectionFactory.
     * @return Value of property mdbConnectionFactory.
     *
     */
    public MdbConnectionFactory getMdbConnectionFactory() {
        return this.mdbConnectionFactory;
    }    

    /** Setter for property mdbConnectionFactory.
     * @param mdbConnectionFactory New value of property mdbConnectionFactory.
     *
     * @throws PropertyVetoException
     *
     */
    public void setMdbConnectionFactory(MdbConnectionFactory mdbConnectionFactory) throws java.beans.PropertyVetoException {
        MdbConnectionFactory oldMdbConnectionFactory = this.mdbConnectionFactory;
        getVCS().fireVetoableChange("mdbConnectionFactory", oldMdbConnectionFactory, mdbConnectionFactory);
        this.mdbConnectionFactory = mdbConnectionFactory;
        getPCS().firePropertyChange("mdbConnectionFactory", oldMdbConnectionFactory, mdbConnectionFactory);
    }
    
    /** Getter for property mdbResourceAdapter.
     * @return Value of property mdbResourceAdapter.
     *
     */
    public MdbResourceAdapter getMdbResourceAdapter() {
        return this.mdbResourceAdapter;
    }
    
    /** Setter for property mdbResourceAdapter.
     * @param mdbResourceAdapter New value of property mdbResourceAdapter.
     *
     * @throws PropertyVetoException
     *
     */
    public void setMdbResourceAdapter(MdbResourceAdapter mdbResourceAdapter) throws java.beans.PropertyVetoException {
        MdbResourceAdapter oldMdbResourceAdapter = this.mdbResourceAdapter;
        getVCS().fireVetoableChange("mdbResourceAdapter", oldMdbResourceAdapter, mdbResourceAdapter);
        this.mdbResourceAdapter = mdbResourceAdapter;
        getPCS().firePropertyChange("mdbResourceAdapter", oldMdbResourceAdapter, mdbResourceAdapter);
    }


    public String getHelpId() {
        return "AS_CFG_MDEjb";                                          //NOI18N
    }

    private String getMessageDestinationInfo(String parameter){
        String msgDstnInfo = null; 
        DDBeanRoot ejbJarRootDD = getDDBean().getRoot();

        if(ejbJarRootDD != null) {
            DDBean[] ejbNameDD = 
                ejbJarRootDD.getChildBean("ejb-jar/enterprise-beans/message-driven/ejb-name"); //NOI18N
            // First, find the ejb-name that corresponds to this bean
            for(int i = 0; i < ejbNameDD.length; i++) {
                //System.out.println("getEjbName(): " + getEjbName());  //NOI18N
                //System.out.println("ejbNameDD[i].getText(): " + ejbNameDD[0].getText());  //NOI18N
                if(getEjbName().equals(ejbNameDD[i].getText())) {
                    if(parameter.equals(MESSAGE_DSTN_NAME)){
                        DDBean[] msgDstnLink = 
                            ejbNameDD[i].getChildBean("../message-destination-link"); //NOI18N
                       if(msgDstnLink.length > 0){
                           msgDstnInfo = msgDstnLink[0].getText();  
                        }
                    } else {
                        if(parameter.equals(MESSAGE_DSTN_TYPE)){
                            DDBean[] msgDstnType = 
                                ejbNameDD[i].getChildBean("../message-destination-type"); //NOI18N
                           if(msgDstnType.length > 0){
                               msgDstnInfo = msgDstnType[0].getText();  
                            }
                        }
                    }
                }
            }
        }
        return msgDstnInfo;
    }
}
