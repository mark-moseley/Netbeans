/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.TreeParser;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Nitya Doraisamy
 */
public class Hk2MessageDestinationManager implements  MessageDestinationDeployment {

    private Hk2DeploymentManager dm;

    private static final String DOMAIN_XML_PATH = "config/domain.xml";
    public static final String JMS_PREFIX = "jms/"; // NOI18N
    public static final String QUEUE = "javax.jms.Queue"; // NOI18N
    public static final String TOPIC = "javax.jms.Topic"; // NOI18N
    public static final String QUEUE_PROP = "PhysicalQueue"; // NOI18N
    public static final String TOPIC_PROP = "PhysicalTopic"; // NOI18N

    public static final String QUEUE_CNTN_FACTORY = "javax.jms.QueueConnectionFactory"; // NOI18N
    public static final String TOPIC_CNTN_FACTORY = "javax.jms.TopicConnectionFactory"; // NOI18N

    
    public Hk2MessageDestinationManager(Hk2DeploymentManager dm) {
        this.dm = dm;
    }

    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        GlassfishModule commonSupport = dm.getCommonServerSupport();
        String domainsDir = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR);
        String domainName = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR);
        // XXX Fix to work with current server domain, not just default domain.
        File domainXml = new File(domainsDir, domainName + File.separatorChar + DOMAIN_XML_PATH);
        return readMessageDestinations(domainXml, "/domain/", null);
    }

    public void deployMessageDestinations(Set<MessageDestination> destinations) throws ConfigurationException {
        Set<File> resourceDirList = new TreeSet<File>();
        for(MessageDestination dest: destinations) {
            if(dest instanceof SunMessageDestination) {
                File resourceDir = ((SunMessageDestination) dest).getResourceDir();
                if(resourceDir != null) {
                    resourceDirList.add(resourceDir);
                }
            }
        }

        // !PW FIXME needs to throw exception when conflicting resources are found.

//        for(File resourceDir: resourceDirList) {
//            registerResourceDir(resourceDir);
//        }
    }

    // ------------------------------------------------------------------------
    //  Used by ModuleConfigurationImpl since
    // ------------------------------------------------------------------------
    public static Set<MessageDestination> getMessageDestinations(File resourceDir) {
        File resourcesXml = new File(resourceDir, "sun-resources.xml");
        return readMessageDestinations(resourcesXml, "/", resourceDir);
    }
    
    private static Set<MessageDestination> readMessageDestinations(File xmlFile, String xPathPrefix, File resourcesDir) {
        Set<MessageDestination> msgDestinations = new HashSet<MessageDestination>();
        if(xmlFile.exists()) {
            Map<String, AdminObjectResource> aoResourceMap = new HashMap<String, AdminObjectResource>();
            List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
            pathList.add(new TreeParser.Path(xPathPrefix + "resources/admin-object-resource", new AdminObjectReader(aoResourceMap)));

            try {
                TreeParser.readXml(xmlFile, pathList);
            } catch(IllegalStateException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }

            for(AdminObjectResource adminObj: aoResourceMap.values()) {
                String type = adminObj.getResType();
                if (type.equals(QUEUE)) {
                    msgDestinations.add(new SunMessageDestination(adminObj.getJndiName(), MessageDestination.Type.QUEUE, resourcesDir));
                } else {
                    msgDestinations.add(new SunMessageDestination(adminObj.getJndiName(), MessageDestination.Type.TOPIC, resourcesDir));
                }
            }
        }
        return msgDestinations;
    }

    public static MessageDestination createMessageDestination(String name, MessageDestination.Type type, File resourceDir) throws ConfigurationException {
        SunMessageDestination msgDest = null;
        if(! name.startsWith(JMS_PREFIX)){
            name = JMS_PREFIX + name;
        }

        DuplicateAOFinder aoFinder = new DuplicateAOFinder(name);
        DuplicateConnectorFinder connFinder = new DuplicateConnectorFinder(name);
        ConnectorConnectionPoolFinder cpFinder = new ConnectorConnectionPoolFinder();

        File xmlFile = new File(resourceDir, "sun-resources.xml");
        if(xmlFile.exists()) {
            List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
            pathList.add(new TreeParser.Path("/resources/admin-object-resource", aoFinder));
            pathList.add(new TreeParser.Path("/resources/connector-resource", connFinder));
            pathList.add(new TreeParser.Path("/resources/connector-connection-pool", cpFinder));
            TreeParser.readXml(xmlFile, pathList);
            if(connFinder.isDuplicate()) {
               throw new ConfigurationException("Resource already exists");
            }
        }

        String connectionFactoryJndiName= name + "Factory"; // NOI18N
        String connectionFactoryPoolName = name + "FactoryPool"; // NOI18N
        try {
            createAdminObject(xmlFile, name, type);
            createConnectorConnectionPool(xmlFile, connectionFactoryPoolName, type);
            createConnector(xmlFile, connectionFactoryJndiName, connectionFactoryPoolName);
        } catch (IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex); // NOI18N
            throw new ConfigurationException(ex.getLocalizedMessage(), ex);
        }


        msgDest = new SunMessageDestination(name, type, resourceDir);
        return msgDest;

    }

    private static final String ATTR_JNDINAME = "jndi-name";
    private static final String ATTR_POOLNAME = "pool-name";
    private static final String ATTR_POOL_NAME = "name";
    
    private static final String AO_TAG_1 =
            "    <admin-object-resource enabled=\"true\" object-type=\"user\" ";
    private static final String ATTR_RESTYPE =
            " res-type";
    private static final String AO_TAG_2 =
            " res-adapter=\"jmsra\">\n";
    private static final String PROP_NAME =
            "Name";
    private static final String AO_TAG_3 =
            "    </admin-object-resource>\n";
    public static void createAdminObject(File sunResourcesXml, String jndiName, Type type) throws IOException {
        // <admin-object-resource res-adapter="jmsra" res-type="javax.jms.Queue" jndi-name="testao"></admin-object-resource>
        StringBuilder xmlBuilder = new StringBuilder(500);
        xmlBuilder.append(AO_TAG_1);
        ResourceModifier.appendAttr(xmlBuilder, ATTR_JNDINAME, jndiName, false);
        if (MessageDestination.Type.QUEUE.equals(type)) {
            ResourceModifier.appendAttr(xmlBuilder, ATTR_RESTYPE, QUEUE, true);
            xmlBuilder.append(AO_TAG_2);
            ResourceModifier.appendProperty(xmlBuilder, PROP_NAME, QUEUE_PROP, true);
        } else if (MessageDestination.Type.TOPIC.equals(type)) {
            ResourceModifier.appendAttr(xmlBuilder, ATTR_RESTYPE, TOPIC, true);
            xmlBuilder.append(AO_TAG_2);
            ResourceModifier.appendProperty(xmlBuilder, PROP_NAME, TOPIC_PROP, true);
        }
        xmlBuilder.append(AO_TAG_3);
        String xmlFragment = xmlBuilder.toString();
        Logger.getLogger("glassfish-javaee").log(Level.FINER, "New Connector resource:\n" + xmlFragment);
        ResourceModifier.appendResource(sunResourcesXml, xmlFragment);
    }

    private static final String CONNECTOR_POOL_TAG_1 =
            "    <connector-connection-pool enabled=\"true\" object-type=\"user\" ";
    private static final String ATTR_CONN_DEFINITION =
            " connection-definition-name";
    private static final String CONNECTOR_POOL_TAG_2 =
            " resource-adapter-name=\"jmsra\"/>\n";
    public static void createConnectorConnectionPool(File sunResourcesXml, String poolName, Type type) throws IOException {
        //<connector-connection-pool name="testconnectorpool" resource-adapter-name="jmsra" connectiondefinition="javax.jms.ConnectionFactory"/>
        StringBuilder xmlBuilder = new StringBuilder(500);
        xmlBuilder.append(CONNECTOR_POOL_TAG_1);
        ResourceModifier.appendAttr(xmlBuilder, ATTR_POOL_NAME, poolName, true);
        if(type.equals(MessageDestination.Type.QUEUE)) {
            ResourceModifier.appendAttr(xmlBuilder, ATTR_CONN_DEFINITION, QUEUE_CNTN_FACTORY, true);
        } else if (type.equals(MessageDestination.Type.TOPIC)) {
            ResourceModifier.appendAttr(xmlBuilder, ATTR_CONN_DEFINITION, TOPIC_CNTN_FACTORY, true);
        }
        xmlBuilder.append(CONNECTOR_POOL_TAG_2);

        String xmlFragment = xmlBuilder.toString();
        Logger.getLogger("glassfish-javaee").log(Level.FINER, "New Connector Connection Pool resource:\n" + xmlFragment);
        ResourceModifier.appendResource(sunResourcesXml, xmlFragment);
    }

    private static final String CONNECTOR_TAG_1 =
            "    <connector-resource ";
    private static final String CONNECTOR_TAG_2 =
            " />\n";
    public static void createConnector(File sunResourcesXml, String jndiName, String poolName) throws IOException {
        // <connector-resource pool-name="testconnectorpool" jndi-name="testconnector" />
        StringBuilder xmlBuilder = new StringBuilder(500);
        xmlBuilder.append(CONNECTOR_TAG_1);
        ResourceModifier.appendAttr(xmlBuilder, ATTR_JNDINAME, jndiName, true);
        ResourceModifier.appendAttr(xmlBuilder, ATTR_POOLNAME, poolName, true);
        xmlBuilder.append(CONNECTOR_TAG_2);

        String xmlFragment = xmlBuilder.toString();
        Logger.getLogger("glassfish-javaee").log(Level.FINER, "New Connector resource:\n" + xmlFragment);
        ResourceModifier.appendResource(sunResourcesXml, xmlFragment);
    }
    
    private static class AdminObjectReader extends TreeParser.NodeReader {

        private final Map<String, AdminObjectResource> resourceMap;

        public AdminObjectReader(Map<String, AdminObjectResource> resourceMap) {
            this.resourceMap = resourceMap;
        }

        //<admin-object-resource
            //enabled="true"
            //object-type="user"
            //jndi-name="jms/testQ"
            //res-type="javax.jms.Queue"
            //res-adapter="jmsra"
           //</admin-object-resource>

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String type = attributes.getValue("object-type");

            // Ignore system resources
            if(type != null && type.startsWith("system-")) {
                return;
            }
            String jndiName = attributes.getValue("jndi-name");
            String resType = attributes.getValue("res-type");
            if(jndiName != null && jndiName.length() > 0 &&
                    resType != null && resType.length() > 0) {
                // add to admin object resource list
                resourceMap.put(jndiName,
                        new AdminObjectResource(jndiName, resType));
            }
        }
    }
    
    private static class AdminObjectResource {

        private final String jndiName;
        private final String resType;

        public AdminObjectResource(String jndiName) {
            this(jndiName, "");
        }

        public AdminObjectResource(String jndiName, String resType) {
            this.jndiName = jndiName;
            this.resType = resType;
        }

        public String getJndiName() {
            return jndiName;
        }

        public String getResType() {
            return resType;
        }
    }

    private static class DuplicateAOFinder extends TreeParser.NodeReader {

        private final String targetJndiName;
        private boolean duplicate;
        private String resType;

        public DuplicateAOFinder(String jndiName) {
            targetJndiName = jndiName;
            duplicate = false;
            resType = null;
        }

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String jndiName = attributes.getValue("jndi-name");
            if(targetJndiName.equals(jndiName)) {
                if(duplicate) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                            "Duplicate jndi-names defined for Admin Object resources.");
                }
                duplicate = true;
                resType = attributes.getValue("res-type");
            }
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getResType() {
            return resType;
        }
    }

    private static class DuplicateConnectorFinder extends TreeParser.NodeReader {

        private final String targetJndiName;
        private boolean duplicate;
        private String poolName;

        public DuplicateConnectorFinder(String jndiName) {
            targetJndiName = jndiName;
            duplicate = false;
            poolName = null;
        }

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String jndiName = attributes.getValue("jndi-name");
            if(targetJndiName.equals(jndiName)) {
                if(duplicate) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                            "Duplicate jndi-names defined for Connector resources.");
                }
                duplicate = true;
                poolName = attributes.getValue("pool-name");
            }
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getPoolName() {
            return poolName;
        }
    }

    private static class ConnectorConnectionPoolFinder extends TreeParser.NodeReader {

        private Map<String, String> properties = null;
        private Map<String, ConnectorPool> pools = new HashMap<String, ConnectorPool>();

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            properties = new HashMap<String, String>();

            String poolName = attributes.getValue("pool-name");
            if(poolName != null && poolName.length() > 0) {
                if(!pools.containsKey(poolName)) {
                    properties.put("pool-name", poolName);
                } else {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                            "Duplicate pool-names defined for JDBC Connection Pools.");
                }
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            properties.put(attributes.getValue("name").toLowerCase(Locale.ENGLISH),
                    attributes.getValue("value"));
        }

        @Override
        public void endNode(String qname) throws SAXException {
            String poolName = properties.get("pool-name");
            ConnectorPool pool = new ConnectorPool(
                    poolName,
                    properties.get("raname")
                    );
            pools.put(poolName, pool);
        }

        public List<String> getPoolNames() {
            return new ArrayList<String>(pools.keySet());
        }

        public Map<String, ConnectorPool> getPoolData() {
            return Collections.unmodifiableMap(pools);
        }

    }

    private static class ConnectorPool {

        private final String poolName;
        private final String raname;

        public ConnectorPool(String poolName, String raname) {
            this.poolName = poolName;
            this.raname = raname;
        }

        public String getPoolName() {
            return poolName;
        }

        public String getRaName() {
            return raname;
        }
    }
}
