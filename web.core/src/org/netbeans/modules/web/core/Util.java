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

package org.netbeans.modules.web.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
//import java.io.*;
import org.xml.sax.*;
//import java.util.*;

/** Utility class
* @author  Petr Jiricka
* @version 1.00, Jun 03, 1999
*/
public class Util {

    /** Waits for startup of a server, waits until the connection has
     * been established. */ 

    public static boolean waitForURLConnection(URL url, int timeout, int retryTime) { 
        Connect connect = new Connect(url, retryTime); 
        Thread t = new Thread(connect);
        t.start();
        try {
            t.join(timeout);
        } catch(InterruptedException ie) {
        }
        if (t.isAlive()) {
            connect.finishLoop();
            t.interrupt();//for thread deadlock
        }
        return connect.getStatus();
    }

    public static String issueGetRequest(URL url) {
        BufferedReader in = null;
        StringBuffer input = new StringBuffer();
        try {
            in = new BufferedReader(new InputStreamReader(
                                        url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                input.append(inputLine);
                input.append("\n"); // NOI18N
            }  
            return input.toString();
        }
        catch (Exception e) {
	    //e.printStackTrace();
            return null;
        }
        finally {
            if (in != null)
                try {
                    in.close();
                }
                catch(IOException e) {
                    //e.printStackTrace();
                }
        }
    }

    private static class Connect implements Runnable  {

        URL url = null;
        int retryTime;
        boolean status = false;
        boolean loop = true;

        public Connect(URL url, int retryTime) {
            this.url = url;
            this.retryTime = retryTime; 
        } 

        public void finishLoop() {
            loop = false;
        }

        public void run() {
            try {
                InetAddress.getByName(url.getHost());
            } catch (UnknownHostException e) {
                return;
            }
            while (loop) {
                try {
                    Socket socket = new Socket(url.getHost(), url.getPort());
                    socket.close();
                    status = true;
                    break;
                } catch (UnknownHostException e) {//nothing to do
                } catch (IOException e) {//nothing to do
                }
                try {
                    Thread.currentThread().sleep(retryTime);
                } catch(InterruptedException ie) {
                }
            }
        }

        boolean getStatus() {
            return status;
        }
    }

    // following block is copy/pasted code from 
    // org.netbeans.modules.j2ee.common.Util
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set testGroups = getTestSourceGroups(project, sourceGroups);
        List result = new ArrayList();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return (SourceGroup[]) result.toArray(new SourceGroup[result.size()]);
    }

    private static Set/*<SourceGroup>*/ getTestSourceGroups(Project project, SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set testGroups = new HashSet();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map result;
        if (sourceGroups.length == 0) {
            result = Collections.EMPTY_MAP;
        } else {
            result = new HashMap(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }

    private static List/*<FileObject>*/ getFileObjects(URL[] urls) {
        List result = new ArrayList();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                if (Logger.getLogger("global").isLoggable(Level.INFO)) {
                    Logger.getLogger("global").log(Level.INFO, null, new IllegalStateException("No FileObject found for the following URL: " + urls[i]));
                }
            }
        }
        return result;
    }
    
    private static List/*<SourceGroup>*/ getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList();
        }
        List result = new ArrayList();
        List sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = (FileObject) sourceRoots.get(i);
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }
    
    /** Parsing to get Set of Strings that correpond to tagName valeus inside elName, e.g.:
     *  to get all <servlet-name> values inside the <servlet> elements (in web.xml)
    */    
    public static Set getTagValues (java.io.InputStream is, String elName, String tagName) throws java.io.IOException, SAXException {
        return getTagValues(is,new String[]{elName},tagName);
    }
    /** Parsing to get Set of Strings that correpond to tagName valeus inside elNames, e.g.:
     *  to get all <name> values inside the <tag> and <tag-file> elements (in TLD)
    */
    public static Set getTagValues (java.io.InputStream is, String[] elNames, String tagName) throws java.io.IOException, SAXException {
        javax.xml.parsers.SAXParserFactory fact = javax.xml.parsers.SAXParserFactory.newInstance();
        fact.setValidating(false);
        try {
            javax.xml.parsers.SAXParser parser = fact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            TLDVersionHandler handler = new TLDVersionHandler(elNames,tagName);
            reader.setContentHandler(handler);
            try {
                reader.parse(new InputSource(is));
            } catch (SAXException ex) {
                String message = ex.getMessage();
            }
            return handler.getValues();
        } catch(javax.xml.parsers.ParserConfigurationException ex) {
            return new java.util.HashSet();
        }
    }
    
    private static class TLDVersionHandler extends org.xml.sax.helpers.DefaultHandler {
        private String tagName;
        private Set elNames;
        private Set values;
        private boolean insideEl, insideTag;

        TLDVersionHandler(String[] elNames, String tagName) {
            this.elNames=new java.util.HashSet();
            for (int i=0;i<elNames.length;i++) {
                this.elNames.add(elNames[i]);
            }
            this.tagName=tagName;
            values = new HashSet();
        }
        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
            if (elNames.contains(rawName)) insideEl=true;
            else if (tagName.equals(rawName) && insideEl) { //NOI18N
                insideTag=true;
            }
        }
        public void endElement(String uri, String localName, String rawName) throws SAXException {
            if (elNames.contains(rawName)) insideEl=false;
            else if (tagName.equals(rawName) && insideEl) { //NOI18N
                insideTag=false;
            }
        }
        
        public void characters(char[] ch,int start,int length) throws SAXException {
            if (insideTag) {
                values.add(String.valueOf(ch,start,length).trim());
            }
        }
        public Set getValues() {
            return values;
        }
    }
    
}
