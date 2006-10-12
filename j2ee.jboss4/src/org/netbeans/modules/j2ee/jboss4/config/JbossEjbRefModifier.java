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

package org.netbeans.modules.j2ee.jboss4.config;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.jboss4.config.EjbDeploymentConfiguration.BEAN_TYPE;
import org.netbeans.modules.j2ee.jboss4.config.gen.EjbRef;
import org.netbeans.modules.j2ee.jboss4.config.gen.EnterpriseBeans;
import org.netbeans.modules.j2ee.jboss4.config.gen.Entity;
import org.netbeans.modules.j2ee.jboss4.config.gen.Jboss;
import org.netbeans.modules.j2ee.jboss4.config.gen.MessageDriven;
import org.netbeans.modules.j2ee.jboss4.config.gen.Session;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * This class implements the core of the jboss.xml file modifications.
 *
 * @author lkotouc
 */
final class JbossEjbRefModifier {

    /**
     * Add a reference to the given ejb to the enterprise beans of the given type if it does not exist yet.
     *
     * @param modifiedJboss Jboss graph instance being modified
     * @param ejbRefName ejb reference name
     * @param beanNames the beans (ejb-name value) which might need to add ejb reference specified by ejbRefName
     * @param beanType type of bean to add ejb reference to
     */
    static void modify(Jboss modifiedJboss, String ejbRefName, Set beanNames, BEAN_TYPE beanType) {

        assert(beanNames.size() > 0);

        if (modifiedJboss.getEnterpriseBeans() == null)
            modifiedJboss.setEnterpriseBeans(new EnterpriseBeans());

        if (beanType == BEAN_TYPE.SESSION) {
            addSessionEjbReference(modifiedJboss, ejbRefName, beanNames);
        } else
        if (beanType == BEAN_TYPE.ENTITY) {
            addEntityEjbReference(modifiedJboss, ejbRefName, beanNames);
        }
    }
    
    /**
     * Add a new ejb reference to the session beans without it.
     * 
     * @param modifiedJboss Jboss instance being modified
     * @param ejbRefName ejb reference name
     * @param sessionNames the sessions (ejb-name value) which might need to add ejb reference specified by ejbRefName
     */
    private static void addSessionEjbReference(Jboss modifiedJboss, String ejbRefName, Set sessionNames) {

        List/*<Session>*/ sesssionsWithoutReference = new LinkedList();

        EnterpriseBeans eb = modifiedJboss.getEnterpriseBeans();
        
        Session[] sessions = eb.getSession();
        for (int i = 0; i < sessions.length; i++) {
            String ejbName = sessions[i].getEjbName();
            if (sessionNames.contains(ejbName)) { // session found -> check whether it has the ejb-ref
                sessionNames.remove(ejbName);     // we don't care about it anymore
                EjbRef[] ejbRefs = sessions[i].getEjbRef();
                int j = 0;
                for ( ; j < ejbRefs.length; j++) {
                    String rrn = ejbRefs[j].getEjbRefName();
                    if (ejbRefName.equals(rrn))
                        break; // ejb-ref found, continuing with the next session
                }
                if (j == ejbRefs.length) // ejb-ref not found
                    sesssionsWithoutReference.add(sessions[i]);
            }
        }

        //no session tag yet (sessions.length == 0) or 
        //there are sessions in sessionNames which were not found among the existing ones (those were not removed)
        for (Iterator it = sessionNames.iterator(); it.hasNext(); ) {
            String sessionName = (String)it.next();
            Session session = new Session();
            session.setEjbName(sessionName);
            session.setJndiName(sessionName);

            //add the new session to enterprise-beans
            eb.addSession(session);

            //add the new session to the list of sessions without the ejb reference
            sesssionsWithoutReference.add(session);
        }

        //the ejb reference will be added to each session without it
        for (Iterator it = sesssionsWithoutReference.iterator(); it.hasNext(); ) {
            EjbRef newER = new EjbRef();
            newER.setEjbRefName(ejbRefName);
            newER.setJndiName(JBDeploymentConfiguration.JBOSS4_EJB_JNDI_PREFIX + ejbRefName);
            Session session = (Session)it.next();
            session.addEjbRef(newER);
        }

    }
    
    /**
     * Add a new ejb reference to the entity beans without it.
     * 
     * @param modifiedJboss Jboss instance being modified
     * @param ejbRefName ejb reference name
     * @param entityNames the entities (ejb-name value) which might need to add ejb reference specified by ejbRefName
     */
    private static void addEntityEjbReference(Jboss modifiedJboss, String ejbRefName, Set entityNames) {

        List/*<Entity>*/ entitiesWithoutReference = new LinkedList();

        EnterpriseBeans eb = modifiedJboss.getEnterpriseBeans();

        Entity[] entities = eb.getEntity();
        for (int i = 0; i < entities.length; i++) {
            String ejbName = entities[i].getEjbName();
            if (entityNames.contains(ejbName)) { // entity found -> check whether it has the ejb-ref
                entityNames.remove(ejbName);     // we don't care about it anymore
                EjbRef[] ejbRefs = entities[i].getEjbRef();
                int j = 0;
                for ( ; j < ejbRefs.length; j++) {
                    String rrn = ejbRefs[j].getEjbRefName();
                    if (ejbRefName.equals(rrn))
                        break; // ejb-ref found, continuing with the next entity
                }
                if (j == ejbRefs.length) // ejb-ref not found
                    entitiesWithoutReference.add(entities[i]);
            }
        }

        //no entity tag yet (entities.length == 0) or 
        //there are entities in entityNames which were not found among the existing ones (those were not removed)
        for (Iterator it = entityNames.iterator(); it.hasNext(); ) {
            String entityName = (String)it.next();
            Entity entity = new Entity();
            entity.setEjbName(entityName);
            entity.setJndiName(entityName);

            //add the new entity to enterprise-beans
            eb.addEntity(entity);

            //add the new entity to the list of entities without the ejb reference
            entitiesWithoutReference.add(entity);
        }

        //the ejb reference will be added to each entity without it
        for (Iterator it = entitiesWithoutReference.iterator(); it.hasNext(); ) {
            EjbRef newER = new EjbRef();
            newER.setEjbRefName(ejbRefName);
            newER.setJndiName(JBDeploymentConfiguration.JBOSS4_EJB_JNDI_PREFIX + ejbRefName);
            Entity entity = (Entity)it.next();
            entity.addEjbRef(newER);
        }

    }

    /**
     * Add a reference to the given ejb to the message-driven beans if it does not exist yet.
     *
     * @param modifiedJboss Jboss graph instance being modified
     * @param ejbRefName ejb reference name
     * @param beans the bean names (ejb-name) mapped to the message destinations (message-destination-link)
     * which might need to add ejb reference specified by ejbRefName
     */
    static void modifyMsgDrv(Jboss modifiedJboss, String ejbRefName, Map beans) {

        assert(beans.size() > 0);

        if (modifiedJboss.getEnterpriseBeans() == null)
            modifiedJboss.setEnterpriseBeans(new EnterpriseBeans());

        addMsgDrvEjbReference(modifiedJboss, ejbRefName, beans);
    }

    /**
     * Add a new ejb reference to the message-driven beans without it.
     * 
     * @param modifiedJboss Jboss instance being modified
     * @param ejbRefName ejb reference name
     * @param beans the bean names (ejb-name) mapped to the message destinations (message-destination-link)
     * which might need to add ejb reference specified by ejbRefName
     */
    private static void addMsgDrvEjbReference(Jboss modifiedJboss, String ejbRefName, Map beans) {

        List/*<Entity>*/ msgdrvsWithoutReference = new LinkedList();

        EnterpriseBeans eb = modifiedJboss.getEnterpriseBeans();

        MessageDriven[] msgDrivens = eb.getMessageDriven();
        for (int i = 0; i < msgDrivens.length; i++) {
            String ejbName = msgDrivens[i].getEjbName();
            if (beans.containsKey(ejbName)) { // msgdrv found -> check whether it has the ejb-ref
                beans.remove(ejbName);        // we don't care about it anymore
                EjbRef[] ejbRefs = msgDrivens[i].getEjbRef();
                int j = 0;
                for ( ; j < ejbRefs.length; j++) {
                    String rrn = ejbRefs[j].getEjbRefName();
                    if (ejbRefName.equals(rrn))
                        break; // ejb-ref found, continuing with the next entity
                }
                if (j == ejbRefs.length) // resource-ref not found
                    msgdrvsWithoutReference.add(msgDrivens[i]);
            }
        }

        //no message-driven tag yet (msgDrivens.length == 0) or 
        //there are MDBs in beans map which were not found among the existing ones (those were not removed)
        for (Iterator it = beans.keySet().iterator(); it.hasNext(); ) {
            String ejbName = (String)it.next();
            MessageDriven mdb = new MessageDriven();
            mdb.setEjbName(ejbName);
            String msgDestination = (String)beans.get(ejbName);
            mdb.setDestinationJndiName(msgDestination);
                
            //add the new mdb to enterprise-beans
            eb.addMessageDriven(mdb);

            //add the new mdb to the list of mdbs without the resource reference
            msgdrvsWithoutReference.add(mdb);
        }

        //the ejb reference will be added to each mdb without it
        for (Iterator it = msgdrvsWithoutReference.iterator(); it.hasNext(); ) {
            EjbRef newER = new EjbRef();
            newER.setEjbRefName(ejbRefName);
            newER.setJndiName(JBDeploymentConfiguration.JBOSS4_EJB_JNDI_PREFIX + ejbRefName);
            MessageDriven mdb = (MessageDriven)it.next();
            mdb.addEjbRef(newER);
        }

    }
    
}
