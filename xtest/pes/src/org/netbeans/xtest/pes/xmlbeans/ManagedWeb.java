/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * ManagedWeb.java
 *
 * Created on May 27, 2002, 9:49 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import java.io.*;
import org.netbeans.xtest.pe.xmlbeans.*;

/**
 *
 * @author  breh
 */
public class ManagedWeb extends XMLBean {
    
    /** Creates a new instance of ManagedWeb */
    public ManagedWeb() {
    }
    
    // elements
    public ManagedGroup[] xmlel_ManagedGroup;
    
    // private stuff
    private File webFile;
    
    private PESWeb pesWeb = null;
    
    // business methods
    
    public static ManagedWeb getDefault(PESWeb web) {
        ManagedWeb mw = new ManagedWeb();
        mw.pesWeb = web;
        mw.xmlel_ManagedGroup = new ManagedGroup[0];
        return mw;
    }
    
    
    public static ManagedWeb loadManagedWeb(PESWeb web) {
        File webData = null;        
        try {
            webData = new File(web.getDataDir(),ManagedWeb.getDataFilename());
            if (webData.isFile()) {
                XMLBean aBean = null;
                try {
                    aBean = XMLBean.loadXMLBean(webData);
                } catch (ClassNotFoundException cnfe) {
                }
                if ((aBean == null) | !(aBean instanceof ManagedWeb)) {
                    ManagedWeb mw =  ManagedWeb.getDefault(web);                    
                    mw.webFile = webData;
                    return mw;
                } else {
                    ManagedWeb mw =  (ManagedWeb) aBean;
                    mw.webFile = webData;
                    mw.pesWeb = web;
                    return mw;
                }
            }
        } catch (IOException ioe) {            
            // exception ? ok, load the default web
        }
        ManagedWeb mw =  ManagedWeb.getDefault(web);
        mw.webFile = webData;
        return mw;
    }
    
    /*
    public void readPESWeb(PESWeb pesWeb) {
        this.xmlat_description = pesWeb.xmlat_description;
        this.xmlat_truncate = pesWeb.xmlat_truncate;
        this.xmlat_webroot = pesWeb.xmlat_webroot;
        this.xmlat_type = pesWeb.xmlat_type;
    }
    */
    
    public void saveManagedWeb(int depth) throws IOException {
        if (webFile != null) {            
            this.saveXMLBean(webFile, depth);
        } else {
            throw new IOException("web file is not specified - cannot save");
        }
    }
    
    
    // get filename of xml file with ManagedReports for this group
    public static String getDataFilename() {
        return "webdata.xml";        
    }
    
}
