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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * Generator of MessageDriven EJBs for EJB 2.1 and 3.0
 *
 * @author Martin Adamek
 */
public final class MessageGenerator {

    private static final String EJB21_EJBCLASS = "Templates/J2EE/EJB21/MessageDrivenEjbClass.java"; // NOI18N
    private static final String EJB30_QUEUE_EJBCLASS = "Templates/J2EE/EJB30/MessageDrivenQueueEjbClass.java"; // NOI18N
    private static final String EJB30_TOPIC_EJBCLASS = "Templates/J2EE/EJB30/MessageDrivenTopicEjbClass.java"; // NOI18N

    // informations collected in wizard
    private final FileObject pkg;
    private final MessageDestination messageDestination;
    private final boolean isSimplified;
    private final boolean isXmlBased;

    // EJB naming options
    private final EJBNameOptions ejbNameOptions;
    private final String ejbName;
    private final String ejbClassName;
    private final String displayName;
    
    private final String packageName;
    private final String packageNameWithDot;
    
    private final Map<String, String> templateParameters;

    public static MessageGenerator create(String wizardTargetName, FileObject pkg, MessageDestination messageDestination, boolean isSimplified, boolean isXmlBased) {
        return new MessageGenerator(wizardTargetName, pkg, messageDestination, isSimplified, isXmlBased);
    }
    
    private MessageGenerator(String wizardTargetName, FileObject pkg, MessageDestination messageDestination, boolean isSimplified, boolean isXmlBased) {
        this.pkg = pkg;
        this.messageDestination = messageDestination;
        this.isSimplified = isSimplified;
        this.isXmlBased = isXmlBased;
        this.ejbNameOptions = new EJBNameOptions();
        this.ejbName = ejbNameOptions.getMessageDrivenEjbNamePrefix() + wizardTargetName + ejbNameOptions.getMessageDrivenEjbNameSuffix();
        this.ejbClassName = ejbNameOptions.getMessageDrivenEjbClassPrefix() + wizardTargetName + ejbNameOptions.getMessageDrivenEjbClassSuffix();
        this.displayName = ejbNameOptions.getMessageDrivenDisplayNamePrefix() + wizardTargetName + ejbNameOptions.getMessageDrivenDisplayNameSuffix();
        this.packageName = EjbGenerationUtil.getSelectedPackageName(pkg);
        this.packageNameWithDot = packageName + ".";
        this.templateParameters = new HashMap<String, String>();
        // fill all possible template parameters
        this.templateParameters.put("package", packageName);
    }
    
    public FileObject generate() throws IOException {
        FileObject resultFileObject = null;
        if (isSimplified) {
            resultFileObject = generateEJB30Classes();
            if (isXmlBased) {
                generateEJB30Xml();
            }
        } else {
            resultFileObject = generateEJB21Classes();
            if (isXmlBased) {
                try {
                    generateEJB21Xml();
                } catch (VersionNotSupportedException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }

            try {
                Project project = FileOwnerQuery.getOwner(pkg);
                J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
                j2eeModuleProvider.getConfigSupport().bindMdbToMessageDestination(
                        ejbName,
                        messageDestination.getName(),
                        messageDestination.getType());
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return resultFileObject;
    }
    
    private FileObject generateEJB21Classes() throws IOException {
        FileObject resultFileObject = GenerationUtils.createClass(EJB21_EJBCLASS,  pkg, ejbClassName, null, templateParameters);
        ///
        Project project = FileOwnerQuery.getOwner(pkg);
        J2eeModuleProvider pwm = project.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        ///
        return resultFileObject;
    }
    
    private boolean isQueue() {
        return MessageDestination.Type.QUEUE.equals(messageDestination.getType());
    }
    
    private FileObject generateEJB30Classes() throws IOException {
        String ejbClassTemplate = isQueue() ? EJB30_QUEUE_EJBCLASS : EJB30_TOPIC_EJBCLASS;
        FileObject resultFileObject = GenerationUtils.createClass(ejbClassTemplate,  pkg, ejbClassName, null, templateParameters);

        //TODO: RETOUCHE we don't have model for annotations yet
//        // Create server resources for this bean.
//        //
//        // !PW Posted via RequestProcessor for now because the merged annotion provider
//        // does not have any information for this bean if this is invoked syncronously.
//        // We need to find a more stable mechanism for the, perhaps new API that directly
//        // accepts the annotation reference created above.  This construct is too fragile.
//        //
//        // Note: Even with 1s (1000ms) delay, sometimes the data was still not available.
//        //
//        Project project = FileOwnerQuery.getOwner(pkg);
//        final J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
//        RequestProcessor.getDefault().post(new Runnable() {
//            public void run() {
//                if(pwm != null) {
//                    pwm.getConfigSupport().ensureResourceDefinedForEjb(ejbName, "message-driven"); //NOI18N
//                }
//            }
//        }, 2000);
        
        return resultFileObject;
    }
    
    @SuppressWarnings("deprecation") //NOI18N
    private void generateEJB21Xml() throws IOException, VersionNotSupportedException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit());
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        MessageDriven messageDriven = null;
        if (beans == null) {
            beans  = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        messageDriven = beans.newMessageDriven();
        ActivationConfig config = messageDriven.newActivationConfig();
        ActivationConfigProperty destProp = config.newActivationConfigProperty();
        destProp.setActivationConfigPropertyName("destinationType"); // NOI18N
        ActivationConfigProperty ackProp = config.newActivationConfigProperty();
        ackProp.setActivationConfigPropertyName("acknowledgeMode"); // NOI18N
        ackProp.setActivationConfigPropertyValue("Auto-acknowledge"); // NOI18N
        config.addActivationConfigProperty(ackProp);
        if (isQueue()) {
            String queue = "javax.jms.Queue"; // NOI18N
            messageDriven.setMessageDestinationType(queue);
            destProp.setActivationConfigPropertyValue(queue);
        } else {
            String topic = "javax.jms.Topic"; // NOI18N
            messageDriven.setMessageDestinationType(topic);
            destProp.setActivationConfigPropertyValue(topic);
            ActivationConfigProperty durabilityProp = config.newActivationConfigProperty();
            durabilityProp.setActivationConfigPropertyName("subscriptionDurability"); // NOI18N
            durabilityProp.setActivationConfigPropertyValue("Durable"); // NOI18N
            config.addActivationConfigProperty(durabilityProp);
            
            ActivationConfigProperty clientIdProp = config.newActivationConfigProperty();
            clientIdProp.setActivationConfigPropertyName("clientId"); // NOI18N
            clientIdProp.setActivationConfigPropertyValue(ejbName); // NOI18N
            config.addActivationConfigProperty(clientIdProp);
            
            ActivationConfigProperty subscriptionNameProp = config.newActivationConfigProperty();
            subscriptionNameProp.setActivationConfigPropertyName("subscriptionName"); // NOI18N
            subscriptionNameProp.setActivationConfigPropertyValue(ejbName); // NOI18N
            config.addActivationConfigProperty(subscriptionNameProp);
            
        }
        config.addActivationConfigProperty(destProp);
        messageDriven.setActivationConfig(config);
        messageDriven.setEjbName(ejbName);
        messageDriven.setDisplayName(displayName);
        messageDriven.setEjbClass(packageNameWithDot + ejbClassName);
        messageDriven.setTransactionType(MessageDriven.TRANSACTION_TYPE_CONTAINER);
        
        beans.addMessageDriven(messageDriven);
        // add transaction requirements
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        if (assemblyDescriptor == null) {
            assemblyDescriptor = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(assemblyDescriptor);
        }
        org.netbeans.modules.j2ee.dd.api.common.MessageDestination messageDestination = assemblyDescriptor.newMessageDestination();
        String destinationLink = ejbName + "Destination"; //NOI18N
        messageDestination.setDisplayName("Destination for " + displayName);
        messageDestination.setMessageDestinationName(destinationLink);
        assemblyDescriptor.addMessageDestination(messageDestination);
        
        messageDriven.setMessageDestinationLink(destinationLink);
        ContainerTransaction containerTransaction = assemblyDescriptor.newContainerTransaction();
        containerTransaction.setTransAttribute("Required"); //NOI18N
        Method method = containerTransaction.newMethod();
        method.setEjbName(ejbName);
        method.setMethodName("*"); //NOI18N
        containerTransaction.addMethod(method);
        assemblyDescriptor.addContainerTransaction(containerTransaction);
        ejbJar.write(ejbModule.getDeploymentDescriptor());
        Project project = FileOwnerQuery.getOwner(pkg);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        try {
	    // TODO: temporary using ejbName as the JNDI name, what should be correct?
            j2eeModuleProvider.getConfigSupport().ensureResourceDefinedForEjb(ejbName, "message-driven", ejbName); //NOI18N
        } catch (ConfigurationException e) {
            // TODO: report this to the user
        }
    }
    
    private void generateEJB30Xml() throws IOException {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }
    
}
