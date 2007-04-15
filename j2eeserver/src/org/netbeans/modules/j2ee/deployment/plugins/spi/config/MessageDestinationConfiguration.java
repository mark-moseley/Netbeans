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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 * Configuration useful for managing module message destinations.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 *
 * @since 1.25
 * @author Libor Kotouc
 */
public interface MessageDestinationConfiguration {
    
    /**
     * Retrieves message destinations stored in the module.
     * 
     * @return set of message destinations
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException;
    
    /**
     * Tests whether a message destination creation is supported.
     *
     * @return true if message destination creation is supported, false otherwise.
     */
    public boolean supportsCreateMessageDestination();
            
    /**
     * Creates and saves a message destination in the module if it does not exist in the module yet.
     * Message destinations are considered to be equal if their JNDI names are equal.
     *
     * @param name name of the message destination
     * @param type message destination type; the value is of 
     * org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type type
     * @return created message destination
     * 
     * @throws UnsupportedOperationException if this opearation is not supported
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) 
    throws UnsupportedOperationException, ConfigurationException;
    
    /**
     * Binds the message destination name with message-driven bean.
     * 
     * @param mdbName MDB name
     * @param name name of the message destination
     * @param type message destination type; the value is of 
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public void bindMdbToMessageDestination(String mdbName, String name, MessageDestination.Type type) throws ConfigurationException;
    
    /**
     * Finds name of message destination which the given MDB listens to
     * 
     * @param mdbName MDB name
     * @return message destination name
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public String findMessageDestinationName(String mdbName) throws ConfigurationException;

    /**
     * Binds the message destination reference name with the corresponding message destination which is
     * identified by the given name.
     * 
     * @param referenceName reference name used to identify the message destination
     * @param connectionFactoryName connection factory name
     * @param destName name of the message destination
     * @param type message destination type
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, 
            String destName, MessageDestination.Type type) throws ConfigurationException;

    /**
     * Binds the message destination reference name with the corresponding message destination which is
     * identified by the given name. The reference is used within the EJB scope.
     * 
     * @param ejbName EJB name
     * @param ejbType EJB type - the possible values are 
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
     * @param referenceName reference name used to identify the message destination
     * @param connectionFactoryName connection factory name
     * @param destName name of the message destination
     * @param type message destination type
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException;
    
}
