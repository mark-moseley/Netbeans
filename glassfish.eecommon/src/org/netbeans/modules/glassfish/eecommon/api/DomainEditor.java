// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
// </editor-fold>

package org.netbeans.modules.glassfish.eecommon.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DomainEditor {
    
    private static final String HTTP_PROXY_HOST = "-Dhttp.proxyHost=";
    private static final String HTTP_PROXY_PORT = "-Dhttp.proxyPort=";
    private static final String HTTPS_PROXY_HOST = "-Dhttps.proxyHost=";
    private static final String HTTPS_PROXY_PORT = "-Dhttps.proxyPort=";
    private static final String HTTP_PROXY_NO_HOST = "-Dhttp.nonProxyHosts=";
    
    private static String SAMPLE_DATASOURCE = "jdbc/sample"; //NOI18N
    private static String SAMPLE_CONNPOOL = "SamplePool"; //NOI18N
    
    private static String NBPROFILERNAME = "NetBeansProfiler"; //NOI18N
    
    private static String CONST_USER = "User"; // NOI18N
    private static String CONST_PASSWORD = "Password"; // NOI18N
    private static String CONST_URL = "URL"; // NOI18N
    private static String CONST_LOWER_DATABASE_NAME = "databaseName"; // NOI18N
    private static String CONST_LOWER_PORT_NUMBER = "portNumber"; // NOI18N
    private static String CONST_DATABASE_NAME = "DatabaseName"; // NOI18N
    private static String CONST_PORT_NUMBER = "PortNumber"; // NOI18N
    private static String CONST_SID = "SID"; // NOI18N
    private static String CONST_SERVER_NAME = "serverName"; // NOI18N
    private static String CONST_DRIVER_CLASS = "driverClass"; // NOI18N
    static private String CONST_NAME = "name"; // NOI18N
    static private String CONST_JVM_OPTIONS = "jvm-options"; // NOI18N
    static private String CONST_DERBY_CONN_ATTRS = "connectionAttributes"; // NOI18N
    private String dmLoc;
    private String dmName;
    private boolean isGlassfishV1OrV2;

    /**
     * Creates a new instance of DomainEditor
     * @param dm Deployment Manager of Target Server
     */
    public DomainEditor(String domainLocation, String domainName, boolean isGlassfishV1OrV2) {
        this.dmLoc = domainLocation;
        this.dmName = domainName;
        this.isGlassfishV1OrV2 = isGlassfishV1OrV2;
    }
    
    /**
     * Get the location of the server's domain.xml
     * @return String representing path to domain.xml
     */
    public String getDomainLocation(){
        String domainScriptFilePath = dmLoc+"/" + dmName + "/config/domain.xml"; //NOI18N
        return domainScriptFilePath;
    }
    
    /**
     * Get Document Object representing the domain.xml    
     * @return Document object representing given domain.xml
     */
    public Document getDomainDocument(){
        String domainLoc = getDomainLocation();
        
        // Load domain.xml
        Document domainScriptDocument = getDomainDocument(domainLoc);
        return domainScriptDocument;
    }
    
    /**
     * Get Document Object representing the domain.xml
     * @param domainLoc Location of domain.xml
     * @return Document object representing given domain.xml
     */
    public Document getDomainDocument(String domainLoc){
        // Load domain.xml
        Document domainScriptDocument = loadDomainScriptFile(domainLoc);
        return domainScriptDocument;
    }
    
    /**
     * Perform server instrumentation for profiling
     * @param domainDoc Document object representing domain.xml
     * @param nativeLibraryPath Native Library Path
     * @param jvmOptions Values for jvm-options to enable profiling
     * @return returns true if server is ready for profiling
     */
    public boolean addProfilerElements(Document domainDoc, String nativeLibraryPath, String[] jvmOptions){
        String domainPath = getDomainLocation();
        
        // Remove any previously defined 'profiler' element(s)
        removeProfiler(domainDoc);
        
        // If no 'profiler' element needs to be defined, the existing one is simply removed (by the code above)
        // (This won't happen for NetBeans Profiler, but is a valid scenario)
        // Otherwise new 'profiler' element is inserted according to provided parameters
        if (nativeLibraryPath != null || jvmOptions != null) {
            
            // Create "profiler" element
            Element profilerElement = domainDoc.createElement("profiler");//NOI18N
            profilerElement.setAttribute("enabled", "true");//NOI18N
            profilerElement.setAttribute(CONST_NAME, NBPROFILERNAME);//NOI18N
            if (nativeLibraryPath != null) {
                profilerElement.setAttribute("native-library-path", nativeLibraryPath);//NOI18N
            }
            
            // Create "jvm-options" element
            if (jvmOptions != null) {
                for (int i = 0; i < jvmOptions.length; i++) {
                    Element jvmOptionsElement = domainDoc.createElement(CONST_JVM_OPTIONS);
                    Text tt = domainDoc.createTextNode(formatJvmOption(jvmOptions[i]));
                    jvmOptionsElement.appendChild(tt);
                    profilerElement.appendChild(jvmOptionsElement);
                }
            }
            
            // Find the "java-config" element
            NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
            if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
                System.err.println("ConfigFilesUtils: cannot find 'java-config' section in domain config file " + domainPath);
                return false;
            }
            
            // Insert the "profiler" element as a first child of "java-config" element
            Node javaConfigNode = javaConfigNodeList.item(0);
            if (javaConfigNode.getFirstChild() != null) 
                javaConfigNode.insertBefore(profilerElement, javaConfigNode.getFirstChild());
            else 
                javaConfigNode.appendChild(profilerElement);
            
        }
        // Save domain.xml
        return saveDomainScriptFile(domainDoc, domainPath);
    }
    
    /**
     * Remove server instrumentation to disable profiling
     * @param domainDoc Document object representing domain.xml
     * @return true if profiling support has been removed
     */
    public boolean removeProfilerElements(Document domainDoc){
        boolean eleRemoved = removeProfiler(domainDoc);
        if(eleRemoved){
            // Save domain.xml
            return saveDomainScriptFile(domainDoc, getDomainLocation());
        }else{
            //no need to save.
            return true;
        }    
    }
    
    private boolean removeProfiler(Document domainDoc){
        // Remove any previously defined 'profiler' element(s)
        NodeList profilerElementNodeList = domainDoc.getElementsByTagName("profiler");//NOI18N
        if (profilerElementNodeList != null && profilerElementNodeList.getLength() > 0){
            Vector<Node> nodes = new Vector<Node>(); //temp storage for the nodes to delete
            //we only want to delete the NBPROFILERNAME nodes.
            // otherwise, see bug # 77026
            for (int i = 0; i < profilerElementNodeList.getLength(); i++) {
                Node n= profilerElementNodeList.item(i);                
                Node a= n.getAttributes().getNamedItem(CONST_NAME);//NOI18N
                if ((a!=null)&&(a.getNodeValue().equals(NBPROFILERNAME))){//NOI18N
                    nodes.add(n);
                }                              
            }
            for(int i=0; i<nodes.size(); i++){
                Node nd = nodes.get(i);
                nd.getParentNode().removeChild(nd);
            }
            return true;
        }
            
        return false;
    }
       
    public String[] getHttpProxyOptions(){
        ArrayList<String> httpProxyOptions = new ArrayList<String>();
        Document domainDoc = getDomainDocument();
        NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
        if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
            return httpProxyOptions.toArray(new String[httpProxyOptions.size()]);
        }
        
        NodeList jvmOptionNodeList = domainDoc.getElementsByTagName(CONST_JVM_OPTIONS);
        for(int i=0; i<jvmOptionNodeList.getLength(); i++){
            Node nd = jvmOptionNodeList.item(i);
            if(nd.hasChildNodes())  {
                Node childNode = nd.getFirstChild();
                String childValue = childNode.getNodeValue();
                if(childValue.indexOf(HTTP_PROXY_HOST) != -1
                        || childValue.indexOf(HTTP_PROXY_PORT) != -1
                        || childValue.indexOf(HTTPS_PROXY_HOST) != -1
                        || childValue.indexOf(HTTPS_PROXY_PORT) != -1
                        || childValue.indexOf(HTTP_PROXY_NO_HOST) != -1){
                    httpProxyOptions.add(childValue);
                }
            }
        }

        String[] opts = new String[httpProxyOptions.size()];
        return httpProxyOptions.toArray(opts);
        
    }
    
    public boolean setHttpProxyOptions(String[] httpProxyOptions){
        Document domainDoc = getDomainDocument();
        NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
        if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
            return false;
        }
        
        //Iterates through the existing proxy attributes and deletes them
        removeProxyOptions(domainDoc, javaConfigNodeList.item(0));
                
        //Add new set of proxy options
        for(int j=0; j<httpProxyOptions.length; j++){
            String option = httpProxyOptions[j];
            Element jvmOptionsElement = domainDoc.createElement(CONST_JVM_OPTIONS);
            Text proxyOption = domainDoc.createTextNode(option);
            jvmOptionsElement.appendChild(proxyOption);
            javaConfigNodeList.item(0).appendChild(jvmOptionsElement);
        }
        
        return saveDomainScriptFile(domainDoc, getDomainLocation(), false);
    }
      
    private boolean removeProxyOptions(Document domainDoc, Node javaConfigNode){
        NodeList jvmOptionNodeList = domainDoc.getElementsByTagName(CONST_JVM_OPTIONS);
        
        Vector<Node> nodes = new Vector<Node>();
        for(int i=0; i<jvmOptionNodeList.getLength(); i++){
            Node nd = jvmOptionNodeList.item(i);
            if(nd.hasChildNodes())  {
                Node childNode = nd.getFirstChild();
                String childValue = childNode.getNodeValue();
                if(childValue.indexOf(HTTP_PROXY_HOST) != -1
                        || childValue.indexOf(HTTP_PROXY_PORT) != -1
                        || childValue.indexOf(HTTPS_PROXY_HOST) != -1
                        || childValue.indexOf(HTTPS_PROXY_PORT) != -1
                        || childValue.indexOf(HTTP_PROXY_NO_HOST) != -1){
                   nodes.add(nd);
                }
            }
        }
        for(int i=0; i<nodes.size(); i++){
            javaConfigNode.removeChild(nodes.get(i));
        }
        
        return saveDomainScriptFile(domainDoc, getDomainLocation(), false);
    }
    
    /*
     * Creates Document instance from domain.xml
     * @param domainScriptFilePath Path to domain.xml
     */
    private Document loadDomainScriptFile(String domainScriptFilePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            dBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
                    InputSource source = new InputSource(reader);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            });
            
            return dBuilder.parse(new File(domainScriptFilePath));
        } catch (Exception e) {
            System.err.println("ConfigFilesUtils: unable to parse domain config file " + domainScriptFilePath);
            return null;
        }
    }
    
    private boolean saveDomainScriptFile(Document domainScriptDocument, String domainScriptFilePath) {
        return saveDomainScriptFile(domainScriptDocument, domainScriptFilePath, true);
    }
    /*
     * Saves Document instance to domain.xml
     * @param domainScriptDocument Document representing the xml
     * @param domainScriptFilePath Path to domain.xml
     */
    private boolean saveDomainScriptFile(Document domainScriptDocument, String domainScriptFilePath, boolean indent) {
        boolean result = false;
        FileWriter domainScriptFileWriter = null;
        try {
            domainScriptFileWriter = new FileWriter(domainScriptFilePath);
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                if(indent) {
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                }
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, domainScriptDocument.getDoctype().getPublicId());
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, domainScriptDocument.getDoctype().getSystemId());
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                
                DOMSource domSource = new DOMSource(domainScriptDocument);
                StreamResult streamResult = new StreamResult(domainScriptFileWriter);
                
                transformer.transform(domSource, streamResult);
                result = true;
            } catch (Exception e) {
                System.err.println("ConfigFilesUtils: Unable to save domain config file " + domainScriptFilePath);
                result = false;
            }
        } catch (IOException ioex) {
            System.err.println("ConfigFilesUtils: cannot create output stream for domain config file " + domainScriptFilePath);
            result = false;
        } finally {
            try { 
                if (domainScriptFileWriter != null)  {
                    domainScriptFileWriter.close(); 
                }
            } catch (IOException ioex2) {
                System.err.println("SunAS8IntegrationProvider: cannot close output stream for " + domainScriptFilePath); 
            };
        }
        
        return result;
    }
    
    // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
    // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140 (AS 8.1 and AS 8.2)
    // or to  "-agentpath:C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\",5140" (GlassFish or AS 9.0)
    private String formatJvmOption(String jvmOption) {
        // only jvmOption containing \" needs to be formatted
        if (jvmOption.indexOf("\"") != -1) { // NOI18N
            // special handling for -agentpath
            if (jvmOption.indexOf("\\\"") != -1 && jvmOption.indexOf("-agentpath") != -1 ){ // NOI18N
            // Modification for AS 8.1, 8.2, initial modification for AS 9.0, GlassFish
            // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
            // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
            String modifiedOption = jvmOption.replaceAll("\\\\\"", "#"); // replace every \" by #
            modifiedOption = modifiedOption.replaceAll("\\\"", ""); // delete all "
            modifiedOption = modifiedOption.replaceAll("#", "\""); // replace every # by "

            // Modification for AS 9.0, GlassFish should be done only if native launcher isn't used,
            // otherwise will cause server startup failure. It seems that currently native launcher is used
            // for starting the servers from the IDE.
   //         boolean usingNativeLauncher = false;
            String osType=System.getProperty("os.name");//NOI18N
            if ((osType.startsWith("Mac OS"))||isGlassfishV1OrV2){//no native for mac of glassfish
  //          if (!usingNativeLauncher) {

                // Modification for AS 9.0, GlassFish
                // Converts -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
                // "-agentpath:C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\",5140"

                    modifiedOption = "\"" + modifiedOption.replaceAll("\\\"", "\\\\\"") + "\"";

            }

            // return correctly formatted jvmOption
            return modifiedOption;
            } else {
                return jvmOption.replace('"', ' ');
        }
        }
        // return original jvmOption
        return jvmOption;
     }
    
    static final String[] sysDatasources = {"jdbc/__TimerPool", "jdbc/__CallFlowPool"}; //NOI18N
    
    
            
    public Map<String,Map> getSunDatasourcesFromXml(){
        Map<String,Map> dSources = new HashMap<String,Map>();
        Document domainDoc = getDomainDocument();
        Map<String,NamedNodeMap> dsMap = getDataSourcesAttrMap(domainDoc);
        Map<String,Node> cpMap = getConnPoolsNodeMap(domainDoc);
        dsMap.keySet().removeAll(Arrays.asList(sysDatasources));    
        String[] ds = dsMap.keySet().toArray(new String[dsMap.size()]);
        
        for(int i=0; i<ds.length; i++){
            String jndiName = ds[i];
            Map<String,String> pValues = new HashMap<String,String>();
            NamedNodeMap dsAttrMap = dsMap.get(jndiName);
            String poolName = dsAttrMap.getNamedItem("pool-name").getNodeValue();
            
            //Get the Connection Pool used by this jdbc-resource
            Node cpNode = cpMap.get(poolName);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            String dsClassName = cpAttrMap.getNamedItem("datasource-classname").getNodeValue();
            String resType = cpAttrMap.getNamedItem("res-type").getNodeValue();
            
            //Get property values
            Element cpElement = (Element) cpNode;
            NodeList propsNodeList = cpElement.getElementsByTagName("property");
                        
            //Cycle through each property element
            Map<String,String> map = new HashMap<String,String>();
            for(int j=0; j<propsNodeList.getLength(); j++){
                Node propNode = propsNodeList.item(j);
                NamedNodeMap propsMap = propNode.getAttributes();
                
                for(int m=0; m<propsMap.getLength(); m++){
                    String mkey = propsMap.getNamedItem(CONST_NAME).getNodeValue();
                    String mkeyValue = propsMap.getNamedItem("value").getNodeValue();
                    if(mkey.equalsIgnoreCase(CONST_USER)){
                        pValues.put(CONST_USER, mkeyValue);
                    }else if (mkey.equalsIgnoreCase(CONST_PASSWORD)){
                        pValues.put(CONST_PASSWORD, mkeyValue);
                    }else if (mkey.equalsIgnoreCase(CONST_URL)){
                        pValues.put(CONST_URL, mkeyValue);
                    }else if (mkey.equalsIgnoreCase(CONST_SERVER_NAME)){
                        pValues.put(CONST_SERVER_NAME, mkeyValue);
                    }else {
                        map.put(mkey, mkeyValue);
                    }
                }
            } // connection-pool properties

            pValues.put(CONST_LOWER_DATABASE_NAME, map.get(CONST_LOWER_DATABASE_NAME));
            pValues.put(CONST_PORT_NUMBER, map.get(CONST_PORT_NUMBER));
            pValues.put(CONST_LOWER_PORT_NUMBER, map.get(CONST_LOWER_PORT_NUMBER));
            pValues.put(CONST_DATABASE_NAME, map.get(CONST_DATABASE_NAME));
            pValues.put(CONST_SID, map.get(CONST_SID));
            pValues.put(CONST_DRIVER_CLASS, map.get(CONST_DRIVER_CLASS));
            pValues.put(CONST_DERBY_CONN_ATTRS, map.get(CONST_DERBY_CONN_ATTRS));
            pValues.put("dsClassName", dsClassName);
            pValues.put("resType", resType);
            
            dSources.put(jndiName, pValues);
        } // for each jdbc-resource
        return dSources;
    }
    
    public Map<String,Map> getConnPoolsFromXml(){
        Map<String,Map> pools = new HashMap<String,Map>();
        Document domainDoc = getDomainDocument();
        Map<String,Node> cpMap = getConnPoolsNodeMap(domainDoc);
        
        String[] cp = cpMap.keySet().toArray(new String[cpMap.size()]);
        for(int i=0; i<cp.length; i++){
            String name = cp[i];
            Map<String,String> pValues = new HashMap<String,String>();
            Node cpNode = cpMap.get(name);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            String dsClassName = cpAttrMap.getNamedItem("datasource-classname").getNodeValue();
            String resType = cpAttrMap.getNamedItem("res-type").getNodeValue();
            
            //Get property values
            Element cpElement = (Element) cpNode;
            NodeList propsNodeList = cpElement.getElementsByTagName("property");
                        
            //Cycle through each property element
            Map<String,String> map = new HashMap<String,String>();
            for(int j=0; j<propsNodeList.getLength(); j++){
                Node propNode = propsNodeList.item(j);
                NamedNodeMap propsMap = propNode.getAttributes();
                
                for(int m=0; m<propsMap.getLength(); m++){
                    String mkey = propsMap.getNamedItem(CONST_NAME).getNodeValue();
                    String mkeyValue = propsMap.getNamedItem("value").getNodeValue();
                    if(mkey.equalsIgnoreCase(CONST_USER)){
                        pValues.put(CONST_USER, mkeyValue);
                    }else if (mkey.equalsIgnoreCase(CONST_PASSWORD)){
                        pValues.put(CONST_PASSWORD, mkeyValue);
                    }else if (mkey.equalsIgnoreCase(CONST_URL)){
                        pValues.put(CONST_URL, mkeyValue);
                    }else if (mkey.equalsIgnoreCase(CONST_SERVER_NAME)){
                        pValues.put(CONST_SERVER_NAME, mkeyValue);
                    }else {
                        map.put(mkey, mkeyValue);
                    }
                }
            } // connection-pool properties
            
            pValues.put(CONST_LOWER_DATABASE_NAME, map.get(CONST_LOWER_DATABASE_NAME));
            pValues.put(CONST_PORT_NUMBER, map.get(CONST_PORT_NUMBER));
            pValues.put(CONST_LOWER_PORT_NUMBER, map.get(CONST_LOWER_PORT_NUMBER));
            pValues.put(CONST_DATABASE_NAME, map.get(CONST_DATABASE_NAME));
            pValues.put(CONST_SID, map.get(CONST_SID));
            pValues.put(CONST_DRIVER_CLASS, map.get(CONST_DRIVER_CLASS));
            pValues.put(CONST_DERBY_CONN_ATTRS, map.get(CONST_DERBY_CONN_ATTRS));
            pValues.put("dsClassName", dsClassName);
            pValues.put("resType", resType);
            
            pools.put(name, pValues);
        }
      
        return pools;
    }
    
    public void createSampleDatasource(){
        Document domainDoc = getDomainDocument();
        updateWithSampleDataSource(domainDoc);
    }
    
    private Map<String,NamedNodeMap> getDataSourcesAttrMap(Document domainDoc){
        Map<String,NamedNodeMap> dataSourceMap = new HashMap<String,NamedNodeMap>();
        updateWithSampleDataSource(domainDoc);
        NodeList dataSourceNodeList = domainDoc.getElementsByTagName("jdbc-resource");
        for(int i=0; i<dataSourceNodeList.getLength(); i++){
            Node dsNode = dataSourceNodeList.item(i);
            NamedNodeMap dsAttrMap = dsNode.getAttributes();
            String jndiName = dsAttrMap.getNamedItem("jndi-name").getNodeValue();
            dataSourceMap.put(jndiName, dsAttrMap);
        }    
        return dataSourceMap;
    }
    
    private boolean updateWithSampleDataSource(Document domainDoc){
        boolean sampleExists = false;
        NodeList dataSourceNodeList = domainDoc.getElementsByTagName("jdbc-resource");
        for(int i=0; i<dataSourceNodeList.getLength(); i++){
            Node dsNode = dataSourceNodeList.item(i);
            NamedNodeMap dsAttrMap = dsNode.getAttributes();
            String jndiName = dsAttrMap.getNamedItem("jndi-name").getNodeValue();
            if(jndiName.equals(SAMPLE_DATASOURCE)) {
                sampleExists = true;
            }
        }
        if(!sampleExists) {
            return createSampleDatasource(domainDoc);
        }
        return true;
    }
    
    private Map<String,Node> getConnPoolsNodeMap(Document domainDoc){
        Map<String,Node> connPoolMap = new HashMap<String,Node>();
        NodeList connPoolNodeList = domainDoc.getElementsByTagName("jdbc-connection-pool");
        for(int i=0; i<connPoolNodeList.getLength(); i++){
            Node cpNode = connPoolNodeList.item(i);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            String cpName = cpAttrMap.getNamedItem(CONST_NAME).getNodeValue();
            connPoolMap.put(cpName, cpNode);
        }    
        return connPoolMap;
    }
        
    public boolean createSampleDatasource(Document domainDoc){
        NodeList resourcesNodeList = domainDoc.getElementsByTagName("resources");
        NodeList serverNodeList = domainDoc.getElementsByTagName("server");
        if (resourcesNodeList == null || resourcesNodeList.getLength() == 0 || 
                serverNodeList == null || serverNodeList.getLength() == 0) {
            return true;
        }
        Node resourcesNode = resourcesNodeList.item(0);
        
        Map<String,Node> cpMap = getConnPoolsNodeMap(domainDoc);
        if(! cpMap.containsKey(SAMPLE_CONNPOOL)){
            Node oldNode = cpMap.get("DerbyPool");
            Node cpNode = oldNode.cloneNode(false);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            cpAttrMap.getNamedItem(CONST_NAME).setNodeValue(SAMPLE_CONNPOOL);
            Map<String,String> poolProps = new HashMap<String,String>();
            poolProps.put(CONST_SERVER_NAME, "localhost");
            poolProps.put(CONST_PASSWORD, "app");
            poolProps.put(CONST_USER, "app");
            poolProps.put(CONST_DATABASE_NAME, "sample");
            poolProps.put(CONST_PORT_NUMBER, "1527");
            poolProps.put(CONST_URL, "jdbc:derby://localhost:1527/sample");
            
            Object[] propNames = poolProps.keySet().toArray();
            for(int i=0; i<propNames.length; i++){
                String keyName = (String)propNames[i];
                Element propElement = domainDoc.createElement("property");
                propElement.setAttribute(CONST_NAME, keyName);
                propElement.setAttribute("value", poolProps.get(keyName));
                cpNode.appendChild(propElement);
            }
            resourcesNode.appendChild(cpNode);
        }
                
        Element dsElement = domainDoc.createElement("jdbc-resource");
        dsElement.setAttribute("jndi-name", SAMPLE_DATASOURCE);
        dsElement.setAttribute("pool-name", SAMPLE_CONNPOOL);
        dsElement.setAttribute("object-type", "user");
        dsElement.setAttribute("enabled", "true");
        
        // Insert the ds __Sample as a first child of "resources" element
        if (resourcesNode.getFirstChild() != null)
            resourcesNode.insertBefore(dsElement, resourcesNode.getFirstChild());
        else
            resourcesNode.appendChild(dsElement);
        
        //<resource-ref enabled="true" ref="jdbc/__default"/>
        Element dsResRefElement = domainDoc.createElement("resource-ref");
        dsResRefElement.setAttribute("ref", SAMPLE_DATASOURCE);
        dsResRefElement.setAttribute("enabled", "true");
        // Insert the ds reference __Sample as last child of "server" element
        Node serverNode = serverNodeList.item(0);
        if (serverNode.getLastChild() != null)
            serverNode.insertBefore(dsResRefElement, serverNode.getLastChild());
        else
            serverNode.appendChild(dsResRefElement);
        
        return saveDomainScriptFile(domainDoc, getDomainLocation());
    }
       
    public Map<String,String> getAdminObjectResourcesFromXml(){
        Map<String,String> aoResources = new HashMap<String,String>();
        Document domainDoc = getDomainDocument();
        NodeList adminObjectNodeList = domainDoc.getElementsByTagName("admin-object-resource");
        for(int i=0; i<adminObjectNodeList.getLength(); i++){
            Node aoNode = adminObjectNodeList.item(i);
            NamedNodeMap aoAttrMap = aoNode.getAttributes();
            String jndiName = aoAttrMap.getNamedItem("jndi-name").getNodeValue();
            String type = aoAttrMap.getNamedItem("res-type").getNodeValue();
            aoResources.put(jndiName, type);
        }    
        return aoResources;
    }    
}
