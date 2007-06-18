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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.projects.jbi.CasaConstants;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Connection;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Endpoint;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.PtConnection;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jqian
 */
public class ConnectionResolver implements CasaConstants {
    
    private boolean saInternalRouting;
    private boolean showLog; 
    private Task task;  

    private Map<String, String> namespaceMap = new HashMap<String, String>();
    
    private List<Connection> connectionList = new ArrayList<Connection>();
        
    // mapping binding component name to a list of two lists
    Map<String, List<Connection>[]> bcConnections =
            new HashMap<String, List<Connection>[]>();
    
    
    public ConnectionResolver(Task task, boolean showLog, boolean saInternalRouting) {
        this.task = task;
        this.showLog = showLog;
        this.saInternalRouting = saInternalRouting;
    }
        
    /**
     * Gets the list of connections that will be written to the connections.xml
     * and SA jbi.xml.
     *
     * @return Returns the connection list.
     */
    public List<Connection> getConnectionList() {
        return connectionList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the namespaceMap.
     */
    public Map<String, String> getNamespaceMap() {
        return namespaceMap;
    }
    
    public Set<String> getBCNames() {
        return bcConnections.keySet();
    }
    
    public Map<String, List<Connection>[]> getBCConnections() {
        return bcConnections;
    }
    
    /**
     * 
     * @param repo 
     * @param oldCasaDocument 
     */
    public void resolveConnections(wsdlRepository repo,
            Document oldCasaDocument) {
        
        // mapping portType QName to ptConnection
        Map<String, PtConnection> ptConnectionMap = repo.getConnections(); 
               
        // loop thru the Pt connections
        for (String pt : ptConnectionMap.keySet()) {
            PtConnection ptConnection = ptConnectionMap.get(pt);
            if (showLog) {
                log(ptConnection.dump());
            }
            
            // check the number of ports
            List<Port> ports = ptConnection.getPorts();
            int numPorts = ports.size();
            if (numPorts == 1) { // OK, only 1 external port...
                Port p = ports.get(0);
                List<Endpoint> providers = ptConnection.getProvides();
                
                // check the number of providers
                int numProviders = providers.size();
                if (numProviders > 1) { // report error... more than 1 providers
                    log("***WARNING: 1 port with " + numProviders + " providers [" + pt + "]");
                    // todo: OK, we will just add the first one in...
                }
                
                // Is there a binding for the port
                String bcName = repo.getBindingComponentName(p);
                QName ptQName = p.getBinding().get().getType().getQName();
                if (bcName == null) { // report error.. no binding
                    log("***WARNING: PORT w/o address: " + ptQName);
                } else {                    
                    // Create the endpoint for port
                    Service sv = (Service) p.getParent();
                    String tns = p.getModel().getDefinitions().getTargetNamespace();
                    Endpoint port = new Endpoint(p.getName(), new QName(tns, sv.getName()), ptQName);
                    
                    // create a connection port -> provider
                    Endpoint provide = null;
                    if (numProviders > 0) {
                        // 03/08/06 use the first one only...
                        provide = providers.get(0);
                        Connection con = new Connection(port, provide);
                        addConnection(con, true, bcName);
                        if (numProviders > 1) {
                            dumpEndpoints(providers, "Provider");
                        }
                    }
                    // loop thru consumers
                    List<Endpoint> consumers = ptConnection.getConsumes();
                    for (Endpoint consume : consumers) {
                        // create a connection consumer -> port
                        // todo: 03/23/06.. replace with consumer -> provider
                        // instead
                        if ((provide != null) && saInternalRouting) {
                            // create a direct connection consumer -> provide
                            Connection con = new Connection(consume, provide);
                            addConnection(con);
                        } else {
                            Connection con = new Connection(consume, port);
                            addConnection(con, false, bcName);
                        }
                    }
                }
                
            } else if (numPorts == 0) { // no external port...
                // report unused wsdl port, or internal connection
                log("***WARNING: there is no WSDL Port implementing PortType [" + pt + "]");
                
                // resolve internal connections...
                List<Endpoint> consumers = ptConnection.getConsumes();
                List<Endpoint> providers = ptConnection.getProvides();
                int numProviders = providers.size();
                int numConsumers = consumers.size();
                if (numConsumers < 1 || numProviders < 1) {
                    // no connection needed...                    
                    // todo: 09/05/06 Create default binding/serivce/port for sole provider...
                    // todo: 1. add port w/default binding/serverice
                    // todo: 2. add connection port -> provider                    
                } else if (numProviders == 1) { // 1 provider, 1 or more consumer
                    Endpoint provider = providers.get(0);
                    for (Endpoint consumer : consumers) {
                        Connection c = new Connection(consumer, provider);
                        addConnection(c);
                    }
                } else if (numProviders > 1) {
                    // report error... more than 1 providers
                    log("***WARNING: 0 port, " +
                            numConsumers + " consumers, " +
                            numProviders + " providers. [" + pt + "]");
                    dumpPorts(ports);
                    dumpEndpoints(consumers, "Consumer");
                    dumpEndpoints(providers, "Provider");
                }
                
            } else { // more than 1 external port
                // OK, if multiple ports, but only 1 provider 0 consumer...
                // Q: Why do we have to have 0 consumer? If we can connect multiple
                // ports to the sole provider, why can't we connect multiple
                // consumers to the sole provider at the same time?
                
                List<Endpoint> consumers = ptConnection.getConsumes();
                List<Endpoint> providers = ptConnection.getProvides();
                int numProviders = providers.size();
                int numConsumers = consumers.size();
                if (numProviders > 1) {
                    // report error... more than 1 providers
                    log("***WARNING: " + numPorts + " ports and " + numProviders
                            + " providers [" + pt + "]");
                    dumpPorts(ports);
                    dumpEndpoints(providers, "Provider");
                } else if (numConsumers > 0) {
                    // report error... more than 1 consumers
                    log("***WARNING: 1 or more consumers with " + numPorts
                            + " ports [" + pt + "]");
                    dumpPorts(ports);
                    dumpEndpoints(consumers, "Consumer");
                } else if (numProviders == 1) {
                    Endpoint provide = providers.get(0);
                    for (Port p : ports) {
                        String bcName = repo.getBindingComponentName(p);
                        QName ptQName = p.getBinding().get().getType().getQName();
                        if (bcName == null) {
                            log("***Warning: PORT w/o address: " + ptQName);
                        } else {                            
                            Service sv = (Service) p.getParent();
                            String tns = ((Definitions) sv.getParent()).getTargetNamespace();
                            Endpoint port = new Endpoint(p.getName(),
                                    new QName(tns, sv.getName()),
                                    ptQName);
                            Connection con = new Connection(port, provide);
                            addConnection(con, true, bcName);
                        }
                    }
                }
            }
        }
        
        if (showLog) {
            log("\n-----------------------------------\n");
//            log(cc.dump());
        }
        
        mergeCasaConnections(oldCasaDocument);
    }
    
    private boolean isInConnectionList(List<Connection> connectionList, Connection connection) {
        for (Connection con : connectionList) {
            if (con.equals(connection)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void removeConnectionFromList(List<Connection> connectionList, Connection connection) {
        for (Connection con : connectionList) {
            if (con.equals(connection)) {
                connectionList.remove(con);
                break;
            }
        }
    }    
    
    private void removeConnection(Connection connection) {       
        removeConnectionFromList(connectionList, connection);        
    }
    
    private void removeConnection(Connection connection, 
            boolean isConsumeAPort, String bcName) {
        
        removeConnection(connection);
        
        List<Connection>[] clist = bcConnections.get(bcName);
        assert clist != null && clist[0] != null && clist[1] != null;
                
        if (isConsumeAPort) {
            clist[0].remove(connection);
        } else {
            clist[1].remove(connection);
        }
        
        // clear binding component that no longer has any connections
        if (clist[0].size() == 0 && clist[1].size() == 0) {
            bcConnections.remove(bcName);
        }        
    }
    
    private void addConnection(Connection connection) {
        if (!isInConnectionList(connectionList, connection)) {
            connectionList.add(connection);
        }
    }
    
    private void addConnection(Connection connection, 
            boolean isConsumeAPort, String bcName) {
        
        addConnection(connection);
        
        List<Connection>[] cmap = bcConnections.get(bcName);
        
        List<Connection>[] clist;
        if (cmap != null) {
            clist = cmap;
        } else {
            clist = new ArrayList[2];
            clist[0] = new ArrayList<Connection>();
            clist[1] = new ArrayList<Connection>();
            bcConnections.put(bcName, clist);
        }
        
        if (isConsumeAPort) {
            if (!isInConnectionList(clist[0], connection)) {
                Endpoint consume = connection.getConsume();
                for (Connection con : clist[0]) {
                    if (con.getConsume().equals(consume)) {
                        log("INFO: A Consume endpoint (" + consume +
                                ") is only allowed to connect to at most 1 provider. " +
                                ". Old connection (" + con +
                                ") is replaced by the new one (" + connection + ").");
                        clist[0].remove(con);
//                        connectionList.remove(con);
                        removeConnectionFromList(connectionList, con); // FIXME
                        break;
                    }
                }
                
                clist[0].add(connection);
            }
        } else {
            if (!isInConnectionList(clist[1], connection)) {
                clist[1].add(connection);
            }
        }
    }
        
    private void mergeCasaConnections(final Document oldCasaDocument) {
                
        if (oldCasaDocument == null) {
            return;
        }
        
        try {
            NodeList oldConnectionList = oldCasaDocument.getElementsByTagName(
                    CASA_CONNECTION_ELEM_NAME);
            Element sus = (Element) oldCasaDocument.getElementsByTagName(
                    CASA_SERVICE_UNITS_ELEM_NAME).item(0);
            
            if (sus == null) {
                log("WARNING: Old version of casa format is not supported.");
                return;
            }
            
            NodeList bcsuNodeList = sus.getElementsByTagName(
                    CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
            
            // mapping endpoint ID to bc name
            Map<String, String> endpointID2BCName = hashBcNames(bcsuNodeList);
            
            for (int i = oldConnectionList.getLength() - 1; i >= 0; i--) {
                Element oldConnection = (Element) oldConnectionList.item(i);
                String oldConnectionState = oldConnection.getAttribute(CASA_STATE_ATTR_NAME);
                
                // we are only interested in user-deleted and user-created connections
                if (!oldConnectionState.equals(CASA_DELETED_ATTR_VALUE) &&
                        !oldConnectionState.equals(CASA_NEW_ATTR_VALUE)) {
                    continue;
                }
                
                String cID = oldConnection.getAttribute(CASA_CONSUMER_ATTR_NAME);
                String pID = oldConnection.getAttribute(CASA_PROVIDER_ATTR_NAME);
                Endpoint c = CasaBuilder.getEndpoint(oldCasaDocument, cID);
                Endpoint p = CasaBuilder.getEndpoint(oldCasaDocument, pID); 
                
                // We could check to make sure the two endpoints of the 
                // connection are still valid here, but that is not really necessary.
                
                if (oldConnectionState.equals(CASA_DELETED_ATTR_VALUE)) {
                    // Remove user-deleted connections.
                    Connection con = new Connection(c, p);
                    
                    // update bc's connection list..
                    String cBcName = endpointID2BCName.get(cID);
                    if (cBcName != null) {
                        removeConnection(con, true, cBcName);
                    }
                    String pBcName = endpointID2BCName.get(pID);
                    if (pBcName != null) {
                        removeConnection(con, false, pBcName);
                    }
                    
                    if (cBcName == null && pBcName == null) {
                        removeConnection(con);
                    }
                } else if (oldConnectionState.equals(CASA_NEW_ATTR_VALUE)) {
                    // Add user-created connections.
                    // (Note that it's possible that user-created connection
                    // have already been auto-generated during compapp rebuild.)
                    Connection con = new Connection(c, p);
                    
                    // update bc's connection list..
                    String cBcName = endpointID2BCName.get(cID);
                    if (cBcName != null) {
                        addConnection(con, true, cBcName);
                    }
                    String pBcName = endpointID2BCName.get(pID);
                    if (pBcName != null) {
                        addConnection(con, false, pBcName);
                    }
                    
                    if (cBcName == null && pBcName == null) {
                        addConnection(con);
                    }
                }
            }
                        
        } catch (Exception e) {
            log("ERROR: Problem merging new/deleted connections from old casa: " + e);
        }
    }
        
    /**
     * Maps endpoint ID to binding component name.
     */
    private static Map<String, String> hashBcNames(NodeList bcsuList) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < bcsuList.getLength(); i++) {
            Element bcSU = (Element) bcsuList.item(i);
            String bcName = bcSU.getAttribute(CASA_UNIT_NAME_ATTR_NAME);
            NodeList cNodeList = bcSU.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
            for (int j = 0; j < cNodeList.getLength(); j++) {
                Element c = (Element) cNodeList.item(j);
                String endpointID = c.getAttribute(CASA_ENDPOINT_ELEM_NAME);
                map.put(endpointID, bcName);
            }
            NodeList pNodeList = bcSU.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
            for (int j = 0; j < pNodeList.getLength(); j++) {
                Element p = (Element) pNodeList.item(j);
                String endpointID = p.getAttribute(CASA_ENDPOINT_ELEM_NAME);
                map.put(endpointID, bcName);
            }
        }
        return map;
    }
        
    private void dumpEndpoints(List<Endpoint> endpoints, String type) {
        for (int k = 0; k < endpoints.size(); k++) {
            Endpoint e = endpoints.get(k);
            log("\t" + type + "[" + k + "]: " + e.getFullyQualifiedName());
        }
    }
    
    private void dumpPorts(List<Port> ports) {
        for (int k = 0; k < ports.size(); k++) {
            Port p = ports.get(k);
            log("\tPort[" + k + "]: " + p.getName() + ", " + p.getBinding().getQName());
        }
    }
    
    public void dumpConnections() {
        log("Connection List : " + connectionList.size() + " connections");  
        for (Connection con : connectionList) {
            Endpoint c = con.getConsume();
            Endpoint p = con.getProvide();
            log("\t" + c.getFullyQualifiedName() + " -> " + p.getFullyQualifiedName());
        }
    }
        
    private void log(String msg) {
        task.log(msg);
    }
    
}
