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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
import static org.netbeans.modules.hudson.constants.HudsonJobBuildConstants.*;
import static org.netbeans.modules.hudson.constants.HudsonJobConstants.*;
import static org.netbeans.modules.hudson.constants.HudsonXmlApiConstants.*;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Hudson Server Connector
 *
 * @author Michal Mocnak
 */
public class HudsonConnector {
    private static final Logger LOG = Logger.getLogger(HudsonConnector.class.getName());
    
    private HudsonInstanceImpl instance;
    
    private HudsonVersion version;
    private boolean connected = false;
    
    private Map<String, HudsonView> cache = new HashMap<String, HudsonView>();
    
    /**
     * Creates a new instance of HudsonConnector
     *
     * @param HudsonInstance
     */
    public HudsonConnector(HudsonInstanceImpl instance) {
        this.instance = instance;
    }
    
    public synchronized Collection<HudsonJob> getAllJobs() {
        Document docInstance = getDocument(instance.getUrl() + XML_API_URL);
        
        if (null == docInstance)
            return new ArrayList<HudsonJob>();
        
        // Clear cache
        cache.clear();
        
        // Get views and jobs
        NodeList views = docInstance.getElementsByTagName(XML_API_VIEW_ELEMENT);
        NodeList jobs = docInstance.getElementsByTagName(XML_API_JOB_ELEMENT);
        
        // Parse views and set them into instance
        Collection<HudsonView> cViews = getViews(views);
        
        if (null == cViews)
            cViews = new ArrayList<HudsonView>();
        
        instance.setViews(cViews);
        
        // Parse jobs and return them
        Collection<HudsonJob> cJobs = getJobs(jobs);
        
        if (null == cJobs)
            cJobs = new ArrayList<HudsonJob>();
        
        return cJobs;
    }
    
    public synchronized boolean startJob(HudsonJob job) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(HudsonInstanceImpl.class, "MSG_Starting", job.getName()));
        
        // Start progress
        handle.start();
        
        try {
            final URL url = new URL(job.getUrl() + XML_API_BUILD_URL);
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        // Start job
                        HttpURLConnection conn = followRedirects(url.openConnection());
                        
                        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            LOG.warning("Cannot start build; HTTP error from " + url + ": " + conn.getResponseMessage());
                        }
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    } finally {
                        // Stop progress
                        handle.finish();
                    }
                }
            });
            
            return true;
        } catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        return false;
    }

    /**
     * Workaround for JDK bug #6810084.
     * @see HttpURLConnection#setInstanceFollowRedirects
     */
    public static HttpURLConnection followRedirects(URLConnection conn) throws IOException {
        switch (((HttpURLConnection) conn).getResponseCode()) {
        case HttpURLConnection.HTTP_MOVED_PERM:
        case HttpURLConnection.HTTP_MOVED_TEMP:
            return followRedirects(new URL(conn.getHeaderField("Location")).openConnection());
        default:
            return (HttpURLConnection) conn;
        }
    }

    /**
     * Gets general information about a build.
     * The changelog ({@code <changeSet>}) can be interpreted separately by {@link HudsonJobBuild#getChanges}.
     */
    public synchronized HudsonJobBuild getJobBuild(HudsonJob job, int build) {
        Document docBuild = getDocument(job.getUrl() + build + "/" + XML_API_URL +
                // Exclude <changeSet> because it can be pretty big and slow to download.
                // To exclude also <culprit> etc., could make an includes list
                // based on those root element names we actually expect to interpret.
                "?xpath=*/*[name()!='changeSet']&wrapper=root");
        
        if (null == docBuild)
            return null;
        
        // Get build details
        NodeList buildDetails = docBuild.getDocumentElement().getChildNodes();
        
        HudsonJobBuild result = new HudsonJobBuild(this, job, build);
        
        for (int i = 0 ; i < buildDetails.getLength() ; i++) {
            Node n = buildDetails.item(i);
            
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals(XML_API_BUILDING_ELEMENT)) {
                    result.putProperty(JOB_BUILD_BUILDING, Boolean.parseBoolean(n.getFirstChild().getTextContent()));
                } else if (n.getNodeName().equals(XML_API_DURATION_ELEMENT)) {
                    result.putProperty(JOB_BUILD_DURATION, Long.parseLong(n.getFirstChild().getTextContent()));
                } else if (n.getNodeName().equals(XML_API_TIMESTAMP_ELEMENT)) {
                    result.putProperty(JOB_BUILD_TIMESTAMP, Long.parseLong(n.getFirstChild().getTextContent()));
                } else if (!result.isBuilding() && n.getNodeName().equals(XML_API_RESULT_ELEMENT)) {
                    result.putProperty(JOB_BUILD_RESULT, (n.getFirstChild().getTextContent().
                            equals("SUCCESS")) ? Result.SUCCESS : Result.FAILURE);
                }
            }
        }

        return result;
    }
    
    protected synchronized HudsonVersion getHudsonVersion() {
        if (null == version)
            version = retrieveHudsonVersion();
        
        return version;
    }
    
    protected boolean isConnected() {
        return connected;
        
    }
    
    private Collection<HudsonView> getViews(NodeList nodes) {
        Collection<HudsonView> views = new ArrayList<HudsonView>();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            
            String name = null;
            String url = null;
            String description = null;
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                        name = o.getFirstChild().getTextContent();
                    } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                        url = Utilities.getURLWithoutSpaces(o.getFirstChild().getTextContent());
                    }
                }
            }
            
            if (null != name && null != url) {
                Document docView = getDocument(url + XML_API_URL);
                
                if (null == docView)
                    continue;
                
                // Retrieve description
                NodeList descriptionList = docView.getElementsByTagName(XML_API_DESCRIPTION_ELEMENT);
                
                try {
                    description = descriptionList.item(0).getFirstChild().getTextContent();
                } catch (NullPointerException e) {
                    description = "";
                }
                
                // Create HudsonView
                HudsonViewImpl view = new HudsonViewImpl(instance, name, description, url);
                
                if (!view.getName().equals(HudsonView.ALL_VIEW)) {
                    
                    // Retrieve jobs
                    NodeList jobsList = docView.getElementsByTagName(XML_API_JOB_ELEMENT);
                    
                    for (int k = 0; k < jobsList.getLength(); k++) {
                        Node d = jobsList.item(k);
                        
                        for (int l = 0; l < d.getChildNodes().getLength(); l++) {
                            Node e = d.getChildNodes().item(l);
                            
                            if (e.getNodeType() == Node.ELEMENT_NODE) {
                                if (e.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                                    cache.put(view.getName() + "/" + e.getFirstChild().getTextContent(), view);
                                }
                            }
                        }
                    }
                }
                
                views.add(view);
            }
            
        }
        
        return views;
    }
    
    private Collection<HudsonJob> getJobs(NodeList nodes) {
        Collection<HudsonJob> jobs = new ArrayList<HudsonJob>();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            
            HudsonJobImpl job = new HudsonJobImpl(instance);
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                        job.putProperty(JOB_NAME, o.getFirstChild().getTextContent());
                    } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                        job.putProperty(JOB_URL, Utilities.getURLWithoutSpaces(o.getFirstChild().getTextContent()));
                    } else if (o.getNodeName().equals(XML_API_COLOR_ELEMENT)) {
                        String color = o.getFirstChild().getTextContent().trim();
                        try {
                            job.putProperty(JOB_COLOR, Color.valueOf(color));
                        } catch (IllegalArgumentException x) {
                            Exceptions.attachMessage(x,
                                    "http://www.netbeans.org/nonav/issues/show_bug.cgi?id=126166 - no Color value '" +
                                    color + "' among " + Arrays.toString(Color.values()));
                            Exceptions.printStackTrace(x);
                            job.putProperty(JOB_COLOR, Color.red_anime);
                        }
                    }
                }
            }
            
            if (null != job.getName() && null != job.getUrl() && null != job.getColor()) {
                Document docJob = getDocument(job.getUrl() + XML_API_URL);
                
                if (null == docJob)
                    continue;
                
                NodeList jobDetails = docJob.getDocumentElement().getChildNodes();
                
                for (int k = 0; k < jobDetails.getLength(); k++) {
                    Node d = jobDetails.item(k);
                    
                    if (d.getNodeType() == Node.ELEMENT_NODE) {
                        if (d.getNodeName().equals(XML_API_DESCRIPTION_ELEMENT)) {
                            try {
                                job.putProperty(JOB_DESCRIPTION, d.getFirstChild().getTextContent());
                            } catch (NullPointerException e) {}
                        } else if (d.getNodeName().equals(XML_API_DISPLAY_NAME_ELEMENT)) {
                            job.putProperty(JOB_DISPLAY_NAME, d.getFirstChild().getTextContent());
                        } else if (d.getNodeName().equals(XML_API_BUILDABLE_ELEMENT)) {
                            job.putProperty(JOB_BUILDABLE, Boolean.valueOf(d.getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_INQUEUE_ELEMENT)) {
                            job.putProperty(JOB_IN_QUEUE, Boolean.valueOf(d.getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_FAILED_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_FAILED_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_STABLE_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_STABLE_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_SUCCESSFUL_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_COMPLETED_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_COMPLETED_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        }
                    }
                }
                
                for (HudsonView v : instance.getViews()) {
                    // All view synchronization
                    if (v.getName().equals(HudsonView.ALL_VIEW)) {
                        job.addView(v);
                        continue;
                    }
                    
                    if (null != cache.get(v.getName() + "/" + job.getName()))
                        job.addView(v);
                }
                
                jobs.add(job);
            }
        }
        
        return jobs;
    }
    
    private synchronized HudsonVersion retrieveHudsonVersion() {
        HudsonVersion v = null;
        
        try {
            URL u = new java.net.URL(instance.getUrl());
            HttpURLConnection conn = followRedirects(u.openConnection());
            String sVersion = conn.getHeaderField("X-Hudson");
            if (sVersion != null) {
                v = new HudsonVersionImpl(sVersion);
            }
        } catch (MalformedURLException e) {
            // Nothing
        } catch (IOException e) {
            // Nothing
        }
        
        return v;
    }
    
    Document getDocument(String url) {
        Document doc = null;
        
        try {
            URL u = new URL(url);
            HttpURLConnection conn = followRedirects(u.openConnection());
            
            int responseCode = conn.getResponseCode();
            // Connected failed
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LOG.log(Level.FINE, "{0}: {1} {2}", new Object[] {url, responseCode, conn.getResponseMessage()});
                connected = false;
                return null;
            }
            
            // Connected successfully
            if (!isConnected()) {
                connected = true;
                version = retrieveHudsonVersion();
            }
            
            // Get input stream
            InputStream stream = conn.getInputStream();
            
            // Parse document
            InputSource source = new InputSource(stream);
            source.setSystemId(url);
            doc = XMLUtil.parse(source, false, false, new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    LOG.log(Level.FINE, "{0}:{1}: {2}", new Object[] {
                        exception.getSystemId(), exception.getLineNumber(), exception.getMessage()});
                }
                public void error(SAXParseException exception) throws SAXException {
                    warning(exception);
                }
                public void fatalError(SAXParseException exception) throws SAXException {
                    warning(exception);
                    throw exception;
                }
            }, null);
            
            // Check for right version
            if (!Utilities.isSupportedVersion(getHudsonVersion())) {
                HudsonVersion v = retrieveHudsonVersion();
                
                if (!Utilities.isSupportedVersion(v))
                    return null;
                
                version = v;
            }
            
            if(conn != null)
                conn.disconnect();
        } catch (SAXParseException x) {
            // already reported
        } catch (Exception x) {
            LOG.log(Level.FINE, url, x);
        }
        
        return doc;
    }
}